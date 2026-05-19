package org.example.backend.Quiz.Services;

import lombok.RequiredArgsConstructor;
import org.example.backend.Leaderboard.LeaderboardService;
import org.example.backend.OpenAI.Service.OpenAIService;
import org.example.backend.Question.AnswerOption;
import org.example.backend.Question.Question;
import org.example.backend.Question.QuestionType;
import org.example.backend.Quiz.AttemptResult;
import org.example.backend.Quiz.Quiz;
import org.example.backend.Quiz.QuizAttempt;
import org.example.backend.Quiz.Repositories.QuizAttemptRepo;
import org.example.backend.Quiz.Repositories.QuizRepo;
import org.example.backend.User.UserAnswer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class QuizAttemptService {

    private final QuizAttemptRepo quizAttemptRepo;
    private final QuizRepo quizRepo;
    private final OpenAIService openAIService;
    private final LeaderboardService leaderboardService;

    public QuizAttempt submitAttempt(QuizAttempt attempt) {
        Quiz quiz = quizRepo.findById(attempt.getQuizId())
                .orElseThrow(() -> new NoSuchElementException("Quiz not found"));

        if (attempt.getAnswers() == null) {
            attempt.setAnswers(List.of());
        }

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

        QuizAttempt savedAttempt = quizAttemptRepo.save(attempt);
        saveLeaderboardResult(savedAttempt);

        return savedAttempt;
    }

    public AttemptResult getAttemptResult(String attemptId, String currentUserId, boolean isAdmin) {
        QuizAttempt attempt = quizAttemptRepo.findById(attemptId)
                .orElseThrow(() -> new NoSuchElementException("Attempt not found"));

        if (!isAdmin && !currentUserId.equals(attempt.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        Quiz quiz = quizRepo.findById(attempt.getQuizId())
                .orElseThrow(() -> new NoSuchElementException("Quiz not found"));

        List<UserAnswer> answers = attempt.getAnswers() == null ? List.of() : attempt.getAnswers();
        List<AttemptResult.QuestionResult> questionResults = quiz.getQuestions().stream()
                .map(question -> toQuestionResult(question, answers))
                .toList();

        return new AttemptResult(
                attempt.getId(),
                attempt.getQuizId(),
                quiz.getTitle(),
                attempt.getScore(),
                attempt.getMaxScore(),
                attempt.getStartedAt(),
                attempt.getFinishedAt(),
                totalTimeSeconds(attempt, questionResults),
                questionResults
        );
    }



    private boolean isAnswerCorrect( Question question, UserAnswer userAnswer) {
        if (question.getType() == QuestionType.SINGLE_CHOICE) {
            if (question.getAnswerOptions() == null) {
                return false;
            }

            List<String> correctOptionIds = question.getAnswerOptions().stream()
                    .filter(AnswerOption::isCorrect)
                    .map(AnswerOption::getId)
                    .toList();

            Set<String> correctSet = new HashSet<>(correctOptionIds);
            Set<String> selectedSet = userAnswer.getSelectedOptionIds() == null
                    ? Set.of()
                    : new HashSet<>(userAnswer.getSelectedOptionIds());

            return correctSet.equals(selectedSet);
        }

        if (question.getType() == QuestionType.TEXT) {
            if (userAnswer.getTextAnswer() == null || userAnswer.getTextAnswer().isBlank()) {
                return false;
            }

            List<String> acceptedTextAnswers = question.getAcceptedTextAnswers() == null
                    ? List.of()
                    : question.getAcceptedTextAnswers();

            boolean exactMatch = acceptedTextAnswers.stream()
                    .anyMatch(answer -> answer.equalsIgnoreCase(userAnswer.getTextAnswer().trim()));

            if (acceptedTextAnswers.isEmpty()) {
                return false;
            }
            if (exactMatch) {
                return true;
            }

            return acceptedTextAnswers.stream()
                    .anyMatch(expectedAnswer ->
                            openAIService.isQuizTextAnswerCorrect(
                                    question.getText(),
                                    expectedAnswer,
                                    userAnswer.getTextAnswer()
                            )
                    );
        }
        return false;
    }

    private AttemptResult.QuestionResult toQuestionResult(Question question, List<UserAnswer> answers) {
        UserAnswer userAnswer = answers.stream()
                .filter(answer -> answer.getQuestionId().equals(question.getId()))
                .findFirst()
                .orElse(null);

        List<AttemptResult.AnswerResult> correctOptions = correctOptions(question);
        List<AttemptResult.AnswerResult> userSelectedOptions = userSelectedOptions(question, userAnswer);
        List<String> acceptedTextAnswers = question.getAcceptedTextAnswers() == null
                ? List.of()
                : question.getAcceptedTextAnswers();

        return new AttemptResult.QuestionResult(
                question.getId(),
                question.getText(),
                question.getType(),
                question.getPoints(),
                userAnswer != null && isAnswerCorrect(question, userAnswer),
                correctOptions,
                userSelectedOptions,
                acceptedTextAnswers,
                userAnswer == null ? null : userAnswer.getTextAnswer(),
                userAnswer == null || userAnswer.getTimeSpentSeconds() == null ? 0 : userAnswer.getTimeSpentSeconds()
        );
    }

    private List<AttemptResult.AnswerResult> correctOptions(Question question) {
        if (question.getAnswerOptions() == null) {
            return List.of();
        }

        return question.getAnswerOptions().stream()
                .filter(AnswerOption::isCorrect)
                .map(option -> new AttemptResult.AnswerResult(option.getId(), option.getText()))
                .toList();
    }

    private List<AttemptResult.AnswerResult> userSelectedOptions(Question question, UserAnswer userAnswer) {
        if (question.getAnswerOptions() == null || userAnswer == null || userAnswer.getSelectedOptionIds() == null) {
            return List.of();
        }

        Set<String> selectedOptionIds = new HashSet<>(userAnswer.getSelectedOptionIds());

        return question.getAnswerOptions().stream()
                .filter(option -> selectedOptionIds.contains(option.getId()))
                .map(option -> new AttemptResult.AnswerResult(option.getId(), option.getText()))
                .toList();
    }

    private long totalTimeSeconds(QuizAttempt attempt, List<AttemptResult.QuestionResult> questionResults) {
        if (attempt.getStartedAt() != null && attempt.getFinishedAt() != null) {
            return Math.max(0, Duration.between(attempt.getStartedAt(), attempt.getFinishedAt()).toSeconds());
        }

        return questionResults.stream()
                .mapToLong(AttemptResult.QuestionResult::timeSpentSeconds)
                .sum();
    }

    private void saveLeaderboardResult(QuizAttempt attempt) {
        if (attempt.getUserId() == null || attempt.getUserId().isBlank()) {
            return;
        }

        leaderboardService.saveResult(
                attempt.getUserId(),
                attempt.getQuizId(),
                attempt.getScore(),
                attemptTimeSeconds(attempt)
        );
    }

    private long attemptTimeSeconds(QuizAttempt attempt) {
        if (attempt.getStartedAt() != null && attempt.getFinishedAt() != null) {
            return Math.max(0, Duration.between(attempt.getStartedAt(), attempt.getFinishedAt()).toSeconds());
        }

        if (attempt.getAnswers() == null) {
            return 0;
        }

        return attempt.getAnswers().stream()
                .map(UserAnswer::getTimeSpentSeconds)
                .filter(seconds -> seconds != null && seconds > 0)
                .mapToLong(Integer::longValue)
                .sum();
    }
}
