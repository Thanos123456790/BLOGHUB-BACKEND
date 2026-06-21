package com.subho.bloghub.server.entity.notifications;

import com.subho.bloghub.server.entity.blogs.Blogs;
import com.subho.bloghub.server.entity.comments.Comments;
import com.subho.bloghub.server.entity.users.Users;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notifications_recipient_id", columnList = "recipient_id"),
        @Index(name = "idx_notifications_unread", columnList = "recipient_id, is_read"),
        @Index(name = "idx_notifications_created_at", columnList = "created_at")
})
@Check(constraints = "type IN ('follow','reaction','comment','reply','mention','comment_reaction')")
public class Notifications {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, unique = true)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Users recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = true)
    private Blogs blog;

    @Column(nullable = false, length = 30)
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = true)
    private Comments comment;

    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean isRead;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate(){
        createdAt = Instant.now();
    }
}
