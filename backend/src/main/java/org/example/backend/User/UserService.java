package org.example.backend.User;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


import java.util.Set;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final String githubAdminId;

    public UserService(
            UserRepo userRepo,
            @Value("${app.github-admin-id:}") String githubAdminId
    ) {
        this.userRepo = userRepo;
        this.githubAdminId = githubAdminId == null ? "" : githubAdminId.trim();
    }

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
                .roles(rolesFor(provider, providerId))
                .build();

        return userRepo.save(user);
    }

    public User findUser(String provider, String providerId) {
        return userRepo.findByProviderAndProviderId(provider, providerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
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

    public User findOrCreateOAuthUser(String provider, OAuth2User oauth2User) {
        String providerId = oauth2User.getAttribute("id").toString();

        return userRepo.findByProviderAndProviderId(provider, providerId)
                .map(user -> updateOAuthUser(user, provider, providerId, oauth2User))
                .orElseGet(() -> createUser(
                        provider,
                        providerId,
                        oauth2User.getAttribute("email"),
                        oauth2User.getAttribute("name"),
                        oauth2User.getAttribute("avatar_url")
                ));
    }

    private User updateOAuthUser(User user, String provider, String providerId, OAuth2User oauth2User) {
        user.setEmail(oauth2User.getAttribute("email"));
        user.setName(oauth2User.getAttribute("name"));
        user.setAvatarUrl(oauth2User.getAttribute("avatar_url"));
        user.setRoles(rolesFor(provider, providerId));
        return userRepo.save(user);
    }

    private Set<Role> rolesFor(String provider, String providerId) {
        if ("github".equals(provider) && githubAdminId != null && githubAdminId.equals(providerId)) {
            return Set.of(Role.ADMIN);
        }

        return Set.of(Role.USER);
    }
}
