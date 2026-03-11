package com.example.backend.modules.analysis.exporter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MistralService {


    private final ObjectMapper mapper =  new ObjectMapper();
    private final String apiKey ;
    private final ClientHttp client;

    public MistralService() {

        String key = System.getenv("API_KEY_MISTRAL");
        if (key == null || key.isEmpty()) {
            throw new IllegalStateException("There's no API key provided for Mistral");
        }
        this.apiKey = key;
        this.client = new ClientHttp("POST", null, null);
    }

    // Constructor only for test
    public MistralService(String apiKey, ClientHttp client) {
        this.apiKey = apiKey;
        this.client = client;
    }


    private Map<String, String> createHeaders() {
        return Map.of(
                "Authorization", "Bearer " + apiKey,
                "Accept", "application/json",
                "Content-Type", "application/json"
        );
    }

    private String prepareRequestBody(String prompt) throws IOException {
        Map<String, Object> requestBody = Map.of(
                "model", "mistral-meduim-latest",
                "inputs", List.of(Map.of("role", "user", "content", prompt))

        );
        return mapper.writeValueAsString(requestBody);
    }

    private String parseReponse(String response) throws IOException {
        JsonNode responseNode = mapper.readTree(response);
        return responseNode.path("outputs").get(0).path("content").asText();
    }


    public String askQuestion(String prompt) throws IOException {

        Map<String,String> headers = createHeaders();

        String body = this.prepareRequestBody(prompt);


        this.client.setHeaders(headers); // Supposons que tu as ces setters
        this.client.setBody(body);
        HttpResponse response = client.execute("https://api.mistral.ai/v1/conversations");

        if (!response.isSuccess()) {
            throw new IOException("Erreur API Mistral: " + response.getCode() + " - " + response.getBody());
        }


        return parseReponse(response.getBody());
    }


}



