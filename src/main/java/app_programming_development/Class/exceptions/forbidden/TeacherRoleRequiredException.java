package app_programming_development.Class.exceptions.forbidden;

import app_programming_development.Class.exceptions.DomainException;
import org.springframework.http.HttpStatus;

public class TeacherRoleRequiredException extends DomainException {
    public TeacherRoleRequiredException() {
        super(HttpStatus.FORBIDDEN, "강사만 가능한 권한입니다.");
    }
}
