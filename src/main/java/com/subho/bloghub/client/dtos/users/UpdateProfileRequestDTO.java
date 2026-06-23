package com.subho.bloghub.client.dtos.users;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body to update the current user's profile")
public class UpdateProfileRequestDTO {

    @Schema(description = "Updated display name", example = "Maya Iyer")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Schema(description = "Updated bio")
    @Size(max = 300, message = "Bio must not exceed 300 characters")
    private String bio;

    @Schema(description = "Updated location string", example = "Mumbai, India")
    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;

    // VLN-04 FIX: Avatar/banner URLs must use https only.
    @Schema(description = "New avatar image URL (use /api/v1/assets/upload first) — must be https://")
    @Size(max = 2048, message = "Avatar URL must not exceed 2048 characters")
    @Pattern(
            regexp = "^(https://.*)?$",
            message = "Avatar URL must use the https scheme"
    )
    private String avatarUrl;

    @Schema(description = "New banner image URL (use /api/v1/assets/upload first) — must be https://")
    @Size(max = 2048, message = "Banner URL must not exceed 2048 characters")
    @Pattern(
            regexp = "^(https://.*)?$",
            message = "Banner URL must use the https scheme"
    )
    private String bannerUrl;
}
