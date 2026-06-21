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
@Schema(description = "Request body to update an existing blog post (all fields optional)")
public class UpdateBlogRequestDTO {

    @Schema(description = "Updated blog title")
    @Size(min = 5, max = 300, message = "Title must be between 5 and 300 characters")
    private String title;

    @Schema(description = "Updated excerpt")
    @Size(min = 10, max = 500, message = "Excerpt must be between 10 and 500 characters")
    private String excerpt;

    @Schema(description = "Updated cover image URL")
    @Size(max = 2048, message = "Cover image URL must not exceed 2048 characters")
    private String coverImageUrl;

    @Schema(description = "Updated cover filter", allowableValues = {"grayscale", "warm", "cool", "vintage", "dramatic"})
    @Pattern(
            regexp = "^(grayscale|warm|cool|vintage|dramatic)?$",
            message = "Cover filter must be one of: grayscale, warm, cool, vintage, dramatic"
    )
    private String coverFilter;

    @Schema(description = "Updated estimated reading time in minutes")
    @Min(value = 1, message = "Read time must be at least 1 minute")
    @Max(value = 120, message = "Read time must not exceed 120 minutes")
    private Integer readTimeMinutes;

    @Schema(description = "Updated list of tag names (max 5)")
    @Size(max = 5, message = "A blog post can have at most 5 tags")
    private List<
            @NotBlank(message = "Tag name must not be blank")
            @Size(min = 1, max = 80, message = "Each tag must be between 1 and 80 characters")
                    String
            > tags;

    @Schema(description = "Full replacement list of content blocks")
    @Size(min = 1, max = 50, message = "A blog must have between 1 and 50 content blocks")
    @Valid
    private List<BlogBlockRequestDTO> blocks;
}

