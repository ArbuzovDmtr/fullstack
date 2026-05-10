package org.example.backend.Controllers;

import org.example.backend.Quiz.Quiz;
import org.example.backend.Quiz.Services.QuizService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminQuizController.class)
@ImportAutoConfiguration(exclude = {
        OAuth2ClientAutoConfiguration.class,
        OAuth2ClientWebSecurityAutoConfiguration.class
})
class AdminQuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuizService quizService;

    @Test
    void getAllQuizzes_shouldReturnList() throws Exception {
        Quiz quiz = new Quiz();
        quiz.setId("1");

        when(quizService.getAllQuizzes())
                .thenReturn(List.of(quiz));

        mockMvc.perform(get("/api/admin/quizzes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"));
    }

    @Test
    void createQuiz_shouldReturnSavedQuiz() throws Exception {
        Quiz quiz = new Quiz();
        quiz.setId("1");
        quiz.setTitle("Test quiz");
        quiz.setDescription("Test description");
        quiz.setPublished(true);
        quiz.setQuestions(List.of());

        when(quizService.createQuiz(any())).thenReturn(quiz);

        mockMvc.perform(post("/api/admin/quizzes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "title": "Test quiz",
                      "description": "Test description",
                      "published": true,
                      "questions": []
                    }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test quiz"))
                .andExpect(jsonPath("$.published").value(true));
    }

    @Test
    void getQuiz_shouldReturnQuiz() throws Exception {
        Quiz quiz = new Quiz();
        quiz.setId("1");

        when(quizService.getQuizById("1")).thenReturn(quiz);

        mockMvc.perform(get("/api/admin/quizzes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));
    }

    @Test
    void updateQuiz_shouldReturnUpdatedQuiz() throws Exception {
        Quiz quiz = new Quiz();
        quiz.setId("1");
        quiz.setTitle("Updated quiz");
        quiz.setPublished(false);

        when(quizService.updateQuiz(any(), any())).thenReturn(quiz);

        mockMvc.perform(put("/api/admin/quizzes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "title": "Updated quiz",
                      "description": "Updated description",
                      "published": false,
                      "questions": []
                    }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated quiz"))
                .andExpect(jsonPath("$.published").value(false));
    }

    @Test
    void deleteQuiz_shouldCallService() throws Exception {
        mockMvc.perform(delete("/api/admin/quizzes/1"))
                .andExpect(status().isOk());

        verify(quizService).deleteQuiz("1");
    }

    @Test
    void publishQuiz_shouldReturnBadRequest_whenQuizIsInvalid() throws Exception {
        when(quizService.publishQuiz("1"))
                .thenThrow(new IllegalArgumentException("Quiz title must not be empty"));

        mockMvc.perform(patch("/api/admin/quizzes/1/publish"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Quiz title must not be empty"));
    }

    @Test
    void unpublishQuiz_shouldReturnQuiz() throws Exception {
        Quiz quiz = new Quiz();
        quiz.setId("1");

        when(quizService.unpublishQuiz("1")).thenReturn(quiz);

        mockMvc.perform(patch("/api/admin/quizzes/1/unpublish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));
    }
}
