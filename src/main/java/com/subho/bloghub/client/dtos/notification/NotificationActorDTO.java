package com.subho.bloghub.client.dtos.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Minimal actor info shown inside a notification")
public class NotificationActorDTO {

    @Schema(description = "Actor UUID")
    private UUID id;

    @Schema(description = "Actor display name", example = "Arjun Mehta")
    private String name;

    @Schema(description = "Actor @handle", example = "arjunwrites")
    private String handle;

    @Schema(description = "Actor avatar URL")
    private String avatarUrl;
}
