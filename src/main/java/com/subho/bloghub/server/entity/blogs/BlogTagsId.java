package com.subho.bloghub.server.entity.blogs;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * Composite primary key for blog_tags (blog_id, tag_id) — matches
 * "pk_blog_tags" composite PK in the dbdiagram schema.
 */
@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlogTagsId implements Serializable {

    private UUID blogId;

    private UUID tagId;
}
