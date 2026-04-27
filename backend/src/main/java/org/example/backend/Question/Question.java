package org.example.backend.Question;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {
    private String id;

    private String text;

    private QuestionType type;

    private List<AnswerOption> answerOptions;

    private List<String> acceptedTextAnswers;

    private int points;

    private int orderIndex;
}
