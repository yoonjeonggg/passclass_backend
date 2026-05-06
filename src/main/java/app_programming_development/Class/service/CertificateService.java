package app_programming_development.Class.service;

import app_programming_development.Class.dto.request.CertificateRequest;
import app_programming_development.Class.dto.response.CertificateResponse;
import app_programming_development.Class.entity.Certificates;
import app_programming_development.Class.entity.Users;
import app_programming_development.Class.enums.UserRole;
import app_programming_development.Class.exceptions.forbidden.AdminRoleRequiredException;
import app_programming_development.Class.exceptions.notFound.CertificateNotFoundException;
import app_programming_development.Class.repository.CertificateRepository;
import app_programming_development.Class.security.SecurityUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final SecurityUtils securityUtils;

    // 자격증 등록
    @Transactional
    public CertificateResponse createCertificate(CertificateRequest request) {
        Users currentUser = securityUtils.getCurrentUser();

        if (!currentUser.getRole().equals(UserRole.ADMIN)) {
            throw new AdminRoleRequiredException();
        }

        Certificates certificate = Certificates.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        certificateRepository.save(certificate);

        return CertificateResponse.from(certificate);
    }

    // 자격증 목록 조회
    @Transactional(readOnly = true)
    public List<CertificateResponse> getCertificates() {
        return certificateRepository.findAll()
                .stream()
                .map(CertificateResponse::from)
                .collect(Collectors.toList());
    }

    // 자격증 검색
    @Transactional(readOnly = true)
    public List<CertificateResponse> searchCertificates(String keyword) {
        return certificateRepository.findByNameContainingIgnoreCase(keyword)
                .stream()
                .map(CertificateResponse::from)
                .collect(Collectors.toList());
    }

    // 자격증 수정
    @Transactional
    public CertificateResponse updateCertificate(Long id, CertificateRequest request) {
        Users currentUser = securityUtils.getCurrentUser();

        if (!currentUser.getRole().equals(UserRole.ADMIN)) {
            throw new AdminRoleRequiredException();
        }

        Certificates certificate = certificateRepository.findById(id)
                .orElseThrow(CertificateNotFoundException::new);

        certificate.setName(request.getName());
        certificate.setDescription(request.getDescription());

        return CertificateResponse.from(certificate);
    }

    // 자격증 삭제
    @Transactional
    public void deleteCertificate(Long id) {
        Users currentUser = securityUtils.getCurrentUser();

        if (!currentUser.getRole().equals(UserRole.ADMIN)) {
            throw new AdminRoleRequiredException();
        }

        Certificates certificate = certificateRepository.findById(id)
                .orElseThrow(CertificateNotFoundException::new);

        certificateRepository.delete(certificate);
    }
}
