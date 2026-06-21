package com.subho.bloghub.server.repository.blogs;

import com.subho.bloghub.server.entity.blogs.BlogReactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BlogReactionsRepository extends JpaRepository<BlogReactions, UUID> {

    Optional<BlogReactions> findByUser_IdAndBlog_Id(UUID userId, UUID blogId);

    @Modifying
    void deleteByUser_IdAndBlog_Id(UUID userId, UUID blogId);

    @Query("""
            SELECT r.reactionType as reactionType, COUNT(r) as count
            FROM BlogReactions r
            WHERE r.blog.id = :blogId
            GROUP BY r.reactionType
            """)
    List<ReactionTypeCount> countByBlogId(@Param("blogId") UUID blogId);

    @Query("""
            SELECT r.blog.id as blogId, r.reactionType as reactionType, COUNT(r) as count
            FROM BlogReactions r
            WHERE r.blog.id IN :blogIds
            GROUP BY r.blog.id, r.reactionType
            """)
    List<BlogReactionTypeCount> countByBlogIdIn(@Param("blogIds") List<UUID> blogIds);

    @Query("""
            SELECT r.blog.id as blogId, r.reactionType as reactionType
            FROM BlogReactions r
            WHERE r.user.id = :userId AND r.blog.id IN :blogIds
            """)
    List<UserBlogReaction> findUserReactions(@Param("userId") UUID userId, @Param("blogIds") List<UUID> blogIds);

    interface ReactionTypeCount {
        String getReactionType();
        long getCount();
    }

    interface BlogReactionTypeCount {
        UUID getBlogId();
        String getReactionType();
        long getCount();
    }

    interface UserBlogReaction {
        UUID getBlogId();
        String getReactionType();
    }
}
