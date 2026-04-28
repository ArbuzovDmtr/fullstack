package org.example.backend.Quiz;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface QuizRepo extends MongoRepository<Quiz, String> {
    List<Quiz> findByPublishedTrue();

    List<Quiz> findByCreatedByUserId(String userId);
}
