package com.subho.bloghub.server.repository.blogs;

import com.subho.bloghub.server.entity.blogs.BlogTags;
import com.subho.bloghub.server.entity.blogs.BlogTagsId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BlogTagsRepository extends JpaRepository<BlogTags, BlogTagsId> {

    @Query("SELECT bt.tag.name FROM BlogTags bt WHERE bt.blog.id = :blogId")
    List<String> findTagNamesByBlogId(@Param("blogId") UUID blogId);

    /**
     * Batch variant — returns (blogId, tagName) pairs for many blogs in one
     * query, so a page of N blogs costs 1 query instead of N for tags.
     */
    @Query("SELECT bt.blog.id as blogId, bt.tag.name as tagName FROM BlogTags bt WHERE bt.blog.id IN :blogIds")
    List<BlogTagProjection> findTagNamesByBlogIdIn(@Param("blogIds") List<UUID> blogIds);

    @Modifying
    @Query("DELETE FROM BlogTags bt WHERE bt.blog.id = :blogId")
    void deleteAllByBlogId(@Param("blogId") UUID blogId);

    interface BlogTagProjection {
        UUID getBlogId();
        String getTagName();
    }
}
