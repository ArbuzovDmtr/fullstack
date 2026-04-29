package org.example.backend.Quiz.Services;

import lombok.RequiredArgsConstructor;
import org.example.backend.Question.AnswerOption;
import org.example.backend.Question.Question;
import org.example.backend.Quiz.Quiz;
import org.example.backend.Quiz.Repositories.QuizRepo;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepo quizRepo;

    public Quiz createQuiz(Quiz quiz) {
        quiz.setCreatedAt(Instant.now());

        for (Question question : quiz.getQuestions()) {
            question.setId(UUID.randomUUID().toString());

            if (question.getAnswerOptions() != null) {
                for (AnswerOption option : question.getAnswerOptions()) {
                    option.setId(UUID.randomUUID().toString());
                }
            }
        }

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