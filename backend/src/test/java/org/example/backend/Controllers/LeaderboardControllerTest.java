package org.example.backend.Controllers;

import org.example.backend.Leaderboard.LeaderboardEntry;
import org.example.backend.Leaderboard.LeaderboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LeaderboardController.class)
@ImportAutoConfiguration(exclude = {
        OAuth2ClientAutoConfiguration.class,
        OAuth2ClientWebSecurityAutoConfiguration.class
})
class LeaderboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LeaderboardService leaderboardService;

    @Test
    void getLeaderboard_shouldReturnSortedEntriesFromService() throws Exception {
        LeaderboardEntry entry = LeaderboardEntry.builder()
                .userId("user-1")
                .quizId("quiz-1")
                .score(10)
                .timeSpentSeconds(25)
                .build();

        when(leaderboardService.getLeaderboardByQuizId("quiz-1"))
                .thenReturn(List.of(entry));

        mockMvc.perform(get("/api/leaderboard/quiz-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value("user-1"))
                .andExpect(jsonPath("$[0].score").value(10))
                .andExpect(jsonPath("$[0].timeSpentSeconds").value(25));
    }
}
