package com.subho.bloghub.server.mapper.comments;

import com.subho.bloghub.client.dtos.comments.CommentResponseDTO;
import com.subho.bloghub.client.dtos.reaction.ReactionCountResponseDTO;
import com.subho.bloghub.client.dtos.users.UserProfileResponseDTO;
import com.subho.bloghub.client.enums.ReactionType;

import java.util.List;

/**
 * Per-comment data that isn't directly on the {@code Comments} entity:
 * reaction counts, the viewer's own reaction, tagged users, and (for
 * top-level comments) already-mapped replies. Computed in batch by
 * {@code CommentService} so mapping a page of comments + their replies
 * never costs more than a fixed, small number of queries.
 */
public record CommentAggregateContext(
        ReactionCountResponseDTO reactions,
        ReactionType myReaction,
        List<UserProfileResponseDTO> taggedUsers,
        List<CommentResponseDTO> replies
) {
    public static CommentAggregateContext empty() {
        return new CommentAggregateContext(
                ReactionCountResponseDTO.builder().build(),
                null,
                List.of(),
                List.of()
        );
    }
}
