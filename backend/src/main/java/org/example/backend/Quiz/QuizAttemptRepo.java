package org.example.backend.Quiz;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface QuizAttemptRepo extends MongoRepository<QuizAttempt, String> {
    List<QuizAttempt> findByQuizId(String quizId);
    List<QuizAttempt> findByUserId(String userId);
}