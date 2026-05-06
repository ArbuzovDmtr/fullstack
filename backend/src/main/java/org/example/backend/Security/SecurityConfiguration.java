package org.example.backend.Security;

import lombok.RequiredArgsConstructor;
import org.example.backend.User.Role;
import org.example.backend.User.User;
import org.example.backend.User.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

import java.util.HashSet;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final UserService userService;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/assets/**").permitAll()
                        .requestMatchers("/api/me").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oauth2UserService())
                        )
                        .defaultSuccessUrl("http://localhost:5173/", true)
                )
                .logout(logout -> logout.logoutSuccessUrl("http://localhost:5173/"))
                .build();
    }

    @Bean
    OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        return request -> {
            OAuth2User oauth2User =
                    new DefaultOAuth2UserService().loadUser(request);

            String provider = request.getClientRegistration().getRegistrationId();
            User user = userService.findOrCreateOAuthUser(provider, oauth2User);

            Set<GrantedAuthority> authorities = new HashSet<>(oauth2User.getAuthorities());
            for (Role role : user.getRoles()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
            }

            String userNameAttributeName = request.getClientRegistration()
                    .getProviderDetails()
                    .getUserInfoEndpoint()
                    .getUserNameAttributeName();

            return new DefaultOAuth2User(
                    authorities,
                    oauth2User.getAttributes(),
                    userNameAttributeName
            );
        };
    }
}
