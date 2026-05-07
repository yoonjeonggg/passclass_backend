package app_programming_development.Class.lecture.service;

import app_programming_development.Class.certificate.entity.Certificates;
import app_programming_development.Class.certificate.repository.CertificateRepository;
import app_programming_development.Class.chapter.repository.LectureChapterRepository;
import app_programming_development.Class.dto.certificate.response.CertificateInfo;
import app_programming_development.Class.dto.chapter.response.ChapterDto;
import app_programming_development.Class.dto.lecture.request.LectureRequest;
import app_programming_development.Class.dto.lecture.response.InstructorDto;
import app_programming_development.Class.dto.lecture.response.LectureCreateResponse;
import app_programming_development.Class.dto.lecture.response.LectureDetailResponse;
import app_programming_development.Class.dto.lecture.response.LectureListDto;
import app_programming_development.Class.enrollment.repository.EnrollmentRepository;
import app_programming_development.Class.enums.SortType;
import app_programming_development.Class.enums.UserRole;
import app_programming_development.Class.exceptions.forbidden.TeacherRoleRequiredException;
import app_programming_development.Class.exceptions.notFound.CertificateNotFoundException;
import app_programming_development.Class.exceptions.notFound.LectureNotFoundException;
import app_programming_development.Class.lecture.entity.Lectures;
import app_programming_development.Class.lecture.repository.LectureRepository;
import app_programming_development.Class.like.repository.LectureLikeRepository;
import app_programming_development.Class.review.repository.ReviewRepository;
import app_programming_development.Class.security.SecurityUtils;
import app_programming_development.Class.user.entity.Users;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LectureService {
    private final LectureRepository lectureRepository;
    private final SecurityUtils securityUtils;
    private final ReviewRepository reviewRepository;
    private final LectureLikeRepository lectureLikeRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LectureChapterRepository lectureChapterRepository;
    private final CertificateRepository certificateRepository;

    @Transactional
    public LectureCreateResponse createLecture(LectureRequest request) {
        Users instructor = securityUtils.getCurrentUser();
        if (!instructor.getRole().equals(UserRole.TEACHER)) {
            throw new TeacherRoleRequiredException();
        }

        Certificates certificate = certificateRepository.findById(request.getCertificateId())
                .orElseThrow(CertificateNotFoundException::new);

        Lectures lecture = Lectures.builder()
                .instructor(instructor)
                .certificates(certificate)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .thumbnailUrl(request.getThumbnailUrl())
                .build();

        lectureRepository.save(lecture);

        return LectureCreateResponse.from(lecture);
    }

    public Page<LectureListDto> getLectures(int page, int size, String category, SortType sort) {
        Sort sorting;
        switch (sort) {
            case POPULAR:
                sorting = Sort.by(Sort.Direction.DESC, "likeCount");
                break;
            case OLDEST:
                sorting = Sort.by(Sort.Direction.ASC, "createdAt");
                break;
            case LATEST:
            default:
                sorting = Sort.by(Sort.Direction.DESC, "createdAt");
        }
        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<Lectures> lectures;
        if (category != null && !category.isEmpty()) {
            lectures = lectureRepository.findByCategory(category, pageable);
        } else {
            lectures = lectureRepository.findAll(pageable);
        }
        return lectures.map(lecture -> {
            Double avgRating = reviewRepository.getAverageRating(lecture.getId());
            return LectureListDto.from(lecture, avgRating != null ? avgRating : 0.0);
        });
    }

    public LectureDetailResponse getLecture(Long lectureId) {
        Lectures lecture = lectureRepository.findById(lectureId)
                .orElseThrow(LectureNotFoundException::new);

        Double rating = reviewRepository.getAverageRating(lectureId);
        Long likeCount = lectureLikeRepository.countByLectures_Id(lectureId);
        Long studentCount = enrollmentRepository.countByLectures_Id(lectureId);

        boolean isLiked = false;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            try {
                Users currentUser = securityUtils.getCurrentUser();
                isLiked = lectureLikeRepository.existsByUser_IdAndLectures_Id(currentUser.getId(), lectureId);
            } catch (Exception ignored) {}
        }

        List<ChapterDto> chapters = lectureChapterRepository.findByLectures_Id(lectureId)
                .stream()
                .map(ch -> ChapterDto.builder()
                        .id(ch.getId())
                        .title(ch.getTitle())
                        .order(ch.getChapterOrder())
                        .build())
                .toList();

        InstructorDto instructor = InstructorDto.builder()
                .nickname(lecture.getInstructor().getNickname())
                .profileImage(lecture.getInstructor().getProfileUrl())
                .build();

        return LectureDetailResponse.builder()
                .id(lecture.getId())
                .title(lecture.getTitle())
                .category(lecture.getCategory())
                .thumbnailUrl(lecture.getThumbnailUrl())
                .description(lecture.getDescription())
                .rating(rating != null ? rating : 0.0)
                .isLiked(isLiked)
                .likeCount(likeCount)
                .studentCount(studentCount)
                .chapterCount(chapters.size())
                .instructor(instructor)
                .chapters(chapters)
                .certificate(CertificateInfo.builder()
                        .id(lecture.getCertificates().getId())
                        .name(lecture.getCertificates().getName())
                        .build())
                .build();
    }
}
