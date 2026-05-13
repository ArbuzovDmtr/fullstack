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
                            You are a strict but fair semantic answer checker for a quiz.
                            Return only TRUE or FALSE.
                                Rules:
                                 - Compare meaning, not exact wording.
                                 - Accept answers in any language.
                                 - Accept synonyms, paraphrases, spelling mistakes, and minor grammar mistakes.
                                 - Accept partial answers if they contain the key meaning required by the expected answer.
                                 - Do not require the same sentence structure.
                                 - For numbers, accept written words and digits in any language.
                                 - Mark false only if the user answer misses the key meaning or contradicts it.
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

        assert response != null;
        String result = response.choices().getFirst().message().content();

        return "TRUE".equalsIgnoreCase(result.trim());
    }
}
