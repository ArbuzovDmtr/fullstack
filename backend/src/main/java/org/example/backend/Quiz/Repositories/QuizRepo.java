package org.example.backend.Quiz.Repositories;

import org.example.backend.Quiz.Quiz;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface QuizRepo extends MongoRepository<Quiz, String> {
    List<Quiz> findByPublishedTrue();

    List<Quiz> findByCreatedByUserId(String userId);
}
