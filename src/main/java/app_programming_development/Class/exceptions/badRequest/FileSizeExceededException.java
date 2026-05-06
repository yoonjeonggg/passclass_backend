package app_programming_development.Class.exceptions.badRequest;

import app_programming_development.Class.exceptions.DomainException;
import org.springframework.http.HttpStatus;

public class FileSizeExceededException extends DomainException {
    public FileSizeExceededException() {
        super(HttpStatus.BAD_REQUEST, "파일 크기는 10MB를 초과할 수 없습니다.");
    }
}
