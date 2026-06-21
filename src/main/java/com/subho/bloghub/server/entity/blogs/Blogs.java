package com.subho.bloghub.server.entity.blogs;

import com.subho.bloghub.server.entity.base.BaseEntity;
import com.subho.bloghub.server.entity.users.Users;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "blogs", indexes = {
        @Index(name = "idx_blogs_author_id", columnList = "author_id"),
        @Index(name = "idx_blogs_created_at", columnList = "created_at")
})
public class Blogs extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Users author;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String excerpt;

    @Column(columnDefinition = "TEXT")
    private String coverImageUrl;

    @Column(length = 30)
    private String coverFilter;

    @Column(nullable = false)
    @ColumnDefault("1")
    private Integer readTimeMinutes;
}
