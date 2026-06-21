package com.subho.bloghub.server.controller.users;

import com.subho.bloghub.client.apis.users.FollowAPI;
import com.subho.bloghub.client.dtos.users.UserProfileResponseDTO;
import com.subho.bloghub.server.service.users.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FollowController implements FollowAPI {

    private final FollowService followService;

    @Override
    public ResponseEntity<UserProfileResponseDTO> followUserProfile(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @PathVariable String id) {
        return ResponseEntity.ok(followService.follow(accessToken, id));
    }

    @Override
    public ResponseEntity<UserProfileResponseDTO> unfollowUserProfile(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @PathVariable String id) {
        return ResponseEntity.ok(followService.unfollow(accessToken, id));
    }
}
