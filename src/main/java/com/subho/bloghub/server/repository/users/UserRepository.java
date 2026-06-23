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

    Optional<Users> findByClerkUserId(String clerkUserId);

    boolean existsByHandle(String handle);

    List<Users> findByIdIn(List<UUID> ids);

    @Query("""
            SELECT u FROM Users u
            LEFT JOIN Follows f ON f.following = u
            GROUP BY u
            ORDER BY COUNT(f) DESC
            """)
    Page<Users> findSuggested(Pageable pageable);

    /**
     * VLN-08 FIX: Caller must escape LIKE wildcards (%, _, \) before passing
     * the query string. See BlogServiceImpl.escapeLikeWildcards() for the
     * escaping utility; the same pattern is used in SearchAndTagsServiceImpl.
     */
    @Query("""
            SELECT u FROM Users u
            WHERE LOWER(u.name)   LIKE LOWER(CONCAT('%', :query, '%')) ESCAPE '\\'
               OR LOWER(u.handle) LIKE LOWER(CONCAT('%', :query, '%')) ESCAPE '\\'
            ORDER BY u.name ASC
            """)
    Page<Users> searchByNameOrHandle(@Param("query") String query, Pageable pageable);
}
