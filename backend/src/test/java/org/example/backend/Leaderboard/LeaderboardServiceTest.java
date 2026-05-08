package org.example.backend.Leaderboard;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

    @Mock
    private LeaderboardRepo leaderboardRepo;

    @InjectMocks
    private LeaderboardService leaderboardService;

    @Test
    void saveResult_shouldCreateEntryWhenUserHasNoResult() {
        when(leaderboardRepo.findByQuizIdAndUserId("quiz-1", "user-1"))
                .thenReturn(Optional.empty());
        when(leaderboardRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LeaderboardEntry result = leaderboardService.saveResult("user-1", "quiz-1", 10, 25);

        assertThat(result.getUserId()).isEqualTo("user-1");
        assertThat(result.getQuizId()).isEqualTo("quiz-1");
        assertThat(result.getScore()).isEqualTo(10);
        assertThat(result.getTimeSpentSeconds()).isEqualTo(25);
    }

    @Test
    void saveResult_shouldKeepExistingEntryWhenNewResultIsWorse() {
        LeaderboardEntry existing = LeaderboardEntry.builder()
                .userId("user-1")
                .quizId("quiz-1")
                .score(10)
                .timeSpentSeconds(25)
                .build();

        when(leaderboardRepo.findByQuizIdAndUserId("quiz-1", "user-1"))
                .thenReturn(Optional.of(existing));

        LeaderboardEntry result = leaderboardService.saveResult("user-1", "quiz-1", 8, 20);

        assertThat(result).isSameAs(existing);
        verify(leaderboardRepo, never()).save(any());
    }

    @Test
    void saveResult_shouldUpdateExistingEntryWhenNewResultIsBetter() {
        LeaderboardEntry existing = LeaderboardEntry.builder()
                .userId("user-1")
                .quizId("quiz-1")
                .score(10)
                .timeSpentSeconds(25)
                .build();

        when(leaderboardRepo.findByQuizIdAndUserId("quiz-1", "user-1"))
                .thenReturn(Optional.of(existing));
        when(leaderboardRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LeaderboardEntry result = leaderboardService.saveResult("user-1", "quiz-1", 10, 18);

        assertThat(result.getScore()).isEqualTo(10);
        assertThat(result.getTimeSpentSeconds()).isEqualTo(18);
    }
}
