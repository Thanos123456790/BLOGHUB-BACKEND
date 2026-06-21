package com.subho.bloghub.server.mapper.blogs;

import com.subho.bloghub.client.dtos.blogs.BlogBlockRequestDTO;
import com.subho.bloghub.client.dtos.blogs.BlogResponseDTO;
import com.subho.bloghub.client.enums.BlockType;
import com.subho.bloghub.server.entity.blogs.BlogBlocks;
import com.subho.bloghub.server.entity.blogs.Blogs;
import com.subho.bloghub.server.mapper.GenericMapper;
import org.springframework.stereotype.Component;

/**
 * Not registered as the "primary" mapper for {@code BlogBlocks} in the
 * generic sense (there's no single natural response DTO independent of its
 * parent blog), but still implements {@link GenericMapper} for consistency.
 * {@link #toResponse(BlogBlocks)} is exposed for completeness; callers
 * needing a list typically go through {@code BlogMapper#toFullResponse}
 * instead, which controls block ordering across the whole blog.
 */
@Component
public class BlogBlockMapper implements GenericMapper<BlogBlocks, BlogBlockRequestDTO, BlogResponseDTO.BlogBlockResponseDTO> {

    @Override
    public BlogBlocks toEntity(BlogBlockRequestDTO request) {
        if (request == null) {
            return null;
        }
        return BlogBlocks.builder()
                .type(request.getType().name().toLowerCase())
                .content(request.getContent())
                .caption(request.getCaption())
                .filter(request.getFilter())
                .position(request.getPosition())
                .build();
    }

    public BlogBlocks toEntity(BlogBlockRequestDTO request, Blogs blog) {
        BlogBlocks entity = toEntity(request);
        if (entity != null) {
            entity.setBlog(blog);
        }
        return entity;
    }

    @Override
    public BlogResponseDTO.BlogBlockResponseDTO toResponse(BlogBlocks entity) {
        if (entity == null) {
            return null;
        }
        return BlogResponseDTO.BlogBlockResponseDTO.builder()
                .id(entity.getId())
                .type(BlockType.valueOf(entity.getType().toUpperCase()))
                .content(entity.getContent())
                .caption(entity.getCaption())
                .filter(entity.getFilter())
                .position(entity.getPosition())
                .build();
    }
}
