package com.subho.bloghub.client.apis.comments;


import com.subho.bloghub.client.dtos.comments.CommentResponseDTO;
import com.subho.bloghub.client.dtos.comments.CreateCommentRequestDTO;
import com.subho.bloghub.client.dtos.reaction.ReactionCountResponseDTO;
import com.subho.bloghub.client.dtos.reaction.ReactionRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Comment APIs", description = "Comments, replies, and comment reactions")
@RequestMapping("/api/v1/comments")
public interface CommentsAPI {

    @Operation(
            summary = "Get top-level comments with replies",
            description = "Returns a paginated list of top-level comments for a blog, each with its direct replies nested inline. Public — no auth required."
    )
    @ApiResponse(responseCode = "200", description = "Comments returned successfully")
    @ApiResponse(responseCode = "404", description = "Blog not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @GetMapping("/blogs/{blogId}")
    ResponseEntity<Page<CommentResponseDTO>> getTopComments(
            String accessToken,
            @PathVariable String blogId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    );


    @Operation(
            summary = "Post a top-level comment",
            description = "Creates a new top-level comment on a blog, optionally @mentioning other users."
    )
    @ApiResponse(responseCode = "201", description = "Comment created successfully")
    @ApiResponse(responseCode = "400", description = "Request validation failed")
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token")
    @ApiResponse(responseCode = "404", description = "Blog not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @PostMapping("/blogs/{blogId}")
    ResponseEntity<CommentResponseDTO> postTopComment(String accessToken, @Valid @RequestBody CreateCommentRequestDTO comment, @PathVariable String blogId);


    @Operation(
            summary = "Reply to a comment",
            description = "Creates a reply nested under an existing top-level comment on the same blog."
    )
    @ApiResponse(responseCode = "201", description = "Reply created successfully")
    @ApiResponse(responseCode = "400", description = "Request validation failed")
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token")
    @ApiResponse(responseCode = "404", description = "Blog or parent comment not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @PostMapping("/blogs/{blogId}/comments/{commentId}/replies")
    ResponseEntity<CommentResponseDTO> replyComment(String accessToken, @Valid @RequestBody CreateCommentRequestDTO comment, @PathVariable String commentId, @PathVariable String blogId);


    @Operation(
            summary = "Edit a comment",
            description = "Updates the content of an existing comment. Only the author may edit."
    )
    @ApiResponse(responseCode = "200", description = "Comment updated successfully")
    @ApiResponse(responseCode = "400", description = "Request validation failed")
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token")
    @ApiResponse(responseCode = "403", description = "Caller is not the comment's author")
    @ApiResponse(responseCode = "404", description = "Comment not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @PutMapping("/{id}")
    ResponseEntity<CommentResponseDTO> updateComment(String accessToken, @PathVariable String id, @Valid @RequestBody CreateCommentRequestDTO comment);


    @Operation(
            summary = "Delete a comment",
            description = "Deletes a comment (and, transitively, its replies). Only the author may delete."
    )
    @ApiResponse(responseCode = "204", description = "Comment deleted successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token")
    @ApiResponse(responseCode = "403", description = "Caller is not the comment's author")
    @ApiResponse(responseCode = "404", description = "Comment not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteComment(String accessToken, @PathVariable String id);


    @Operation(
            summary = "Add or change comment reaction",
            description = "Sets (or replaces) the current user's reaction on a comment and returns the updated counts."
    )
    @ApiResponse(responseCode = "200", description = "Reaction applied successfully")
    @ApiResponse(responseCode = "400", description = "Invalid reaction type")
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token")
    @ApiResponse(responseCode = "404", description = "Comment not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @PostMapping("/{id}/reactions")
    ResponseEntity<ReactionCountResponseDTO> addReactionOnComment(String accessToken, @PathVariable String id, @Valid @RequestBody ReactionRequestDTO reaction);


    @Operation(
            summary = "Remove comment reaction",
            description = "Removes the current user's reaction from a comment, if any, and returns the updated counts."
    )
    @ApiResponse(responseCode = "200", description = "Reaction removed successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token")
    @ApiResponse(responseCode = "404", description = "Comment not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @DeleteMapping("/{id}/reactions")
    ResponseEntity<ReactionCountResponseDTO> removeReactionOnComment(String accessToken, @PathVariable String id);


}
