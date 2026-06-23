package com.subho.bloghub.server.service.impl;

import com.subho.bloghub.client.dtos.blogs.BlogBlockRequestDTO;
import com.subho.bloghub.client.dtos.blogs.BlogCardResponseDTO;
import com.subho.bloghub.client.dtos.blogs.BlogResponseDTO;
import com.subho.bloghub.client.dtos.blogs.CreateBlogRequestDTO;
import com.subho.bloghub.client.dtos.blogs.UpdateBlogRequestDTO;
import com.subho.bloghub.client.dtos.reaction.ReactionCountResponseDTO;
import com.subho.bloghub.client.dtos.reaction.ReactionRequestDTO;
import com.subho.bloghub.client.enums.BlockType;
import com.subho.bloghub.client.enums.ReactionType;
import com.subho.bloghub.server.common.CurrentUserResolver;
import com.subho.bloghub.server.common.UuidUtils;
import com.subho.bloghub.server.config.S3Properties;
import com.subho.bloghub.server.entity.blogs.*;
import com.subho.bloghub.server.entity.users.Users;
import com.subho.bloghub.server.exception.BadRequestException;
import com.subho.bloghub.server.exception.ForbiddenException;
import com.subho.bloghub.server.exception.ResourceNotFoundException;
import com.subho.bloghub.server.mapper.blogs.BlogAggregateContext;
import com.subho.bloghub.server.mapper.blogs.BlogBlockMapper;
import com.subho.bloghub.server.mapper.blogs.BlogMapper;
import com.subho.bloghub.server.repository.blogs.*;
import com.subho.bloghub.server.repository.comments.CommentsRepository;
import com.subho.bloghub.server.repository.notifications.NotificationsRepository;
import com.subho.bloghub.server.repository.users.UserRepository;
import com.subho.bloghub.server.service.blogs.BlogService;
import com.subho.bloghub.server.service.notifications.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlogServiceImpl implements BlogService {

    private static final int TRENDING_WINDOW_DAYS = 7;
    // VLN-13 FIX: Hard cap on total tags in the system to prevent unbounded growth.
    private static final int MAX_TOTAL_TAGS = 10_000;

    private final BlogRepository blogRepository;
    private final BlogBlocksRepository blogBlocksRepository;
    private final BlogTagsRepository blogTagsRepository;
    private final TagsRepository tagsRepository;
    private final BlogReactionsRepository blogReactionsRepository;
    private final BookmarksRepository bookmarksRepository;
    private final CommentsRepository commentsRepository;
    private final NotificationsRepository notificationsRepository;
    private final UserRepository userRepository;
    private final BlogMapper blogMapper;
    private final BlogBlockMapper blogBlockMapper;
    private final CurrentUserResolver currentUserResolver;
    private final NotificationService notificationService;
    private final S3Properties s3Properties;

    @Override
    public Page<BlogCardResponseDTO> getFeed(String accessToken, String feed, String tag, Pageable pageable) {
        if (StringUtils.hasText(tag)) {
            return getBlogsByTag(tag, pageable, accessToken);
        }
        Page<Blogs> page;
        if ("following".equalsIgnoreCase(feed)) {
            UUID viewerId = currentUserResolver.requireCurrentUserId(accessToken);
            page = blogRepository.findFollowingFeed(viewerId, pageable);
        } else {
            page = blogRepository.findFeed(pageable);
        }
        return toCardPage(page, accessToken);
    }

    @Override
    @Transactional
    public BlogResponseDTO createBlog(String accessToken, CreateBlogRequestDTO request) {
        UUID authorId = currentUserResolver.requireCurrentUserId(accessToken);
        Users author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", authorId));

        // VLN-04 FIX: Validate cover image URL against trusted hosts
        validateImageUrl(request.getCoverImageUrl(), "coverImageUrl");

        // VLN-04 FIX: Validate all IMAGE block URLs
        if (request.getBlocks() != null) {
            for (BlogBlockRequestDTO block : request.getBlocks()) {
                if (BlockType.IMAGE.name().equals(block.getType() != null ? block.getType().name() : null)) {
                    validateImageUrl(block.getContent(), "image block content");
                }
            }
        }

        Blogs blog = blogMapper.toEntity(request);
        blog.setAuthor(author);
        Blogs saved = blogRepository.save(blog);

        List<com.subho.bloghub.server.entity.blogs.BlogBlocks> blocks = request.getBlocks().stream()
                .map(b -> blogBlockMapper.toEntity(b, saved))
                .toList();
        blogBlocksRepository.saveAll(blocks);

        attachTags(saved, request.getTags());

        log.info("AUDIT: User {} created blog {}", authorId, saved.getId());
        return toFullResponse(saved, blocks, null, accessToken);
    }

    @Override
    public BlogResponseDTO getFullBlog(String accessToken, String id) {
        UUID blogId = UuidUtils.parse(id, "blog id");
        Blogs blog = blogRepository.findWithAuthorById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", id));
        List<com.subho.bloghub.server.entity.blogs.BlogBlocks> blocks = blogBlocksRepository.findByBlog_IdOrderByPositionAsc(blogId);
        UUID viewerId = currentUserResolver.resolveCurrentUserIdOrNull(accessToken);
        return toFullResponse(blog, blocks, viewerId, accessToken);
    }

    @Override
    @Transactional
    public BlogResponseDTO updateBlog(String accessToken, String id, UpdateBlogRequestDTO request) {
        UUID blogId = UuidUtils.parse(id, "blog id");
        UUID callerId = currentUserResolver.requireCurrentUserId(accessToken);

        Blogs blog = blogRepository.findWithAuthorById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", id));

        if (!blog.getAuthor().getId().equals(callerId)) {
            throw new ForbiddenException("Only the author can edit this blog");
        }

        // VLN-04 FIX: Validate cover image URL if being updated
        if (request.getCoverImageUrl() != null) {
            validateImageUrl(request.getCoverImageUrl(), "coverImageUrl");
            blog.setCoverImageUrl(request.getCoverImageUrl());
        }

        if (request.getTitle() != null) blog.setTitle(request.getTitle());
        if (request.getExcerpt() != null) blog.setExcerpt(request.getExcerpt());
        if (request.getCoverFilter() != null) blog.setCoverFilter(request.getCoverFilter());
        if (request.getReadTimeMinutes() != null) blog.setReadTimeMinutes(request.getReadTimeMinutes());
        Blogs saved = blogRepository.save(blog);

        if (request.getTags() != null) {
            blogTagsRepository.deleteAllByBlogId(blogId);
            attachTags(saved, request.getTags());
        }

        List<com.subho.bloghub.server.entity.blogs.BlogBlocks> blocks;
        if (request.getBlocks() != null) {
            // VLN-04 FIX: Validate image blocks on update too
            for (var block : request.getBlocks()) {
                if (BlockType.IMAGE.name().equals(block.getType() != null ? block.getType().name() : null)) {
                    validateImageUrl(block.getContent(), "image block content");
                }
            }
            blogBlocksRepository.deleteAllByBlogId(blogId);
            blocks = request.getBlocks().stream()
                    .map(b -> blogBlockMapper.toEntity(b, saved))
                    .toList();
            blogBlocksRepository.saveAll(blocks);
        } else {
            blocks = blogBlocksRepository.findByBlog_IdOrderByPositionAsc(blogId);
        }

        log.info("AUDIT: User {} updated blog {}", callerId, blogId);
        return toFullResponse(saved, blocks, callerId, accessToken);
    }

    @Override
    @Transactional
    public void deleteBlog(String accessToken, String id) {
        UUID blogId = UuidUtils.parse(id, "blog id");
        UUID callerId = currentUserResolver.requireCurrentUserId(accessToken);

        Blogs blog = blogRepository.findWithAuthorById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", id));

        if (!blog.getAuthor().getId().equals(callerId)) {
            throw new ForbiddenException("Only the author can delete this blog");
        }

        // VLN-11 FIX: Clean up all related rows before deleting the blog to
        // avoid FK violations and prevent orphaned data accumulation.
        blogBlocksRepository.deleteAllByBlogId(blogId);
        blogTagsRepository.deleteAllByBlogId(blogId);
        blogReactionsRepository.deleteByBlog_Id(blogId);
        bookmarksRepository.deleteByBlog_Id(blogId);
        notificationsRepository.deleteByBlog_Id(blogId);
        blogRepository.delete(blog);
        log.info("AUDIT: User {} deleted blog {}", callerId, blogId);
    }

    @Override
    public Page<BlogCardResponseDTO> getTrendingBlogs(Pageable pageable, String accessToken) {
        Instant since = Instant.now().minus(TRENDING_WINDOW_DAYS, ChronoUnit.DAYS);
        Page<Blogs> page = blogRepository.findTrending(since, pageable);
        return toCardPage(page, accessToken);
    }

    @Override
    @Transactional
    public ReactionCountResponseDTO addReaction(String accessToken, String blogId, ReactionRequestDTO request) {
        UUID id = UuidUtils.parse(blogId, "blog id");
        UUID userId = currentUserResolver.requireCurrentUserId(accessToken);

        if (request == null || request.getReactionType() == null) {
            throw new BadRequestException("reactionType is required");
        }

        Blogs blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", blogId));
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        BlogReactions reaction = blogReactionsRepository.findByUser_IdAndBlog_Id(userId, id)
                .orElseGet(() -> BlogReactions.builder().user(user).blog(blog).build());
        boolean isNewReaction = reaction.getId() == null;
        reaction.setReactionType(request.getReactionType().name().toLowerCase());
        blogReactionsRepository.save(reaction);

        // VLN-10 FIX: Only notify on genuinely new reactions (not reaction type changes)
        if (isNewReaction) {
            notificationService.notify(blog.getAuthor(), user, "reaction", blog, null, null);
        }
        return buildReactionCounts(id);
    }

    @Override
    @Transactional
    public ReactionCountResponseDTO removeReaction(String accessToken, String blogId) {
        UUID id = UuidUtils.parse(blogId, "blog id");
        UUID userId = currentUserResolver.requireCurrentUserId(accessToken);
        if (!blogRepository.existsById(id)) {
            throw new ResourceNotFoundException("Blog", blogId);
        }
        blogReactionsRepository.deleteByUser_IdAndBlog_Id(userId, id);
        return buildReactionCounts(id);
    }

    @Override
    public Page<BlogCardResponseDTO> getBookmarkedBlogs(String accessToken, Pageable pageable) {
        UUID userId = currentUserResolver.requireCurrentUserId(accessToken);
        Page<Bookmarks> page = bookmarksRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable);
        Page<Blogs> blogPage = page.map(Bookmarks::getBlog);
        return toCardPage(blogPage, accessToken);
    }

    @Override
    @Transactional
    public void addBookmark(String accessToken, String blogId) {
        UUID id = UuidUtils.parse(blogId, "blog id");
        UUID userId = currentUserResolver.requireCurrentUserId(accessToken);
        if (bookmarksRepository.existsByUser_IdAndBlog_Id(userId, id)) {
            return;
        }
        Blogs blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", blogId));
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        bookmarksRepository.save(Bookmarks.builder().user(user).blog(blog).build());
    }

    @Override
    @Transactional
    public void removeBookmark(String accessToken, String blogId) {
        UUID id = UuidUtils.parse(blogId, "blog id");
        UUID userId = currentUserResolver.requireCurrentUserId(accessToken);
        bookmarksRepository.deleteByUser_IdAndBlog_Id(userId, id);
    }

    @Override
    public Page<BlogCardResponseDTO> searchBlogs(String query, Pageable pageable, String accessToken) {
        if (!StringUtils.hasText(query)) {
            throw new BadRequestException("Search query must not be blank");
        }
        // VLN-08 FIX: Escape SQL LIKE wildcards to prevent wildcard injection / ReDoS
        String safeQuery = escapeLikeWildcards(query.trim());
        return toCardPage(blogRepository.searchByTitleOrExcerpt(safeQuery, pageable), accessToken);
    }

    @Override
    public Page<BlogCardResponseDTO> getBlogsByTag(String tagName, Pageable pageable, String accessToken) {
        if (!StringUtils.hasText(tagName)) {
            throw new BadRequestException("Tag name must not be blank");
        }
        return toCardPage(blogRepository.findByTagName(tagName.trim(), pageable), accessToken);
    }

    @Override
    public Page<BlogCardResponseDTO> getBlogsByAuthorHandle(String handle, Pageable pageable, String accessToken) {
        if (!userRepository.existsByHandle(handle)) {
            throw new ResourceNotFoundException("User with handle", handle);
        }
        return toCardPage(blogRepository.findByAuthor_HandleOrderByCreatedAtDesc(handle, pageable), accessToken);
    }

    // ── VLN-04: URL validation ───────────────────────────────────────────────

    /**
     * Validates that an image URL uses https and originates from a trusted host.
     * Null/blank values are allowed (optional fields).
     */
    private void validateImageUrl(String url, String fieldName) {
        if (url == null || url.isBlank()) {
            return;
        }
        if (!url.startsWith("https://")) {
            throw new BadRequestException(fieldName + " must use the https scheme");
        }
        List<String> trustedHosts = s3Properties.getTrustedImageHosts();
        if (trustedHosts != null && !trustedHosts.isEmpty()) {
            try {
                String host = URI.create(url).getHost();
                boolean trusted = trustedHosts.stream()
                        .anyMatch(h -> host != null && (host.equals(h) || host.endsWith("." + h)));
                if (!trusted) {
                    throw new BadRequestException(fieldName + " must point to a trusted image host");
                }
            } catch (IllegalArgumentException e) {
                throw new BadRequestException(fieldName + " is not a valid URL");
            }
        }
    }

    // ── VLN-08: LIKE wildcard escaping ──────────────────────────────────────

    /**
     * Escapes SQL LIKE special characters so user-supplied search queries
     * don't act as wildcards or trigger catastrophic backtracking.
     */
    private String escapeLikeWildcards(String input) {
        return input
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    // ── Tag attachment ───────────────────────────────────────────────────────

    private void attachTags(Blogs blog, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return;
        }
        Set<String> distinctNames = tagNames.stream()
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSetByCaseInsensitive::new));

        List<Tags> existing = tagsRepository.findByNameInIgnoreCase(new ArrayList<>(distinctNames));
        Map<String, Tags> byLowerName = existing.stream()
                .collect(Collectors.toMap(t -> t.getName().toLowerCase(), t -> t));

        List<Tags> toCreate = new ArrayList<>();
        for (String name : distinctNames) {
            if (!byLowerName.containsKey(name.toLowerCase())) {
                toCreate.add(Tags.builder().name(name).build());
            }
        }

        // VLN-13 FIX: Check global tag count before creating new tags
        if (!toCreate.isEmpty()) {
            long currentTotal = tagsRepository.count();
            if (currentTotal + toCreate.size() > MAX_TOTAL_TAGS) {
                throw new BadRequestException(
                        "Cannot create new tags: system tag limit reached. Use an existing tag.");
            }
            List<Tags> saved = tagsRepository.saveAll(toCreate);
            for (Tags t : saved) {
                byLowerName.put(t.getName().toLowerCase(), t);
            }
        }

        List<BlogTags> links = distinctNames.stream()
                .map(byLowerName::get)
                .filter(Objects::nonNull)
                .map(tag -> BlogTags.builder()
                        .id(new BlogTagsId(blog.getId(), tag.getId()))
                        .blog(blog)
                        .tag(tag)
                        .build())
                .toList();
        blogTagsRepository.saveAll(links);
    }

    private static class LinkedHashSetByCaseInsensitive extends java.util.LinkedHashSet<String> {
        @Override
        public boolean add(String s) {
            for (String existing : this) {
                if (existing.equalsIgnoreCase(s)) return false;
            }
            return super.add(s);
        }
    }

    // ── Batched aggregate building ───────────────────────────────────────────

    private Page<BlogCardResponseDTO> toCardPage(Page<Blogs> page, String accessToken) {
        List<Blogs> blogs = page.getContent();
        if (blogs.isEmpty()) {
            return page.map(b -> blogMapper.toCardResponse(b, BlogAggregateContext.empty()));
        }
        List<UUID> blogIds = blogs.stream().map(Blogs::getId).toList();
        UUID viewerId = currentUserResolver.resolveCurrentUserIdOrNull(accessToken);
        Map<UUID, List<String>> tagsByBlog = fetchTagsByBlog(blogIds);
        Map<UUID, ReactionCountResponseDTO> reactionsByBlog = fetchReactionCountsByBlog(blogIds);
        Map<UUID, ReactionType> myReactionByBlog = fetchMyReactionsByBlog(blogIds, viewerId);
        Set<UUID> bookmarkedBlogIds = fetchBookmarkedBlogIds(blogIds, viewerId);
        Map<UUID, Long> commentCountByBlog = fetchCommentCountsByBlog(blogIds);
        return page.map(blog -> blogMapper.toCardResponse(blog, new BlogAggregateContext(
                tagsByBlog.getOrDefault(blog.getId(), List.of()),
                reactionsByBlog.getOrDefault(blog.getId(), ReactionCountResponseDTO.builder().build()),
                myReactionByBlog.get(blog.getId()),
                bookmarkedBlogIds.contains(blog.getId()),
                commentCountByBlog.getOrDefault(blog.getId(), 0L)
        )));
    }

    private BlogResponseDTO toFullResponse(Blogs blog, List<com.subho.bloghub.server.entity.blogs.BlogBlocks> blocks, UUID viewerId, String accessToken) {
        UUID resolvedViewerId = viewerId != null ? viewerId : currentUserResolver.resolveCurrentUserIdOrNull(accessToken);
        List<UUID> single = List.of(blog.getId());
        List<String> tags = blogTagsRepository.findTagNamesByBlogId(blog.getId());
        ReactionCountResponseDTO reactions = buildReactionCounts(blog.getId());
        ReactionType myReaction = fetchMyReactionsByBlog(single, resolvedViewerId).get(blog.getId());
        boolean bookmarked = resolvedViewerId != null
                && bookmarksRepository.existsByUser_IdAndBlog_Id(resolvedViewerId, blog.getId());
        long commentsCount = commentsRepository.countByBlog_Id(blog.getId());
        BlogAggregateContext context = new BlogAggregateContext(tags, reactions, myReaction, bookmarked, commentsCount);
        return blogMapper.toFullResponse(blog, blocks, context);
    }

    private ReactionCountResponseDTO buildReactionCounts(UUID blogId) {
        List<com.subho.bloghub.server.repository.blogs.BlogReactionsRepository.ReactionTypeCount> counts =
                blogReactionsRepository.countByBlogId(blogId);
        return toReactionCountDTO(counts.stream()
                .collect(Collectors.toMap(
                        com.subho.bloghub.server.repository.blogs.BlogReactionsRepository.ReactionTypeCount::getReactionType,
                        com.subho.bloghub.server.repository.blogs.BlogReactionsRepository.ReactionTypeCount::getCount)));
    }

    private Map<UUID, List<String>> fetchTagsByBlog(List<UUID> blogIds) {
        Map<UUID, List<String>> result = new HashMap<>();
        for (var projection : blogTagsRepository.findTagNamesByBlogIdIn(blogIds)) {
            result.computeIfAbsent(projection.getBlogId(), k -> new ArrayList<>()).add(projection.getTagName());
        }
        return result;
    }

    private Map<UUID, ReactionCountResponseDTO> fetchReactionCountsByBlog(List<UUID> blogIds) {
        Map<UUID, Map<String, Long>> raw = new HashMap<>();
        for (var row : blogReactionsRepository.countByBlogIdIn(blogIds)) {
            raw.computeIfAbsent(row.getBlogId(), k -> new HashMap<>()).put(row.getReactionType(), row.getCount());
        }
        Map<UUID, ReactionCountResponseDTO> result = new HashMap<>();
        for (var entry : raw.entrySet()) {
            result.put(entry.getKey(), toReactionCountDTO(entry.getValue()));
        }
        return result;
    }

    private Map<UUID, ReactionType> fetchMyReactionsByBlog(List<UUID> blogIds, UUID viewerId) {
        if (viewerId == null) return Map.of();
        Map<UUID, ReactionType> result = new HashMap<>();
        for (var row : blogReactionsRepository.findUserReactions(viewerId, blogIds)) {
            result.put(row.getBlogId(), ReactionType.valueOf(row.getReactionType().toUpperCase()));
        }
        return result;
    }

    private Set<UUID> fetchBookmarkedBlogIds(List<UUID> blogIds, UUID viewerId) {
        if (viewerId == null) return Set.of();
        return new HashSet<>(bookmarksRepository.findBookmarkedBlogIds(viewerId, blogIds));
    }

    private Map<UUID, Long> fetchCommentCountsByBlog(List<UUID> blogIds) {
        Map<UUID, Long> result = new HashMap<>();
        for (var row : commentsRepository.countByBlogIdIn(blogIds)) {
            result.put(row.getBlogId(), row.getCount());
        }
        return result;
    }

    private ReactionCountResponseDTO toReactionCountDTO(Map<String, Long> counts) {
        return ReactionCountResponseDTO.builder()
                .like(counts.getOrDefault("like", 0L))
                .clap(counts.getOrDefault("clap", 0L))
                .love(counts.getOrDefault("love", 0L))
                .insightful(counts.getOrDefault("insightful", 0L))
                .build();
    }
}
