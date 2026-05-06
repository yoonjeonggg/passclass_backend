package app_programming_development.Class.service;

import app_programming_development.Class.dto.response.EnrollmentResponse;
import app_programming_development.Class.entity.Enrollments;
import app_programming_development.Class.entity.Lectures;
import app_programming_development.Class.entity.Users;
import app_programming_development.Class.exceptions.conflict.AlreadyEnrolledException;
import app_programming_development.Class.exceptions.notFound.EnrollmentNotFoundException;
import app_programming_development.Class.exceptions.notFound.LectureNotFoundException;
import app_programming_development.Class.repository.EnrollmentRepository;
import app_programming_development.Class.repository.LectureRepository;
import app_programming_development.Class.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final LectureRepository lectureRepository;
    private final SecurityUtils securityUtils;

    // 수강 신청
    @Transactional
    public EnrollmentResponse enroll(Long lectureId) {
        Users currentUser = securityUtils.getCurrentUser();

        if (enrollmentRepository.existsByUserIdAndLecturesId(currentUser.getId(), lectureId)) {
            throw new AlreadyEnrolledException();
        }

        Lectures lecture = lectureRepository.findById(lectureId)
                .orElseThrow(LectureNotFoundException::new);

        Enrollments enrollment = Enrollments.builder()
                .user(currentUser)
                .lectures(lecture)
                .build();

        enrollmentRepository.save(enrollment);
        return EnrollmentResponse.from(enrollment);
    }

    // 수강 취소
    @Transactional
    public void cancelEnrollment(Long lectureId) {
        Users currentUser = securityUtils.getCurrentUser();

        Enrollments enrollment = enrollmentRepository
                .findByUserIdAndLecturesId(currentUser.getId(), lectureId)
                .orElseThrow(EnrollmentNotFoundException::new);

        enrollmentRepository.delete(enrollment);
    }

    // 내 수강 목록 조회
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getMyEnrollments() {
        Users currentUser = securityUtils.getCurrentUser();
        return enrollmentRepository.findByUserId(currentUser.getId())
                .stream()
                .map(EnrollmentResponse::from)
                .toList();
    }
}
