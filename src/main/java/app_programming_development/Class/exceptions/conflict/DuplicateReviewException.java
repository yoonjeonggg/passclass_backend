package app_programming_development.Class.exceptions.conflict;

import app_programming_development.Class.exceptions.DomainException;
import org.springframework.http.HttpStatus;

public class DuplicateReviewException extends DomainException {
    public DuplicateReviewException() {
        super(HttpStatus.CONFLICT, "이미 리뷰를 작성한 강의입니다.");
    }
}
