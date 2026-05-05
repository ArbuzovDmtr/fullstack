package org.example.backend.OpenAI.Records;

import java.util.List;

public record OpenAIRequest(
        String model,
        List<Message> messages
) {}