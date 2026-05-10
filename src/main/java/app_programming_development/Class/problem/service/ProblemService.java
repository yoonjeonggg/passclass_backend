package app_programming_development.Class.problem.service;

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
import app_programming_development.Class.exceptions.notFound.ProblemNotFoundException;
import app_programming_development.Class.problem.entity.ProblemSolves;
import app_programming_development.Class.problem.entity.Problems;
import app_programming_development.Class.problem.entity.WrongNotes;
import app_programming_development.Class.problem.repository.ProblemRepository;
import app_programming_development.Class.problem.repository.ProblemSolvesRepository;
import app_programming_development.Class.problem.repository.WrongNotesRepository;
import app_programming_development.Class.security.SecurityUtils;
import app_programming_development.Class.user.entity.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final ProblemSolvesRepository problemSolvesRepository;
    private final WrongNotesRepository wrongNotesRepository;
    private final CertificateRepository certificateRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public ProblemCreateResponse createProblem(ProblemCreateRequest request) {
        Users user = securityUtils.getCurrentUser();
        if (user.getRole() == UserRole.USER) {
            throw new TeacherRoleRequiredException();
        }

        Certificates certificate = certificateRepository.findById(request.getCertificateId())
                .orElseThrow(() -> new app_programming_development.Class.exceptions.notFound.CertificateNotFoundException());

        Problems problem = Problems.builder()
                .certificates(certificate)
                .user(user)
                .content(request.getContent())
                .option1(request.getOption1())
                .option2(request.getOption2())
                .option3(request.getOption3())
                .option4(request.getOption4())
                .correctAnswer(request.getCorrectAnswer())
                .explanation(request.getExplanation())
                .build();

        problemRepository.save(problem);

        log.info("Problem created: problemId={}, creatorId={}", problem.getId(), user.getId());
        return ProblemCreateResponse.from(problem);
    }

    public List<ProblemListResponse> getProblems(Long certificateId) {
        return problemRepository.findByCertificates_IdOrderByCreatedAtDesc(certificateId)
                .stream()
                .map(ProblemListResponse::from)
                .toList();
    }

    @Transactional
    public ProblemCreateResponse updateProblem(Long problemId, ProblemUpdateRequest request) {
        Users user = securityUtils.getCurrentUser();
        if (user.getRole() == UserRole.USER) {
            throw new TeacherRoleRequiredException();
        }

        Problems problem = problemRepository.findById(problemId)
                .orElseThrow(ProblemNotFoundException::new);

        problem.setContent(request.getContent());
        problem.setOption1(request.getOption1());
        problem.setOption2(request.getOption2());
        problem.setOption3(request.getOption3());
        problem.setOption4(request.getOption4());
        problem.setCorrectAnswer(request.getCorrectAnswer());
        problem.setExplanation(request.getExplanation());

        log.info("Problem updated: problemId={}, updatedBy={}", problemId, user.getId());
        return ProblemCreateResponse.from(problem);
    }

    @Transactional
    public void deleteProblem(Long problemId) {
        Users user = securityUtils.getCurrentUser();
        if (user.getRole() == UserRole.USER) {
            throw new TeacherRoleRequiredException();
        }

        Problems problem = problemRepository.findById(problemId)
                .orElseThrow(ProblemNotFoundException::new);

        problemRepository.delete(problem);
        log.info("Problem deleted: problemId={}, deletedBy={}", problemId, user.getId());
    }

    @Transactional
    public ProblemSolveResponse solveProblem(Long problemId, ProblemSolveRequest request) {
        Users user = securityUtils.getCurrentUser();

        Problems problem = problemRepository.findById(problemId)
                .orElseThrow(ProblemNotFoundException::new);

        boolean correct = problem.getCorrectAnswer() == request.getSelectedAnswer();

        ProblemSolves solve = ProblemSolves.builder()
                .user(user)
                .problems(problem)
                .selectedAnswer(request.getSelectedAnswer())
                .isCorrect(correct)
                .build();
        problemSolvesRepository.save(solve);

        if (!correct) {
            WrongNotes existing = wrongNotesRepository.findByUser_IdAndProblems_Id(user.getId(), problemId)
                    .orElse(null);
            if (existing != null) {
                existing.setSelectedAnswer(request.getSelectedAnswer());
                wrongNotesRepository.save(existing);
            } else {
                wrongNotesRepository.save(WrongNotes.builder()
                        .user(user)
                        .problems(problem)
                        .selectedAnswer(request.getSelectedAnswer())
                        .build());
            }
        }

        log.info("Problem solved: problemId={}, userId={}, correct={}", problemId, user.getId(), correct);
        return ProblemSolveResponse.builder()
                .correct(correct)
                .explanation(problem.getExplanation())
                .build();
    }
}
