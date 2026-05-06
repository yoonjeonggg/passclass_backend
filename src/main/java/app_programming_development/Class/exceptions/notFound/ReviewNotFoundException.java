package app_programming_development.Class.exceptions.notFound;

import app_programming_development.Class.exceptions.DomainException;
import org.springframework.http.HttpStatus;

public class ReviewNotFoundException extends DomainException {
    public ReviewNotFoundException() {
        super(HttpStatus.NOT_FOUND, "해당 리뷰를 찾을 수 없습니다.");
    }
}
