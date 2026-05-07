package org.example.backend.Quiz.Services;


import org.example.backend.Leaderboard.LeaderboardService;
import org.example.backend.Question.AnswerOption;
import org.example.backend.Question.Question;
import org.example.backend.Question.QuestionType;
import org.example.backend.Quiz.AttemptResult;
import org.example.backend.Quiz.Quiz;
import org.example.backend.Quiz.QuizAttempt;
import org.example.backend.Quiz.Repositories.QuizAttemptRepo;
import org.example.backend.Quiz.Repositories.QuizRepo;
import org.example.backend.User.UserAnswer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizAttemptServiceTest {

    @Mock
    private QuizAttemptRepo quizAttemptRepo;

    @Mock
    private QuizRepo quizRepo;


    @Mock
    private LeaderboardService leaderboardService;


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
    void submitAttempt_shouldReturnZeroScoreForEmptyQuiz() {
        Quiz quiz = new Quiz();
        quiz.setId("1");
        quiz.setQuestions(List.of());

        UserAnswer invalidAnswer = new UserAnswer();
        invalidAnswer.setQuestionId("missing-question");
        invalidAnswer.setSelectedOptionIds(List.of("1"));

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuizId("1");
        attempt.setAnswers(List.of(invalidAnswer));

        when(quizRepo.findById("1")).thenReturn(Optional.of(quiz));
        when(quizAttemptRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        QuizAttempt result = quizAttemptService.submitAttempt(attempt);

        assertThat(result.getScore()).isEqualTo(0);
        assertThat(result.getMaxScore()).isEqualTo(0);
    }

    @Test
    void submitAttempt_shouldGiveZeroPointsForInvalidAnswers() {
        AnswerOption correctOption = new AnswerOption();
        correctOption.setId("1");
        correctOption.setCorrect(true);

        Question question = new Question();
        question.setId("question-1");
        question.setType(QuestionType.SINGLE_CHOICE);
        question.setPoints(10);
        question.setAnswerOptions(List.of(correctOption));

        Quiz quiz = new Quiz();
        quiz.setId("1");
        quiz.setQuestions(List.of(question));

        UserAnswer unknownQuestionAnswer = new UserAnswer();
        unknownQuestionAnswer.setQuestionId("unknown-question");
        unknownQuestionAnswer.setSelectedOptionIds(List.of("1"));

        UserAnswer invalidOptionAnswer = new UserAnswer();
        invalidOptionAnswer.setQuestionId("question-1");
        invalidOptionAnswer.setSelectedOptionIds(List.of("missing-option"));

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuizId("1");
        attempt.setAnswers(List.of(unknownQuestionAnswer, invalidOptionAnswer));

        when(quizRepo.findById("1")).thenReturn(Optional.of(quiz));
        when(quizAttemptRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        QuizAttempt result = quizAttemptService.submitAttempt(attempt);

        assertThat(result.getScore()).isEqualTo(0);
        assertThat(result.getMaxScore()).isEqualTo(10);
    }

    @ParameterizedTest
    @ValueSource(strings = {"test", "TEST", "TeST", "  tEsT  "})
    void submitAttempt_shouldGivePointsForCorrectTextAnswerIgnoringCase(String textAnswer) {

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
        userAnswer.setTextAnswer(textAnswer);

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
    void submitAttempt_shouldGiveZeroPointsForPartiallyCorrectSingleChoiceAnswer() {
        AnswerOption correctOption1 = new AnswerOption();
        correctOption1.setId("1");
        correctOption1.setCorrect(true);

        AnswerOption correctOption2 = new AnswerOption();
        correctOption2.setId("2");
        correctOption2.setCorrect(true);

        AnswerOption wrongOption = new AnswerOption();
        wrongOption.setId("3");
        wrongOption.setCorrect(false);

        Question question = new Question();
        question.setId("question-1");
        question.setType(QuestionType.SINGLE_CHOICE);
        question.setPoints(10);
        question.setAnswerOptions(List.of(correctOption1, correctOption2, wrongOption));

        Quiz quiz = new Quiz();
        quiz.setId("1");
        quiz.setQuestions(List.of(question));

        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setQuestionId("question-1");
        userAnswer.setSelectedOptionIds(List.of("1"));

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
    void submitAttempt_shouldSaveLeaderboardResultForUser() {
        AnswerOption correctOption = new AnswerOption();
        correctOption.setId("1");
        correctOption.setCorrect(true);

        Question question = new Question();
        question.setId("question-1");
        question.setType(QuestionType.SINGLE_CHOICE);
        question.setPoints(10);
        question.setAnswerOptions(List.of(correctOption));

        Quiz quiz = new Quiz();
        quiz.setId("quiz-1");
        quiz.setQuestions(List.of(question));

        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setQuestionId("question-1");
        userAnswer.setSelectedOptionIds(List.of("1"));
        userAnswer.setTimeSpentSeconds(14);

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuizId("quiz-1");
        attempt.setUserId("user-1");
        attempt.setAnswers(List.of(userAnswer));

        when(quizRepo.findById("quiz-1")).thenReturn(Optional.of(quiz));
        when(quizAttemptRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        QuizAttempt result = quizAttemptService.submitAttempt(attempt);

        assertThat(result.getScore()).isEqualTo(10);
        verify(leaderboardService).saveResult("user-1", "quiz-1", 10, 14);
    }

    @Test
    void submitAttempt_shouldNotSaveLeaderboardResultWithoutUser() {
        Quiz quiz = new Quiz();
        quiz.setId("quiz-1");
        quiz.setQuestions(List.of());

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuizId("quiz-1");
        attempt.setAnswers(List.of());

        when(quizRepo.findById("quiz-1")).thenReturn(Optional.of(quiz));
        when(quizAttemptRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        quizAttemptService.submitAttempt(attempt);

        verifyNoInteractions(leaderboardService);
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
    @Test
    void submitAttempt_shouldThrowExceptionWhenQuizNotFound() {

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuizId("999");
        attempt.setAnswers(List.of());

        when(quizRepo.findById("999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quizAttemptService.submitAttempt(attempt))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Quiz not found");
    }

    @Test
    void getAttemptResult_shouldReturnCorrectAnswersUserAnswersAndTime() {
        AnswerOption correctOption = new AnswerOption();
        correctOption.setId("1");
        correctOption.setText("Correct answer");
        correctOption.setCorrect(true);

        AnswerOption wrongOption = new AnswerOption();
        wrongOption.setId("2");
        wrongOption.setText("Wrong answer");
        wrongOption.setCorrect(false);

        Question question = new Question();
        question.setId("question-1");
        question.setText("Question text");
        question.setType(QuestionType.SINGLE_CHOICE);
        question.setPoints(10);
        question.setAnswerOptions(List.of(correctOption, wrongOption));

        Quiz quiz = new Quiz();
        quiz.setId("quiz-1");
        quiz.setTitle("Quiz title");
        quiz.setQuestions(List.of(question));

        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setQuestionId("question-1");
        userAnswer.setSelectedOptionIds(List.of("2"));
        userAnswer.setTimeSpentSeconds(7);

        QuizAttempt attempt = new QuizAttempt();
        attempt.setId("attempt-1");
        attempt.setQuizId("quiz-1");
        attempt.setAnswers(List.of(userAnswer));
        attempt.setScore(0);
        attempt.setMaxScore(10);
        attempt.setStartedAt(Instant.parse("2026-05-06T10:00:00Z"));
        attempt.setFinishedAt(Instant.parse("2026-05-06T10:00:30Z"));

        when(quizAttemptRepo.findById("attempt-1")).thenReturn(Optional.of(attempt));
        when(quizRepo.findById("quiz-1")).thenReturn(Optional.of(quiz));

        AttemptResult result = quizAttemptService.getAttemptResult("attempt-1");

        assertThat(result.attemptId()).isEqualTo("attempt-1");
        assertThat(result.quizTitle()).isEqualTo("Quiz title");
        assertThat(result.totalTimeSeconds()).isEqualTo(30);
        assertThat(result.questions().getFirst().correct()).isFalse();
        assertThat(result.questions().getFirst().correctOptions().getFirst().text()).isEqualTo("Correct answer");
        assertThat(result.questions().getFirst().userSelectedOptions().getFirst().text()).isEqualTo("Wrong answer");
        assertThat(result.questions().getFirst().timeSpentSeconds()).isEqualTo(7);
    }
}
