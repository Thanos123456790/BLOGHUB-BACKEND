package com.subho.bloghub.server.mapper.blogs;

import com.subho.bloghub.client.dtos.blogs.BlogCardResponseDTO;
import com.subho.bloghub.client.dtos.blogs.BlogResponseDTO;
import com.subho.bloghub.client.dtos.blogs.CreateBlogRequestDTO;
import com.subho.bloghub.client.enums.BlockType;
import com.subho.bloghub.server.entity.blogs.BlogBlocks;
import com.subho.bloghub.server.entity.blogs.Blogs;
import com.subho.bloghub.server.mapper.GenericMapper;
import com.subho.bloghub.server.mapper.users.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Maps between {@link Blogs} and its request/response DTOs.
 *
 * Aggregate data (tags, reaction counts, my-reaction, bookmark state,
 * comment count) is never queried here — it's computed in batch by
 * {@code BlogService} and passed in as a {@link BlogAggregateContext} per
 * blog, so mapping a whole page never triggers N+1 queries.
 */
@Component
@RequiredArgsConstructor
public class BlogMapper implements GenericMapper<Blogs, CreateBlogRequestDTO, BlogCardResponseDTO> {

    private final UserMapper userMapper;

    /**
     * Builds a new {@code Blogs} entity from the create request. Tags and
     * blocks are intentionally NOT set here — they reference other entities
     * (Tags, BlogBlocks) that the service resolves/persists separately as
     * part of the create transaction. The author is also set by the service,
     * once the current user is resolved.
     */
    @Override
    public Blogs toEntity(CreateBlogRequestDTO request) {
        if (request == null) {
            return null;
        }
        return Blogs.builder()
                .title(request.getTitle())
                .excerpt(request.getExcerpt())
                .coverImageUrl(request.getCoverImageUrl())
                .coverFilter(request.getCoverFilter())
                .readTimeMinutes(request.getReadTimeMinutes())
                .build();
    }

    /**
     * Fulfils the {@link GenericMapper} contract with empty aggregates.
     * Real call sites should use {@link #toCardResponse} /
     * {@link #toFullResponse} with a populated {@link BlogAggregateContext}.
     */
    @Override
    public BlogCardResponseDTO toResponse(Blogs entity) {
        return toCardResponse(entity, BlogAggregateContext.empty());
    }

    public BlogCardResponseDTO toCardResponse(Blogs entity, BlogAggregateContext context) {
        if (entity == null) {
            return null;
        }
        return BlogCardResponseDTO.builder()
                .id(entity.getId())
                .author(userMapper.toResponse(entity.getAuthor()))
                .title(entity.getTitle())
                .excerpt(entity.getExcerpt())
                .coverImageUrl(entity.getCoverImageUrl())
                .coverFilter(entity.getCoverFilter())
                .tags(context.tags())
                .readTimeMinutes(entity.getReadTimeMinutes())
                .reactions(context.reactions())
                .myReaction(context.myReaction())
                .bookmarked(context.bookmarked())
                .commentsCount(context.commentsCount())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public BlogResponseDTO toFullResponse(Blogs entity, List<BlogBlocks> blocks, BlogAggregateContext context) {
        if (entity == null) {
            return null;
        }
        List<BlogResponseDTO.BlogBlockResponseDTO> blockDTOs = blocks.stream()
                .sorted(Comparator.comparingInt(BlogBlocks::getPosition))
                .map(this::toBlockResponse)
                .toList();

        return BlogResponseDTO.builder()
                .id(entity.getId())
                .author(userMapper.toResponse(entity.getAuthor()))
                .title(entity.getTitle())
                .excerpt(entity.getExcerpt())
                .coverImageUrl(entity.getCoverImageUrl())
                .coverFilter(entity.getCoverFilter())
                .readTimeMinutes(entity.getReadTimeMinutes())
                .tags(context.tags())
                .blocks(blockDTOs)
                .reactions(context.reactions())
                .myReaction(context.myReaction())
                .bookmarked(context.bookmarked())
                .commentsCount(context.commentsCount())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private BlogResponseDTO.BlogBlockResponseDTO toBlockResponse(BlogBlocks block) {
        return BlogResponseDTO.BlogBlockResponseDTO.builder()
                .id(block.getId())
                .type(BlockType.valueOf(block.getType().toUpperCase()))
                .content(block.getContent())
                .caption(block.getCaption())
                .filter(block.getFilter())
                .position(block.getPosition())
                .build();
    }
}
