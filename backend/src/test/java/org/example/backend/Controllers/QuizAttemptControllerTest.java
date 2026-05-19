package org.example.backend.Controllers;

import org.example.backend.Quiz.AttemptResult;
import org.example.backend.Quiz.QuizAttempt;
import org.example.backend.Quiz.Services.QuizAttemptService;
import org.example.backend.User.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuizAttemptController.class)
@ImportAutoConfiguration(exclude = {
        OAuth2ClientAutoConfiguration.class,
        OAuth2ClientWebSecurityAutoConfiguration.class
})
class QuizAttemptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuizAttemptService quizAttemptService;

    @MockitoBean
    private UserService userService;

    @Test
    void submitAttempt_shouldReturnResult() throws Exception {
        QuizAttempt result = new QuizAttempt();
        result.setScore(10);
        result.setMaxScore(10);

        when(quizAttemptService.submitAttempt(any()))
                .thenReturn(result);

        mockMvc.perform(post("/api/attempts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "quizId": "1",
                  "answers": []
                }
            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(10))
                .andExpect(jsonPath("$.maxScore").value(10));
    }

    @Test
    void getAttemptResult_shouldReturnDetailedResult() throws Exception {
        AttemptResult result = new AttemptResult(
                "attempt-1",
                "quiz-1",
                "Quiz title",
                10,
                10,
                null,
                null,
                12,
                List.of()
        );

        when(userService.findCurrentUser()).thenReturn(Optional.empty());
        when(quizAttemptService.getAttemptResult(eq("attempt-1"), eq(null), eq(false)))
                .thenReturn(result);

        mockMvc.perform(get("/api/attempts/attempt-1/result"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attemptId").value("attempt-1"))
                .andExpect(jsonPath("$.quizTitle").value("Quiz title"))
                .andExpect(jsonPath("$.totalTimeSeconds").value(12));
    }
}
