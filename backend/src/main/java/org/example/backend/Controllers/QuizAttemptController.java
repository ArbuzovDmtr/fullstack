package org.example.backend.Controllers;


import lombok.RequiredArgsConstructor;
import org.example.backend.Quiz.QuizAttempt;
import org.example.backend.Quiz.Services.QuizAttemptService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class QuizAttemptController {
    private final QuizAttemptService quizAttemptService;


    @PostMapping("/attempts")
    public QuizAttempt submitAttempt(@RequestBody QuizAttempt attempt) {
        return quizAttemptService.submitAttempt(attempt);
    }

}
