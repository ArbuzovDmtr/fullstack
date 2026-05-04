package org.example.backend.Quiz.Services;

import org.example.backend.Question.AnswerOption;
import org.example.backend.Question.Question;
import org.example.backend.Question.QuestionType;
import org.example.backend.Quiz.Quiz;
import org.example.backend.Quiz.QuizAttempt;
import org.example.backend.Quiz.Repositories.QuizAttemptRepo;
import org.example.backend.Quiz.Repositories.QuizRepo;
import org.example.backend.User.UserAnswer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizAttemptServiceTest {

    @Mock
    private QuizAttemptRepo quizAttemptRepo;

    @Mock
    private QuizRepo quizRepo;

    @InjectMocks
    private QuizAttemptService quizAttemptService;

    @Test
    void submitAttempt_shouldGivePointsForCorrectSingleChoiceAnswer() {



        AnswerOption option1 = new AnswerOption();
        option1.setId("1");
        option1.setCorrect(true);


        AnswerOption option2 = new AnswerOption();
        option2.setCorrect(false);


        Question question = new Question();
        question.setType(QuestionType.SINGLE_CHOICE);
        question.setId("1");
        question.setPoints(10);
        question.setAnswerOptions(List.of(option1, option2));


        Quiz quiz = new Quiz();
        quiz.setId("1");
        quiz.setQuestions(List.of(question));


        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setQuestionId("1");
        userAnswer.setSelectedOptionIds(List.of("1"));


        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuizId("1");
        attempt.setAnswers(List.of(userAnswer));

        when(quizRepo.findById("1")).thenReturn(Optional.of(quiz));
        when(quizAttemptRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));


        QuizAttempt result = quizAttemptService.submitAttempt(attempt);


        assertThat(result.getScore()).isEqualTo(10);
        assertThat(result.getMaxScore()).isEqualTo(10);
    }
    @Test
    void submitAttempt_shouldGiveZeroPointsForWrongSingleChoiceAnswer(){


        AnswerOption option1 = new AnswerOption();
        option1.setId("1");
        option1.setCorrect(true);


        AnswerOption option2 = new AnswerOption();
        option2.setId("2");
        option2.setCorrect(false);


        Question question = new Question();
        question.setType(QuestionType.SINGLE_CHOICE);
        question.setId("1");
        question.setPoints(10);
        question.setAnswerOptions(List.of(option1, option2));


        Quiz quiz = new Quiz();
        quiz.setId("1");
        quiz.setQuestions(List.of(question));


        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setQuestionId("1");
        userAnswer.setSelectedOptionIds(List.of("2"));


        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuizId("1");
        attempt.setAnswers(List.of(userAnswer));

        when(quizRepo.findById("1")).thenReturn(Optional.of(quiz));
        when(quizAttemptRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));


        QuizAttempt result = quizAttemptService.submitAttempt(attempt);


        assertThat(result.getScore()).isEqualTo(0);
        assertThat(result.getMaxScore()).isEqualTo(10);

    }

    @Test
    void submitAttempt_shouldCalculateMaxScore() {

        AnswerOption correctOption1 = new AnswerOption();
        correctOption1.setId("1");
        correctOption1.setCorrect(true);

        Question question1 = new Question();
        question1.setId("1");
        question1.setType(QuestionType.SINGLE_CHOICE);
        question1.setPoints(10);
        question1.setAnswerOptions(List.of(correctOption1));

        AnswerOption correctOption2 = new AnswerOption();
        correctOption2.setId("2");
        correctOption2.setCorrect(true);

        Question question2 = new Question();
        question2.setId("2");
        question2.setType(QuestionType.SINGLE_CHOICE);
        question2.setPoints(20);
        question2.setAnswerOptions(List.of(correctOption2));

        Quiz quiz = new Quiz();
        quiz.setId("1");
        quiz.setQuestions(List.of(question1, question2));

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuizId("1");
        attempt.setAnswers(List.of());

        when(quizRepo.findById("1")).thenReturn(Optional.of(quiz));
        when(quizAttemptRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        QuizAttempt result = quizAttemptService.submitAttempt(attempt);

        assertThat(result.getScore()).isEqualTo(0);
        assertThat(result.getMaxScore()).isEqualTo(30);
    }

    @Test
    void submitAttempt_shouldGivePointsForCorrectTextAnswerIgnoringCase() {

        Question question = new Question();
        question.setId("1");
        question.setType(QuestionType.TEXT);
        question.setPoints(10);
        question.setAcceptedTextAnswers(List.of("test"));

        Quiz quiz = new Quiz();
        quiz.setId("1");
        quiz.setQuestions(List.of(question));


        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setQuestionId("1");
        userAnswer.setTextAnswer("TeST");

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuizId("1");
        attempt.setAnswers(List.of(userAnswer));

        when(quizRepo.findById("1")).thenReturn(Optional.of(quiz));
        when(quizAttemptRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        QuizAttempt result = quizAttemptService.submitAttempt(attempt);

        assertThat(result.getScore()).isEqualTo(10);
        assertThat(result.getMaxScore()).isEqualTo(10);
    }
    @Test
    void submitAttempt_shouldSaveAttemptWithScoreAndFinishedAt() {

        Question question = new Question();
        question.setId("1");
        question.setType(QuestionType.SINGLE_CHOICE);
        question.setPoints(10);

        AnswerOption correctOption = new AnswerOption();
        correctOption.setId("1");
        correctOption.setCorrect(true);
        question.setAnswerOptions(List.of(correctOption));

        Quiz quiz = new Quiz();
        quiz.setId("1");
        quiz.setQuestions(List.of(question));

        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setQuestionId("1");
        userAnswer.setSelectedOptionIds(List.of("1"));

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuizId("1");
        attempt.setAnswers(List.of(userAnswer));

        when(quizRepo.findById("1")).thenReturn(Optional.of(quiz));
        when(quizAttemptRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Instant before = Instant.now();
        QuizAttempt result = quizAttemptService.submitAttempt(attempt);
        Instant after = Instant.now();


        assertThat(result.getScore()).isEqualTo(10);
        assertThat(result.getMaxScore()).isEqualTo(10);
        assertThat(result.getFinishedAt())
                .isNotNull()
                .isBetween(before, after);
    }
}