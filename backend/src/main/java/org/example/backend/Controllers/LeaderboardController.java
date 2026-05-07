package org.example.backend.Controllers;

import lombok.RequiredArgsConstructor;
import org.example.backend.Leaderboard.LeaderboardEntry;
import org.example.backend.Leaderboard.LeaderboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping("/{quizId}")
    public List<LeaderboardEntry> getLeaderboard(@PathVariable String quizId) {
        return leaderboardService.getLeaderboardByQuizId(quizId);
    }
}