package com.subho.bloghub.client.apis.blogs;

import com.subho.bloghub.client.dtos.blogs.BlogCardResponseDTO;
import com.subho.bloghub.client.dtos.blogs.BlogResponseDTO;
import com.subho.bloghub.client.dtos.blogs.CreateBlogRequestDTO;
import com.subho.bloghub.client.dtos.blogs.UpdateBlogRequestDTO;
import com.subho.bloghub.client.dtos.reaction.ReactionCountResponseDTO;
import com.subho.bloghub.client.dtos.reaction.ReactionRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Blogs APIs", description = "Blog feed, CRUD, reactions, and bookmarks")
@RequestMapping("/api/v1/blogs")
public interface BlogsAPI {

    @Operation(
            summary = "Get feed (for-you / following)",
            description = "Returns a paginated feed of blogs, either ranked for-you (default, most recent) " +
                    "or restricted to authors the current user follows. Optionally filter by tag."
    )
    @ApiResponse(responseCode = "200", description = "Feed returned successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @GetMapping()
    ResponseEntity<Page<BlogCardResponseDTO>> getFeed(
            String accessToken,
            @RequestParam(defaultValue = "for-you") String feed,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    );


    @Operation(
            summary = "Create a new blog",
            description = "Publishes a new blog post for the authenticated user, including its content blocks and tags."
    )
    @ApiResponse(responseCode = "201", description = "Blog created successfully")
    @ApiResponse(responseCode = "400", description = "Request validation failed")
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @PostMapping()
    ResponseEntity<BlogResponseDTO> createBlog(String accessToken, @Valid @RequestBody CreateBlogRequestDTO blog);


    @Operation(
            summary = "Get a single blog with blocks, comments",
            description = "Returns the full detail of a blog post, including ordered content blocks, tags, " +
                    "reaction counts, and the caller's bookmark/reaction state. Public — no auth required."
    )
    @ApiResponse(responseCode = "200", description = "Blog returned successfully")
    @ApiResponse(responseCode = "404", description = "Blog not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @GetMapping("/{id}")
    ResponseEntity<BlogResponseDTO> getFullBlog(String accessToken, @PathVariable String id);

    @Operation(
            summary = "Edit a blog",
            description = "Updates an existing blog. Only the author may edit. All fields are optional; " +
                    "omitted fields are left unchanged, except 'blocks'/'tags' which fully replace the existing set when present."
    )
    @ApiResponse(responseCode = "200", description = "Blog updated successfully")
    @ApiResponse(responseCode = "400", description = "Request validation failed")
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token")
    @ApiResponse(responseCode = "403", description = "Caller is not the blog's author")
    @ApiResponse(responseCode = "404", description = "Blog not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @PutMapping("/{id}")
    ResponseEntity<BlogResponseDTO> updateBlog(String accessToken, @PathVariable String id, @Valid @RequestBody UpdateBlogRequestDTO blog);


    @Operation(
            summary = "Delete a blog",
            description = "Deletes a blog and all of its dependent rows (blocks, tags, reactions, comments, bookmarks). Only the author may delete."
    )
    @ApiResponse(responseCode = "204", description = "Blog deleted successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token")
    @ApiResponse(responseCode = "403", description = "Caller is not the blog's author")
    @ApiResponse(responseCode = "404", description = "Blog not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteBlog(String accessToken, @PathVariable String id);

    @Operation(
            summary = "Get trending blogs",
            description = "Returns blogs ranked by reaction volume over a recent rolling window. Public — no auth required."
    )
    @ApiResponse(responseCode = "200", description = "Trending blogs returned successfully")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @GetMapping("/trending")
    ResponseEntity<Page<BlogCardResponseDTO>> getTrendingBlogs(
            String accessToken,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    );

    @Operation(
            summary = "Add or change a reaction",
            description = "Sets (or replaces) the current user's reaction on a blog and returns the updated counts."
    )
    @ApiResponse(responseCode = "200", description = "Reaction applied successfully")
    @ApiResponse(responseCode = "400", description = "Invalid reaction type")
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token")
    @ApiResponse(responseCode = "404", description = "Blog not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @PostMapping("/{id}/reactions")
    ResponseEntity<ReactionCountResponseDTO> addReactionsOnBlog(String accessToken, @PathVariable String id, @Valid @RequestBody ReactionRequestDTO reaction);


    @Operation(
            summary = "Remove reaction",
            description = "Removes the current user's reaction from a blog, if any, and returns the updated counts."
    )
    @ApiResponse(responseCode = "200", description = "Reaction removed successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token")
    @ApiResponse(responseCode = "404", description = "Blog not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @DeleteMapping("/{id}/reactions")
    ResponseEntity<ReactionCountResponseDTO> removeReactionsOnBlog(String accessToken, @PathVariable String id);


    @Operation(
            summary = "Get all bookmarked blogs",
            description = "Returns a paginated list of blogs the current user has bookmarked, most recent first."
    )
    @ApiResponse(responseCode = "200", description = "Bookmarks returned successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @GetMapping("/bookmarks")
    ResponseEntity<Page<BlogCardResponseDTO>> getBookmarkedBlogs(
            String accessToken,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    );


    @Operation(
            summary = "Bookmark a blog",
            description = "Adds the blog to the current user's bookmarks. Idempotent — bookmarking an already-bookmarked blog is a no-op."
    )
    @ApiResponse(responseCode = "204", description = "Blog bookmarked successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token")
    @ApiResponse(responseCode = "404", description = "Blog not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @PostMapping("/{id}/bookmark")
    ResponseEntity<Void> addBookmarkOnBlog(String accessToken, @PathVariable String id);


    @Operation(
            summary = "Remove bookmark",
            description = "Removes the blog from the current user's bookmarks, if present."
    )
    @ApiResponse(responseCode = "204", description = "Bookmark removed successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token")
    @ApiResponse(responseCode = "404", description = "Blog not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @DeleteMapping("/{id}/bookmark")
    ResponseEntity<Void> removeBookmarkFromBlog(String accessToken, @PathVariable String id);

}
