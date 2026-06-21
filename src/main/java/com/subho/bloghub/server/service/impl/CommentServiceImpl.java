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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

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
    public Page<CommentResponseDTO> getTopComments(String blogId, Pageable pageable) {
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

        // All comment ids (top-level + replies) that need reaction/mention aggregates,
        // fetched in one batch each instead of per-comment.
        List<UUID> allCommentIds = new ArrayList<>(topIds);
        allCommentIds.addAll(replies.stream().map(Comments::getId).toList());

        UUID viewerId = currentUserResolver.resolveCurrentUserIdOrNull(null);
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
    public CommentResponseDTO postTopComment(String blogId, CreateCommentRequestDTO request) {
        UUID id = UuidUtils.parse(blogId, "blog id");
        UUID authorId = currentUserResolver.requireCurrentUserId(null);

        Blogs blog = blogRepository.findWithAuthorById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", blogId));
        Users author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", authorId));

        Comments comment = commentMapper.toEntity(request);
        comment.setBlog(blog);
        comment.setAuthor(author);
        Comments saved = commentsRepository.save(comment);

        List<UserProfileResponseDTO> taggedUsers = attachMentions(saved, request.getTaggedUserIds(), author);

        notificationService.notify(blog.getAuthor(), author, "comment", blog, saved, null);

        return commentMapper.toResponse(saved, new CommentAggregateContext(
                ReactionCountResponseDTO.builder().build(), null, taggedUsers, List.of()));
    }

    @Override
    @Transactional
    public CommentResponseDTO replyToComment(String blogId, String commentId, CreateCommentRequestDTO request) {
        UUID blogUuid = UuidUtils.parse(blogId, "blog id");
        UUID parentId = UuidUtils.parse(commentId, "comment id");
        UUID authorId = currentUserResolver.requireCurrentUserId(null);

        Blogs blog = blogRepository.findWithAuthorById(blogUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", blogId));
        Comments parent = commentsRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));

        if (!parent.getBlog().getId().equals(blogUuid)) {
            throw new BadRequestException("Comment does not belong to the specified blog");
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
    public CommentResponseDTO updateComment(String commentId, CreateCommentRequestDTO request) {
        UUID id = UuidUtils.parse(commentId, "comment id");
        UUID callerId = currentUserResolver.requireCurrentUserId(null);

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
    public void deleteComment(String commentId) {
        UUID id = UuidUtils.parse(commentId, "comment id");
        UUID callerId = currentUserResolver.requireCurrentUserId(null);

        Comments comment = commentsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));

        if (!comment.getAuthor().getId().equals(callerId)) {
            throw new ForbiddenException("Only the author can delete this comment");
        }

        // Replies reference this comment via parent_id; deleting them explicitly
        // here keeps behaviour predictable regardless of DB-level cascade config.
        List<Comments> replies = commentsRepository.findByParent_IdInOrderByCreatedAtAsc(List.of(id));
        commentsRepository.deleteAll(replies);
        commentsRepository.delete(comment);
    }

    @Override
    @Transactional
    public ReactionCountResponseDTO addReaction(String commentId, ReactionRequestDTO request) {
        UUID id = UuidUtils.parse(commentId, "comment id");
        UUID userId = currentUserResolver.requireCurrentUserId(null);

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
    public ReactionCountResponseDTO removeReaction(String commentId) {
        UUID id = UuidUtils.parse(commentId, "comment id");
        UUID userId = currentUserResolver.requireCurrentUserId(null);

        if (!commentsRepository.existsById(id)) {
            throw new ResourceNotFoundException("Comment", commentId);
        }

        commentReactionsRepository.deleteByUser_IdAndComment_Id(userId, id);
        return buildReactionCounts(id);
    }

    // ── Mentions ──────────────────────────────────────────────────────────

    private List<UserProfileResponseDTO> attachMentions(Comments comment, List<UUID> taggedUserIds, Users commentAuthor) {
        if (taggedUserIds == null || taggedUserIds.isEmpty()) {
            return List.of();
        }

        Set<UUID> distinctIds = Set.copyOf(taggedUserIds);
        List<Users> taggedUsers = userRepository.findByIdIn(new ArrayList<>(distinctIds));

        List<CommentMentions> mentions = taggedUsers.stream()
                .map(u -> CommentMentions.builder().comment(comment).mentionedUser(u).build())
                .toList();
        commentMentionsRepository.saveAll(mentions);

        for (Users mentioned : taggedUsers) {
            notificationService.notify(mentioned, commentAuthor, "mention", comment.getBlog(), comment, null);
        }

        return taggedUsers.stream().map(userMapper::toResponse).toList();
    }

    private Map<UUID, List<UserProfileResponseDTO>> fetchMentions(List<UUID> commentIds) {
        Map<UUID, List<UserProfileResponseDTO>> result = new HashMap<>();
        for (CommentMentions m : commentMentionsRepository.findByComment_IdIn(commentIds)) {
            result.computeIfAbsent(m.getComment().getId(), k -> new ArrayList<>())
                    .add(userMapper.toResponse(m.getMentionedUser()));
        }
        return result;
    }

    // ── Reactions ─────────────────────────────────────────────────────────

    private ReactionCountResponseDTO buildReactionCounts(UUID commentId) {
        return fetchReactionCounts(List.of(commentId))
                .getOrDefault(commentId, ReactionCountResponseDTO.builder().build());
    }

    private Map<UUID, ReactionCountResponseDTO> fetchReactionCounts(List<UUID> commentIds) {
        if (commentIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, Map<String, Long>> raw = new HashMap<>();
        for (var row : commentReactionsRepository.countByCommentIdIn(commentIds)) {
            raw.computeIfAbsent(row.getCommentId(), k -> new HashMap<>()).put(row.getReactionType(), row.getCount());
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
        if (viewerId == null || commentIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, ReactionType> result = new HashMap<>();
        for (var row : commentReactionsRepository.findUserReactions(viewerId, commentIds)) {
            result.put(row.getCommentId(), ReactionType.valueOf(row.getReactionType().toUpperCase()));
        }
        return result;
    }
}
