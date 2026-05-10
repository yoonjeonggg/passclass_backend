package app_programming_development.Class.dto.wrongnote.response;

import app_programming_development.Class.problem.entity.WrongNotes;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WrongNoteResponse {
    private Long wrongNoteId;
    private Long problemId;
    private String content;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private int selectedAnswer;
    private int correctAnswer;
    private String explanation;
    private String memo;

    public static WrongNoteResponse from(WrongNotes note) {
        return WrongNoteResponse.builder()
                .wrongNoteId(note.getId())
                .problemId(note.getProblems().getId())
                .content(note.getProblems().getContent())
                .option1(note.getProblems().getOption1())
                .option2(note.getProblems().getOption2())
                .option3(note.getProblems().getOption3())
                .option4(note.getProblems().getOption4())
                .selectedAnswer(note.getSelectedAnswer())
                .correctAnswer(note.getProblems().getCorrectAnswer())
                .explanation(note.getProblems().getExplanation())
                .memo(note.getMemo())
                .build();
    }
}
