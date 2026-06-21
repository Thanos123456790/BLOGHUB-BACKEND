package com.subho.bloghub.server.service.impl;

import com.subho.bloghub.client.dtos.notification.NotificationActorDTO;
import com.subho.bloghub.client.dtos.notification.NotificationResponseDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

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
        // Never notify yourself about your own action (e.g. reacting to your own post)
        if (recipient == null || actor == null || recipient.getId().equals(actor.getId())) {
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

        return switch (notification.getType()) {
            case "follow" -> actorPart + " followed you";
            case "reaction" -> actorPart + " reacted to your post";
            case "comment" -> actorPart + " commented on your post";
            case "reply" -> actorPart + " replied to your comment";
            case "mention" -> actorPart + " mentioned you in a comment";
            case "comment_reaction" -> actorPart + " reacted to your comment";
            default -> actorPart + " interacted with your content";
        };
    }
}
