package com.subho.bloghub.server.service.comments;

import com.subho.bloghub.client.dtos.comments.CommentResponseDTO;
import com.subho.bloghub.client.dtos.comments.CreateCommentRequestDTO;
import com.subho.bloghub.client.dtos.reaction.ReactionCountResponseDTO;
import com.subho.bloghub.client.dtos.reaction.ReactionRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {

    Page<CommentResponseDTO> getTopComments(String accessToken, String blogId, Pageable pageable);

    CommentResponseDTO postTopComment(String accessToken, String blogId, CreateCommentRequestDTO request);

    CommentResponseDTO replyToComment(String accessToken, String blogId, String commentId, CreateCommentRequestDTO request);

    CommentResponseDTO updateComment(String accessToken, String commentId, CreateCommentRequestDTO request);

    void deleteComment(String accessToken, String commentId);

    ReactionCountResponseDTO addReaction(String accessToken, String commentId, ReactionRequestDTO request);

    ReactionCountResponseDTO removeReaction(String accessToken, String commentId);
}
