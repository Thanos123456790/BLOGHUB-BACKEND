package com.subho.bloghub.server.repository.notifications;

import com.subho.bloghub.server.entity.notifications.Notifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface NotificationsRepository extends JpaRepository<Notifications, UUID> {

    @EntityGraph(attributePaths = "blog")
    Page<Notifications> findByRecipient_IdOrderByCreatedAtDesc(UUID recipientId, Pageable pageable);

    @Modifying
    @Query("UPDATE Notifications n SET n.isRead = true WHERE n.recipient.id = :recipientId AND n.isRead = false")
    int markAllAsRead(@Param("recipientId") UUID recipientId);

    @Modifying
    @Query("UPDATE Notifications n SET n.isRead = true WHERE n.id = :id AND n.recipient.id = :recipientId")
    int markAsRead(@Param("id") UUID id, @Param("recipientId") UUID recipientId);

    /** VLN-11 FIX: Bulk delete all notifications referencing a blog being deleted. */
    @Modifying
    void deleteByBlog_Id(UUID blogId);

    @Query("""
            SELECT COUNT(n) > 0
            FROM Notifications n
            JOIN NotificationActors na ON na.notification = n
            WHERE n.recipient.id = :recipientId
              AND na.actor.id    = :actorId
              AND n.type         = :type
              AND ((:blogId    IS NULL AND n.blog    IS NULL) OR n.blog.id    = :blogId)
              AND ((:commentId IS NULL AND n.comment IS NULL) OR n.comment.id = :commentId)
              AND n.createdAt   >= :since
            """)
    boolean existsDuplicate(
            @Param("recipientId") UUID recipientId,
            @Param("actorId")     UUID actorId,
            @Param("type")        String type,
            @Param("blogId")      UUID blogId,
            @Param("commentId")   UUID commentId,
            @Param("since")       Instant since
    );
}
