package app_programming_development.Class.exceptions.unauthorized;

import app_programming_development.Class.exceptions.DomainException;
import org.springframework.http.HttpStatus;

public class NotAuthenticatedException extends DomainException {
    public NotAuthenticatedException() {
        super(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
    }
}
