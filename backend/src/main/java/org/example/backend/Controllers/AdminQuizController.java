package org.example.backend.Controllers;

import lombok.RequiredArgsConstructor;
import org.example.backend.Quiz.Quiz;
import org.example.backend.Quiz.Services.QuizService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/quizzes")
@RequiredArgsConstructor
public class AdminQuizController {

    private final QuizService quizService;

    @GetMapping
    public List<Quiz> getAllQuizzes() {
        return quizService.getAllQuizzes();
    }

    @PostMapping
    public Quiz createQuiz(@RequestBody Quiz quiz) {
        return quizService.createQuiz(quiz);
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
