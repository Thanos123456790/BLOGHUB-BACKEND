package com.subho.bloghub.client.dtos.comments;


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
@Schema(description = "A comment or reply with its nested replies")
public class CommentResponseDTO {

    @Schema(description = "Comment UUID")
    private UUID id;

    @Schema(description = "Author's public profile")
    private UserProfileResponseDTO author;

    @Schema(description = "Comment text content")
    private String content;

    @Schema(description = "Reaction counts by type")
    private ReactionCountResponseDTO reactions;

    @Schema(description = "Current user's reaction on this comment, null if none", nullable = true)
    private ReactionType myReaction;

    @Schema(description = "Users @mentioned inside this comment")
    private List<UserProfileResponseDTO> taggedUsers;

    @Schema(description = "Direct replies to this comment (max 1 level deep in response)")
    private List<CommentResponseDTO> replies;

    @Schema(description = "When the comment was posted")
    private Instant createdAt;

    @Schema(description = "When the comment was last edited")
    private Instant updatedAt;
}

