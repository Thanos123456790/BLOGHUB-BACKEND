package com.subho.bloghub.server.service.impl;

import com.subho.bloghub.client.dtos.blogs.BlogCardResponseDTO;
import com.subho.bloghub.client.dtos.users.UpdateProfileRequestDTO;
import com.subho.bloghub.client.dtos.users.UserProfileResponseDTO;
import com.subho.bloghub.server.common.CurrentUserResolver;
import com.subho.bloghub.server.config.S3Properties;
import com.subho.bloghub.server.entity.users.Follows;
import com.subho.bloghub.server.entity.users.Users;
import com.subho.bloghub.server.exception.BadRequestException;
import com.subho.bloghub.server.mapper.blogs.BlogAggregateContext;
import com.subho.bloghub.server.mapper.blogs.BlogMapper;
import com.subho.bloghub.server.mapper.users.UserMapper;
import com.subho.bloghub.server.repository.blogs.BlogRepository;
import com.subho.bloghub.server.repository.users.FollowRepository;
import com.subho.bloghub.server.repository.users.UserRepository;
import com.subho.bloghub.server.service.notifications.NotificationService;
import com.subho.bloghub.server.service.users.UserNotFoundException;
import com.subho.bloghub.server.service.users.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final BlogRepository blogRepository;
    private final UserMapper userMapper;
    private final BlogMapper blogMapper;
    private final CurrentUserResolver currentUserResolver;
    private final NotificationService notificationService;
    private final S3Properties s3Properties;

    @Override
    public UserProfileResponseDTO getCurrentUserProfile(String accessToken) {
        UUID currentUserId = currentUserResolver.requireCurrentUserId(accessToken);
        Users user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));
        return buildProfileResponse(user, currentUserId);
    }

    @Override
    @Transactional
    public UserProfileResponseDTO updateCurrentUserProfile(String accessToken, UpdateProfileRequestDTO request) {
        UUID currentUserId = currentUserResolver.requireCurrentUserId(accessToken);
        Users user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));

        // VLN-04 FIX: Validate avatar/banner URLs against trusted hosts before saving
        validateImageUrl(request.getAvatarUrl(), "avatarUrl");
        validateImageUrl(request.getBannerUrl(), "bannerUrl");

        userMapper.applyUpdate(request, user);
        Users saved = userRepository.save(user);

        log.info("AUDIT: User {} updated their profile", currentUserId);
        return buildProfileResponse(saved, currentUserId);
    }

    @Override
    public UserProfileResponseDTO getPublicProfile(String accessToken, String handle) {
        UUID viewerId = currentUserResolver.resolveCurrentUserIdOrNull(accessToken);
        Users user = userRepository.findByHandle(handle)
                .orElseThrow(() -> new UserNotFoundException("No user found with handle: " + handle));
        return buildProfileResponse(user, viewerId);
    }

    @Override
    public Page<BlogCardResponseDTO> getPublicProfileBlogs(String handle, Pageable pageable) {
        ensureUserExists(handle);
        return blogRepository.findByAuthor_Handle(handle, pageable)
                .map(blog -> blogMapper.toCardResponse(blog, BlogAggregateContext.empty()));
    }

    @Override
    public Page<UserProfileResponseDTO> getPublicProfileFollowers(String handle, Pageable pageable) {
        ensureUserExists(handle);
        return followRepository.findByFollowing_Handle(handle, pageable)
                .map(Follows::getFollower)
                .map(u -> buildProfileResponse(u, null));
    }

    @Override
    public Page<UserProfileResponseDTO> getPublicProfileFollowing(String handle, Pageable pageable) {
        ensureUserExists(handle);
        return followRepository.findByFollower_Handle(handle, pageable)
                .map(Follows::getFollowing)
                .map(u -> buildProfileResponse(u, null));
    }

    @Override
    public Page<UserProfileResponseDTO> getSuggestedUsers(String accessToken, Pageable pageable) {
        UUID viewerId = currentUserResolver.resolveCurrentUserIdOrNull(accessToken);
        return userRepository.findSuggested(pageable)
                .map(u -> buildProfileResponse(u, viewerId));
    }

    @Override
    @Transactional
    public void followUser(String accessToken, String handle) {
        UUID followerId = currentUserResolver.requireCurrentUserId(accessToken);
        Users target = userRepository.findById(UUID.fromString(handle))
                .orElseThrow(() -> new UserNotFoundException("No user found with handle: " + handle));
        if (followerId.equals(target.getId())) {
            throw new BadRequestException("Cannot follow yourself");
        }
        if (followRepository.existsByFollower_IdAndFollowing_Id(followerId, target.getId())) {
            return;
        }
        Users follower = userRepository.findById(followerId)
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));
        followRepository.save(Follows.builder().follower(follower).following(target).build());
        notificationService.notify(target, follower, "follow", null, null, null);
        log.info("AUDIT: User {} followed {}", followerId, target.getId());
    }

    @Override
    @Transactional
    public void unfollowUser(String accessToken, String handle) {
        UUID followerId = currentUserResolver.requireCurrentUserId(accessToken);
        Users target = userRepository.findById(UUID.fromString(handle))
                .orElseThrow(() -> new UserNotFoundException("No user found with handle: " + handle));
        followRepository.deleteByFollower_IdAndFollowing_Id(followerId, target.getId());
        log.info("AUDIT: User {} unfollowed {}", followerId, target.getId());
    }

    // ── VLN-04: URL validation ────────────────────────────────────────────────

    private void validateImageUrl(String url, String fieldName) {
        if (url == null || url.isBlank()) return;
        if (!url.startsWith("https://")) {
            throw new BadRequestException(fieldName + " must use the https scheme");
        }
        List<String> trustedHosts = s3Properties.getTrustedImageHosts();
        if (trustedHosts != null && !trustedHosts.isEmpty()) {
            try {
                String host = URI.create(url).getHost();
                boolean trusted = trustedHosts.stream()
                        .anyMatch(h -> host != null && (host.equals(h) || host.endsWith("." + h)));
                if (!trusted) {
                    throw new BadRequestException(fieldName + " must point to a trusted image host");
                }
            } catch (IllegalArgumentException e) {
                throw new BadRequestException(fieldName + " is not a valid URL");
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private UserProfileResponseDTO buildProfileResponse(Users user, UUID viewerId) {
        long followers  = followRepository.countByFollowing_Id(user.getId());
        long following  = followRepository.countByFollower_Id(user.getId());
        long posts      = blogRepository.countByAuthor_Id(user.getId());
        boolean isFollowing = viewerId != null
                && followRepository.existsByFollower_IdAndFollowing_Id(viewerId, user.getId());
        return userMapper.toResponse(user, followers, following, posts, isFollowing);
    }

    private void ensureUserExists(String handle) {
        if (!userRepository.existsByHandle(handle)) {
            throw new UserNotFoundException("No user found with handle: " + handle);
        }
    }
}
