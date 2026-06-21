package com.subho.bloghub.server.service.impl;

import com.subho.bloghub.client.dtos.blogs.BlogCardResponseDTO;
import com.subho.bloghub.client.dtos.users.UpdateProfileRequestDTO;
import com.subho.bloghub.client.dtos.users.UserProfileResponseDTO;
import com.subho.bloghub.server.common.CurrentUserResolver;
import com.subho.bloghub.server.entity.users.Follows;
import com.subho.bloghub.server.entity.users.Users;
import com.subho.bloghub.server.mapper.blogs.BlogAggregateContext;
import com.subho.bloghub.server.mapper.blogs.BlogMapper;
import com.subho.bloghub.server.mapper.users.UserMapper;
import com.subho.bloghub.server.repository.blogs.BlogRepository;
import com.subho.bloghub.server.repository.users.FollowRepository;
import com.subho.bloghub.server.repository.users.UserRepository;
import com.subho.bloghub.server.service.users.UserNotFoundException;
import com.subho.bloghub.server.service.users.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    public UserProfileResponseDTO getCurrentUserProfile(String accessToken) {
        Users user = userRepository.findById(currentUserResolver.requireCurrentUserId(accessToken))
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));
        return buildProfileResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponseDTO updateCurrentUserProfile(String accessToken, UpdateProfileRequestDTO request) {
        Users user = userRepository.findById(currentUserResolver.requireCurrentUserId(accessToken))
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));

        // Partial update — only non-null fields on the request are applied. See UserMapper#applyUpdate;
        // UserMapper#toEntity is intentionally NOT used here (it would build a brand-new, mostly-null entity).
        userMapper.applyUpdate(request, user);
        Users saved = userRepository.save(user);

        return buildProfileResponse(saved);
    }

    @Override
    public UserProfileResponseDTO getPublicProfile(String handle) {
        Users user = userRepository.findByHandle(handle)
                .orElseThrow(() -> new UserNotFoundException("No user found with handle: " + handle));
        return buildProfileResponse(user);
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
                .map(this::buildProfileResponse);
    }

    @Override
    public Page<UserProfileResponseDTO> getPublicProfileFollowing(String handle, Pageable pageable) {
        ensureUserExists(handle);
        return followRepository.findByFollower_Handle(handle, pageable)
                .map(Follows::getFollowing)
                .map(this::buildProfileResponse);
    }

    @Override
    public Page<UserProfileResponseDTO> getSuggestedUsers(Pageable pageable) {
        // No personalization yet — that would require knowing the current viewer (to exclude
        // accounts they already follow), which needs accessToken resolution. v1 just ranks by
        // follower count. See UserRepository#findSuggested.
        return userRepository.findSuggested(pageable)
                .map(this::buildProfileResponse);
    }

    private UserProfileResponseDTO buildProfileResponse(Users user) {
        long followers = followRepository.countByFollowing_Id(user.getId());
        long following = followRepository.countByFollower_Id(user.getId());
        long posts = blogRepository.countByAuthor_Id(user.getId());
        // isFollowing stays false until the current viewer can be resolved from accessToken
        return userMapper.toResponse(user, followers, following, posts, false);
    }

    private void ensureUserExists(String handle) {
        if (!userRepository.existsByHandle(handle)) {
            throw new UserNotFoundException("No user found with handle: " + handle);
        }
    }
}
