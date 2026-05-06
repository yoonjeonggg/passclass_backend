package app_programming_development.Class.exceptions.forbidden;

import app_programming_development.Class.exceptions.DomainException;
import org.springframework.http.HttpStatus;

public class AdminRoleRequiredException extends DomainException {
    public AdminRoleRequiredException() {
        super(HttpStatus.FORBIDDEN, "관리자만 가능한 권한입니다.");
    }
}
