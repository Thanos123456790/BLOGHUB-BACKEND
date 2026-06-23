package com.subho.bloghub.server.service.blogs;

import com.subho.bloghub.client.dtos.blogs.BlogCardResponseDTO;
import com.subho.bloghub.client.dtos.blogs.BlogResponseDTO;
import com.subho.bloghub.client.dtos.blogs.CreateBlogRequestDTO;
import com.subho.bloghub.client.dtos.blogs.UpdateBlogRequestDTO;
import com.subho.bloghub.client.dtos.reaction.ReactionCountResponseDTO;
import com.subho.bloghub.client.dtos.reaction.ReactionRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BlogService {

    Page<BlogCardResponseDTO> getFeed(String accessToken, String feed, String tag, Pageable pageable);

    BlogResponseDTO createBlog(String accessToken, CreateBlogRequestDTO request);

    BlogResponseDTO getFullBlog(String accessToken, String id);

    BlogResponseDTO updateBlog(String accessToken, String id, UpdateBlogRequestDTO request);

    void deleteBlog(String accessToken, String id);

    Page<BlogCardResponseDTO> getTrendingBlogs(Pageable pageable, String accessToken);

    ReactionCountResponseDTO addReaction(String accessToken, String blogId, ReactionRequestDTO request);

    ReactionCountResponseDTO removeReaction(String accessToken, String blogId);

    Page<BlogCardResponseDTO> getBookmarkedBlogs(String accessToken, Pageable pageable);

    void addBookmark(String accessToken, String blogId);

    void removeBookmark(String accessToken, String blogId);

    Page<BlogCardResponseDTO> searchBlogs(String query, Pageable pageable, String accessToken);

    Page<BlogCardResponseDTO> getBlogsByTag(String tagName, Pageable pageable, String accessToken);

    Page<BlogCardResponseDTO> getBlogsByAuthorHandle(String handle, Pageable pageable, String accessToken);
}
