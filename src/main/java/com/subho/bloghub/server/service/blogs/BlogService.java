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

    Page<BlogCardResponseDTO> getFeed(String feed, String tag, Pageable pageable);

    BlogResponseDTO createBlog(CreateBlogRequestDTO request);

    BlogResponseDTO getFullBlog(String id);

    BlogResponseDTO updateBlog(String id, UpdateBlogRequestDTO request);

    void deleteBlog(String id);

    Page<BlogCardResponseDTO> getTrendingBlogs(Pageable pageable);

    ReactionCountResponseDTO addReaction(String blogId, ReactionRequestDTO request);

    ReactionCountResponseDTO removeReaction(String blogId);

    Page<BlogCardResponseDTO> getBookmarkedBlogs(Pageable pageable);

    void addBookmark(String blogId);

    void removeBookmark(String blogId);

    Page<BlogCardResponseDTO> searchBlogs(String query, Pageable pageable);

    Page<BlogCardResponseDTO> getBlogsByTag(String tagName, Pageable pageable);

    Page<BlogCardResponseDTO> getBlogsByAuthorHandle(String handle, Pageable pageable);
}
