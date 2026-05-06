package app_programming_development.Class.exceptions.notFound;

import app_programming_development.Class.exceptions.DomainException;
import org.springframework.http.HttpStatus;

public class ChapterNotFoundException extends DomainException {
    public ChapterNotFoundException() {
        super(HttpStatus.NOT_FOUND, "해당 강의 챕터를 찾을 수 없습니다.");
    }
}
