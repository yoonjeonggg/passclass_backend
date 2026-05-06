package app_programming_development.Class.service;

import app_programming_development.Class.dto.request.ReviewRequest;
import app_programming_development.Class.dto.response.ReviewResponse;
import app_programming_development.Class.dto.response.ReviewSummaryResponse;
import app_programming_development.Class.entity.Lectures;
import app_programming_development.Class.entity.Reviews;
import app_programming_development.Class.entity.Users;
import app_programming_development.Class.exceptions.conflict.DuplicateReviewException;
import app_programming_development.Class.exceptions.forbidden.NotEnrolledException;
import app_programming_development.Class.exceptions.forbidden.NotReviewOwnerException;
import java.util.Objects;
import app_programming_development.Class.exceptions.notFound.LectureNotFoundException;
import app_programming_development.Class.exceptions.notFound.ReviewNotFoundException;
import app_programming_development.Class.repository.EnrollmentRepository;
import app_programming_development.Class.repository.LectureRepository;
import app_programming_development.Class.repository.ReviewRepository;
import app_programming_development.Class.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final LectureRepository lectureRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SecurityUtils securityUtils;

    // 리뷰 등록
    @Transactional
    public void createReview(ReviewRequest request) {
        Users currentUser = securityUtils.getCurrentUser();
        Lectures lecture = lectureRepository.findById(request.getLectureId())
                .orElseThrow(LectureNotFoundException::new);

        if (!enrollmentRepository.existsByUserIdAndLecturesId(currentUser.getId(), lecture.getId())) {
            throw new NotEnrolledException();
        }
        if (reviewRepository.existsByUser_IdAndLectures_Id(currentUser.getId(), lecture.getId())) {
            throw new DuplicateReviewException();
        }

        Reviews review = Reviews.builder()
                .lectures(lecture)
                .user(currentUser)
                .rating(request.getRating())
                .content(request.getContent())
                .build();

        reviewRepository.save(review);
    }

    // 리뷰 수정
    @Transactional
    public void updateReview(Long reviewId, ReviewRequest request) {
        Users currentUser = securityUtils.getCurrentUser();
        Reviews review = reviewRepository.findById(reviewId)
                .orElseThrow(ReviewNotFoundException::new);

        if (!Objects.equals(review.getUser().getId(), currentUser.getId())) {
            throw new NotReviewOwnerException();
        }

        review.setRating(request.getRating());
        review.setContent(request.getContent());
    }

    // 리뷰 요약 조회 (평균 별점, 리뷰 개수)
    @Transactional(readOnly = true)
    public ReviewSummaryResponse getReviewSummary(Long lectureId) {
        if (!lectureRepository.existsById(lectureId)) {
            throw new LectureNotFoundException();
        }
        Double avgRating = reviewRepository.getAverageRating(lectureId);
        Long count = reviewRepository.countByLectures_Id(lectureId);
        return new ReviewSummaryResponse(avgRating != null ? avgRating : 0.0, count);
    }

    // 리뷰 목록 조회
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviews(Long lectureId) {
        if (!lectureRepository.existsById(lectureId)) {
            throw new LectureNotFoundException();
        }
        return reviewRepository.findByLectures_IdOrderByCreatedAtDesc(lectureId)
                .stream()
                .map(ReviewResponse::from)
                .toList();
    }
}
