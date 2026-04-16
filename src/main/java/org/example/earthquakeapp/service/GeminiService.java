package org.example.earthquakeapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.earthquakeapp.dto.AiResponse;
import org.example.earthquakeapp.entity.Earthquake;
import org.example.earthquakeapp.exception.GeminiException;
import org.example.earthquakeapp.repository.EarthquakeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import java.util.List;

@Service
public class GeminiService {

    private final EarthquakeRepository repository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api.key}")
    private String apiKey;

    public GeminiService(EarthquakeRepository repository, RestTemplate restTemplate) {
        this.repository = repository;
        this.restTemplate = restTemplate;
    }

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.systemDefault());

    public AiResponse askQuestion(String question) {
        String prompt = buildPrompt(question);
        String response = callGemini(prompt);
        return new AiResponse(extractText(response));
    }


    public String buildPrompt(String question) {

        List<Earthquake> earthquakes = repository.findAll();

        StringBuilder context = new StringBuilder();
        context.append("Recent earthquakes:\n");

        if (earthquakes.isEmpty()) {
            context.append("No earthquake data available in the last hour.\n");
        } else {
            for (int i = 0; i < Math.min(10, earthquakes.size()); i++) {
                Earthquake e = earthquakes.get(i);

                context.append("- Mag ")
                        .append(e.getMag())
                        .append(", Place: ")
                        .append(e.getPlace())
                        .append(", Time (UTC): ")
                        .append(Instant.ofEpochMilli(e.getTime()).toString())
                        .append(", Depth: ")
                        .append(e.getDepth())
                        .append(", Latitude: ")
                        .append(e.getLatitude())
                        .append(", Longitude: ")
                        .append(e.getLongitude())
                        .append(", Tsunami: ")
                        .append(e.getTsunami())
                        .append("\n");
            }
        }

        return """
        You are an Earthquake Data Assistant.

        IMPORTANT RULES:
        - You are given data ONLY for earthquakes from the LAST 1 HOUR.
        - You can use your general knowledge about earthquakes to answer questions.
        - Do NOT invent earthquakes or locations.

        FORMAT RULES:
        - Keep answers under 120-150 words.
        - Do NOT use formatting like bold or new lines.
        - Do NOT return raw JSON.

        EARTHQUAKE DATA:
        %s

        USER QUESTION:
        %s
        """.formatted(context, question);
    }


    public String callGemini(String prompt) {

        String url =
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
                        + apiKey;

        String safePrompt = prompt
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");

        String body = """
        {
          "contents": [
            {
              "parts": [
                { "text": "%s" }
              ]
            }
          ]
        }
        """.formatted(safePrompt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {
            return restTemplate.postForObject(url, request, String.class);
        } catch (Exception e) {
            e.printStackTrace();

            throw new GeminiException(
                    "Failed to call Gemini API: " + e.getMessage()
            );
        }
    }


    public String extractText(String response) {

        try {
            JsonNode root = objectMapper.readTree(response);

            return root
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

        } catch (Exception e) {
            throw new GeminiException("Failed to parse Gemini response");
        }
    }
}