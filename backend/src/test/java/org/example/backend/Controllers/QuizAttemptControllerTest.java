package org.example.backend.Controllers;

import org.example.backend.Quiz.QuizAttempt;
import org.example.backend.Quiz.Services.QuizAttemptService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuizAttemptController.class)
class QuizAttemptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuizAttemptService quizAttemptService;

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
}