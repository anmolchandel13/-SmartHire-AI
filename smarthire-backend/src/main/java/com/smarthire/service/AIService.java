package com.smarthire.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthire.model.AIResult;
import com.smarthire.model.Profile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AIService — Connects to Gemini API, builds prompts, and parses the scorecard.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.ai.gemini.api-key}")
    private String apiKey;

    @Value("${app.ai.gemini.model}")
    private String modelName;

    @Value("${app.ai.gemini.api-url}")
    private String apiUrl;

    /**
     * Sends the candidate profile and resume text to Gemini API, parses the response,
     * and maps it to a database-ready AIResult entity.
     */
    public AIResult analyzeResume(Profile profile, String resumeText) {
        log.info("Starting AI analysis for candidate: {}", profile.getFullName());

        // Check if API key is not configured
        if (apiKey == null || apiKey.trim().isEmpty() || "your-gemini-api-key-here".equalsIgnoreCase(apiKey.trim())) {
            log.info("Gemini API key is not configured. Running in local Mock Evaluation Mode...");
            
            // Determine a suitable role based on candidate's skills or branch
            String skillsLower = (profile.getSkills() != null) ? profile.getSkills().toLowerCase() : "";
            String recommendedRole = "Software Developer";
            String readiness = "Needs Improvement";
            int baseScore = 65;

            if (skillsLower.contains("java") || skillsLower.contains("spring")) {
                recommendedRole = "Junior Backend Developer";
                baseScore = 78;
                readiness = "Ready";
            } else if (skillsLower.contains("react") || skillsLower.contains("html") || skillsLower.contains("javascript")) {
                recommendedRole = "Junior Frontend Developer";
                baseScore = 75;
                readiness = "Ready";
            } else if (skillsLower.contains("python") || skillsLower.contains("sql") || skillsLower.contains("data")) {
                recommendedRole = "Data Analyst";
                baseScore = 70;
            }

            // Adjust score based on percentage
            if (profile.getPercentage() != null) {
                if (profile.getPercentage() > 85) {
                    baseScore += 10;
                } else if (profile.getPercentage() > 70) {
                    baseScore += 5;
                }
            }
            if (baseScore > 100) baseScore = 100;

            return AIResult.builder()
                    .profile(profile)
                    .score(baseScore)
                    .summary("Candidate shows a solid profile with foundational capability in " + (profile.getSkills() != null ? profile.getSkills() : "development") + ". Evaluated locally in Mock Mode.")
                    .strengths("- Good academic track record with " + (profile.getPercentage() != null ? profile.getPercentage() : "decent") + "%\n- Practical skill alignment in " + (profile.getSkills() != null ? profile.getSkills() : "relevant technologies"))
                    .weaknesses("- Cloud architectural patterns could be improved\n- Needs more project experience using standard pipelines")
                    .recommendedRole(recommendedRole)
                    .readinessLevel(readiness)
                    .build();
        }

        String prompt = buildPrompt(profile, resumeText);

        try {
            String rawJsonResponse = callGeminiApi(prompt);
            return parseAiResponse(rawJsonResponse, profile);
        } catch (Exception ex) {
            log.error("Failed to perform AI analysis for user: {}. Error: {}", profile.getFullName(), ex.getMessage());
            throw new RuntimeException("AI analysis failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * Constructs the structured prompt. We mandate a JSON output structure.
     */
    private String buildPrompt(Profile profile, String resumeText) {
        return "You are an elite corporate Recruiter and Technical Screener. Analyze this candidate's profile and extracted resume text.\n\n" +
                "Candidate Information:\n" +
                "- Name: " + profile.getFullName() + "\n" +
                "- Branch: " + (profile.getBranch() != null ? profile.getBranch() : "Not Specified") + "\n" +
                "- Academic Percentage/CGPA: " + (profile.getPercentage() != null ? profile.getPercentage() : "Not Specified") + "%\n" +
                "- Claimed Skills: " + (profile.getSkills() != null ? profile.getSkills() : "Not Specified") + "\n\n" +
                "Extracted Resume PDF Content:\n" +
                "\"\"\"\n" + resumeText + "\n\"\"\"\n\n" +
                "CRITICAL INSTRUCTIONS:\n" +
                "1. Provide a comprehensive scorecard evaluation.\n" +
                "2. The 'strengths' and 'weaknesses' should be detailed summaries, formatted as structured paragraphs or clear markdown bullet lists.\n" +
                "3. Recommend the best fit 'recommended_role' (e.g. Java Developer, Frontend Developer, Data Analyst).\n" +
                "4. Assess interview 'readiness_level' as exactly one of: 'Ready', 'Needs Improvement', or 'Not Ready'.\n" +
                "5. Evaluate a numeric 'score' out of 100 based on core qualifications, academic performance, and industry relevance.\n\n" +
                "You MUST return the output in raw JSON matching this structure EXACTLY. Do not add any backticks, markdown formatting, or extra text wrapper: \n" +
                "{\n" +
                "  \"score\": 75,\n" +
                "  \"summary\": \"Brief candidate summary overview...\",\n" +
                "  \"strengths\": \"A detailed bulleted description of strengths...\",\n" +
                "  \"weaknesses\": \"A detailed description of weaknesses or gaps in profile...\",\n" +
                "  \"recommended_role\": \"Java Developer\",\n" +
                "  \"readiness_level\": \"Ready\"\n" +
                "}";
    }

    /**
     * Calls Gemini API v1beta API endpoint using RestTemplate.
     */
    private String callGeminiApi(String prompt) {
        String fullUrl = apiUrl + "/" + modelName + ":generateContent?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Build the request body matching Gemini API spec:
        // { "contents": [ { "parts": [ { "text": "prompt" } ] } ] }
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

        Map<String, Object> partsMap = new HashMap<>();
        partsMap.put("parts", List.of(textPart));

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("contents", List.of(partsMap));

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(bodyMap, headers);

        log.debug("Sending POST call to Gemini API: {}", fullUrl);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(fullUrl, requestEntity, String.class);

        return responseEntity.getBody();
    }

    /**
     * Parses the JSON payload returned by Gemini.
     */
    private AIResult parseAiResponse(String rawResponse, Profile profile) throws Exception {
        JsonNode rootNode = objectMapper.readTree(rawResponse);
        
        // Extract text from: candidates[0].content.parts[0].text
        JsonNode candidates = rootNode.path("candidates");
        if (candidates.isMissingNode() || !candidates.isArray() || candidates.isEmpty()) {
            throw new RuntimeException("Invalid response format from Gemini API: candidates block missing");
        }

        String rawContentText = candidates.get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text")
                .asText();

        // Standardize the content text (sometimes LLM wraps response in ```json ... ```)
        String jsonText = rawContentText;
        if (jsonText.contains("```json")) {
            jsonText = jsonText.substring(jsonText.indexOf("```json") + 7);
            if (jsonText.contains("```")) {
                jsonText = jsonText.substring(0, jsonText.lastIndexOf("```"));
            }
        } else if (jsonText.contains("```")) {
            jsonText = jsonText.substring(jsonText.indexOf("```") + 3);
            if (jsonText.contains("```")) {
                jsonText = jsonText.substring(0, jsonText.lastIndexOf("```"));
            }
        }
        jsonText = jsonText.trim();

        log.debug("Extracted Inner JSON content: {}", jsonText);

        JsonNode parsedScorecard = objectMapper.readTree(jsonText);

        return AIResult.builder()
                .profile(profile)
                .score(parsedScorecard.path("score").asInt(50))
                .summary(parsedScorecard.path("summary").asText("No summary provided by AI."))
                .strengths(parsedScorecard.path("strengths").asText("None noted."))
                .weaknesses(parsedScorecard.path("weaknesses").asText("None noted."))
                .recommendedRole(parsedScorecard.path("recommended_role").asText("General Software Engineer"))
                .readinessLevel(parsedScorecard.path("readiness_level").asText("Needs Improvement"))
                .build();
    }
}
