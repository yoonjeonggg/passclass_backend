package app_programming_development.Class.exceptions.badRequest;

import app_programming_development.Class.exceptions.DomainException;
import org.springframework.http.HttpStatus;

public class InvalidFileExtensionException extends DomainException {
    public InvalidFileExtensionException() {
        super(HttpStatus.BAD_REQUEST, "파일 확장자를 확인할 수 없습니다.");
    }
}
