package com.subho.bloghub.server.mapper.blogs;

import com.subho.bloghub.client.dtos.reaction.ReactionCountResponseDTO;
import com.subho.bloghub.client.enums.ReactionType;

import java.util.List;

/**
 * Carries everything a single blog card/detail needs that is NOT directly on
 * the {@code Blogs} entity: tags, reaction counts, the viewer's own
 * reaction, bookmark state, and comment count. The service layer computes
 * all of these in batched queries for a whole page of blogs up front, then
 * passes one context per blog into the mapper — so the mapper itself never
 * triggers extra queries (no N+1).
 */
public record BlogAggregateContext(
        List<String> tags,
        ReactionCountResponseDTO reactions,
        ReactionType myReaction,
        boolean bookmarked,
        long commentsCount
) {
    public static BlogAggregateContext empty() {
        return new BlogAggregateContext(
                List.of(),
                ReactionCountResponseDTO.builder().build(),
                null,
                false,
                0L
        );
    }
}
