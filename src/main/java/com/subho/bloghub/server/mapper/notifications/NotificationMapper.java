package com.subho.bloghub.server.mapper.notifications;

import com.subho.bloghub.client.dtos.notification.NotificationResponseDTO;
import com.subho.bloghub.client.enums.NotificationType;
import com.subho.bloghub.server.entity.notifications.Notifications;
import com.subho.bloghub.server.mapper.GenericMapper;
import org.springframework.stereotype.Component;

/**
 * {@code Void} is used as the request-DTO type parameter because
 * notifications are never created directly through a client request — they
 * are always system-generated side effects of other actions (follow,
 * react, comment, reply, mention). {@link #toEntity} therefore intentionally
 * throws, same pattern as other read-only-from-the-client mappers in this
 * codebase.
 */
@Component
public class NotificationMapper implements GenericMapper<Notifications, Void, NotificationResponseDTO> {

    @Override
    public Notifications toEntity(Void request) {
        throw new UnsupportedOperationException(
                "Notifications are system-generated; use NotificationService#create instead");
    }

    @Override
    public NotificationResponseDTO toResponse(Notifications entity) {
        return toResponse(entity, NotificationAggregateContext.empty());
    }

    public NotificationResponseDTO toResponse(Notifications entity, NotificationAggregateContext context) {
        if (entity == null) {
            return null;
        }

        NotificationResponseDTO.RelatedBlogDTO relatedBlog = entity.getBlog() == null ? null :
                NotificationResponseDTO.RelatedBlogDTO.builder()
                        .id(entity.getBlog().getId())
                        .title(entity.getBlog().getTitle())
                        .build();

        return NotificationResponseDTO.builder()
                .id(entity.getId())
                .type(NotificationType.valueOf(entity.getType().toUpperCase()))
                .actors(context.actors())
                .blog(relatedBlog)
                .commentId(entity.getComment() == null ? null : entity.getComment().getId())
                .message(context.message())
                .isRead(Boolean.TRUE.equals(entity.getIsRead()))
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
