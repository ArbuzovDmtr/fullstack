package org.example.backend.Security;

import lombok.RequiredArgsConstructor;
import org.example.backend.User.User;
import org.example.backend.User.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/me")
    public User getCurrentUser() {
        return userService.getCurrentUser();
    }
}