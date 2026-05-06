package app_programming_development.Class.exceptions.forbidden;

import app_programming_development.Class.exceptions.DomainException;
import org.springframework.http.HttpStatus;

public class NotNotificationOwnerException extends DomainException {
    public NotNotificationOwnerException() {
        super(HttpStatus.FORBIDDEN, "본인의 알림만 읽음 처리할 수 있습니다.");
    }
}
