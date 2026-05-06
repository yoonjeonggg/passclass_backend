package app_programming_development.Class.exceptions.badRequest;

import app_programming_development.Class.exceptions.DomainException;
import org.springframework.http.HttpStatus;

public class EmptyFileException extends DomainException {
    public EmptyFileException() {
        super(HttpStatus.BAD_REQUEST, "파일이 비어있습니다.");
    }
}
