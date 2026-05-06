package app_programming_development.Class.service;

import app_programming_development.Class.dto.request.AutoLoginRequest;
import app_programming_development.Class.dto.request.LoginRequest;
import app_programming_development.Class.dto.request.SignupRequest;
import app_programming_development.Class.dto.response.SignupResponse;
import app_programming_development.Class.dto.response.TokenResponse;
import app_programming_development.Class.entity.RefreshTokens;
import app_programming_development.Class.entity.Users;
import app_programming_development.Class.enums.UserRole;
import app_programming_development.Class.exceptions.conflict.UserAlreadyExistsException;
import app_programming_development.Class.exceptions.notFound.RefreshTokenNotFoundException;
import app_programming_development.Class.exceptions.notFound.UserNotFoundException;
import app_programming_development.Class.exceptions.unauthorized.PasswordMismatchException;
import app_programming_development.Class.global.TokenProvider;
import app_programming_development.Class.repository.RefreshTokenRepository;
import app_programming_development.Class.repository.UserRepository;
import app_programming_development.Class.security.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    // 로그인
    public TokenResponse login(LoginRequest request) {
        Users user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(UserNotFoundException::new);
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new PasswordMismatchException();
        }

        String accessToken = tokenProvider.createToken(authentication.getName());
        String refreshToken = tokenProvider.createRefreshToken(authentication.getName());

        // 기존 token 삭제 + 새 token 저장
        refreshTokenRepository.deleteByUser(user);

        RefreshTokens refreshTokens = RefreshTokens.builder()
                .user(user)
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();
        refreshTokenRepository.save(refreshTokens);

        return new TokenResponse(accessToken, refreshToken);
    }

    // 회원가입
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException();
        }

        Users user = Users.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .profileUrl(request.getProfileImage())
                .role(UserRole.USER)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
        return SignupResponse.from(user);
    }

    public TokenResponse autoLogin(AutoLoginRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new RefreshTokenNotFoundException();
        }

        RefreshTokens existingToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(RefreshTokenNotFoundException::new);

        // 토큰으로 사용자 추출
        String email = tokenProvider.getEmail(refreshToken);

        Users user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        // 새로운 accessToken, RefreshToken 생성
        String newAccessToken = tokenProvider.createToken(user.getEmail());
        String newRefreshToken = tokenProvider.createRefreshToken(user.getEmail());

        // 기존 token 삭제 + 새 token 저장
        refreshTokenRepository.deleteByUser(user);

        RefreshTokens refreshTokens = RefreshTokens.builder()
                .user(user)
                .token(newRefreshToken)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();
        refreshTokenRepository.save(refreshTokens);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    public void logOut() {
        Users user = securityUtils.getCurrentUser();
        // 토큰 삭제
        refreshTokenRepository.deleteByUser(user);
    }


}
