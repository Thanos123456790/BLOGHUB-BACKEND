package com.subho.bloghub.server.service.impl;

import com.subho.bloghub.client.dtos.notification.NotificationActorDTO;
import com.subho.bloghub.client.dtos.notification.NotificationResponseDTO;
import com.subho.bloghub.client.enums.NotificationType;
import com.subho.bloghub.server.common.CurrentUserResolver;
import com.subho.bloghub.server.common.UuidUtils;
import com.subho.bloghub.server.entity.blogs.Blogs;
import com.subho.bloghub.server.entity.comments.Comments;
import com.subho.bloghub.server.entity.notifications.NotificationActors;
import com.subho.bloghub.server.entity.notifications.Notifications;
import com.subho.bloghub.server.entity.users.Users;
import com.subho.bloghub.server.mapper.notifications.NotificationActorMapper;
import com.subho.bloghub.server.mapper.notifications.NotificationAggregateContext;
import com.subho.bloghub.server.mapper.notifications.NotificationMapper;
import com.subho.bloghub.server.repository.notifications.NotificationActorsRepository;
import com.subho.bloghub.server.repository.notifications.NotificationsRepository;
import com.subho.bloghub.server.service.notifications.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private static final long DEDUP_WINDOW_MINUTES = 30;

    private final NotificationsRepository notificationsRepository;
    private final NotificationActorsRepository notificationActorsRepository;
    private final NotificationMapper notificationMapper;
    private final NotificationActorMapper notificationActorMapper;
    private final CurrentUserResolver currentUserResolver;

    @Override
    public Page<NotificationResponseDTO> getMyNotifications(String accessToken, Pageable pageable) {
        UUID userId = currentUserResolver.requireCurrentUserId(accessToken);
        Page<Notifications> page = notificationsRepository.findByRecipient_IdOrderByCreatedAtDesc(userId, pageable);

        List<Notifications> notifications = page.getContent();
        if (notifications.isEmpty()) {
            return page.map(n -> notificationMapper.toResponse(n, NotificationAggregateContext.empty()));
        }

        List<UUID> notificationIds = notifications.stream().map(Notifications::getId).toList();
        Map<UUID, List<NotificationActorDTO>> actorsByNotification = fetchActors(notificationIds);

        return page.map(n -> notificationMapper.toResponse(n, new NotificationAggregateContext(
                actorsByNotification.getOrDefault(n.getId(), List.of()),
                buildMessage(n, actorsByNotification.getOrDefault(n.getId(), List.of()))
        )));
    }

    @Override
    @Transactional
    public void markAllAsRead(String accessToken) {
        UUID userId = currentUserResolver.requireCurrentUserId(accessToken);
        notificationsRepository.markAllAsRead(userId);
    }

    @Override
    @Transactional
    public void markAsRead(String accessToken, String notificationId) {
        UUID userId = currentUserResolver.requireCurrentUserId(accessToken);
        UUID id = UuidUtils.parse(notificationId, "notification id");
        notificationsRepository.markAsRead(id, userId);
    }

    @Override
    @Transactional
    public void notify(Users recipient, Users actor, String type, Blogs blog, Comments comment, String message) {
        if (recipient == null || actor == null || recipient.getId().equals(actor.getId())) {
            return;
        }

        // VLN-08b FIX: Validate type against the known NotificationType enum values.
        // Unknown types are silently dropped instead of creating junk notifications.
        try {
            NotificationType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Ignoring notification with unknown type '{}' from actor {} to recipient {}",
                    type, actor.getId(), recipient.getId());
            return;
        }

        UUID blogId    = blog    != null ? blog.getId()    : null;
        UUID commentId = comment != null ? comment.getId() : null;
        Instant since  = Instant.now().minus(DEDUP_WINDOW_MINUTES, ChronoUnit.MINUTES);

        boolean alreadyNotified = notificationsRepository.existsDuplicate(
                recipient.getId(), actor.getId(), type, blogId, commentId, since);

        if (alreadyNotified) {
            return;
        }

        Notifications notification = Notifications.builder()
                .recipient(recipient)
                .blog(blog)
                .comment(comment)
                .type(type)
                .isRead(false)
                .build();
        Notifications saved = notificationsRepository.save(notification);

        notificationActorsRepository.save(NotificationActors.builder()
                .notification(saved)
                .actor(actor)
                .build());
    }

    private Map<UUID, List<NotificationActorDTO>> fetchActors(List<UUID> notificationIds) {
        Map<UUID, List<NotificationActorDTO>> result = new HashMap<>();
        for (NotificationActors na : notificationActorsRepository.findByNotification_IdIn(notificationIds)) {
            result.computeIfAbsent(na.getNotification().getId(), k -> new ArrayList<>())
                    .add(notificationActorMapper.toResponse(na.getActor()));
        }
        return result;
    }

    private String buildMessage(Notifications notification, List<NotificationActorDTO> actors) {
        String actorPart = actors.isEmpty() ? "Someone"
                : actors.size() == 1 ? actors.get(0).getName()
                : actors.get(0).getName() + " and " + (actors.size() - 1) + " other" + (actors.size() > 2 ? "s" : "");

        return switch (notification.getType().toUpperCase()) {
            case "FOLLOW"           -> actorPart + " followed you";
            case "REACTION"         -> actorPart + " reacted to your post";
            case "COMMENT"          -> actorPart + " commented on your post";
            case "REPLY"            -> actorPart + " replied to your comment";
            case "MENTION"          -> actorPart + " mentioned you in a comment";
            case "COMMENT_REACTION" -> actorPart + " reacted to your comment";
            default                 -> actorPart + " interacted with your content";
        };
    }
}
