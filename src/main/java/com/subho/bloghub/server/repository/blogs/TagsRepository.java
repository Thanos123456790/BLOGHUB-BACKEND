package com.subho.bloghub.server.repository.blogs;

import com.subho.bloghub.server.entity.blogs.Tags;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TagsRepository extends JpaRepository<Tags, UUID> {

    Optional<Tags> findByNameIgnoreCase(String name);

    List<Tags> findByNameInIgnoreCase(List<String> names);

    @Query("""
            SELECT t.name as name, COUNT(bt) as postCount
            FROM Tags t
            JOIN BlogTags bt ON bt.tag = t
            GROUP BY t.name
            ORDER BY COUNT(bt) DESC
            """)
    Page<TagCount> findTrendingTags(Pageable pageable);

    interface TagCount {
        String getName();
        long getPostCount();
    }
}
