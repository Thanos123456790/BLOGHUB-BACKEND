package com.subho.bloghub.server.service.users;

import com.subho.bloghub.client.dtos.users.UserProfileResponseDTO;

public interface FollowService {

    UserProfileResponseDTO follow(String accessToken, String targetUserId);

    UserProfileResponseDTO unfollow(String accessToken, String targetUserId);
}
