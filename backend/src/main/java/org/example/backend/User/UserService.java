package org.example.backend.User;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepo;

    public User createUser(String provider, String providerId, String email, String name, String avatarUrl) {
        if (userRepo.findByProviderAndProviderId(provider, providerId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
        }

        User user = User.builder()
                .provider(provider)
                .providerId(providerId)
                .email(email)
                .name(name)
                .avatarUrl(avatarUrl)
                .roles(Set.of(Role.USER))
                .build();

        return userRepo.save(user);
    }

    public User findUser(String provider, String providerId) {
        return userRepo.findByProviderAndProviderId(provider, providerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public List<User> findAllUsers() {
        return userRepo.findAll();
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication.getPrincipal() instanceof OAuth2User oauth2User)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }

        String provider = "github";
        String providerId = oauth2User.getAttribute("id").toString();

        return findUser(provider, providerId);
    }
}