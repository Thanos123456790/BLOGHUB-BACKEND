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

@Tag(name = "Users", description = "Profile management, public profiles, the follow graph, and user discovery")
@RequestMapping("/api/v1/users")
public interface UsersAPI {

    @Operation(
            summary = "Get current user profile",
            description = "Returns the full profile of the currently authenticated user."
    )
    @ApiResponse(responseCode = "200", description = "Profile returned successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @GetMapping("/me")
    ResponseEntity<UserProfileResponseDTO> getCurrentUserProfile(String accessToken);


    @Operation(
            summary = "Update current user profile",
            description = "Updates editable fields (name, bio, location, avatar, banner) on the authenticated user's profile. Only fields present in the request body are changed; omitted fields are left untouched."
    )
    @ApiResponse(responseCode = "200", description = "Profile updated successfully")
    @ApiResponse(responseCode = "400", description = "One or more fields failed validation")
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @PutMapping("/me")
    ResponseEntity<UserProfileResponseDTO> updateCurrentUserProfile(String accessToken, @Valid @RequestBody UpdateProfileRequestDTO dto);

    @Operation(
            summary = "Get a user's public profile",
            description = "Returns the public-facing profile for the given handle. No authentication required."
    )
    @ApiResponse(responseCode = "200", description = "Profile returned successfully")
    @ApiResponse(responseCode = "404", description = "No user exists with the given handle")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @GetMapping("/{handle}")
    ResponseEntity<UserProfileResponseDTO> getPublicProfile(@PathVariable String handle);

    @Operation(
            summary = "List a user's blogs",
            description = "Returns a paginated, most-recent-first list of blog posts published by the given handle."
    )
    @ApiResponse(responseCode = "200", description = "Blogs returned successfully")
    @ApiResponse(responseCode = "404", description = "No user exists with the given handle")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @GetMapping("/{handle}/blogs")
    ResponseEntity<Page<BlogCardResponseDTO>> getPublicProfileBlogs(
            @PathVariable String handle,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    );

    @Operation(
            summary = "List a user's followers",
            description = "Returns a paginated list of users who follow the given handle."
    )
    @ApiResponse(responseCode = "200", description = "Followers returned successfully")
    @ApiResponse(responseCode = "404", description = "No user exists with the given handle")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @GetMapping("/{handle}/followers")
    ResponseEntity<Page<UserProfileResponseDTO>> getPublicProfileFollowers(
            @PathVariable String handle,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    );

    @Operation(
            summary = "List who a user is following",
            description = "Returns a paginated list of users that the given handle follows."
    )
    @ApiResponse(responseCode = "200", description = "Following list returned successfully")
    @ApiResponse(responseCode = "404", description = "No user exists with the given handle")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @GetMapping("/{handle}/following")
    ResponseEntity<Page<UserProfileResponseDTO>> getPublicProfileFollowing(
            @PathVariable String handle,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    );

    @Operation(
            summary = "Get suggested users to follow",
            description = "Returns a paginated list of users recommended for the visitor to follow. v1 ranks by follower count only; no authentication required and no personalization yet."
    )
    @ApiResponse(responseCode = "200", description = "Suggestions returned successfully")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @GetMapping("/suggested")
    ResponseEntity<Page<UserProfileResponseDTO>> getSuggestedUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    );
}
