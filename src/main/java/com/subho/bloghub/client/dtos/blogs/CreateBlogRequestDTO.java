package com.subho.bloghub.client.dtos.blogs;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body to publish a new blog post")
public class CreateBlogRequestDTO {

    @Schema(description = "Blog post title", example = "What 6 years of on-call taught me", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 300, message = "Title must be between 5 and 300 characters")
    private String title;

    @Schema(description = "Short excerpt shown in feed cards", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Excerpt is required")
    @Size(min = 10, max = 500, message = "Excerpt must be between 10 and 500 characters")
    private String excerpt;

    // VLN-04 FIX: coverImageUrl must be https:// only. Validated further in service layer
    // against the trusted-hosts allowlist.
    @Schema(description = "Cover image URL — must be https://")
    @Size(max = 2048, message = "Cover image URL must not exceed 2048 characters")
    @Pattern(
            regexp = "^(https://.*)?$",
            message = "Cover image URL must use the https scheme"
    )
    private String coverImageUrl;

    @Schema(description = "CSS filter applied to cover image", allowableValues = {"grayscale", "warm", "cool", "vintage", "dramatic"})
    @Pattern(
            regexp = "^(grayscale|warm|cool|vintage|dramatic)?$",
            message = "Cover filter must be one of: grayscale, warm, cool, vintage, dramatic"
    )
    private String coverFilter;

    @Schema(description = "Estimated reading time in minutes", example = "7", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Read time is required")
    @Min(value = 1, message = "Read time must be at least 1 minute")
    @Max(value = 120, message = "Read time must not exceed 120 minutes")
    private Integer readTimeMinutes;

    @Schema(description = "List of tag names (max 5)")
    @Size(max = 5, message = "A blog post can have at most 5 tags")
    private List<
            @NotBlank(message = "Tag name must not be blank")
            @Size(min = 1, max = 80, message = "Each tag must be between 1 and 80 characters")
            // VLN-13 FIX: Tags restricted to alphanumeric + spaces + hyphens only.
            @Pattern(regexp = "^[\\w\\s\\-]+$", message = "Tag names may only contain letters, numbers, spaces, and hyphens")
                    String
            > tags;

    @Schema(description = "Ordered list of content blocks", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Blocks list is required")
    @Size(min = 1, max = 50, message = "A blog must have between 1 and 50 content blocks")
    @Valid
    private List<BlogBlockRequestDTO> blocks;
}
