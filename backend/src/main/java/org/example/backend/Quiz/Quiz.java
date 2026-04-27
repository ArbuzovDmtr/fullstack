package org.example.backend.Quiz;


import lombok.*;
import org.example.backend.Question.Question;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz {

    private String id;

    private String title;

    private String description;

    private String createdByUserId;

    private Integer timeLimitSeconds;

    private List<Question> questions;
    @Builder.Default
    private boolean published = false;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
