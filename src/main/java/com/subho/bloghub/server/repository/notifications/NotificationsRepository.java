package com.subho.bloghub.server.repository.notifications;

import com.subho.bloghub.server.entity.notifications.Notifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
