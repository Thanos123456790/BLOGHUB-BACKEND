package com.subho.bloghub.server.mapper.notifications;

import com.subho.bloghub.client.dtos.notification.NotificationActorDTO;

import java.util.List;

public record NotificationAggregateContext(
        List<NotificationActorDTO> actors,
        String message
) {
    public static NotificationAggregateContext empty() {
        return new NotificationAggregateContext(List.of(), "");
    }
}
