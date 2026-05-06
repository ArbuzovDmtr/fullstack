package org.example.backend.Quiz;

import org.example.backend.Question.QuestionType;

import java.time.Instant;
import java.util.List;

public record AttemptResult(
        String attemptId,
        String quizId,
        String quizTitle,
        int score,
        int maxScore,
        Instant startedAt,
        Instant finishedAt,
        long totalTimeSeconds,
        List<QuestionResult> questions
) {

    public record QuestionResult(
            String questionId,
            String questionText,
            QuestionType type,
            int points,
            boolean correct,
            List<AnswerResult> correctOptions,
            List<AnswerResult> userSelectedOptions,
            List<String> acceptedTextAnswers,
            String userTextAnswer,
            int timeSpentSeconds
    ) {
    }

    public record AnswerResult(
            String id,
            String text
    ) {
    }
}
