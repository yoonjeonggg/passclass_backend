package app_programming_development.Class.exceptions.forbidden;

import app_programming_development.Class.exceptions.DomainException;
import org.springframework.http.HttpStatus;

public class NotWrongNoteOwnerException extends DomainException {
    public NotWrongNoteOwnerException() {
        super(HttpStatus.FORBIDDEN, "본인의 오답노트만 삭제할 수 있습니다.");
    }
}
