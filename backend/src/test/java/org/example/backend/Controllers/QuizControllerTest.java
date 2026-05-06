package org.example.backend.Controllers;

import org.example.backend.Quiz.Quiz;
import org.example.backend.Quiz.Services.QuizService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuizController.class)
@ImportAutoConfiguration(exclude = {
        OAuth2ClientAutoConfiguration.class,
        OAuth2ClientWebSecurityAutoConfiguration.class
})
class QuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuizService quizService;

    @Test
    void getAllPublishedQuizzes_shouldReturnList() throws Exception {
        Quiz quiz = new Quiz();
        quiz.setId("1");

        when(quizService.getAllPublishedQuizzes())
                .thenReturn(List.of(quiz));

        mockMvc.perform(get("/api/quizzes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"));
    }
    @Test
    void getPublishedQuizById_shouldReturnQuiz() throws Exception {
        Quiz quiz = new Quiz();
        quiz.setId("1");

        when(quizService.getPublishedQuizById("1")).thenReturn(quiz);

        mockMvc.perform(get("/api/quizzes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));
    }

    @Test
    void getPublishedQuizById_shouldReturnNotFound_whenQuizDoesNotExist() throws Exception {
        when(quizService.getPublishedQuizById("404"))
                .thenThrow(new NoSuchElementException("Quiz not found"));

        mockMvc.perform(get("/api/quizzes/404"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Quiz not found"));
    }
}
