package app_programming_development.Class.problem.repository;

import app_programming_development.Class.problem.entity.WrongNotes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WrongNotesRepository extends JpaRepository<WrongNotes, Long> {
    List<WrongNotes> findByUser_IdOrderByCreatedAtDesc(Long userId);
    Optional<WrongNotes> findByUser_IdAndProblems_Id(Long userId, Long problemId);
}
