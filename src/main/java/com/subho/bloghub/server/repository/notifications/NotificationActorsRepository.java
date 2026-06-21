package com.subho.bloghub.server.repository.notifications;

import com.subho.bloghub.server.entity.notifications.NotificationActors;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationActorsRepository extends JpaRepository<NotificationActors, UUID> {

    @EntityGraph(attributePaths = "actor")
    List<NotificationActors> findByNotification_IdIn(List<UUID> notificationIds);
}
