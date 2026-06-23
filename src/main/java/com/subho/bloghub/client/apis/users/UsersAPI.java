package com.subho.bloghub.client.apis.users;

import com.subho.bloghub.client.dtos.blogs.BlogCardResponseDTO;
import com.subho.bloghub.client.dtos.users.UpdateProfileRequestDTO;
import com.subho.bloghub.client.dtos.users.UserProfileResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Users")
@RequestMapping("/api/v1/users")
public interface UsersAPI {

    @GetMapping("/me")
    ResponseEntity<UserProfileResponseDTO> getCurrentUserProfile(
            @RequestHeader("Authorization") String accessToken);

    @PutMapping("/me")
    ResponseEntity<UserProfileResponseDTO> updateCurrentUserProfile(
            @RequestHeader("Authorization") String accessToken,
            @Valid @RequestBody UpdateProfileRequestDTO dto);

    @GetMapping("/{handle}")
    ResponseEntity<UserProfileResponseDTO> getPublicProfile(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @PathVariable String handle);

    @GetMapping("/{handle}/blogs")
    ResponseEntity<Page<BlogCardResponseDTO>> getPublicProfileBlogs(
            @PathVariable String handle,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size);

    @GetMapping("/{handle}/followers")
    ResponseEntity<Page<UserProfileResponseDTO>> getPublicProfileFollowers(
            @PathVariable String handle,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size);

    @GetMapping("/{handle}/following")
    ResponseEntity<Page<UserProfileResponseDTO>> getPublicProfileFollowing(
            @PathVariable String handle,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size);

    @GetMapping("/suggested")
    ResponseEntity<Page<UserProfileResponseDTO>> getSuggestedUsers(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size);

    @Operation(summary = "Follow a user")
    @ApiResponse(responseCode = "204", description = "Followed successfully")
    @ApiResponse(responseCode = "400", description = "Cannot follow yourself")
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token")
    @ApiResponse(responseCode = "404", description = "Target user not found")
    @PostMapping("/{handle}/follow")
    ResponseEntity<Void> followUser(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable String handle);

    @Operation(summary = "Unfollow a user")
    @ApiResponse(responseCode = "204", description = "Unfollowed successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token")
    @ApiResponse(responseCode = "404", description = "Target user not found")
    @DeleteMapping("/{handle}/follow")
    ResponseEntity<Void> unfollowUser(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable String handle);
}