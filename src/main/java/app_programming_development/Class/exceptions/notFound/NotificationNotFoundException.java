package app_programming_development.Class.exceptions.notFound;

import app_programming_development.Class.exceptions.DomainException;
import org.springframework.http.HttpStatus;

public class NotificationNotFoundException extends DomainException {
    public NotificationNotFoundException() {
        super(HttpStatus.NOT_FOUND, "해당 알림을 찾을 수 없습니다.");
    }
}
