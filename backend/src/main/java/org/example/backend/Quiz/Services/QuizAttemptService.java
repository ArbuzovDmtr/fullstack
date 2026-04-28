package org.example.backend.Quiz.Services;

import lombok.RequiredArgsConstructor;
import org.example.backend.Question.AnswerOption;
import org.example.backend.Question.Question;
import org.example.backend.Question.QuestionType;
import org.example.backend.Quiz.Quiz;
import org.example.backend.Quiz.QuizAttempt;
import org.example.backend.Quiz.Repositories.QuizAttemptRepo;
import org.example.backend.Quiz.Repositories.QuizRepo;
import org.example.backend.User.UserAnswer;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class QuizAttemptService {

    private final QuizAttemptRepo quizAttemptRepo;
    private final QuizRepo quizRepo;

    public QuizAttempt submitAttempt(QuizAttempt attempt) {
        Quiz quiz = quizRepo.findById(attempt.getQuizId())
                .orElseThrow(() -> new NoSuchElementException("Quiz not found"));


        int score = 0;
        int maxScore = 0;

        for (Question question : quiz.getQuestions()) {
            maxScore += question.getPoints();

            UserAnswer userAnswer = attempt.getAnswers().stream()
                    .filter(answer -> answer.getQuestionId().equals(question.getId()))
                    .findFirst()
                    .orElse(null);

            if (userAnswer == null) {
                continue;
            }

            if (isAnswerCorrect(question, userAnswer)) {
                score += question.getPoints();
            }
        }

        attempt.setScore(score);
        attempt.setMaxScore(maxScore);
        attempt.setFinishedAt(Instant.now());

        return quizAttemptRepo.save(attempt);
    }

    private boolean isAnswerCorrect( Question question, UserAnswer userAnswer) {
        if (question.getType() == QuestionType.SINGLE_CHOICE) {
            List<String> correctOptionIds = question.getAnswerOptions().stream()
                    .filter(AnswerOption::isCorrect)
                    .map(AnswerOption::getId)
                    .toList();

            return correctOptionIds.equals(userAnswer.getSelectedOptionIds());
        }

        if (question.getType() == QuestionType.TEXT) {
            return question.getAcceptedTextAnswers().stream()
                    .anyMatch(answer -> answer.equalsIgnoreCase(userAnswer.getTextAnswer()));
        }

        return false;
    }
}
