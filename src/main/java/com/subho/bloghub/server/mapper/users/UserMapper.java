package com.subho.bloghub.server.mapper.users;

import com.subho.bloghub.client.dtos.users.UpdateProfileRequestDTO;
import com.subho.bloghub.client.dtos.users.UserProfileResponseDTO;
import com.subho.bloghub.server.entity.users.Users;
import com.subho.bloghub.server.mapper.GenericMapper;
import org.springframework.stereotype.Component;

@Component
public class UserMapper implements GenericMapper<Users, UpdateProfileRequestDTO, UserProfileResponseDTO> {

    /**
     * Builds a brand-new entity from the request, per the GenericMapper contract.
     * UpdateProfileRequestDTO is a partial-update payload (every field optional/nullable),
     * so this is NOT what PUT /me uses — see {@link #applyUpdate} below for that. This only
     * matters if the DTO is ever reused for an actual create flow.
     */
    @Override
    public Users toEntity(UpdateProfileRequestDTO request) {
        if (request == null) {
            return null;
        }
        return Users.builder()
                .name(request.getName())
                .bio(request.getBio())
                .location(request.getLocation())
                .avatarUrl(request.getAvatarUrl())
                .bannerUrl(request.getBannerUrl())
                .build();
    }

    /**
     * Fulfils the GenericMapper contract. Aggregate stats (followers/following/posts/isFollowing)
     * require repository queries this mapper doesn't have access to, so they default to 0/false.
     * The service layer uses {@link #toResponse(Users, long, long, long, boolean)} instead.
     */
    @Override
    public UserProfileResponseDTO toResponse(Users entity) {
        return toResponse(entity, 0L, 0L, 0L, false);
    }

    public UserProfileResponseDTO toResponse(Users entity, long followersCount, long followingCount, long postsCount, boolean isFollowing) {
        if (entity == null) {
            return null;
        }
        return UserProfileResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .handle(entity.getHandle())
                .avatarUrl(entity.getAvatarUrl())
                .bannerUrl(entity.getBannerUrl())
                .bio(entity.getBio())
                .location(entity.getLocation())
                .isVerified(Boolean.TRUE.equals(entity.getIsVerified()))
                .followersCount(followersCount)
                .followingCount(followingCount)
                .postsCount(postsCount)
                .joinedAt(entity.getCreatedAt())
                .isFollowing(isFollowing)
                .build();
    }

    /**
     * Applies only the non-null fields from a partial-update DTO onto an existing managed entity.
     * This is the real mapping behind PUT /me.
     */
    public void applyUpdate(UpdateProfileRequestDTO request, Users entity) {
        if (request == null || entity == null) {
            return;
        }
        if (request.getName() != null) entity.setName(request.getName());
        if (request.getBio() != null) entity.setBio(request.getBio());
        if (request.getLocation() != null) entity.setLocation(request.getLocation());
        if (request.getAvatarUrl() != null) entity.setAvatarUrl(request.getAvatarUrl());
        if (request.getBannerUrl() != null) entity.setBannerUrl(request.getBannerUrl());
    }
}
