package app_programming_development.Class.service;

import app_programming_development.Class.certificate.entity.Certificates;
import app_programming_development.Class.certificate.repository.CertificateRepository;
import app_programming_development.Class.dto.mockexam.request.MockExamAddQuestionRequest;
import app_programming_development.Class.dto.mockexam.request.MockExamAnswerRequest;
import app_programming_development.Class.dto.mockexam.request.MockExamCreateRequest;
import app_programming_development.Class.dto.mockexam.request.MockExamSubmitRequest;
import app_programming_development.Class.dto.mockexam.response.MockExamCreateResponse;
import app_programming_development.Class.dto.mockexam.response.MockExamListResponse;
import app_programming_development.Class.dto.mockexam.response.MockExamQuestionsResponse;
import app_programming_development.Class.dto.mockexam.response.MockExamSubmitResponse;
import app_programming_development.Class.enums.UserRole;
import app_programming_development.Class.exceptions.forbidden.TeacherRoleRequiredException;
import app_programming_development.Class.exceptions.notFound.CertificateNotFoundException;
import app_programming_development.Class.exceptions.notFound.MockExamNotFoundException;
import app_programming_development.Class.exceptions.notFound.ProblemNotFoundException;
import app_programming_development.Class.mockexam.entity.MockExamQuestions;
import app_programming_development.Class.mockexam.entity.MockExamResults;
import app_programming_development.Class.mockexam.entity.MockExams;
import app_programming_development.Class.mockexam.repository.MockExamQuestionsRepository;
import app_programming_development.Class.mockexam.repository.MockExamRepository;
import app_programming_development.Class.mockexam.repository.MockExamResultsRepository;
import app_programming_development.Class.mockexam.service.MockExamService;
import app_programming_development.Class.problem.entity.Problems;
import app_programming_development.Class.problem.repository.ProblemRepository;
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
class MockExamServiceTest {

    @Mock private MockExamRepository mockExamRepository;
    @Mock private MockExamQuestionsRepository mockExamQuestionsRepository;
    @Mock private MockExamResultsRepository mockExamResultsRepository;
    @Mock private CertificateRepository certificateRepository;
    @Mock private ProblemRepository problemRepository;
    @Mock private SecurityUtils securityUtils;

    @InjectMocks
    private MockExamService mockExamService;

    private Users teacher;
    private Users admin;
    private Users student;
    private Certificates certificate;
    private MockExams exam;
    private Problems problem1;
    private Problems problem2;
    private Problems problem3;
    private MockExamQuestions question1;
    private MockExamQuestions question2;
    private MockExamQuestions question3;

    @BeforeEach
    void setUp() {
        teacher = Users.builder().email("teacher@test.com").password("pw").nickname("강사").role(UserRole.TEACHER).build();
        teacher.setId(1L);

        admin = Users.builder().email("admin@test.com").password("pw").nickname("관리자").role(UserRole.ADMIN).build();
        admin.setId(2L);

        student = Users.builder().email("student@test.com").password("pw").nickname("학생").role(UserRole.USER).build();
        student.setId(3L);

        certificate = Certificates.builder().name("정보처리기사").description("설명").build();

        exam = MockExams.builder().certificates(certificate).creator(teacher).title("1회 모의고사").timeLimit(60).build();
        exam.setId(100L);

        problem1 = Problems.builder().content("문제1").correctAnswer(1).explanation("해설1").build();
        problem1.setId(1L);

        problem2 = Problems.builder().content("문제2").correctAnswer(2).explanation("해설2").build();
        problem2.setId(2L);

        problem3 = Problems.builder().content("문제3").correctAnswer(3).explanation("해설3").build();
        problem3.setId(3L);

        question1 = MockExamQuestions.builder().mockExams(exam).problems(problem1).build();
        question2 = MockExamQuestions.builder().mockExams(exam).problems(problem2).build();
        question3 = MockExamQuestions.builder().mockExams(exam).problems(problem3).build();
    }

