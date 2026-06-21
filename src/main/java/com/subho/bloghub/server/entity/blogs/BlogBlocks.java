package com.subho.bloghub.server.entity.blogs;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "blog_blocks", indexes = {
        @Index(name = "idx_blog_blocks_blog_id", columnList = "blog_id"),
        @Index(name = "idx_blog_blocks_order", columnList = "blog_id, position")
})
@Check(constraints = "type IN ('paragraph', 'image', 'quote', 'heading')")
public class BlogBlocks {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, unique = true)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blogs blog;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String caption;

    @Column(length = 30)
    private String filter;

    @Column(nullable = false)
    private Integer position;
}
