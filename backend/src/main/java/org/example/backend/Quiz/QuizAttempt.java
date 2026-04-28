package org.example.backend.Quiz;

import org.example.backend.User.UserAnswer;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "quizAttempts")
public class QuizAttempt {
    @Id
    private String id;

    private String quizId;

    private String userId;

    private List<UserAnswer> answers;

    private int score;
    private int maxScore;

    private Instant startedAt;

    private Instant finishedAt;
}
