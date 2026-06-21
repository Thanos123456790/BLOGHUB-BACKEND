package com.subho.bloghub.server.mapper.notifications;

import com.subho.bloghub.client.dtos.notification.NotificationActorDTO;
import com.subho.bloghub.server.entity.users.Users;
import com.subho.bloghub.server.mapper.GenericMapper;
import org.springframework.stereotype.Component;

@Component
public class NotificationActorMapper implements GenericMapper<Users, Void, NotificationActorDTO> {

    @Override
    public Users toEntity(Void request) {
        throw new UnsupportedOperationException("NotificationActorDTO is read-only / derived from Users");
    }

    @Override
    public NotificationActorDTO toResponse(Users entity) {
        if (entity == null) {
            return null;
        }
        return NotificationActorDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .handle(entity.getHandle())
                .avatarUrl(entity.getAvatarUrl())
                .build();
    }
}
