package app_programming_development.Class.exceptions.conflict;

import app_programming_development.Class.exceptions.DomainException;
import org.springframework.http.HttpStatus;

public class AlreadyEnrolledException extends DomainException {
    public AlreadyEnrolledException() {
        super(HttpStatus.CONFLICT, "이미 수강된 강의입니다.");
    }
}
