package org.example.backend.Leaderboard;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("leaderboard")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardEntry {

    @Id
    private String id;

    private String userId;
    private String quizId;

    private int score;
    private long timeSpentSeconds;
}