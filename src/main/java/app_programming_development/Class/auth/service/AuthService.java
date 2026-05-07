package app_programming_development.Class.auth.service;

import app_programming_development.Class.auth.entity.RefreshTokens;
import app_programming_development.Class.auth.repository.RefreshTokenRepository;
import app_programming_development.Class.dto.auth.request.AutoLoginRequest;
import app_programming_development.Class.dto.auth.request.LoginRequest;
import app_programming_development.Class.dto.auth.request.SignupRequest;
import app_programming_development.Class.dto.auth.response.SignupResponse;
import app_programming_development.Class.dto.auth.response.TokenResponse;
import app_programming_development.Class.enums.UserRole;
import app_programming_development.Class.exceptions.conflict.UserAlreadyExistsException;
import app_programming_development.Class.exceptions.notFound.RefreshTokenNotFoundException;
import app_programming_development.Class.exceptions.notFound.UserNotFoundException;
import app_programming_development.Class.exceptions.unauthorized.PasswordMismatchException;
import app_programming_development.Class.global.TokenProvider;
import app_programming_development.Class.security.SecurityUtils;
import app_programming_development.Class.user.entity.Users;
import app_programming_development.Class.user.repository.UserRepository;
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

        refreshTokenRepository.deleteByUser(user);

        RefreshTokens refreshTokens = RefreshTokens.builder()
                .user(user)
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();
        refreshTokenRepository.save(refreshTokens);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

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

        String email = tokenProvider.getEmail(refreshToken);

        Users user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        String newAccessToken = tokenProvider.createToken(user.getEmail());
        String newRefreshToken = tokenProvider.createRefreshToken(user.getEmail());

        refreshTokenRepository.deleteByUser(user);

        RefreshTokens refreshTokens = RefreshTokens.builder()
                .user(user)
                .token(newRefreshToken)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();
        refreshTokenRepository.save(refreshTokens);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    public void logOut() {
        Users user = securityUtils.getCurrentUser();
        refreshTokenRepository.deleteByUser(user);
    }
}
