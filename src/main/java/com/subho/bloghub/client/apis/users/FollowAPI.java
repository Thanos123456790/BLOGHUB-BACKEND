package com.subho.bloghub.client.apis.users;


import com.subho.bloghub.client.dtos.users.UserProfileResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Follow", description = "The follow graph between users")
@RequestMapping("/api/v1/users/{id}")
public interface FollowAPI {

    @Operation(
            summary = "Follow a user profile",
            description = "Makes the current user follow the given user. Idempotent — following an already-followed user is a no-op. Returns the target user's updated public profile."
    )
    @ApiResponse(responseCode = "200", description = "Now following this user")
    @ApiResponse(responseCode = "400", description = "Cannot follow yourself")
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @PostMapping("/follow")
    ResponseEntity<UserProfileResponseDTO> followUserProfile(String accessToken, @PathVariable String id);


    @Operation(
            summary = "Unfollow a user profile",
            description = "Makes the current user unfollow the given user, if currently following. Returns the target user's updated public profile."
    )
    @ApiResponse(responseCode = "200", description = "No longer following this user")
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @DeleteMapping("/follow")
    ResponseEntity<UserProfileResponseDTO> unfollowUserProfile(String accessToken, @PathVariable String id);

}
