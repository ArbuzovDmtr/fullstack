package org.example.backend.Quiz.Services;

import org.example.backend.Question.AnswerOption;
import org.example.backend.Question.Question;
import org.example.backend.Quiz.Quiz;
import org.example.backend.Quiz.Repositories.QuizRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock
    private QuizRepo quizRepo;

    @InjectMocks
    private QuizService quizService;
    @Test
    void createQuiz_shouldSetIdsForQuestionsAndAnswerOptions(){
        Quiz quiz = new Quiz();

        Question question = new Question();
        AnswerOption option1 = new AnswerOption();
        option1.setText("Yes");
        option1.setCorrect(true);

        AnswerOption option2 = new AnswerOption();
        option2.setText("No");
        option2.setCorrect(false);

        question.setAnswerOptions(List.of(option1, option2));

        quiz.setQuestions(List.of(question));

        when(quizRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Quiz savedQuiz = quizService.createQuiz(quiz);

        assertNotNull(savedQuiz.getQuestions().getFirst().getId());

        assertNotNull(
                savedQuiz.getQuestions()
                        .getFirst()
                        .getAnswerOptions()
                        .getFirst()
                        .getId()
        );
    }
    @Test
    void createQuiz_setsCreatedAt() {
        Quiz quiz = new Quiz();

        AnswerOption option1 = new AnswerOption();
        option1.setText("Yes");
        option1.setCorrect(true);

        AnswerOption option2 = new AnswerOption();
        option2.setText("No");
        option2.setCorrect(false);

        Question question = new Question();
        question.setAnswerOptions(List.of(option1, option2));

        quiz.setQuestions(List.of(question));

        when(quizRepo.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Instant before = Instant.now();
        Quiz savedQuiz = quizService.createQuiz(quiz);
        Instant after = Instant.now();

        assertThat(savedQuiz.getCreatedAt())
                .isBetween(before, after);
    }
    @Test
    void createQuiz_returnsRepoResult() {
        Quiz quiz = new Quiz();

        AnswerOption option1 = new AnswerOption();
        option1.setText("Yes");
        option1.setCorrect(true);

        AnswerOption option2 = new AnswerOption();
        option2.setText("No");
        option2.setCorrect(false);

        Question question = new Question();
        question.setAnswerOptions(List.of(option1, option2));
        quiz.setQuestions(List.of(question));


        when(quizRepo.save(any())).thenReturn(quiz);

        Quiz savedQuiz = quizService.createQuiz(quiz);

        assertThat(savedQuiz).isSameAs(quiz);
    }

    @Test
    void publishQuiz_shouldRejectQuizWithNoQuestions() {
        Quiz quiz = new Quiz();
        quiz.setId("1");
        quiz.setTitle("Empty quiz");
        quiz.setQuestions(List.of());

        when(quizRepo.findById("1")).thenReturn(Optional.of(quiz));

        assertThatThrownBy(() -> quizService.publishQuiz("1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quiz must contain at least one question");
    }

    @Test
    void publishQuiz_shouldRejectQuizWithNullQuestions() {
        Quiz quiz = new Quiz();
        quiz.setId("1");
        quiz.setTitle("Empty quiz");
        quiz.setQuestions(null);

        when(quizRepo.findById("1")).thenReturn(Optional.of(quiz));

        assertThatThrownBy(() -> quizService.publishQuiz("1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quiz must contain at least one question");
    }
}
