package com.subho.bloghub.server.repository.blogs;

import com.subho.bloghub.server.entity.blogs.BlogBlocks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BlogBlocksRepository extends JpaRepository<BlogBlocks, UUID> {

    List<BlogBlocks> findByBlog_IdOrderByPositionAsc(UUID blogId);

    /**
     * Bulk-fetches ordered blocks for several blogs in one round trip (used
     * when rendering a page of full blogs, avoiding one query per blog).
     */
    List<BlogBlocks> findByBlog_IdInOrderByBlog_IdAscPositionAsc(List<UUID> blogIds);

    @Modifying
    @Query("DELETE FROM BlogBlocks bb WHERE bb.blog.id = :blogId")
    void deleteAllByBlogId(@Param("blogId") UUID blogId);
}
