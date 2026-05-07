package app_programming_development.Class.notification.service;

import app_programming_development.Class.dto.notification.response.NotificationResponse;
import app_programming_development.Class.dto.notification.response.UnreadCountResponse;
import app_programming_development.Class.enums.NotificationType;
import app_programming_development.Class.exceptions.forbidden.NotNotificationOwnerException;
import app_programming_development.Class.exceptions.notFound.NotificationNotFoundException;
import app_programming_development.Class.notification.entity.Notifications;
import app_programming_development.Class.notification.repository.NotificationRepository;
import app_programming_development.Class.security.SecurityUtils;
import app_programming_development.Class.user.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(int page, int size) {
        Users currentUser = securityUtils.getCurrentUser();
        return notificationRepository
                .findByUser_IdOrderByCreatedAtDesc(currentUser.getId(), PageRequest.of(page, size))
                .map(NotificationResponse::from);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Users currentUser = securityUtils.getCurrentUser();
        Notifications notification = notificationRepository.findById(notificationId)
                .orElseThrow(NotificationNotFoundException::new);

        if (!Objects.equals(notification.getUser().getId(), currentUser.getId())) {
            throw new NotNotificationOwnerException();
        }

        notification.setRead(true);
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount() {
        Users currentUser = securityUtils.getCurrentUser();
        Long count = notificationRepository.countByUser_IdAndIsRead(currentUser.getId(), false);
        return UnreadCountResponse.builder().unreadCount(count).build();
    }

    @Transactional
    public void createNotification(Users recipient, NotificationType type, String content) {
        Notifications notification = Notifications.builder()
                .user(recipient)
                .type(type)
                .content(content)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }
}
