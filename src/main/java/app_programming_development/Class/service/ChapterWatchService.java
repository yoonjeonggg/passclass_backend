package app_programming_development.Class.service;

import app_programming_development.Class.dto.response.ChapterWatchResponse;
import app_programming_development.Class.entity.ChapterProgress;
import app_programming_development.Class.entity.LectureChapters;
import app_programming_development.Class.entity.Users;
import app_programming_development.Class.exceptions.forbidden.NotEnrolledException;
import app_programming_development.Class.exceptions.notFound.ChapterNotFoundException;
import app_programming_development.Class.repository.ChapterProgressRepository;
import app_programming_development.Class.repository.EnrollmentRepository;
import app_programming_development.Class.repository.LectureChapterRepository;
import app_programming_development.Class.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChapterWatchService {

    private final LectureChapterRepository lectureChapterRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ChapterProgressRepository chapterProgressRepository;
    private final SecurityUtils securityUtils;

    // 챕터 영상 시청 (수강생 전용)
    @Transactional
    public ChapterWatchResponse watchChapter(Long chapterId) {
        Users currentUser = securityUtils.getCurrentUser();

        LectureChapters chapter = lectureChapterRepository.findById(chapterId)
                .orElseThrow(ChapterNotFoundException::new);

        Long lectureId = chapter.getLectures().getId();

        if (!enrollmentRepository.existsByUserIdAndLecturesId(currentUser.getId(), lectureId)) {
            throw new NotEnrolledException();
        }

        // 진도 레코드 없으면 자동 생성 (처음 시청)
        ChapterProgress progress = chapterProgressRepository
                .findByUserIdAndChapterId(currentUser.getId(), chapterId)
                .orElseGet(() -> chapterProgressRepository.save(
                        ChapterProgress.builder()
                                .user(currentUser)
                                .chapter(chapter)
                                .build()
                ));

        return ChapterWatchResponse.of(chapter, progress.isCompleted());
    }

    // 챕터 시청 완료 처리
    @Transactional
    public void completeChapter(Long chapterId) {
        Users currentUser = securityUtils.getCurrentUser();

        LectureChapters chapter = lectureChapterRepository.findById(chapterId)
                .orElseThrow(ChapterNotFoundException::new);

        if (!enrollmentRepository.existsByUserIdAndLecturesId(
                currentUser.getId(), chapter.getLectures().getId())) {
            throw new NotEnrolledException();
        }

        ChapterProgress progress = chapterProgressRepository
                .findByUserIdAndChapterId(currentUser.getId(), chapterId)
                .orElseGet(() -> chapterProgressRepository.save(
                        ChapterProgress.builder()
                                .user(currentUser)
                                .chapter(chapter)
                                .build()
                ));

        progress.markCompleted();
    }
}