    // ── 모의고사 등록 ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("모의고사 등록 - 강사 성공")
    void createMockExam_강사_성공() {
        given(securityUtils.getCurrentUser()).willReturn(teacher);
        given(certificateRepository.findById(1L)).willReturn(Optional.of(certificate));
        given(mockExamRepository.save(any())).willAnswer(inv -> {
            MockExams e = inv.getArgument(0);
            e.setId(100L);
            return e;
        });

        MockExamCreateResponse result = mockExamService.createMockExam(new MockExamCreateRequest(1L, "1회 모의고사"));

        assertThat(result.getId()).isEqualTo(100L);
        then(mockExamRepository).should().save(any());
    }

    @Test
    @DisplayName("모의고사 등록 - 관리자도 성공")
    void createMockExam_관리자_성공() {
        given(securityUtils.getCurrentUser()).willReturn(admin);
        given(certificateRepository.findById(1L)).willReturn(Optional.of(certificate));
        given(mockExamRepository.save(any())).willReturn(exam);

        assertThatNoException().isThrownBy(() ->
                mockExamService.createMockExam(new MockExamCreateRequest(1L, "1회 모의고사")));
    }

    @Test
    @DisplayName("모의고사 등록 - 일반 사용자 권한 예외")
    void createMockExam_USER_예외() {
        given(securityUtils.getCurrentUser()).willReturn(student);

        assertThatThrownBy(() -> mockExamService.createMockExam(new MockExamCreateRequest(1L, "모의고사")))
                .isInstanceOf(TeacherRoleRequiredException.class);
        then(mockExamRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("모의고사 등록 - 존재하지 않는 자격증 예외")
    void createMockExam_자격증없음_예외() {
        given(securityUtils.getCurrentUser()).willReturn(teacher);
        given(certificateRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> mockExamService.createMockExam(new MockExamCreateRequest(999L, "모의고사")))
                .isInstanceOf(CertificateNotFoundException.class);
    }

    // ── 모의고사 목록 조회 ─────────────────────────────────────────────────────

    @Test
    @DisplayName("모의고사 목록 조회 - 성공")
    void getMockExams_성공() {
        given(mockExamRepository.findByCertificates_IdOrderByCreatedAtDesc(1L))
                .willReturn(List.of(exam));

        List<MockExamListResponse> result = mockExamService.getMockExams(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("1회 모의고사");
        assertThat(result.get(0).getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("모의고사 목록 조회 - 빈 목록 반환")
    void getMockExams_빈목록() {
        given(mockExamRepository.findByCertificates_IdOrderByCreatedAtDesc(1L))
                .willReturn(List.of());

        List<MockExamListResponse> result = mockExamService.getMockExams(1L);

        assertThat(result).isEmpty();
    }

    // ── 모의고사 응시 ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("모의고사 응시 - 문제 목록 반환 성공")
    void getMockExamQuestions_성공() {
        given(mockExamRepository.findById(100L)).willReturn(Optional.of(exam));
        given(mockExamQuestionsRepository.findByMockExams_Id(100L))
                .willReturn(List.of(question1, question2, question3));

        MockExamQuestionsResponse result = mockExamService.getMockExamQuestions(100L);

        assertThat(result.getMockExamId()).isEqualTo(100L);
        assertThat(result.getTitle()).isEqualTo("1회 모의고사");
        assertThat(result.getQuestions()).hasSize(3);
        assertThat(result.getQuestions().get(0).getProblemId()).isEqualTo(1L);
        assertThat(result.getQuestions().get(0).getContent()).isEqualTo("문제1");
    }

    @Test
    @DisplayName("모의고사 응시 - 문제 없는 모의고사도 빈 목록 반환")
    void getMockExamQuestions_문제없음() {
        given(mockExamRepository.findById(100L)).willReturn(Optional.of(exam));
        given(mockExamQuestionsRepository.findByMockExams_Id(100L)).willReturn(List.of());

        MockExamQuestionsResponse result = mockExamService.getMockExamQuestions(100L);

        assertThat(result.getQuestions()).isEmpty();
    }

    @Test
    @DisplayName("모의고사 응시 - 존재하지 않는 모의고사 예외")
    void getMockExamQuestions_모의고사없음_예외() {
        given(mockExamRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> mockExamService.getMockExamQuestions(999L))
                .isInstanceOf(MockExamNotFoundException.class);
    }

    // ── 모의고사 제출 ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("모의고사 제출 - 전부 정답 → 점수 100")
    void submitMockExam_전부정답_100점() {
        given(securityUtils.getCurrentUser()).willReturn(student);
        given(mockExamRepository.findById(100L)).willReturn(Optional.of(exam));
        given(mockExamQuestionsRepository.findByMockExams_Id(100L))
                .willReturn(List.of(question1, question2, question3));

        MockExamSubmitRequest req = new MockExamSubmitRequest(List.of(
                new MockExamAnswerRequest(1L, 1),
                new MockExamAnswerRequest(2L, 2),
                new MockExamAnswerRequest(3L, 3)
        ));

        MockExamSubmitResponse result = mockExamService.submitMockExam(100L, req);

        assertThat(result.getScore()).isEqualTo(100);
        assertThat(result.getResults()).hasSize(3);
        assertThat(result.getResults()).allMatch(r -> r.isCorrect());
    }

    @Test
    @DisplayName("모의고사 제출 - 일부 정답 → 점수 67")
    void submitMockExam_일부정답_67점() {
        given(securityUtils.getCurrentUser()).willReturn(student);
        given(mockExamRepository.findById(100L)).willReturn(Optional.of(exam));
        given(mockExamQuestionsRepository.findByMockExams_Id(100L))
                .willReturn(List.of(question1, question2, question3));

        // 3문제 중 2개 정답 → Math.round(2/3.0*100) = 67
        MockExamSubmitRequest req = new MockExamSubmitRequest(List.of(
                new MockExamAnswerRequest(1L, 1),  // 정답
                new MockExamAnswerRequest(2L, 1),  // 오답 (정답은 2)
                new MockExamAnswerRequest(3L, 3)   // 정답
        ));

        MockExamSubmitResponse result = mockExamService.submitMockExam(100L, req);

        assertThat(result.getScore()).isEqualTo(67);
        assertThat(result.getResults().stream().filter(r -> r.isCorrect()).count()).isEqualTo(2);
    }

    @Test
    @DisplayName("모의고사 제출 - 전부 오답 → 점수 0")
    void submitMockExam_전부오답_0점() {
        given(securityUtils.getCurrentUser()).willReturn(student);
        given(mockExamRepository.findById(100L)).willReturn(Optional.of(exam));
        given(mockExamQuestionsRepository.findByMockExams_Id(100L))
                .willReturn(List.of(question1, question2));

        MockExamSubmitRequest req = new MockExamSubmitRequest(List.of(
                new MockExamAnswerRequest(1L, 4),
                new MockExamAnswerRequest(2L, 4)
        ));

        MockExamSubmitResponse result = mockExamService.submitMockExam(100L, req);

        assertThat(result.getScore()).isEqualTo(0);
        assertThat(result.getResults()).allMatch(r -> !r.isCorrect());
    }

    @Test
    @DisplayName("모의고사 제출 - 빈 답안 제출 → 점수 0")
    void submitMockExam_빈답안_0점() {
        given(securityUtils.getCurrentUser()).willReturn(student);
        given(mockExamRepository.findById(100L)).willReturn(Optional.of(exam));
        given(mockExamQuestionsRepository.findByMockExams_Id(100L)).willReturn(List.of());

        MockExamSubmitRequest req = new MockExamSubmitRequest(List.of());
        MockExamSubmitResponse result = mockExamService.submitMockExam(100L, req);

        assertThat(result.getScore()).isEqualTo(0);
        assertThat(result.getResults()).isEmpty();
    }

    @Test
    @DisplayName("모의고사 제출 - 재제출 시 기존 결과 삭제 후 새로 저장")
    void submitMockExam_재제출_기존결과_삭제() {
        given(securityUtils.getCurrentUser()).willReturn(student);
        given(mockExamRepository.findById(100L)).willReturn(Optional.of(exam));
        given(mockExamQuestionsRepository.findByMockExams_Id(100L))
                .willReturn(List.of(question1));

        MockExamSubmitRequest req = new MockExamSubmitRequest(List.of(
                new MockExamAnswerRequest(1L, 1)
        ));

        mockExamService.submitMockExam(100L, req);

        then(mockExamResultsRepository).should().deleteByUser_IdAndMockExams_Id(3L, 100L);
        then(mockExamResultsRepository).should().save(any(MockExamResults.class));
    }

    @Test
    @DisplayName("모의고사 제출 - 모의고사에 없는 문제 답 제출 예외")
    void submitMockExam_없는문제_예외() {
        given(securityUtils.getCurrentUser()).willReturn(student);
        given(mockExamRepository.findById(100L)).willReturn(Optional.of(exam));
        given(mockExamQuestionsRepository.findByMockExams_Id(100L))
                .willReturn(List.of(question1));  // problem1만 포함

        // problem999는 이 모의고사에 없음
        MockExamSubmitRequest req = new MockExamSubmitRequest(List.of(
                new MockExamAnswerRequest(999L, 1)
        ));

        assertThatThrownBy(() -> mockExamService.submitMockExam(100L, req))
                .isInstanceOf(ProblemNotFoundException.class);
    }

    @Test
    @DisplayName("모의고사 제출 - 존재하지 않는 모의고사 예외")
    void submitMockExam_모의고사없음_예외() {
        given(securityUtils.getCurrentUser()).willReturn(student);
        given(mockExamRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> mockExamService.submitMockExam(999L, new MockExamSubmitRequest(List.of())))
                .isInstanceOf(MockExamNotFoundException.class);
    }

    // ── 모의고사 결과 조회 ─────────────────────────────────────────────────────

    @Test
    @DisplayName("모의고사 결과 조회 - 성공")
    void getMockExamResults_성공() {
        MockExamResults r1 = MockExamResults.builder().user(student).mockExams(exam).problems(problem1).selectedAnswer(1).isCorrect(true).build();
        MockExamResults r2 = MockExamResults.builder().user(student).mockExams(exam).problems(problem2).selectedAnswer(1).isCorrect(false).build();

        given(securityUtils.getCurrentUser()).willReturn(student);
        given(mockExamRepository.findById(100L)).willReturn(Optional.of(exam));
        given(mockExamResultsRepository.findByUser_IdAndMockExams_Id(3L, 100L))
                .willReturn(List.of(r1, r2));

        MockExamSubmitResponse result = mockExamService.getMockExamResults(100L);

        assertThat(result.getScore()).isEqualTo(50);
        assertThat(result.getResults()).hasSize(2);
        assertThat(result.getResults().get(0).isCorrect()).isTrue();
        assertThat(result.getResults().get(1).isCorrect()).isFalse();
    }

    @Test
    @DisplayName("모의고사 결과 조회 - 응시 이력 없으면 점수 0")
    void getMockExamResults_이력없음_0점() {
        given(securityUtils.getCurrentUser()).willReturn(student);
        given(mockExamRepository.findById(100L)).willReturn(Optional.of(exam));
        given(mockExamResultsRepository.findByUser_IdAndMockExams_Id(3L, 100L))
                .willReturn(List.of());

        MockExamSubmitResponse result = mockExamService.getMockExamResults(100L);

        assertThat(result.getScore()).isEqualTo(0);
        assertThat(result.getResults()).isEmpty();
    }

    @Test
    @DisplayName("모의고사 결과 조회 - 존재하지 않는 모의고사 예외")
    void getMockExamResults_모의고사없음_예외() {
        given(securityUtils.getCurrentUser()).willReturn(student);
        given(mockExamRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> mockExamService.getMockExamResults(999L))
                .isInstanceOf(MockExamNotFoundException.class);
    }

    // ── 모의고사 삭제 ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("모의고사 삭제 - 성공")
    void deleteMockExam_성공() {
        given(securityUtils.getCurrentUser()).willReturn(teacher);
        given(mockExamRepository.findById(100L)).willReturn(Optional.of(exam));

        assertThatNoException().isThrownBy(() -> mockExamService.deleteMockExam(100L));
        then(mockExamRepository).should().delete(exam);
    }

    @Test
    @DisplayName("모의고사 삭제 - 일반 사용자 권한 예외")
    void deleteMockExam_USER_예외() {
        given(securityUtils.getCurrentUser()).willReturn(student);

        assertThatThrownBy(() -> mockExamService.deleteMockExam(100L))
                .isInstanceOf(TeacherRoleRequiredException.class);
        then(mockExamRepository).should(never()).delete(any());
    }

    @Test
    @DisplayName("모의고사 삭제 - 존재하지 않는 모의고사 예외")
    void deleteMockExam_모의고사없음_예외() {
        given(securityUtils.getCurrentUser()).willReturn(teacher);
        given(mockExamRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> mockExamService.deleteMockExam(999L))
                .isInstanceOf(MockExamNotFoundException.class);
    }

    // ── 모의고사 문제 추가 ─────────────────────────────────────────────────────

    @Test
    @DisplayName("모의고사 문제 추가 - 성공")
    void addQuestion_성공() {
        given(securityUtils.getCurrentUser()).willReturn(teacher);
        given(mockExamRepository.findById(100L)).willReturn(Optional.of(exam));
        given(problemRepository.findById(1L)).willReturn(Optional.of(problem1));
        given(mockExamQuestionsRepository.existsByMockExams_IdAndProblems_Id(100L, 1L)).willReturn(false);

        assertThatNoException().isThrownBy(() ->
                mockExamService.addQuestion(100L, new MockExamAddQuestionRequest(1L)));
        then(mockExamQuestionsRepository).should().save(any(MockExamQuestions.class));
    }

    @Test
    @DisplayName("모의고사 문제 추가 - 이미 추가된 문제 → 저장 생략")
    void addQuestion_중복문제_저장생략() {
        given(securityUtils.getCurrentUser()).willReturn(teacher);
        given(mockExamRepository.findById(100L)).willReturn(Optional.of(exam));
        given(problemRepository.findById(1L)).willReturn(Optional.of(problem1));
        given(mockExamQuestionsRepository.existsByMockExams_IdAndProblems_Id(100L, 1L)).willReturn(true);

        assertThatNoException().isThrownBy(() ->
                mockExamService.addQuestion(100L, new MockExamAddQuestionRequest(1L)));
        then(mockExamQuestionsRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("모의고사 문제 추가 - 일반 사용자 권한 예외")
    void addQuestion_USER_예외() {
        given(securityUtils.getCurrentUser()).willReturn(student);

        assertThatThrownBy(() -> mockExamService.addQuestion(100L, new MockExamAddQuestionRequest(1L)))
                .isInstanceOf(TeacherRoleRequiredException.class);
    }

    @Test
    @DisplayName("모의고사 문제 추가 - 존재하지 않는 모의고사 예외")
    void addQuestion_모의고사없음_예외() {
        given(securityUtils.getCurrentUser()).willReturn(teacher);
        given(mockExamRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> mockExamService.addQuestion(999L, new MockExamAddQuestionRequest(1L)))
                .isInstanceOf(MockExamNotFoundException.class);
    }

    @Test
    @DisplayName("모의고사 문제 추가 - 존재하지 않는 문제 예외")
    void addQuestion_문제없음_예외() {
        given(securityUtils.getCurrentUser()).willReturn(teacher);
        given(mockExamRepository.findById(100L)).willReturn(Optional.of(exam));
        given(problemRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> mockExamService.addQuestion(100L, new MockExamAddQuestionRequest(999L)))
                .isInstanceOf(ProblemNotFoundException.class);
    }
}
