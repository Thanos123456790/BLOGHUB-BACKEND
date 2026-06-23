package com.subho.bloghub.server.service.impl;

import com.subho.bloghub.client.dtos.comments.CommentResponseDTO;
import com.subho.bloghub.client.dtos.comments.CreateCommentRequestDTO;
import com.subho.bloghub.client.dtos.reaction.ReactionCountResponseDTO;
import com.subho.bloghub.client.dtos.reaction.ReactionRequestDTO;
import com.subho.bloghub.client.dtos.users.UserProfileResponseDTO;
import com.subho.bloghub.client.enums.ReactionType;
import com.subho.bloghub.server.common.CurrentUserResolver;
import com.subho.bloghub.server.common.UuidUtils;
import com.subho.bloghub.server.entity.blogs.Blogs;
import com.subho.bloghub.server.entity.comments.CommentMentions;
import com.subho.bloghub.server.entity.comments.CommentReactions;
import com.subho.bloghub.server.entity.comments.Comments;
import com.subho.bloghub.server.entity.users.Users;
import com.subho.bloghub.server.exception.BadRequestException;
import com.subho.bloghub.server.exception.ForbiddenException;
import com.subho.bloghub.server.exception.ResourceNotFoundException;
import com.subho.bloghub.server.mapper.comments.CommentAggregateContext;
import com.subho.bloghub.server.mapper.comments.CommentMapper;
import com.subho.bloghub.server.mapper.users.UserMapper;
import com.subho.bloghub.server.repository.blogs.BlogRepository;
import com.subho.bloghub.server.repository.comments.CommentMentionsRepository;
import com.subho.bloghub.server.repository.comments.CommentReactionsRepository;
import com.subho.bloghub.server.repository.comments.CommentsRepository;
import com.subho.bloghub.server.repository.users.UserRepository;
import com.subho.bloghub.server.service.comments.CommentService;
import com.subho.bloghub.server.service.notifications.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    // VLN-09 FIX: Regex to extract @handles from comment text for cross-validation.
    private static final Pattern MENTION_PATTERN = Pattern.compile("@([\\w]+)");

    private final CommentsRepository commentsRepository;
    private final CommentReactionsRepository commentReactionsRepository;
    private final CommentMentionsRepository commentMentionsRepository;
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final CurrentUserResolver currentUserResolver;
    private final NotificationService notificationService;

    @Override
    public Page<CommentResponseDTO> getTopComments(String accessToken, String blogId, Pageable pageable) {
        UUID id = UuidUtils.parse(blogId, "blog id");
        if (!blogRepository.existsById(id)) {
            throw new ResourceNotFoundException("Blog", blogId);
        }
        Page<Comments> page = commentsRepository.findByBlog_IdAndParentIsNullOrderByCreatedAtAsc(id, pageable);
        List<Comments> topComments = page.getContent();
        if (topComments.isEmpty()) {
            return page.map(c -> commentMapper.toResponse(c, CommentAggregateContext.empty()));
        }
        List<UUID> topIds = topComments.stream().map(Comments::getId).toList();
        List<Comments> replies = commentsRepository.findByParent_IdInOrderByCreatedAtAsc(topIds);

        List<UUID> allCommentIds = new ArrayList<>(topIds);
        allCommentIds.addAll(replies.stream().map(Comments::getId).toList());

        UUID viewerId = currentUserResolver.resolveCurrentUserIdOrNull(accessToken);
        Map<UUID, ReactionCountResponseDTO> reactionsByComment = fetchReactionCounts(allCommentIds);
        Map<UUID, ReactionType> myReactionByComment = fetchMyReactions(allCommentIds, viewerId);
        Map<UUID, List<UserProfileResponseDTO>> mentionsByComment = fetchMentions(allCommentIds);

        Map<UUID, List<Comments>> repliesByParent = replies.stream()
                .collect(Collectors.groupingBy(c -> c.getParent().getId()));

        return page.map(top -> {
            List<CommentResponseDTO> replyDTOs = repliesByParent.getOrDefault(top.getId(), List.of()).stream()
                    .map(reply -> commentMapper.toResponse(reply, new CommentAggregateContext(
                            reactionsByComment.getOrDefault(reply.getId(), ReactionCountResponseDTO.builder().build()),
                            myReactionByComment.get(reply.getId()),
                            mentionsByComment.getOrDefault(reply.getId(), List.of()),
                            List.of()
                    )))
                    .toList();
            return commentMapper.toResponse(top, new CommentAggregateContext(
                    reactionsByComment.getOrDefault(top.getId(), ReactionCountResponseDTO.builder().build()),
                    myReactionByComment.get(top.getId()),
                    mentionsByComment.getOrDefault(top.getId(), List.of()),
                    replyDTOs
            ));
        });
    }

    @Override
    @Transactional
    public CommentResponseDTO postTopComment(String accessToken, String blogId, CreateCommentRequestDTO request) {
        UUID id = UuidUtils.parse(blogId, "blog id");
        UUID authorId = currentUserResolver.requireCurrentUserId(accessToken);

        Blogs blog = blogRepository.findWithAuthorById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", blogId));
        Users author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", authorId));

        Comments comment = commentMapper.toEntity(request);
        comment.setBlog(blog);
        comment.setAuthor(author);
        Comments saved = commentsRepository.save(comment);

        // VLN-09 FIX: validate mentions against comment text before persisting
        List<UserProfileResponseDTO> taggedUsers = attachMentions(saved, request.getTaggedUserIds(), author);

        notificationService.notify(blog.getAuthor(), author, "comment", blog, saved, null);
        log.info("AUDIT: User {} posted comment {} on blog {}", authorId, saved.getId(), id);

        return commentMapper.toResponse(saved, new CommentAggregateContext(
                ReactionCountResponseDTO.builder().build(), null, taggedUsers, List.of()));
    }

    @Override
    @Transactional
    public CommentResponseDTO replyToComment(String accessToken, String blogId, String commentId, CreateCommentRequestDTO request) {
        UUID blogUuid   = UuidUtils.parse(blogId, "blog id");
        UUID parentId   = UuidUtils.parse(commentId, "comment id");
        UUID authorId   = currentUserResolver.requireCurrentUserId(accessToken);

        Blogs blog = blogRepository.findWithAuthorById(blogUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", blogId));
        Comments parent = commentsRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));

        if (!parent.getBlog().getId().equals(blogUuid)) {
            throw new BadRequestException("Comment does not belong to the specified blog");
        }

        // VLN-10b FIX: Limit reply depth to 1 level. Replies-to-replies are not
        // allowed — they create unbounded nesting that breaks the feed query and
        // can cause stack overflows in recursive mappers.
        if (parent.getParent() != null) {
            throw new BadRequestException("Replies to replies are not supported. Reply to the top-level comment instead.");
        }

        Users author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", authorId));

        Comments reply = commentMapper.toEntity(request);
        reply.setBlog(blog);
        reply.setAuthor(author);
        reply.setParent(parent);
        Comments saved = commentsRepository.save(reply);

        List<UserProfileResponseDTO> taggedUsers = attachMentions(saved, request.getTaggedUserIds(), author);
        notificationService.notify(parent.getAuthor(), author, "reply", blog, saved, null);

        return commentMapper.toResponse(saved, new CommentAggregateContext(
                ReactionCountResponseDTO.builder().build(), null, taggedUsers, List.of()));
    }

    @Override
    @Transactional
    public CommentResponseDTO updateComment(String accessToken, String commentId, CreateCommentRequestDTO request) {
        UUID id       = UuidUtils.parse(commentId, "comment id");
        UUID callerId = currentUserResolver.requireCurrentUserId(accessToken);

        Comments comment = commentsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));

        if (!comment.getAuthor().getId().equals(callerId)) {
            throw new ForbiddenException("Only the author can edit this comment");
        }

        comment.setContent(request.getContent());
        Comments saved = commentsRepository.save(comment);

        commentMentionsRepository.deleteAllByCommentId(id);
        List<UserProfileResponseDTO> taggedUsers = attachMentions(saved, request.getTaggedUserIds(), comment.getAuthor());

        ReactionCountResponseDTO reactions = buildReactionCounts(id);
        ReactionType myReaction = fetchMyReactions(List.of(id), callerId).get(id);

        return commentMapper.toResponse(saved, new CommentAggregateContext(reactions, myReaction, taggedUsers, List.of()));
    }

    @Override
    @Transactional
    public void deleteComment(String accessToken, String commentId) {
        UUID id       = UuidUtils.parse(commentId, "comment id");
        UUID callerId = currentUserResolver.requireCurrentUserId(accessToken);

        Comments comment = commentsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));

        if (!comment.getAuthor().getId().equals(callerId)) {
            throw new ForbiddenException("Only the author can delete this comment");
        }

        List<Comments> replies = commentsRepository.findByParent_IdInOrderByCreatedAtAsc(List.of(id));
        commentsRepository.deleteAll(replies);
        commentsRepository.delete(comment);
        log.info("AUDIT: User {} deleted comment {}", callerId, id);
    }

    @Override
    @Transactional
    public ReactionCountResponseDTO addReaction(String accessToken, String commentId, ReactionRequestDTO request) {
        UUID id     = UuidUtils.parse(commentId, "comment id");
        UUID userId = currentUserResolver.requireCurrentUserId(accessToken);

        if (request == null || request.getReactionType() == null) {
            throw new BadRequestException("reactionType is required");
        }

        Comments comment = commentsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        CommentReactions reaction = commentReactionsRepository.findByUser_IdAndComment_Id(userId, id)
                .orElseGet(() -> CommentReactions.builder().user(user).comment(comment).build());
        boolean isNewReaction = reaction.getId() == null;
        reaction.setReactionType(request.getReactionType().name().toLowerCase());
        commentReactionsRepository.save(reaction);

        if (isNewReaction) {
            notificationService.notify(comment.getAuthor(), user, "comment_reaction", comment.getBlog(), comment, null);
        }

        return buildReactionCounts(id);
    }

    @Override
    @Transactional
    public ReactionCountResponseDTO removeReaction(String accessToken, String commentId) {
        UUID id     = UuidUtils.parse(commentId, "comment id");
        UUID userId = currentUserResolver.requireCurrentUserId(accessToken);

        if (!commentsRepository.existsById(id)) {
            throw new ResourceNotFoundException("Comment", commentId);
        }
        commentReactionsRepository.deleteByUser_IdAndComment_Id(userId, id);
        return buildReactionCounts(id);
    }

    // ── VLN-09: Validated mention attachment ─────────────────────────────────

    /**
     * Persists mention rows only for users whose @handle is actually present
     * in the comment text. Silently drops IDs that don't match a handle in
     * the content — preventing notification spam to arbitrary users.
     */
    private List<UserProfileResponseDTO> attachMentions(Comments comment,
                                                         List<UUID> taggedUserIds,
                                                         Users commentAuthor) {
        if (taggedUserIds == null || taggedUserIds.isEmpty()) {
            return List.of();
        }

        // Extract @handles present in the comment text
        Set<String> mentionedHandles = new HashSet<>();
        var matcher = MENTION_PATTERN.matcher(comment.getContent());
        while (matcher.find()) {
            mentionedHandles.add(matcher.group(1).toLowerCase());
        }

        Set<UUID> distinctIds = Set.copyOf(taggedUserIds);
        List<Users> taggedUsers = userRepository.findByIdIn(new ArrayList<>(distinctIds));

        // VLN-09 FIX: Only keep users whose handle appears in the comment content
        List<Users> validMentions = taggedUsers.stream()
                .filter(u -> mentionedHandles.contains(u.getHandle().toLowerCase()))
                .toList();

        if (validMentions.isEmpty()) {
            return List.of();
        }

        List<CommentMentions> mentions = validMentions.stream()
                .map(u -> CommentMentions.builder().comment(comment).mentionedUser(u).build())
                .toList();
        commentMentionsRepository.saveAll(mentions);

        for (Users mentioned : validMentions) {
            notificationService.notify(mentioned, commentAuthor, "mention", comment.getBlog(), comment, null);
        }

        return validMentions.stream().map(userMapper::toResponse).toList();
    }

    private Map<UUID, List<UserProfileResponseDTO>> fetchMentions(List<UUID> commentIds) {
        Map<UUID, List<UserProfileResponseDTO>> result = new HashMap<>();
        for (CommentMentions m : commentMentionsRepository.findByComment_IdIn(commentIds)) {
            result.computeIfAbsent(m.getComment().getId(), k -> new ArrayList<>())
                    .add(userMapper.toResponse(m.getMentionedUser()));
        }
        return result;
    }

    // ── Reactions ─────────────────────────────────────────────────────────────

    private ReactionCountResponseDTO buildReactionCounts(UUID commentId) {
        return fetchReactionCounts(List.of(commentId))
                .getOrDefault(commentId, ReactionCountResponseDTO.builder().build());
    }

    private Map<UUID, ReactionCountResponseDTO> fetchReactionCounts(List<UUID> commentIds) {
        if (commentIds.isEmpty()) return Map.of();
        Map<UUID, Map<String, Long>> raw = new HashMap<>();
        for (var row : commentReactionsRepository.countByCommentIdIn(commentIds)) {
            raw.computeIfAbsent(row.getCommentId(), k -> new HashMap<>())
               .put(row.getReactionType(), row.getCount());
        }
        Map<UUID, ReactionCountResponseDTO> result = new HashMap<>();
        for (var entry : raw.entrySet()) {
            Map<String, Long> counts = entry.getValue();
            result.put(entry.getKey(), ReactionCountResponseDTO.builder()
                    .like(counts.getOrDefault("like", 0L))
                    .clap(counts.getOrDefault("clap", 0L))
                    .love(counts.getOrDefault("love", 0L))
                    .insightful(counts.getOrDefault("insightful", 0L))
                    .build());
        }
        return result;
    }

    private Map<UUID, ReactionType> fetchMyReactions(List<UUID> commentIds, UUID viewerId) {
        if (viewerId == null || commentIds.isEmpty()) return Map.of();
        Map<UUID, ReactionType> result = new HashMap<>();
        for (var row : commentReactionsRepository.findUserReactions(viewerId, commentIds)) {
            result.put(row.getCommentId(), ReactionType.valueOf(row.getReactionType().toUpperCase()));
        }
        return result;
    }
}
