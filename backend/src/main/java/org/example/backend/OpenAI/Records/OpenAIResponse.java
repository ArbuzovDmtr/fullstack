package org.example.backend.OpenAI.Records;

import java.util.List;

public record OpenAIResponse(List<Choice> choices
) {}