package com.subho.bloghub.server.entity.blogs;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FIX: this entity was previously missing @Entity and a primary key
 * altogether (it would not have functioned as a JPA entity at all).
 * It now uses an @EmbeddedId composite key (blog_id, tag_id) matching
 * "pk_blog_tags" in the dbdiagram schema, plus the two relations.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "blog_tags", indexes = {
        @Index(name = "idx_blog_tags_tag_id", columnList = "tag_id")
})
public class BlogTags {

    @EmbeddedId
    private BlogTagsId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("blogId")
    @JoinColumn(name = "blog_id", nullable = false)
    private Blogs blog;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id", nullable = false)
    private Tags tag;
}
