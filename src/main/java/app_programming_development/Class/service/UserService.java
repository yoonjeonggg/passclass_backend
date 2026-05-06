package app_programming_development.Class.service;

import app_programming_development.Class.dto.request.PatchMyProfileRequest;
import app_programming_development.Class.dto.response.MyProfileResponse;
import app_programming_development.Class.dto.response.ProfileResponse;
import app_programming_development.Class.entity.Users;
import app_programming_development.Class.exceptions.notFound.UserNotFoundException;
import app_programming_development.Class.repository.UserRepository;
import app_programming_development.Class.security.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    // 프로필 조회
    public ProfileResponse getProfile(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        return ProfileResponse.from(user);
    }

    // 내 프로필 조회
    public MyProfileResponse getMyProfile() {
        Users user = securityUtils.getCurrentUser();
        return MyProfileResponse.from(user);
    }

    // 내 프로필 수정
    public MyProfileResponse patchMyProfile(PatchMyProfileRequest request) {
        Users user = securityUtils.getCurrentUser();
        user.setNickname(request.getNickname());
        user.setProfileUrl(request.getProfileImage());
        userRepository.save(user);
        return MyProfileResponse.from(user);
    }

}
