package org.example.backend.Quiz.Services;

import lombok.RequiredArgsConstructor;
import org.example.backend.Question.AnswerOption;
import org.example.backend.Question.Question;
import org.example.backend.Quiz.Quiz;
import org.example.backend.Quiz.Repositories.QuizRepo;
import org.springframework.stereotype.Service;
import org.example.backend.Question.QuestionType;

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

        if (quiz.getQuestions() != null) {
            for (Question question : quiz.getQuestions()) {
                question.setId(UUID.randomUUID().toString());

                if (question.getAnswerOptions() != null) {
                    for (AnswerOption option : question.getAnswerOptions()) {
                        option.setId(UUID.randomUUID().toString());
                    }
                }
            }
        }


        return quizRepo.save(quiz);
    }

    public List<Quiz> getAllPublishedQuizzes() {
        return quizRepo.findByPublishedTrue();
    }

    public List<Quiz> getAllQuizzes() {
        return quizRepo.findAll();
    }

    public Quiz getPublishedQuizById(String id) {
        Quiz quiz = getQuizById(id);
        if (!quiz.isPublished()) {
            throw new NoSuchElementException("Quiz not found");
        }

        return quiz;
    }

    public Quiz getQuizById(String id) {
        return quizRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Quiz not found"));
    }

    public void deleteQuiz(String id) {
        quizRepo.deleteById(id);
    }


    public Quiz publishQuiz(String id) {
        Quiz quiz = getQuizById(id);

        validateQuizBeforePublishing(quiz);

        quiz.setPublished(true);
        return quizRepo.save(quiz);
    }

    public Quiz unpublishQuiz(String id) {
        Quiz quiz = getQuizById(id);

        quiz.setPublished(false);
        return quizRepo.save(quiz);
    }


    private void validateQuizBeforePublishing(Quiz quiz) {
        if (quiz.getTitle() == null || quiz.getTitle().isBlank()) {
            throw new IllegalArgumentException("Quiz title must not be empty");
        }

        if (quiz.getQuestions() == null || quiz.getQuestions().isEmpty()) {
            throw new IllegalArgumentException("Quiz must contain at least one question");
        }

        for (Question question : quiz.getQuestions()) {
            validateQuestionBeforePublishing(question);
        }
    }

    private void validateQuestionBeforePublishing(Question question) {
        if (question.getText() == null || question.getText().isBlank()) {
            throw new IllegalArgumentException("Question text must not be empty");
        }

        if (question.getPoints() <= 0) {
            throw new IllegalArgumentException("Question points must be greater than 0");
        }

        if (question.getType() == QuestionType.SINGLE_CHOICE) {
            validateSingleChoiceQuestion(question);
        }

        if (question.getType() == QuestionType.TEXT) {
            validateTextQuestion(question);
        }
    }

    private void validateSingleChoiceQuestion(Question question) {
        if (question.getAnswerOptions() == null || question.getAnswerOptions().isEmpty()) {
            throw new IllegalArgumentException("SINGLE_CHOICE question must contain answer options");
        }

        boolean hasCorrectAnswer = question.getAnswerOptions()
                .stream()
                .anyMatch(AnswerOption::isCorrect);

        if (!hasCorrectAnswer) {
            throw new IllegalArgumentException("SINGLE_CHOICE question must contain a correct answer");
        }
    }

    private void validateTextQuestion(Question question) {
        if (question.getAcceptedTextAnswers() == null || question.getAcceptedTextAnswers().isEmpty()) {
            throw new IllegalArgumentException("TEXT question must contain accepted text answers");
        }
    }

}
