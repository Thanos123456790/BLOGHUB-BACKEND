package com.subho.bloghub.client.dtos.notification;

import com.subho.bloghub.client.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A single notification in the user's inbox")
public class NotificationResponseDTO {

    @Schema(description = "Notification UUID")
    private UUID id;

    @Schema(description = "Notification event type")
    private NotificationType type;

    @Schema(description = "Users who triggered this notification (grouped, e.g. 3 people reacted)")
    private List<NotificationActorDTO> actors;

    @Schema(description = "Related blog summary (nullable)", nullable = true)
    private RelatedBlogDTO blog;

    @Schema(description = "Related comment ID (nullable)", nullable = true)
    private UUID commentId;

    @Schema(description = "Human-readable message suffix", example = "reacted to your post")
    private String message;

    @Schema(description = "Whether the notification has been read")
    private boolean isRead;

    @Schema(description = "When the notification was created")
    private Instant createdAt;

    // ── Nested blog summary ────────────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Minimal blog info attached to a notification")
    public static class RelatedBlogDTO {

        @Schema(description = "Blog UUID")
        private UUID id;

        @Schema(description = "Blog title")
        private String title;
    }
}
