package app_programming_development.Class.exceptions.conflict;

import app_programming_development.Class.exceptions.DomainException;
import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends DomainException {
    public UserAlreadyExistsException() {
        super(HttpStatus.CONFLICT, "이미 존재하는 사용자입니다.");
    }
}
