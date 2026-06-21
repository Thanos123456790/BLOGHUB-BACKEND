package com.subho.bloghub.server.entity.notifications;


import com.subho.bloghub.server.entity.users.Users;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * NEW FILE: this table existed in the dbdiagram schema (notification_actors)
 * but had no corresponding entity class anywhere in the upload. Added to
 * match the schema, including its relations and indexes.
 *
 * Allows grouping multiple actors into one notification, e.g.
 * "3 people reacted to your post".
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "notification_actors", indexes = {
        @Index(name = "idx_notification_actors_unique", columnList = "notification_id, actor_id", unique = true),
        @Index(name = "idx_notification_actors_notif_id", columnList = "notification_id")
})
public class NotificationActors {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, unique = true)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notifications notification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private Users actor;
}
