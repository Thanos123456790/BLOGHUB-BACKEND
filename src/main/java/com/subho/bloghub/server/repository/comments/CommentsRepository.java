package com.subho.bloghub.server.repository.comments;

import com.subho.bloghub.server.entity.comments.Comments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CommentsRepository extends JpaRepository<Comments, UUID> {

    @EntityGraph(attributePaths = "author")
    Page<Comments> findByBlog_IdAndParentIsNullOrderByCreatedAtAsc(UUID blogId, Pageable pageable);

    @EntityGraph(attributePaths = "author")
    List<Comments> findByParent_IdInOrderByCreatedAtAsc(List<UUID> parentIds);

    long countByBlog_Id(UUID blogId);

    @Query("SELECT c.blog.id as blogId, COUNT(c) as count FROM Comments c WHERE c.blog.id IN :blogIds GROUP BY c.blog.id")
    List<BlogCommentCount> countByBlogIdIn(@Param("blogIds") List<UUID> blogIds);

    interface BlogCommentCount {
        UUID getBlogId();
        long getCount();
    }
}
