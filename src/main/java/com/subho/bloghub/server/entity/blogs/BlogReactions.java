package com.subho.bloghub.server.entity.blogs;

import com.subho.bloghub.server.entity.users.Users;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "blog_reactions", indexes = {
        @Index(name = "idx_blog_reactions_unique_pair", columnList = "user_id, blog_id", unique = true),
        @Index(name = "idx_blog_reactions_blog_id", columnList = "blog_id")
})
@Check(constraints = "reaction_type IN ('like','clap','love','insightful')")
public class BlogReactions {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, unique = true)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blogs blog;

    @Column(nullable = false, length = 20)
    private String reactionType;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate(){
        createdAt = Instant.now();
    }
}
