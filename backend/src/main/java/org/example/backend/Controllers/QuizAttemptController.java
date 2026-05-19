package org.example.backend.Controllers;


import lombok.RequiredArgsConstructor;
import org.example.backend.Quiz.AttemptResult;
import org.example.backend.Quiz.QuizAttempt;
import org.example.backend.Quiz.Services.QuizAttemptService;
import org.example.backend.User.Role;
import org.example.backend.User.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class QuizAttemptController {
    private final QuizAttemptService quizAttemptService;
    private final UserService userService;

    @PostMapping("/attempts")
    public QuizAttempt submitAttempt(@RequestBody QuizAttempt attempt) {
        userService.findCurrentUser()
                .ifPresent(user -> attempt.setUserId(user.getId()));
        return quizAttemptService.submitAttempt(attempt);
    }

    @GetMapping("/attempts/{attemptId}/result")
    public AttemptResult getAttemptResult(@PathVariable String attemptId) {
        return userService.findCurrentUser()
                .map(user -> quizAttemptService.getAttemptResult(
                        attemptId,
                        user.getId(),
                        user.getRoles().contains(Role.ADMIN)
                ))
                .orElseGet(() -> quizAttemptService.getAttemptResult(attemptId, null, false));
    }

}
