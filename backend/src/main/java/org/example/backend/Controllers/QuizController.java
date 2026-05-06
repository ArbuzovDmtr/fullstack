package org.example.backend.Controllers;

import lombok.RequiredArgsConstructor;
import org.example.backend.Quiz.Quiz;
import org.example.backend.Quiz.Services.QuizService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @GetMapping
    public List<Quiz> getAllPublishedQuizzes() {
        return quizService.getAllPublishedQuizzes();
    }

    @GetMapping("/{id}")
    public Quiz getPublishedQuizById(@PathVariable String id) {
        return quizService.getPublishedQuizById(id);
    }
}
