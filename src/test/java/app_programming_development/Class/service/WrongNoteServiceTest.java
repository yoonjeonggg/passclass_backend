package app_programming_development.Class.service;

import app_programming_development.Class.dto.wrongnote.response.WrongNoteResponse;
import app_programming_development.Class.enums.UserRole;
import app_programming_development.Class.exceptions.forbidden.NotWrongNoteOwnerException;
import app_programming_development.Class.exceptions.notFound.WrongNoteNotFoundException;
import app_programming_development.Class.problem.entity.Problems;
import app_programming_development.Class.problem.entity.WrongNotes;
import app_programming_development.Class.problem.repository.WrongNotesRepository;
import app_programming_development.Class.problem.service.WrongNoteService;
import app_programming_development.Class.security.SecurityUtils;
import app_programming_development.Class.user.entity.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class WrongNoteServiceTest {

    @Mock private WrongNotesRepository wrongNotesRepository;
    @Mock private SecurityUtils securityUtils;

    @InjectMocks
    private WrongNoteService wrongNoteService;

    private Users student;
    private Users otherStudent;
    private Problems problem;
    private WrongNotes wrongNote;

    @BeforeEach
    void setUp() {
        student = Users.builder().email("student@test.com").password("pw").nickname("학생").role(UserRole.USER).build();
        student.setId(1L);

        otherStudent = Users.builder().email("other@test.com").password("pw").nickname("다른학생").role(UserRole.USER).build();
        otherStudent.setId(2L);

        problem = Problems.builder()
                .content("스프링이란?")
                .option1("프레임워크").option2("라이브러리").option3("언어").option4("운영체제")
                .correctAnswer(1)
                .explanation("스프링은 Java 프레임워크입니다.")
                .build();
        problem.setId(10L);

        wrongNote = WrongNotes.builder()
                .user(student)
                .problems(problem)
                .selectedAnswer(3)
                .memo(null)
                .build();
        wrongNote.setId(100L);
    }

    // ── 오답노트 목록 조회 ─────────────────────────────────────────────────────

    @Test
    @DisplayName("오답노트 조회 - 성공")
    void getMyWrongNotes_성공() {
        given(securityUtils.getCurrentUser()).willReturn(student);
        given(wrongNotesRepository.findByUser_IdOrderByCreatedAtDesc(1L))
                .willReturn(List.of(wrongNote));

        List<WrongNoteResponse> result = wrongNoteService.getMyWrongNotes();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getWrongNoteId()).isEqualTo(100L);
        assertThat(result.get(0).getProblemId()).isEqualTo(10L);
        assertThat(result.get(0).getContent()).isEqualTo("스프링이란?");
        assertThat(result.get(0).getSelectedAnswer()).isEqualTo(3);
        assertThat(result.get(0).getCorrectAnswer()).isEqualTo(1);
        assertThat(result.get(0).getExplanation()).isEqualTo("스프링은 Java 프레임워크입니다.");
    }

    @Test
    @DisplayName("오답노트 조회 - 오답노트 없으면 빈 목록 반환")
    void getMyWrongNotes_빈목록() {
        given(securityUtils.getCurrentUser()).willReturn(student);
        given(wrongNotesRepository.findByUser_IdOrderByCreatedAtDesc(1L)).willReturn(List.of());

        List<WrongNoteResponse> result = wrongNoteService.getMyWrongNotes();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("오답노트 조회 - 여러 오답노트 모두 반환")
    void getMyWrongNotes_여러개() {
        Problems problem2 = Problems.builder().content("JPA란?").correctAnswer(2).explanation("ORM 기술").build();
        problem2.setId(11L);

        WrongNotes wrongNote2 = WrongNotes.builder()
                .user(student).problems(problem2).selectedAnswer(1).build();
        wrongNote2.setId(101L);

        given(securityUtils.getCurrentUser()).willReturn(student);
        given(wrongNotesRepository.findByUser_IdOrderByCreatedAtDesc(1L))
                .willReturn(List.of(wrongNote, wrongNote2));

        List<WrongNoteResponse> result = wrongNoteService.getMyWrongNotes();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getProblemId()).isEqualTo(10L);
        assertThat(result.get(1).getProblemId()).isEqualTo(11L);
    }

    // ── 오답노트 삭제 ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("오답노트 삭제 - 성공")
    void deleteWrongNote_성공() {
        given(securityUtils.getCurrentUser()).willReturn(student);
        given(wrongNotesRepository.findById(100L)).willReturn(Optional.of(wrongNote));

        assertThatNoException().isThrownBy(() -> wrongNoteService.deleteWrongNote(100L));
        then(wrongNotesRepository).should().delete(wrongNote);
    }

    @Test
    @DisplayName("오답노트 삭제 - 존재하지 않는 오답노트 예외")
    void deleteWrongNote_오답노트없음_예외() {
        given(securityUtils.getCurrentUser()).willReturn(student);
        given(wrongNotesRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> wrongNoteService.deleteWrongNote(999L))
                .isInstanceOf(WrongNoteNotFoundException.class);
        then(wrongNotesRepository).should(never()).delete(any());
    }

    @Test
    @DisplayName("오답노트 삭제 - 본인 오답노트가 아닌 경우 예외")
    void deleteWrongNote_본인아님_예외() {
        given(securityUtils.getCurrentUser()).willReturn(otherStudent);
        given(wrongNotesRepository.findById(100L)).willReturn(Optional.of(wrongNote));

        assertThatThrownBy(() -> wrongNoteService.deleteWrongNote(100L))
                .isInstanceOf(NotWrongNoteOwnerException.class);
        then(wrongNotesRepository).should(never()).delete(any());
    }
}
