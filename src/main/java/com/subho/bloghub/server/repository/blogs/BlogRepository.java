package com.subho.bloghub.server.repository.blogs;

import com.subho.bloghub.server.entity.blogs.Blogs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BlogRepository extends JpaRepository<Blogs, UUID> {

    Page<Blogs> findByAuthor_Handle(String handle, Pageable pageable);

    long countByAuthor_Id(UUID authorId);

    /**
     * Fetches the blog together with its author in a single query (LEFT JOIN
     * FETCH) to avoid the N+1 lazy-load that {@code findById} + later
     * {@code getAuthor()} access would otherwise trigger.
     */
    @EntityGraph(attributePaths = "author")
    Optional<Blogs> findWithAuthorById(UUID id);

    @EntityGraph(attributePaths = "author")
    Page<Blogs> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "author")
    Page<Blogs> findByAuthor_HandleOrderByCreatedAtDesc(String handle, Pageable pageable);

    /**
     * "For you" / general feed ordered by recency. Kept as a dedicated query
     * (rather than reusing findAll) so feed ranking can evolve independently
     * later (e.g. adding a trending score) without touching callers.
     */
    @EntityGraph(attributePaths = "author")
    @Query("SELECT b FROM Blogs b ORDER BY b.createdAt DESC")
    Page<Blogs> findFeed(Pageable pageable);

    /**
     * "Following" feed — blogs authored by anyone the given user follows.
     */
    @EntityGraph(attributePaths = "author")
    @Query("""
            SELECT b FROM Blogs b
            WHERE b.author.id IN (
                SELECT f.following.id FROM Follows f WHERE f.follower.id = :viewerId
            )
            ORDER BY b.createdAt DESC
            """)
    Page<Blogs> findFollowingFeed(@Param("viewerId") UUID viewerId, Pageable pageable);

    @EntityGraph(attributePaths = "author")
    @Query("""
            SELECT b FROM Blogs b
            JOIN BlogTags bt ON bt.blog = b
            JOIN bt.tag t
            WHERE LOWER(t.name) = LOWER(:tagName)
            ORDER BY b.createdAt DESC
            """)
    Page<Blogs> findByTagName(@Param("tagName") String tagName, Pageable pageable);

    /**
     * Trending = most total reactions in the last N days. Computed at query
     * time (no stored counter) to stay consistent with the 3NF design notes
     * in backend_design.md.
     */
    @EntityGraph(attributePaths = "author")
    @Query("""
            SELECT b FROM Blogs b
            LEFT JOIN BlogReactions r ON r.blog = b AND r.createdAt >= :since
            GROUP BY b
            ORDER BY COUNT(r) DESC, MAX(b.createdAt) DESC
            """)
    Page<Blogs> findTrending(@Param("since") java.time.Instant since, Pageable pageable);

    @EntityGraph(attributePaths = "author")
    @Query("""
            SELECT b FROM Blogs b
            WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(b.excerpt) LIKE LOWER(CONCAT('%', :query, '%'))
            ORDER BY b.createdAt DESC
            """)
    Page<Blogs> searchByTitleOrExcerpt(@Param("query") String query, Pageable pageable);

    List<Blogs> findByIdIn(List<UUID> ids);
}
