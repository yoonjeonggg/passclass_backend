package app_programming_development.Class.service;

import app_programming_development.Class.certificate.entity.Certificates;
import app_programming_development.Class.certificate.repository.CertificateRepository;
import app_programming_development.Class.dto.problem.request.ProblemCreateRequest;
import app_programming_development.Class.dto.problem.request.ProblemSolveRequest;
import app_programming_development.Class.dto.problem.request.ProblemUpdateRequest;
import app_programming_development.Class.dto.problem.response.ProblemCreateResponse;
import app_programming_development.Class.dto.problem.response.ProblemListResponse;
import app_programming_development.Class.dto.problem.response.ProblemSolveResponse;
import app_programming_development.Class.enums.UserRole;
import app_programming_development.Class.exceptions.forbidden.TeacherRoleRequiredException;
import app_programming_development.Class.exceptions.notFound.CertificateNotFoundException;
import app_programming_development.Class.exceptions.notFound.ProblemNotFoundException;
import app_programming_development.Class.problem.entity.Problems;
import app_programming_development.Class.problem.entity.WrongNotes;
import app_programming_development.Class.problem.repository.ProblemRepository;
import app_programming_development.Class.problem.repository.ProblemSolvesRepository;
import app_programming_development.Class.problem.repository.WrongNotesRepository;
import app_programming_development.Class.problem.service.ProblemService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ProblemServiceTest {

    @Mock private ProblemRepository problemRepository;
    @Mock private ProblemSolvesRepository problemSolvesRepository;
    @Mock private WrongNotesRepository wrongNotesRepository;
    @Mock private CertificateRepository certificateRepository;
    @Mock private SecurityUtils securityUtils;

    @InjectMocks
    private ProblemService problemService;

    private Users teacher;
    private Users admin;
    private Users student;
    private Certificates certificate;
    private Problems problem;

    @BeforeEach
    void setUp() {
        teacher = Users.builder().email("teacher@test.com").password("pw").nickname("강사").role(UserRole.TEACHER).build();
        teacher.setId(1L);

        admin = Users.builder().email("admin@test.com").password("pw").nickname("관리자").role(UserRole.ADMIN).build();
        admin.setId(2L);

        student = Users.builder().email("student@test.com").password("pw").nickname("학생").role(UserRole.USER).build();
        student.setId(3L);

        certificate = Certificates.builder().name("정보처리기사").description("설명").build();

        problem = Problems.builder()
                .certificates(certificate)
                .user(teacher)
                .content("스프링이란?")
                .option1("프레임워크").option2("라이브러리").option3("언어").option4("운영체제")
                .correctAnswer(1)
                .explanation("스프링은 Java 프레임워크입니다.")
                .build();
        problem.setId(10L);
    }

    // ── 문제 등록 ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("문제 등록 - 강사 성공")
    void createProblem_강사_성공() {
        given(securityUtils.getCurrentUser()).willReturn(teacher);
        given(certificateRepository.findById(1L)).willReturn(Optional.of(certificate));
        given(problemRepository.save(any())).willAnswer(inv -> {
            Problems p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        ProblemCreateRequest req = new ProblemCreateRequest(1L, "문제", "①", "②", "③", "④", 1, "해설");
        ProblemCreateResponse result = problemService.createProblem(req);

        assertThat(result.getId()).isEqualTo(10L);
        then(problemRepository).should().save(any());
    }

    @Test
    @DisplayName("문제 등록 - 관리자도 성공")
    void createProblem_관리자_성공() {
        given(securityUtils.getCurrentUser()).willReturn(admin);
        given(certificateRepository.findById(1L)).willReturn(Optional.of(certificate));
        given(problemRepository.save(any())).willAnswer(inv -> {
            Problems p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        ProblemCreateRequest req = new ProblemCreateRequest(1L, "문제", "①", "②", "③", "④", 2, "해설");
        assertThatNoException().isThrownBy(() -> problemService.createProblem(req));
    }

    @Test
    @DisplayName("문제 등록 - 일반 사용자 권한 예외")
    void createProblem_USER_예외() {
        given(securityUtils.getCurrentUser()).willReturn(student);

        ProblemCreateRequest req = new ProblemCreateRequest(1L, "문제", "①", "②", "③", "④", 1, "해설");
        assertThatThrownBy(() -> problemService.createProblem(req))
                .isInstanceOf(TeacherRoleRequiredException.class);
        then(problemRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("문제 등록 - 존재하지 않는 자격증 예외")
    void createProblem_자격증없음_예외() {
        given(securityUtils.getCurrentUser()).willReturn(teacher);
        given(certificateRepository.findById(999L)).willReturn(Optional.empty());

        ProblemCreateRequest req = new ProblemCreateRequest(999L, "문제", "①", "②", "③", "④", 1, "해설");
        assertThatThrownBy(() -> problemService.createProblem(req))
                .isInstanceOf(CertificateNotFoundException.class);
    }

    // ── 문제 목록 조회 ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("문제 목록 조회 - 성공")
    void getProblems_성공() {
        given(problemRepository.findByCertificates_IdOrderByCreatedAtDesc(1L))
                .willReturn(List.of(problem));

        List<ProblemListResponse> result = problemService.getProblems(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("스프링이란?");
    }

    @Test
    @DisplayName("문제 목록 조회 - 빈 목록 반환")
    void getProblems_빈목록() {
        given(problemRepository.findByCertificates_IdOrderByCreatedAtDesc(1L))
                .willReturn(List.of());

        List<ProblemListResponse> result = problemService.getProblems(1L);

        assertThat(result).isEmpty();
    }

    // ── 문제 수정 ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("문제 수정 - 성공")
    void updateProblem_성공() {
        given(securityUtils.getCurrentUser()).willReturn(teacher);
        given(problemRepository.findById(10L)).willReturn(Optional.of(problem));

        ProblemUpdateRequest req = new ProblemUpdateRequest("수정된 문제", "①", "②", "③", "④", 2, "수정된 해설");
        ProblemCreateResponse result = problemService.updateProblem(10L, req);

        assertThat(problem.getContent()).isEqualTo("수정된 문제");
        assertThat(problem.getCorrectAnswer()).isEqualTo(2);
        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("문제 수정 - 일반 사용자 권한 예외")
    void updateProblem_USER_예외() {
        given(securityUtils.getCurrentUser()).willReturn(student);

        ProblemUpdateRequest req = new ProblemUpdateRequest("수정", "①", "②", "③", "④", 1, "해설");
        assertThatThrownBy(() -> problemService.updateProblem(10L, req))
                .isInstanceOf(TeacherRoleRequiredException.class);
    }

    @Test
    @DisplayName("문제 수정 - 존재하지 않는 문제 예외")
    void updateProblem_문제없음_예외() {
        given(securityUtils.getCurrentUser()).willReturn(teacher);
        given(problemRepository.findById(999L)).willReturn(Optional.empty());

        ProblemUpdateRequest req = new ProblemUpdateRequest("수정", "①", "②", "③", "④", 1, "해설");
        assertThatThrownBy(() -> problemService.updateProblem(999L, req))
                .isInstanceOf(ProblemNotFoundException.class);
    }

    // ── 문제 삭제 ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("문제 삭제 - 성공")
    void deleteProblem_성공() {
        given(securityUtils.getCurrentUser()).willReturn(teacher);
        given(problemRepository.findById(10L)).willReturn(Optional.of(problem));

        assertThatNoException().isThrownBy(() -> problemService.deleteProblem(10L));
        then(problemRepository).should().delete(problem);
    }

    @Test
    @DisplayName("문제 삭제 - 일반 사용자 권한 예외")
    void deleteProblem_USER_예외() {
        given(securityUtils.getCurrentUser()).willReturn(student);

        assertThatThrownBy(() -> problemService.deleteProblem(10L))
                .isInstanceOf(TeacherRoleRequiredException.class);
        then(problemRepository).should(never()).delete(any());
    }

    @Test
    @DisplayName("문제 삭제 - 존재하지 않는 문제 예외")
    void deleteProblem_문제없음_예외() {
        given(securityUtils.getCurrentUser()).willReturn(teacher);
        given(problemRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> problemService.deleteProblem(999L))
                .isInstanceOf(ProblemNotFoundException.class);
    }

    // ── 문제 풀이 제출 ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("문제 풀이 - 정답 제출 성공")
    void solveProblem_정답_성공() {
        given(securityUtils.getCurrentUser()).willReturn(student);
        given(problemRepository.findById(10L)).willReturn(Optional.of(problem));

        ProblemSolveResponse result = problemService.solveProblem(10L, new ProblemSolveRequest(1));

        assertThat(result.isCorrect()).isTrue();
        assertThat(result.getExplanation()).isEqualTo("스프링은 Java 프레임워크입니다.");
        then(wrongNotesRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("문제 풀이 - 오답 제출 → 오답노트 신규 생성")
    void solveProblem_오답_오답노트_신규생성() {
        given(securityUtils.getCurrentUser()).willReturn(student);
        given(problemRepository.findById(10L)).willReturn(Optional.of(problem));
        given(wrongNotesRepository.findByUser_IdAndProblems_Id(3L, 10L)).willReturn(Optional.empty());

        ProblemSolveResponse result = problemService.solveProblem(10L, new ProblemSolveRequest(2));

        assertThat(result.isCorrect()).isFalse();
        assertThat(result.getExplanation()).isEqualTo("스프링은 Java 프레임워크입니다.");
        then(wrongNotesRepository).should().save(any(WrongNotes.class));
    }

    @Test
    @DisplayName("문제 풀이 - 오답 재제출 → 기존 오답노트 selectedAnswer 업데이트")
    void solveProblem_오답_오답노트_업데이트() {
        WrongNotes existing = WrongNotes.builder()
                .user(student).problems(problem).selectedAnswer(2).build();
        existing.setId(100L);

        given(securityUtils.getCurrentUser()).willReturn(student);
        given(problemRepository.findById(10L)).willReturn(Optional.of(problem));
        given(wrongNotesRepository.findByUser_IdAndProblems_Id(3L, 10L)).willReturn(Optional.of(existing));

        problemService.solveProblem(10L, new ProblemSolveRequest(3));

        assertThat(existing.getSelectedAnswer()).isEqualTo(3);
        then(wrongNotesRepository).should().save(existing);
    }

    @Test
    @DisplayName("문제 풀이 - 존재하지 않는 문제 예외")
    void solveProblem_문제없음_예외() {
        given(securityUtils.getCurrentUser()).willReturn(student);
        given(problemRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> problemService.solveProblem(999L, new ProblemSolveRequest(1)))
                .isInstanceOf(ProblemNotFoundException.class);
    }
}
