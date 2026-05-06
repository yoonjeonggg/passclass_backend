package app_programming_development.Class.security;

import app_programming_development.Class.entity.Users;
import app_programming_development.Class.exceptions.notFound.UserNotFoundException;
import app_programming_development.Class.exceptions.unauthorized.NotAuthenticatedException;
import app_programming_development.Class.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository usersRepository;

    public Users getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new NotAuthenticatedException();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof org.springframework.security.core.userdetails.User springUser) {
            return usersRepository.findByEmail(springUser.getUsername())
                    .orElseThrow(UserNotFoundException::new);
        } else {
            throw new NotAuthenticatedException();
        }
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
