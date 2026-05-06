package app_programming_development.Class.exceptions.notFound;

import app_programming_development.Class.exceptions.DomainException;
import org.springframework.http.HttpStatus;

public class CertificateNotFoundException extends DomainException {
    public CertificateNotFoundException() {
        super(HttpStatus.NOT_FOUND, "해당 자격증을 찾을 수 없습니다.");
    }
}
