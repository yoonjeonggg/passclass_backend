package app_programming_development.Class.exceptions.unauthorized;

import app_programming_development.Class.exceptions.DomainException;
import org.springframework.http.HttpStatus;

public class PasswordMismatchException extends DomainException {
    public PasswordMismatchException() {
        super(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
    }
}
