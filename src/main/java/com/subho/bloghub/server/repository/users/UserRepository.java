package com.subho.bloghub.server.repository.users;

import com.subho.bloghub.server.entity.users.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<Users, UUID> {

    Optional<Users> findByHandle(String handle);

    boolean existsByHandle(String handle);

    List<Users> findByIdIn(List<UUID> ids);

    /**
     * Ranks users by follower count, most-followed first. This is a deliberately simple v1 of
     * "suggested users" — no personalization (e.g. excluding people the viewer already follows,
     * mutual-follow signals, shared tags) since that needs a resolved current user.
     */
    @Query("""
            SELECT u FROM Users u
            LEFT JOIN Follows f ON f.following = u
            GROUP BY u
            ORDER BY COUNT(f) DESC
            """)
    Page<Users> findSuggested(Pageable pageable);

    @Query("""
            SELECT u FROM Users u
            WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(u.handle) LIKE LOWER(CONCAT('%', :query, '%'))
            ORDER BY u.name ASC
            """)
    Page<Users> searchByNameOrHandle(@Param("query") String query, Pageable pageable);
}
