package com.subho.bloghub.client.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Category of asset being uploaded — controls S3 key prefix and any per-type validation")
public enum AssetType {
    @Schema(description = "User avatar image")
    AVATAR,
    @Schema(description = "User profile banner image")
    BANNER,
    @Schema(description = "Blog cover image")
    BLOG_COVER,
    @Schema(description = "Image used inside a blog content block")
    BLOG_BLOCK_IMAGE
}
