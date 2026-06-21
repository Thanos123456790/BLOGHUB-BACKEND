package com.subho.bloghub.client.enums;


import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "Content block types inside a blog post")
public enum BlockType {
    @Schema(description = "Plain text paragraph")
    PARAGRAPH,
    @Schema(description = "Section heading (h2-level)")
    HEADING,
    @Schema(description = "Pull quote or block quote")
    QUOTE,
    @Schema(description = "Image with optional caption and filter")
    IMAGE
}
