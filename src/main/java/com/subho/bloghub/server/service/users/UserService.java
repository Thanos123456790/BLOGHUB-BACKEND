package com.subho.bloghub.server.service.users;

import com.subho.bloghub.client.dtos.blogs.BlogCardResponseDTO;
import com.subho.bloghub.client.dtos.users.UpdateProfileRequestDTO;
import com.subho.bloghub.client.dtos.users.UserProfileResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserProfileResponseDTO getCurrentUserProfile(String accessToken);

    UserProfileResponseDTO updateCurrentUserProfile(String accessToken, UpdateProfileRequestDTO request);

    UserProfileResponseDTO getPublicProfile(String accessToken, String handle);

    Page<BlogCardResponseDTO> getPublicProfileBlogs(String handle, Pageable pageable);

    Page<UserProfileResponseDTO> getPublicProfileFollowers(String handle, Pageable pageable);

    Page<UserProfileResponseDTO> getPublicProfileFollowing(String handle, Pageable pageable);

    Page<UserProfileResponseDTO> getSuggestedUsers(String accessToken, Pageable pageable);

    void followUser(String accessToken, String handle);

    void unfollowUser(String accessToken, String handle);
}