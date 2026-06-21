package com.subho.bloghub.server.controller.blogs;

import com.subho.bloghub.client.apis.blogs.BlogsAPI;
import com.subho.bloghub.client.dtos.blogs.BlogCardResponseDTO;
import com.subho.bloghub.client.dtos.blogs.BlogResponseDTO;
import com.subho.bloghub.client.dtos.blogs.CreateBlogRequestDTO;
import com.subho.bloghub.client.dtos.blogs.UpdateBlogRequestDTO;
import com.subho.bloghub.client.dtos.reaction.ReactionCountResponseDTO;
import com.subho.bloghub.client.dtos.reaction.ReactionRequestDTO;
import com.subho.bloghub.server.common.PageRequestFactory;
import com.subho.bloghub.server.service.blogs.BlogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class BlogController implements BlogsAPI {

    private final BlogService blogService;
    private final PageRequestFactory pageRequestFactory;

    @Override
    public ResponseEntity<Page<BlogCardResponseDTO>> getFeed(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @RequestParam(defaultValue = "for-you") String feed,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = pageRequestFactory.of(page, size);
        return ResponseEntity.ok(blogService.getFeed(feed, tag, pageable));
    }

    @Override
    public ResponseEntity<BlogResponseDTO> createBlog(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @Valid @RequestBody CreateBlogRequestDTO blog) {
        return ResponseEntity.status(HttpStatus.CREATED).body(blogService.createBlog(blog));
    }

    @Override
    public ResponseEntity<BlogResponseDTO> getFullBlog(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @PathVariable String id) {
        return ResponseEntity.ok(blogService.getFullBlog(id));
    }

    @Override
    public ResponseEntity<BlogResponseDTO> updateBlog(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @PathVariable String id,
            @Valid @RequestBody UpdateBlogRequestDTO blog) {
        return ResponseEntity.ok(blogService.updateBlog(id, blog));
    }

    @Override
    public ResponseEntity<Void> deleteBlog(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @PathVariable String id) {
        blogService.deleteBlog(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Page<BlogCardResponseDTO>> getTrendingBlogs(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = pageRequestFactory.of(page, size);
        return ResponseEntity.ok(blogService.getTrendingBlogs(pageable));
    }

    @Override
    public ResponseEntity<ReactionCountResponseDTO> addReactionsOnBlog(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @PathVariable String id,
            @Valid @RequestBody ReactionRequestDTO reaction) {
        return ResponseEntity.ok(blogService.addReaction(id, reaction));
    }

    @Override
    public ResponseEntity<ReactionCountResponseDTO> removeReactionsOnBlog(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @PathVariable String id) {
        return ResponseEntity.ok(blogService.removeReaction(id));
    }

    @Override
    public ResponseEntity<Page<BlogCardResponseDTO>> getBookmarkedBlogs(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = pageRequestFactory.of(page, size);
        return ResponseEntity.ok(blogService.getBookmarkedBlogs(pageable));
    }

    @Override
    public ResponseEntity<Void> addBookmarkOnBlog(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @PathVariable String id) {
        blogService.addBookmark(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> removeBookmarkFromBlog(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @PathVariable String id) {
        blogService.removeBookmark(id);
        return ResponseEntity.noContent().build();
    }
}
