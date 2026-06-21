package com.subho.bloghub.server.entity.comments;

import com.subho.bloghub.server.entity.base.BaseEntity;
import com.subho.bloghub.server.entity.blogs.Blogs;
import com.subho.bloghub.server.entity.users.Users;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "comments", indexes = {
        @Index(name = "idx_comments_blog_id", columnList = "blog_id"),
        @Index(name = "idx_comments_author_id", columnList = "author_id"),
        @Index(name = "idx_comments_parent_id", columnList = "parent_id")
})
public class Comments extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blogs blog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Users author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = true)
    private Comments parent;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
}
