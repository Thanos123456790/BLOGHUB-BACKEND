package com.subho.bloghub.server.entity.comments;


import com.subho.bloghub.server.entity.users.Users;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "comment_mentions", indexes = {
        @Index(name = "idx_comment_mentions_unique", columnList = "comment_id, mentioned_user_id", unique = true),
        @Index(name = "idx_comment_mentions_user_id", columnList = "mentioned_user_id")
})
public class CommentMentions {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, unique = true)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comments comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentioned_user_id", nullable = false)
    private Users mentionedUser;
}
