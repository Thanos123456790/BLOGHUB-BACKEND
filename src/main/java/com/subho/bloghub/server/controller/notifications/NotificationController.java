package com.subho.bloghub.server.controller.notifications;

import com.subho.bloghub.client.apis.notifications.NotificationsAPI;
import com.subho.bloghub.client.dtos.notification.NotificationResponseDTO;
import com.subho.bloghub.server.common.PageRequestFactory;
import com.subho.bloghub.server.service.notifications.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NotificationController implements NotificationsAPI {

    private final NotificationService notificationService;
    private final PageRequestFactory pageRequestFactory;

    @Override
    public ResponseEntity<Page<NotificationResponseDTO>> getAllNotifications(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = pageRequestFactory.of(page, size);
        return ResponseEntity.ok(notificationService.getMyNotifications(accessToken, pageable));
    }

    @Override
    public ResponseEntity<Void> markAllNotificationAsRead(
            @RequestHeader(value = "Authorization", required = false) String accessToken) {
        notificationService.markAllAsRead(accessToken);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> markSingleNotificationAsRead(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @PathVariable String id) {
        notificationService.markAsRead(accessToken, id);
        return ResponseEntity.ok().build();
    }
}
