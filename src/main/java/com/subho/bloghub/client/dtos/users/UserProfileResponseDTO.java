package com.subho.bloghub.client.dtos.users;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Public-facing user profile")
public class UserProfileResponseDTO {

    @Schema(description = "User UUID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "Display name", example = "Maya Iyer")
    private String name;

    @Schema(description = "@handle used in URLs", example = "mayaiyer")
    private String handle;

    @Schema(description = "Profile picture URL")
    private String avatarUrl;

    @Schema(description = "Profile banner URL")
    private String bannerUrl;

    @Schema(description = "Short bio")
    private String bio;

    @Schema(description = "Location string", example = "Mumbai, India")
    private String location;

    @Schema(description = "Whether the user has a verified badge")
    private boolean isVerified;

    @Schema(description = "Total follower count", example = "5230")
    private long followersCount;

    @Schema(description = "Total following count", example = "184")
    private long followingCount;

    @Schema(description = "Total blog posts published", example = "64")
    private long postsCount;

    @Schema(description = "Account creation date")
    private Instant joinedAt;

    @Schema(description = "Whether the currently authenticated user follows this profile")
    private boolean isFollowing;
}
