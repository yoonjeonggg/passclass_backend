package app_programming_development.Class.exceptions.notFound;

import app_programming_development.Class.exceptions.DomainException;
import org.springframework.http.HttpStatus;

public class EnrollmentNotFoundException extends DomainException {
    public EnrollmentNotFoundException() {
        super(HttpStatus.NOT_FOUND, "해당 수강 내역을 찾을 수 없습니다.");
    }
}
