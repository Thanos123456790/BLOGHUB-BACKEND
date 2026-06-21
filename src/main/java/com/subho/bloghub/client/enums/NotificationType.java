package com.subho.bloghub.client.enums;

import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "All possible notification event types")
public enum NotificationType {
    @Schema(description = "Someone followed you")
    FOLLOW,
    @Schema(description = "Someone reacted to your blog post")
    REACTION,
    @Schema(description = "Someone commented on your blog post")
    COMMENT,
    @Schema(description = "Someone replied to your comment")
    REPLY,
    @Schema(description = "Someone @mentioned you in a comment")
    MENTION,
    @Schema(description = "Someone reacted to your comment")
    COMMENT_REACTION
}