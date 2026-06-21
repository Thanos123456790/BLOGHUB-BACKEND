package com.subho.bloghub.client.apis.search_and_tags;


import com.subho.bloghub.client.dtos.blogs.BlogCardResponseDTO;
import com.subho.bloghub.client.dtos.tags.TagResponseDTO;
import com.subho.bloghub.client.dtos.users.UserProfileResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Tag(name = "Search and Tags APIs", description = "Full-text search and tag-based discovery")
@RequestMapping("/api/v1")
public interface SearchAndTagsAPI {


    @Operation(
            summary = "Full-text search blogs",
            description = "Searches blog titles and excerpts for the given query string. Public — no auth required."
    )
    @ApiResponse(responseCode = "200", description = "Search results returned successfully")
    @ApiResponse(responseCode = "400", description = "Missing or blank query")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @GetMapping("/search/blogs")
    ResponseEntity<Page<BlogCardResponseDTO>> getBlogsOnSearch(
            String accessToken,
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    );


    @Operation(
            summary = "Search users by name or handle",
            description = "Searches users whose display name or @handle contains the given query string. Public — no auth required."
    )
    @ApiResponse(responseCode = "200", description = "Search results returned successfully")
    @ApiResponse(responseCode = "400", description = "Missing or blank query")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @GetMapping("/search/users")
    ResponseEntity<Page<UserProfileResponseDTO>> getProfilesByNameOrHandle(
            String accessToken,
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    );


    @Operation(
            summary = "Get trending tags with post counts",
            description = "Returns tags ranked by number of blogs using them. Public — no auth required."
    )
    @ApiResponse(responseCode = "200", description = "Trending tags returned successfully")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @GetMapping("/tags/trending")
    ResponseEntity<Page<TagResponseDTO>> getTrendingTags(
            String accessToken,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    );


    @Operation(
            summary = "Get blogs under a specific tag",
            description = "Returns a paginated list of blogs tagged with the given tag name. Public — no auth required."
    )
    @ApiResponse(responseCode = "200", description = "Tagged blogs returned successfully")
    @ApiResponse(responseCode = "404", description = "Tag not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @GetMapping("/tags/{tagName}/blogs")
    ResponseEntity<Page<BlogCardResponseDTO>> getSpecificTaggedBlogs(
            String accessToken,
            @PathVariable String tagName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    );


}
