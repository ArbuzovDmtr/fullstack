package org.example.backend.Leaderboard;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface LeaderboardRepo extends MongoRepository<LeaderboardEntry, String> {

    List<LeaderboardEntry> findByQuizIdOrderByScoreDescTimeSpentSecondsAsc(String quizId);

    Optional<LeaderboardEntry> findByQuizIdAndUserId(String quizId, String userId);
}