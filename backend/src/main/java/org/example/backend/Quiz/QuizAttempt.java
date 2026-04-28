package org.example.backend.Quiz;

import org.example.backend.User.UserAnswer;

import java.time.Instant;
import java.util.List;

public class QuizAttempt {
    private String id;

    private String quizId;

    private String userId;

    private List<UserAnswer> answers;

    private int score;

    private Instant startedAt;

    private Instant finishedAt;
}
