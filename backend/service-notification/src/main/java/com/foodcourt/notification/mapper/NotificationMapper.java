package com.foodcourt.notification.mapper;

import com.foodcourt.notification.dto.NotificationResponse;
import com.foodcourt.notification.entity.Notification;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponse toResponse(Notification notification);

    List<NotificationResponse> toResponseList(List<Notification> notifications);
}
