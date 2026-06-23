package com.subho.bloghub.server.controller.comments;

import com.subho.bloghub.client.apis.comments.CommentsAPI;
import com.subho.bloghub.client.dtos.comments.CommentResponseDTO;
import com.subho.bloghub.client.dtos.comments.CreateCommentRequestDTO;
import com.subho.bloghub.client.dtos.reaction.ReactionCountResponseDTO;
import com.subho.bloghub.client.dtos.reaction.ReactionRequestDTO;
import com.subho.bloghub.server.common.PageRequestFactory;
import com.subho.bloghub.server.service.comments.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CommentController implements CommentsAPI {

    private final CommentService commentService;
    private final PageRequestFactory pageRequestFactory;

    @Override
    public ResponseEntity<Page<CommentResponseDTO>> getTopComments(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @PathVariable String blogId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = pageRequestFactory.of(page, size);
        return ResponseEntity.ok(commentService.getTopComments(accessToken, blogId, pageable));
    }

    @Override
    public ResponseEntity<CommentResponseDTO> postTopComment(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @Valid @RequestBody CreateCommentRequestDTO comment,
            @PathVariable String blogId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.postTopComment(accessToken, blogId, comment));
    }

    @Override
    public ResponseEntity<CommentResponseDTO> replyComment(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @Valid @RequestBody CreateCommentRequestDTO comment,
            @PathVariable String commentId,
            @PathVariable String blogId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.replyToComment(accessToken, blogId, commentId, comment));
    }

    @Override
    public ResponseEntity<CommentResponseDTO> updateComment(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @PathVariable String id,
            @Valid @RequestBody CreateCommentRequestDTO comment) {
        return ResponseEntity.ok(commentService.updateComment(accessToken, id, comment));
    }

    @Override
    public ResponseEntity<Void> deleteComment(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @PathVariable String id) {
        commentService.deleteComment(accessToken, id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ReactionCountResponseDTO> addReactionOnComment(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @PathVariable String id,
            @Valid @RequestBody ReactionRequestDTO reaction) {
        return ResponseEntity.ok(commentService.addReaction(accessToken, id, reaction));
    }

    @Override
    public ResponseEntity<ReactionCountResponseDTO> removeReactionOnComment(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @PathVariable String id) {
        return ResponseEntity.ok(commentService.removeReaction(accessToken, id));
    }
}
