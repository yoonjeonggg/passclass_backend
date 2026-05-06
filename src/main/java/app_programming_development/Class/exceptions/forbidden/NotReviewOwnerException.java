package app_programming_development.Class.exceptions.forbidden;

import app_programming_development.Class.exceptions.DomainException;
import org.springframework.http.HttpStatus;

public class NotReviewOwnerException extends DomainException {
    public NotReviewOwnerException() {
        super(HttpStatus.FORBIDDEN, "본인의 리뷰만 수정할 수 있습니다.");
    }
}
