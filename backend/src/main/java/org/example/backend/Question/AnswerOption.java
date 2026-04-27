package org.example.backend.Question;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerOption {

    private String id;

    private String text;

    private boolean correct;
}
