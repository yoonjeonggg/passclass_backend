package app_programming_development.Class.service;

import app_programming_development.Class.dto.response.NotificationResponse;
import app_programming_development.Class.dto.response.UnreadCountResponse;
import app_programming_development.Class.entity.Notifications;
import app_programming_development.Class.entity.Users;
import app_programming_development.Class.enums.NotificationType;
import app_programming_development.Class.exceptions.forbidden.NotNotificationOwnerException;
import java.util.Objects;
import app_programming_development.Class.exceptions.notFound.NotificationNotFoundException;
import app_programming_development.Class.repository.NotificationRepository;
import app_programming_development.Class.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SecurityUtils securityUtils;

    // 알림 목록 조회
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(int page, int size) {
        Users currentUser = securityUtils.getCurrentUser();
        return notificationRepository
                .findByUser_IdOrderByCreatedAtDesc(currentUser.getId(), PageRequest.of(page, size))
                .map(NotificationResponse::from);
    }

    // 알림 읽음 처리
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

    // 읽지 않은 알림 개수 조회
    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount() {
        Users currentUser = securityUtils.getCurrentUser();
        Long count = notificationRepository.countByUser_IdAndIsRead(currentUser.getId(), false);
        return new UnreadCountResponse(count);
    }

    // 알림 생성 (내부 호출용)
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
