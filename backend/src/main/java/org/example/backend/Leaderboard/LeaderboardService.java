package org.example.backend.Leaderboard;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final LeaderboardRepo leaderboardRepo;

    public LeaderboardEntry saveResult(
            String userId,
            String quizId,
            int score,
            long timeSpentSeconds
    ) {
        LeaderboardEntry entry = leaderboardRepo
                .findByQuizIdAndUserId(quizId, userId)
                .orElse(null);

        if (entry == null) {
            entry = LeaderboardEntry.builder()
                    .userId(userId)
                    .quizId(quizId)
                    .score(score)
                    .timeSpentSeconds(timeSpentSeconds)
                    .build();
        } else {
            boolean newResultIsBetter =
                    score > entry.getScore()
                            || score == entry.getScore()
                            && timeSpentSeconds < entry.getTimeSpentSeconds();

            if (!newResultIsBetter) {
                return entry;
            }

            entry.setScore(score);
            entry.setTimeSpentSeconds(timeSpentSeconds);
        }

        return leaderboardRepo.save(entry);
    }

    public List<LeaderboardEntry> getLeaderboardByQuizId(String quizId) {
        return leaderboardRepo.findByQuizIdOrderByScoreDescTimeSpentSecondsAsc(quizId);
    }
}