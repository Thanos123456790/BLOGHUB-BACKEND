package com.subho.bloghub.server.service.impl;

import com.subho.bloghub.client.dtos.blogs.BlogCardResponseDTO;
import com.subho.bloghub.client.dtos.tags.TagResponseDTO;
import com.subho.bloghub.client.dtos.users.UserProfileResponseDTO;
import com.subho.bloghub.server.entity.users.Users;
import com.subho.bloghub.server.exception.BadRequestException;
import com.subho.bloghub.server.mapper.users.UserMapper;
import com.subho.bloghub.server.repository.blogs.BlogRepository;
import com.subho.bloghub.server.repository.blogs.TagsRepository;
import com.subho.bloghub.server.repository.users.FollowRepository;
import com.subho.bloghub.server.repository.users.UserRepository;
import com.subho.bloghub.server.service.blogs.BlogService;
import com.subho.bloghub.server.service.tags.SearchAndTagsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchAndTagsServiceImpl implements SearchAndTagsService {

    private final BlogService blogService;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final BlogRepository blogRepository;
    private final UserMapper userMapper;
    private final TagsRepository tagsRepository;

    @Override
    public Page<BlogCardResponseDTO> searchBlogs(String query, Pageable pageable, String accessToken) {
        return blogService.searchBlogs(query, pageable, accessToken);
    }

    @Override
    public Page<UserProfileResponseDTO> searchUsers(String query, Pageable pageable) {
        if (!StringUtils.hasText(query)) {
            throw new BadRequestException("Search query must not be blank");
        }
        // VLN-08 FIX: Escape SQL LIKE wildcards before passing to the repository query
        String safeQuery = escapeLikeWildcards(query.trim());
        return userRepository.searchByNameOrHandle(safeQuery, pageable)
                .map(this::buildProfileResponse);
    }

    @Override
    public Page<TagResponseDTO> getTrendingTags(Pageable pageable) {
        return tagsRepository.findTrendingTags(pageable)
                .map(row -> TagResponseDTO.builder()
                        .name(row.getName())
                        .postCount(row.getPostCount())
                        .build());
    }

    @Override
    public Page<BlogCardResponseDTO> getBlogsByTag(String tagName, Pageable pageable, String accessToken) {
        return blogService.getBlogsByTag(tagName, pageable, accessToken);
    }

    private String escapeLikeWildcards(String input) {
        return input
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    private UserProfileResponseDTO buildProfileResponse(Users user) {
        long followers = followRepository.countByFollowing_Id(user.getId());
        long following = followRepository.countByFollower_Id(user.getId());
        long posts = blogRepository.countByAuthor_Id(user.getId());
        return userMapper.toResponse(user, followers, following, posts, false);
    }
}
