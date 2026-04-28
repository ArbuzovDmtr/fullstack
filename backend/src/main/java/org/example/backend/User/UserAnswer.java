package org.example.backend.User;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserAnswer {

    private String questionId;


    private List<String> selectedOptionIds;


    private String textAnswer;
}