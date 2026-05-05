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

    @PostMapping
    public Quiz createQuiz(@RequestBody Quiz quiz) {
        return quizService.createQuiz(quiz);
    }

    @GetMapping
    public List<Quiz> getAllPublishedQuizzes() {
        return quizService.getAllPublishedQuizzes();
    }

    @GetMapping("/{id}")
    public Quiz getQuizById(@PathVariable String id) {
        return quizService.getQuizById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteQuiz(@PathVariable String id) {
        quizService.deleteQuiz(id);
    }

    @PatchMapping("/{id}/publish")
    public Quiz publishQuiz(@PathVariable String id) {
        return quizService.publishQuiz(id);
    }

    @PatchMapping("/{id}/unpublish")
    public Quiz unpublishQuiz(@PathVariable String id) {
        return quizService.unpublishQuiz(id);
    }
}