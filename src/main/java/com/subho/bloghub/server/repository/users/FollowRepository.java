package com.subho.bloghub.server.repository.users;

import com.subho.bloghub.server.entity.users.Follows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follows, UUID> {

    // People who follow the given handle (followers list)
    Page<Follows> findByFollowing_Handle(String handle, Pageable pageable);

    // People the given handle follows (following list)
    Page<Follows> findByFollower_Handle(String handle, Pageable pageable);

    long countByFollowing_Id(UUID followingId);

    long countByFollower_Id(UUID followerId);

    // Reserved for resolving UserProfileResponseDTO#isFollowing once accessToken resolution exists
    boolean existsByFollower_IdAndFollowing_Id(UUID followerId, UUID followingId);

    @Modifying
    void deleteByFollower_IdAndFollowing_Id(UUID followerId, UUID followingId);
}
