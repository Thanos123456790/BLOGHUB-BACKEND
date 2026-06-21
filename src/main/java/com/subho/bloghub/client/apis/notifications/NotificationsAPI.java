package com.subho.bloghub.client.apis.notifications;


import com.subho.bloghub.client.dtos.notification.NotificationResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notifications APIs", description = "Notification inbox for the currently authenticated user")
@RequestMapping("/api/v1/notifications")
public interface NotificationsAPI {

    @Operation(
            summary = "Get all notifications for current user",
            description = "Returns a paginated, most-recent-first list of notifications for the authenticated user."
    )
    @ApiResponse(responseCode = "200", description = "Notifications returned successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @GetMapping()
    ResponseEntity<Page<NotificationResponseDTO>> getAllNotifications(
            String accessToken,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    );


    @Operation(
            summary = "Mark all notifications as read",
            description = "Marks every unread notification belonging to the current user as read."
    )
    @ApiResponse(responseCode = "200", description = "Notifications marked as read")
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @PostMapping("/read-all")
    ResponseEntity<Void> markAllNotificationAsRead(String accessToken);


    @Operation(
            summary = "Mark a single notification as read",
            description = "Marks one notification, identified by id, as read. No-op if it was already read."
    )
    @ApiResponse(responseCode = "200", description = "Notification marked as read")
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token")
    @ApiResponse(responseCode = "404", description = "Notification not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @PatchMapping("/{id}/read")
    ResponseEntity<Void> markSingleNotificationAsRead(String accessToken, @PathVariable String id);

}
