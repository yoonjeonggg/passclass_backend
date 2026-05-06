package app_programming_development.Class.exceptions.badRequest;

import app_programming_development.Class.exceptions.DomainException;
import org.springframework.http.HttpStatus;

public class InvalidFileTypeException extends DomainException {
    public InvalidFileTypeException() {
        super(HttpStatus.BAD_REQUEST, "허용되지 않는 파일 형식입니다.");
    }
}
