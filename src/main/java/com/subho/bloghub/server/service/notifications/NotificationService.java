package com.subho.bloghub.server.service.notifications;

import com.subho.bloghub.client.dtos.notification.NotificationResponseDTO;
import com.subho.bloghub.server.entity.blogs.Blogs;
import com.subho.bloghub.server.entity.comments.Comments;
import com.subho.bloghub.server.entity.users.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    Page<NotificationResponseDTO> getMyNotifications(String accessToken, Pageable pageable);

    void markAllAsRead(String accessToken);

    void markAsRead(String accessToken, String notificationId);

    /**
     * Internal hook used by other services (Follow, Blog, Comment) to record
     * a notification as a side effect of their own action — e.g. FollowService
     * calls this after persisting a new follow row. Not exposed over HTTP;
     * notifications are never created directly by a client request.
     */
    void notify(Users recipient, Users actor, String type, Blogs blog, Comments comment, String message);
}
