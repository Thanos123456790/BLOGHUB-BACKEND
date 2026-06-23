package com.subho.bloghub.server.controller.search_and_tags;

import com.subho.bloghub.client.apis.search_and_tags.SearchAndTagsAPI;
import com.subho.bloghub.client.dtos.blogs.BlogCardResponseDTO;
import com.subho.bloghub.client.dtos.tags.TagResponseDTO;
import com.subho.bloghub.client.dtos.users.UserProfileResponseDTO;
import com.subho.bloghub.server.common.PageRequestFactory;
import com.subho.bloghub.server.service.tags.SearchAndTagsService;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
public class SearchAndTagsController implements SearchAndTagsAPI {

    private final SearchAndTagsService searchAndTagsService;
    private final PageRequestFactory pageRequestFactory;

    @Override
    public ResponseEntity<Page<BlogCardResponseDTO>> getBlogsOnSearch(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = pageRequestFactory.of(page, size);
        return ResponseEntity.ok(searchAndTagsService.searchBlogs(query, pageable, accessToken));
    }

    @Override
    public ResponseEntity<Page<UserProfileResponseDTO>> getProfilesByNameOrHandle(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = pageRequestFactory.of(page, size);
        return ResponseEntity.ok(searchAndTagsService.searchUsers(query, pageable));
    }

    @Override
    public ResponseEntity<Page<TagResponseDTO>> getTrendingTags(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = pageRequestFactory.of(page, size);
        return ResponseEntity.ok(searchAndTagsService.getTrendingTags(pageable));
    }

    @Override
    public ResponseEntity<Page<BlogCardResponseDTO>> getSpecificTaggedBlogs(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @PathVariable String tagName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = pageRequestFactory.of(page, size);
        return ResponseEntity.ok(searchAndTagsService.getBlogsByTag(tagName, pageable, accessToken));
    }
}
