package com.subho.bloghub.server.controller.users;

import com.subho.bloghub.client.apis.users.UsersAPI;
import com.subho.bloghub.client.dtos.blogs.BlogCardResponseDTO;
import com.subho.bloghub.client.dtos.users.UpdateProfileRequestDTO;
import com.subho.bloghub.client.dtos.users.UserProfileResponseDTO;
import com.subho.bloghub.server.common.PageRequestFactory;
import com.subho.bloghub.server.service.users.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController implements UsersAPI {

    private final UserService userService;
    private final PageRequestFactory pageRequestFactory;

    @Override
    public ResponseEntity<UserProfileResponseDTO> getCurrentUserProfile(
            @RequestHeader("Authorization") String accessToken) {
        return ResponseEntity.ok(userService.getCurrentUserProfile(accessToken));
    }

    @Override
    public ResponseEntity<UserProfileResponseDTO> updateCurrentUserProfile(
            @RequestHeader("Authorization") String accessToken,
            @Valid @RequestBody UpdateProfileRequestDTO dto) {
        return ResponseEntity.ok(userService.updateCurrentUserProfile(accessToken, dto));
    }

    @Override
    public ResponseEntity<UserProfileResponseDTO> getPublicProfile(@PathVariable String handle) {
        return ResponseEntity.ok(userService.getPublicProfile(handle));
    }

    @Override
    public ResponseEntity<Page<BlogCardResponseDTO>> getPublicProfileBlogs(
            @PathVariable String handle,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = pageRequestFactory.of(page, size);
        return ResponseEntity.ok(userService.getPublicProfileBlogs(handle, pageable));
    }

    @Override
    public ResponseEntity<Page<UserProfileResponseDTO>> getPublicProfileFollowers(
            @PathVariable String handle,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = pageRequestFactory.of(page, size);
        return ResponseEntity.ok(userService.getPublicProfileFollowers(handle, pageable));
    }

    @Override
    public ResponseEntity<Page<UserProfileResponseDTO>> getPublicProfileFollowing(
            @PathVariable String handle,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = pageRequestFactory.of(page, size);
        return ResponseEntity.ok(userService.getPublicProfileFollowing(handle, pageable));
    }

    @Override
    public ResponseEntity<Page<UserProfileResponseDTO>> getSuggestedUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = pageRequestFactory.of(page, size);
        return ResponseEntity.ok(userService.getSuggestedUsers(pageable));
    }
}
