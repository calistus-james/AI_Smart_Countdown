package com.smartcountdown.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

public class GeminiService {

    private final Client client;

    public GeminiService() {
        String apiKey = System.getenv("GOOGLE_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "GOOGLE_API_KEY is not configured."
            );
        }

        client = Client.builder()
                .apiKey(apiKey)
                .build();
    }

    public String getTaskSuggestions(String taskDetails) {

        String prompt = """
                You are an intelligent task planning assistant.

                Review the following pending tasks.
                Suggest three practical ways to prioritize
                and organize these tasks.

                Keep your suggestions clear and concise.

                Pending tasks:
                """ + taskDetails;

        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-3.5-flash-lite",
                        prompt,
                        null
                );

        return response.text();
    }

    public String getPriorityAnalysis(String taskDetails) {

        String prompt = """
            You are an intelligent AI task priority assistant.

            Today's date is: %s

            Analyze the following pending tasks.

            Provide your analysis in these sections:

            1. MOST URGENT TASK
               Identify the task that needs immediate attention
               and explain why.

            2. PRIORITY ORDER
               Rank all pending tasks from highest to lowest
               priority based on deadline and existing priority.

            3. REASONS
               Briefly explain why each task has its position.

            4. DAILY ACTION PLAN
               Suggest a practical plan for completing these tasks.

            Important:
            - Consider overdue tasks as urgent.
            - Do not invent deadlines.
            - Keep the response clear and practical.

            Pending Tasks:
            %s
            """.formatted(
                java.time.LocalDate.now(),
                taskDetails
        );

        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-3.5-flash-lite",
                        prompt,
                        null
                );

        return response.text();
    }

    public String getTaskBreakdown(String taskTitle,
                                   String category,
                                   String deadline,
                                   String priority) {

        String prompt = """
            You are an intelligent task planning assistant.

            Break the following task into 3 to 5 smaller,
            practical and actionable steps.

            Task Details:
            Task Name: %s
            Category: %s
            Deadline: %s
            Priority: %s

            Instructions:
            1. Provide 3 to 5 clear steps.
            2. Arrange steps in a logical order.
            3. Make each step specific and achievable.
            4. Consider the deadline and priority.
            5. Keep the explanation concise.

            Return the result as a numbered list.
            """.formatted(
                taskTitle,
                category,
                deadline,
                priority
        );

        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-3.5-flash-lite",
                        prompt,
                        null
                );

        return response.text();
    }
}