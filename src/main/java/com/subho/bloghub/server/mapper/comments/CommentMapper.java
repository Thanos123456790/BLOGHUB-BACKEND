package com.subho.bloghub.server.mapper.comments;

import com.subho.bloghub.client.dtos.comments.CommentResponseDTO;
import com.subho.bloghub.client.dtos.comments.CreateCommentRequestDTO;
import com.subho.bloghub.server.entity.comments.Comments;
import com.subho.bloghub.server.mapper.GenericMapper;
import com.subho.bloghub.server.mapper.users.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentMapper implements GenericMapper<Comments, CreateCommentRequestDTO, CommentResponseDTO> {

    private final UserMapper userMapper;

    /**
     * Builds a bare entity from the request. The blog, author, and parent
     * (for replies) are set by the service after resolving the path
     * variables / current user — none of that is available to a mapper.
     */
    @Override
    public Comments toEntity(CreateCommentRequestDTO request) {
        if (request == null) {
            return null;
        }
        return Comments.builder()
                .content(request.getContent())
                .build();
    }

    /**
     * Fulfils the {@link GenericMapper} contract with empty aggregates. Real
     * call sites should use {@link #toResponse(Comments, CommentAggregateContext)}.
     */
    @Override
    public CommentResponseDTO toResponse(Comments entity) {
        return toResponse(entity, CommentAggregateContext.empty());
    }

    public CommentResponseDTO toResponse(Comments entity, CommentAggregateContext context) {
        if (entity == null) {
            return null;
        }
        return CommentResponseDTO.builder()
                .id(entity.getId())
                .author(userMapper.toResponse(entity.getAuthor()))
                .content(entity.getContent())
                .reactions(context.reactions())
                .myReaction(context.myReaction())
                .taggedUsers(context.taggedUsers())
                .replies(context.replies())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
