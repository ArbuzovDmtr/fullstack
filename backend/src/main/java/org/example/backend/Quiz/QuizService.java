package org.example.backend.Quiz;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepo quizRepo;

    public Quiz createQuiz(Quiz quiz) {
        return quizRepo.save(quiz);
    }

    public List<Quiz> getAllPublishedQuizzes() {
        return quizRepo.findByPublishedTrue();
    }

    public Quiz getQuizById(String id) {
        return quizRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Quiz not found"));
    }

    public void deleteQuiz(String id) {
        quizRepo.deleteById(id);
    }
}