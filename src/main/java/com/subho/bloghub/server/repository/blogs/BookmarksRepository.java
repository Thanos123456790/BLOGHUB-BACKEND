package com.subho.bloghub.server.repository.blogs;

import com.subho.bloghub.server.entity.blogs.Bookmarks;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BookmarksRepository extends JpaRepository<Bookmarks, UUID> {

    boolean existsByUser_IdAndBlog_Id(UUID userId, UUID blogId);

    @Modifying
    void deleteByUser_IdAndBlog_Id(UUID userId, UUID blogId);

    @EntityGraph(attributePaths = {"blog", "blog.author"})
    Page<Bookmarks> findByUser_IdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    @Query("SELECT b.blog.id FROM Bookmarks b WHERE b.user.id = :userId AND b.blog.id IN :blogIds")
    List<UUID> findBookmarkedBlogIds(@Param("userId") UUID userId, @Param("blogIds") List<UUID> blogIds);
}
