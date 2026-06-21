package com.subho.bloghub.server.service.impl;

import com.subho.bloghub.client.dtos.users.UserProfileResponseDTO;
import com.subho.bloghub.server.common.CurrentUserResolver;
import com.subho.bloghub.server.common.UuidUtils;
import com.subho.bloghub.server.entity.users.Follows;
import com.subho.bloghub.server.entity.users.Users;
import com.subho.bloghub.server.exception.BadRequestException;
import com.subho.bloghub.server.exception.ResourceNotFoundException;
import com.subho.bloghub.server.mapper.users.UserMapper;
import com.subho.bloghub.server.repository.blogs.BlogRepository;
import com.subho.bloghub.server.repository.users.FollowRepository;
import com.subho.bloghub.server.repository.users.UserRepository;
import com.subho.bloghub.server.service.notifications.NotificationService;
import com.subho.bloghub.server.service.users.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final BlogRepository blogRepository;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final CurrentUserResolver currentUserResolver;

    @Override
    @Transactional
    public UserProfileResponseDTO follow(String accessToken, String targetUserId) {
        UUID followerId = currentUserResolver.requireCurrentUserId(accessToken);
        UUID targetId = UuidUtils.parse(targetUserId, "user id");

        if (followerId.equals(targetId)) {
            throw new BadRequestException("You cannot follow yourself");
        }

        Users target = userRepository.findById(targetId)
                .orElseThrow(() -> new ResourceNotFoundException("User", targetUserId));

        if (!followRepository.existsByFollower_IdAndFollowing_Id(followerId, targetId)) {
            Users follower = userRepository.findById(followerId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", followerId));

            followRepository.save(Follows.builder().follower(follower).following(target).build());
            notificationService.notify(target, follower, "follow", null, null, null);
        }

        return buildProfileResponse(target, followerId);
    }

    @Override
    @Transactional
    public UserProfileResponseDTO unfollow(String accessToken, String targetUserId) {
        UUID followerId = currentUserResolver.requireCurrentUserId(accessToken);
        UUID targetId = UuidUtils.parse(targetUserId, "user id");

        Users target = userRepository.findById(targetId)
                .orElseThrow(() -> new ResourceNotFoundException("User", targetUserId));

        followRepository.deleteByFollower_IdAndFollowing_Id(followerId, targetId);

        return buildProfileResponse(target, followerId);
    }

    private UserProfileResponseDTO buildProfileResponse(Users target, UUID viewerId) {
        long followers = followRepository.countByFollowing_Id(target.getId());
        long following = followRepository.countByFollower_Id(target.getId());
        long posts = blogRepository.countByAuthor_Id(target.getId());
        boolean isFollowing = followRepository.existsByFollower_IdAndFollowing_Id(viewerId, target.getId());
        return userMapper.toResponse(target, followers, following, posts, isFollowing);
    }
}
