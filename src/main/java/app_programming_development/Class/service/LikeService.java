package app_programming_development.Class.service;

import app_programming_development.Class.dto.response.LikeResponse;
import app_programming_development.Class.entity.LectureLikes;
import app_programming_development.Class.entity.Lectures;
import app_programming_development.Class.entity.Users;
import app_programming_development.Class.enums.NotificationType;
import app_programming_development.Class.exceptions.notFound.LectureNotFoundException;
import app_programming_development.Class.repository.LectureLikeRepository;
import app_programming_development.Class.repository.LectureRepository;
import app_programming_development.Class.security.SecurityUtils;
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

    // 강의 찜 등록/취소 토글
    @Transactional
    public LikeResponse toggleLike(Long lectureId) {
        Users currentUser = securityUtils.getCurrentUser();
        Lectures lecture = lectureRepository.findById(lectureId)
                .orElseThrow(LectureNotFoundException::new);

        boolean alreadyLiked = lectureLikeRepository.existsByUser_IdAndLectures_Id(currentUser.getId(), lectureId);

        if (alreadyLiked) {
            lectureLikeRepository.deleteByUser_IdAndLectures_Id(currentUser.getId(), lectureId);
            return new LikeResponse(false);
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
            return new LikeResponse(true);
        }
    }
}
