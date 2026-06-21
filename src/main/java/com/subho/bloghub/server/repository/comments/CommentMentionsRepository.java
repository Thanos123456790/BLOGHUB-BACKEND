package com.subho.bloghub.server.repository.comments;

import com.subho.bloghub.server.entity.comments.CommentMentions;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CommentMentionsRepository extends JpaRepository<CommentMentions, UUID> {

    @EntityGraph(attributePaths = "mentionedUser")
    List<CommentMentions> findByComment_IdIn(List<UUID> commentIds);

    @Modifying
    @Query("DELETE FROM CommentMentions cm WHERE cm.comment.id = :commentId")
    void deleteAllByCommentId(@Param("commentId") UUID commentId);
}
