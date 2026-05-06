package app_programming_development.Class.exceptions.notFound;

import app_programming_development.Class.exceptions.DomainException;
import org.springframework.http.HttpStatus;

public class LectureNotFoundException extends DomainException {
    public LectureNotFoundException() {
        super(HttpStatus.NOT_FOUND, "해당 강의를 찾을 수 없습니다.");
    }
}
