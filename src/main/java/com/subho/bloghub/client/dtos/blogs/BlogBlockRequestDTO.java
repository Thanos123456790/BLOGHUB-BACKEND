package com.subho.bloghub.client.dtos.blogs;

import com.subho.bloghub.client.enums.BlockType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A single content block inside a blog post")
public class BlogBlockRequestDTO {

    @Schema(description = "Block content type", example = "PARAGRAPH", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Block type is required")
    private BlockType type;

    @Schema(
            description = "Text content for paragraph/heading/quote blocks, or image URL for image blocks",
            example = "Somewhere around year three of carrying a pager...",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Block content is required")
    @Size(max = 10000, message = "Block content must not exceed 10,000 characters")
    private String content;

    @Schema(description = "Caption for image blocks (optional)", example = "Our incident board from the night this post was born.")
    @Size(max = 300, message = "Caption must not exceed 300 characters")
    private String caption;

    @Schema(description = "CSS filter for image blocks", example = "grayscale", allowableValues = {"grayscale", "warm", "cool", "vintage", "dramatic"})
    @Pattern(
            regexp = "^(grayscale|warm|cool|vintage|dramatic)?$",
            message = "Filter must be one of: grayscale, warm, cool, vintage, dramatic"
    )
    private String filter;

    @Schema(description = "0-based position/order index within the blog", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Block position is required")
    @Min(value = 0, message = "Position must be 0 or greater")
    private Integer position;
}
