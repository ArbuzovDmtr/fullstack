package org.example.backend.Controllers;

import org.example.backend.Quiz.Quiz;
import org.example.backend.Quiz.Services.QuizService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuizController.class)
class QuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuizService quizService;

    @Test
    void createQuiz_shouldReturnSavedQuiz() throws Exception {
        Quiz quiz = new Quiz();
        quiz.setId("1");
        quiz.setTitle("Test quiz");
        quiz.setDescription("Test description");
        quiz.setPublished(true);
        quiz.setQuestions(List.of());

        when(quizService.createQuiz(any())).thenReturn(quiz);

        mockMvc.perform(post("/api/quizzes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "title": "Test quiz",
                      "description": "Test description",
                      "published": true,
                      "questions": []
                    }
                """
                ))
                .andExpect(jsonPath("$.title").value("Test quiz"))
                .andExpect(jsonPath("$.published").value(true))
                .andExpect(status().isOk());
    }
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
    void getQuizById_shouldReturnQuiz() throws Exception {
        Quiz quiz = new Quiz();
        quiz.setId("1");

        when(quizService.getQuizById("1")).thenReturn(quiz);

        mockMvc.perform(get("/api/quizzes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));
    }

    @Test
    void deleteQuiz_shouldCallService() throws Exception {
        mockMvc.perform(delete("/api/quizzes/1"))
                .andExpect(status().isOk());

        verify(quizService).deleteQuiz("1");
    }
}