package app_programming_development.Class.like.service;

import app_programming_development.Class.dto.like.response.LikeResponse;
import app_programming_development.Class.enums.NotificationType;
import app_programming_development.Class.exceptions.notFound.LectureNotFoundException;
import app_programming_development.Class.lecture.entity.Lectures;
import app_programming_development.Class.lecture.repository.LectureRepository;
import app_programming_development.Class.like.entity.LectureLikes;
import app_programming_development.Class.like.repository.LectureLikeRepository;
import app_programming_development.Class.notification.service.NotificationService;
import app_programming_development.Class.security.SecurityUtils;
import app_programming_development.Class.user.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LectureLikeRepository lectureLikeRepository;
    private final LectureRepository lectureRepository;
    private final NotificationService notificationService;
    private final SecurityUtils securityUtils;

    @Transactional
    public LikeResponse toggleLike(Long lectureId) {
        Users currentUser = securityUtils.getCurrentUser();
        Lectures lecture = lectureRepository.findById(lectureId)
                .orElseThrow(LectureNotFoundException::new);

        boolean alreadyLiked = lectureLikeRepository.existsByUser_IdAndLectures_Id(currentUser.getId(), lectureId);

        if (alreadyLiked) {
            lectureLikeRepository.deleteByUser_IdAndLectures_Id(currentUser.getId(), lectureId);
            return LikeResponse.builder().isLiked(false).build();
        } else {
            LectureLikes like = LectureLikes.builder()
                    .user(currentUser)
                    .lectures(lecture)
                    .build();
            lectureLikeRepository.save(like);
            notificationService.createNotification(
                    lecture.getInstructor(),
                    NotificationType.LECTURE_LIKED,
                    lecture.getTitle() + " 강의에 새로운 좋아요가 추가되었습니다."
            );
            return LikeResponse.builder().isLiked(true).build();
        }
    }
}
