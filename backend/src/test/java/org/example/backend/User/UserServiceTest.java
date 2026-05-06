package org.example.backend.User;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepo userRepo;

    @Test
    void findOrCreateOAuthUser_assignsAdminRole_whenGithubIdMatchesConfiguredAdminId() {
        UserService userService = new UserService(userRepo, "12345");
        OAuth2User oauth2User = githubUser("12345");

        when(userRepo.findByProviderAndProviderId("github", "12345"))
                .thenReturn(Optional.empty());
        when(userRepo.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User user = userService.findOrCreateOAuthUser("github", oauth2User);

        assertThat(user.getRoles()).containsExactly(Role.ADMIN);
    }

    @Test
    void findOrCreateOAuthUser_assignsUserRole_whenGithubIdDoesNotMatchConfiguredAdminId() {
        UserService userService = new UserService(userRepo, "12345");
        OAuth2User oauth2User = githubUser("67890");

        when(userRepo.findByProviderAndProviderId("github", "67890"))
                .thenReturn(Optional.empty());
        when(userRepo.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User user = userService.findOrCreateOAuthUser("github", oauth2User);

        assertThat(user.getRoles()).containsExactly(Role.USER);
    }

    @Test
    void findOrCreateOAuthUser_refreshesRoleForExistingUserOnLogin() {
        UserService userService = new UserService(userRepo, "12345");
        OAuth2User oauth2User = githubUser("12345");
        User existingUser = User.builder()
                .provider("github")
                .providerId("12345")
                .roles(Set.of(Role.USER))
                .build();

        when(userRepo.findByProviderAndProviderId("github", "12345"))
                .thenReturn(Optional.of(existingUser));
        when(userRepo.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User user = userService.findOrCreateOAuthUser("github", oauth2User);

        assertThat(user.getRoles()).containsExactly(Role.ADMIN);
    }

    @Test
    void findOrCreateOAuthUser_savesRefreshedExistingUser() {
        UserService userService = new UserService(userRepo, "12345");
        OAuth2User oauth2User = githubUser("12345");
        User existingUser = User.builder()
                .provider("github")
                .providerId("12345")
                .roles(Set.of(Role.USER))
                .build();
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        when(userRepo.findByProviderAndProviderId("github", "12345"))
                .thenReturn(Optional.of(existingUser));
        when(userRepo.save(userCaptor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userService.findOrCreateOAuthUser("github", oauth2User);

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("user@example.com");
        assertThat(savedUser.getName()).isEqualTo("GitHub User");
        assertThat(savedUser.getAvatarUrl()).isEqualTo("https://example.com/avatar.png");
        assertThat(savedUser.getRoles()).containsExactly(Role.ADMIN);
    }

    private OAuth2User githubUser(String id) {
        return new DefaultOAuth2User(
                Set.of(new SimpleGrantedAuthority("OAUTH2_USER")),
                Map.of(
                        "id", id,
                        "email", "user@example.com",
                        "name", "GitHub User",
                        "avatar_url", "https://example.com/avatar.png"
                ),
                "id"
        );
    }
}
