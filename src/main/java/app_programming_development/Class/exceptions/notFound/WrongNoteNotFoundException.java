package app_programming_development.Class.exceptions.notFound;

import app_programming_development.Class.exceptions.DomainException;
import org.springframework.http.HttpStatus;

public class WrongNoteNotFoundException extends DomainException {
    public WrongNoteNotFoundException() {
        super(HttpStatus.NOT_FOUND, "해당 오답노트를 찾을 수 없습니다.");
    }
}
