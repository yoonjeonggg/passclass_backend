package app_programming_development.Class.exceptions.forbidden;

import app_programming_development.Class.exceptions.DomainException;
import org.springframework.http.HttpStatus;

public class NotEnrolledException extends DomainException {
    public NotEnrolledException() {
        super(HttpStatus.FORBIDDEN, "수강 신청 후 이용 가능합니다.");
    }
}
