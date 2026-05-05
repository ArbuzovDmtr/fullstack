package org.example.backend.OpenAI.Service;

import org.example.backend.OpenAI.Records.Message;
import org.example.backend.OpenAI.Records.OpenAIRequest;
import org.example.backend.OpenAI.Records.OpenAIResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
@Service
public class OpenAIService {

    private final RestClient restClient;

    public OpenAIService(@Value("${app.openai-api-key}") String openaiApiKey) {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.openai.com/v1/chat/completions")
                .defaultHeader("Authorization", "Bearer " + openaiApiKey)
                .build();
    }

    public boolean isQuizTextAnswerCorrect(String questionText, String expectedAnswer, String userAnswer) {
        OpenAIRequest request = new OpenAIRequest(
                "gpt-5.4-mini",
                List.of(
                        new Message("system", """
                            You are checking quiz answers.
                            Return only TRUE or FALSE.
                            Do not explain.
                            The user's answer is correct if it has the same meaning as the expected answer.
                            Minor grammar mistakes are allowed.
                            """),
                        new Message("user", """
                            Question:
                            %s
                            
                            Expected answer:
                            %s
                            
                            User answer:
                            %s
                            """.formatted(questionText, expectedAnswer, userAnswer))
                )
        );

        OpenAIResponse response = restClient.post()
                .body(request)
                .retrieve()
                .body(OpenAIResponse.class);

        String result = response.choices().getFirst().message().content();

        return "TRUE".equalsIgnoreCase(result.trim());
    }
}
