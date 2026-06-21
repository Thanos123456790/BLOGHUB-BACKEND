package com.subho.bloghub.server.service.tags;

import com.subho.bloghub.client.dtos.blogs.BlogCardResponseDTO;
import com.subho.bloghub.client.dtos.tags.TagResponseDTO;
import com.subho.bloghub.client.dtos.users.UserProfileResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchAndTagsService {

    Page<BlogCardResponseDTO> searchBlogs(String query, Pageable pageable);

    Page<UserProfileResponseDTO> searchUsers(String query, Pageable pageable);

    Page<TagResponseDTO> getTrendingTags(Pageable pageable);

    Page<BlogCardResponseDTO> getBlogsByTag(String tagName, Pageable pageable);
}
