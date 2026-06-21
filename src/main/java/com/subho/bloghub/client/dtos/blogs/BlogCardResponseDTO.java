package com.subho.bloghub.client.dtos.blogs;


import com.subho.bloghub.client.dtos.reaction.ReactionCountResponseDTO;
import com.subho.bloghub.client.dtos.users.UserProfileResponseDTO;
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
@Schema(description = "Lightweight blog summary shown in feed cards")
public class BlogCardResponseDTO {

    @Schema(description = "Blog UUID")
    private UUID id;

    @Schema(description = "Author's public profile (minimal)")
    private UserProfileResponseDTO author;

    @Schema(description = "Blog title")
    private String title;

    @Schema(description = "Short excerpt")
    private String excerpt;

    @Schema(description = "Cover image URL")
    private String coverImageUrl;

    @Schema(description = "Cover CSS filter")
    private String coverFilter;

    @Schema(description = "Tags assigned to this blog")
    private List<String> tags;

    @Schema(description = "Estimated reading time in minutes")
    private int readTimeMinutes;

    @Schema(description = "Reaction counts broken down by type")
    private ReactionCountResponseDTO reactions;

    @Schema(description = "Current user's reaction, null if none", nullable = true)
    private ReactionType myReaction;

    @Schema(description = "Whether the current user has bookmarked this post")
    private boolean bookmarked;

    @Schema(description = "Total number of comments on this blog")
    private long commentsCount;

    @Schema(description = "Publish timestamp")
    private Instant createdAt;
}

