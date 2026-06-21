package com.subho.bloghub.server.repository.comments;

import com.subho.bloghub.server.entity.comments.CommentReactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentReactionsRepository extends JpaRepository<CommentReactions, UUID> {

    Optional<CommentReactions> findByUser_IdAndComment_Id(UUID userId, UUID commentId);

    @Modifying
    void deleteByUser_IdAndComment_Id(UUID userId, UUID commentId);

    @Query("""
            SELECT r.comment.id as commentId, r.reactionType as reactionType, COUNT(r) as count
            FROM CommentReactions r
            WHERE r.comment.id IN :commentIds
            GROUP BY r.comment.id, r.reactionType
            """)
    List<CommentReactionTypeCount> countByCommentIdIn(@Param("commentIds") List<UUID> commentIds);

    @Query("""
            SELECT r.comment.id as commentId, r.reactionType as reactionType
            FROM CommentReactions r
            WHERE r.user.id = :userId AND r.comment.id IN :commentIds
            """)
    List<UserCommentReaction> findUserReactions(@Param("userId") UUID userId, @Param("commentIds") List<UUID> commentIds);

    interface CommentReactionTypeCount {
        UUID getCommentId();
        String getReactionType();
        long getCount();
    }

    interface UserCommentReaction {
        UUID getCommentId();
        String getReactionType();
    }
}
