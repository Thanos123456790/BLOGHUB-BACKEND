package com.subho.bloghub.client.dtos.blogs;

import com.subho.bloghub.client.dtos.reaction.ReactionCountResponseDTO;
import com.subho.bloghub.client.dtos.users.UserProfileResponseDTO;
import com.subho.bloghub.client.enums.BlockType;
import com.subho.bloghub.client.enums.ReactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Full blog post detail response (includes all blocks)")
public class BlogResponseDTO {

    @Schema(description = "Blog UUID")
    private UUID id;

    @Schema(description = "Author's public profile")
    private UserProfileResponseDTO author;

    @Schema(description = "Blog title")
    private String title;

    @Schema(description = "Short excerpt")
    private String excerpt;

    @Schema(description = "Cover image URL")
    private String coverImageUrl;

    @Schema(description = "Cover CSS filter")
    private String coverFilter;

    @Schema(description = "Estimated reading time in minutes")
    private int readTimeMinutes;

    @Schema(description = "Tags assigned to this blog")
    private List<String> tags;

    @Schema(description = "Ordered list of content blocks")
    private List<BlogBlockResponseDTO> blocks;

    @Schema(description = "Reaction counts by type")
    private ReactionCountResponseDTO reactions;

    @Schema(description = "Current user's reaction, null if none", nullable = true)
    private ReactionType myReaction;

    @Schema(description = "Whether the current user has bookmarked this post")
    private boolean bookmarked;

    @Schema(description = "Total number of comments")
    private long commentsCount;

    @Schema(description = "Publish timestamp")
    private Instant createdAt;

    @Schema(description = "Last edit timestamp")
    private Instant updatedAt;

    // ── Nested block response ──────────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "A single rendered content block")
    public static class BlogBlockResponseDTO {

        @Schema(description = "Block UUID")
        private UUID id;

        @Schema(description = "Block type")
        private BlockType type;

        @Schema(description = "Text or image URL")
        private String content;

        @Schema(description = "Image caption (nullable)", nullable = true)
        private String caption;

        @Schema(description = "CSS filter (nullable)", nullable = true)
        private String filter;

        @Schema(description = "0-based ordering index")
        private int position;
    }
}

