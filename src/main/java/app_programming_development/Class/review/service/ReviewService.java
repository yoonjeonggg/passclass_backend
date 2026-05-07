package app_programming_development.Class.review.service;

import app_programming_development.Class.dto.review.request.ReviewRequest;
import app_programming_development.Class.dto.review.response.ReviewResponse;
import app_programming_development.Class.dto.review.response.ReviewSummaryResponse;
import app_programming_development.Class.enrollment.repository.EnrollmentRepository;
import app_programming_development.Class.exceptions.conflict.DuplicateReviewException;
import app_programming_development.Class.exceptions.forbidden.NotEnrolledException;
import app_programming_development.Class.exceptions.forbidden.NotReviewOwnerException;
import app_programming_development.Class.exceptions.notFound.LectureNotFoundException;
import app_programming_development.Class.exceptions.notFound.ReviewNotFoundException;
import app_programming_development.Class.lecture.entity.Lectures;
import app_programming_development.Class.lecture.repository.LectureRepository;
import app_programming_development.Class.review.entity.Reviews;
import app_programming_development.Class.review.repository.ReviewRepository;
import app_programming_development.Class.security.SecurityUtils;
import app_programming_development.Class.user.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final LectureRepository lectureRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SecurityUtils securityUtils;

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

    @Transactional(readOnly = true)
    public ReviewSummaryResponse getReviewSummary(Long lectureId) {
        if (!lectureRepository.existsById(lectureId)) {
            throw new LectureNotFoundException();
        }
        Double avgRating = reviewRepository.getAverageRating(lectureId);
        Long count = reviewRepository.countByLectures_Id(lectureId);
        return ReviewSummaryResponse.builder()
                .averageRating(avgRating != null ? avgRating : 0.0)
                .reviewCount(count)
                .build();
    }

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
