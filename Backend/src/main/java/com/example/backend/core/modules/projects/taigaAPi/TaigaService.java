package com.example.backend.core.modules.projects.taigaAPi;

import com.example.backend.core.modules.projects.taigaAPi.dto.TaigaAuthResponse;
import com.example.backend.core.modules.projects.taigaAPi.dto.TaigaUserStory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TaigaService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BASE_URL = "https://api.taiga.io/api/v1";

    public String authenticate(String username, String password){

        String url = BASE_URL + "/auth";
        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);
        body.put("type", "normal");

        try{
            TaigaAuthResponse response = restTemplate.postForObject(url, body, TaigaAuthResponse.class);
            return (response != null) ? response.getAuthtoken() : null;
        }
        catch (Exception e){
            return null;
        }


    }

    public Integer getProjectIdBySlug(String slug, String token){
        String url = BASE_URL + "/projects/by_slug?slug=" + slug;
        HttpHeaders headers = createAuthHeaders(token);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        return (Integer) response.getBody().get("id");
    }

    public List<TaigaUserStory> getUserStories(Integer projetId, String token){
        String url = BASE_URL + "/userstories?project=" + projetId;
        HttpHeaders headers = createAuthHeaders(token);

        ResponseEntity<TaigaUserStory[]> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), TaigaUserStory[].class);
        return Arrays.asList(response.getBody());

    }

    private HttpHeaders createAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }








}
