package com.example.backend.modules.projects.audit.taiga.service;

import com.example.backend.modules.projects.audit.taiga.dto.TaigaAuthResponse;
import com.example.backend.modules.projects.audit.taiga.dto.TaigaUserStory;
import com.example.backend.modules.projects.audit.taiga.exception.IncorrectIdentifiersException;
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

    /**
     * This Java function authenticates a user by sending their username and password to a specified
     * URL and returns an authentication token if successful, otherwise throws an exception.
     * 
     * @param username The `authenticate` method you provided seems to be a part of a service that
     * authenticates users based on their username and password. The method constructs a request body
     * with the provided username and password, sends a POST request to the authentication endpoint,
     * and returns the authentication token if the authentication is successful.
     * @param password It seems like the password parameter is missing in your message. Could you
     * please provide the password so that I can assist you further with the authentication process?
     * @return The method `authenticate` is returning an authentication token (authtoken) if the
     * response from the `restTemplate` is not null. If the response is null, it will return null.
     */
    public String authenticate(String username, String password) throws IncorrectIdentifiersException {

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
            throw new IncorrectIdentifiersException("The provided identifiers do not match " +e.getMessage());
        }


    }

   /**
    * The function `getProjectIdBySlug` retrieves the project ID by slug using a REST API call with
    * authentication headers.
    * 
    * @param slug The `slug` parameter in the `getProjectIdBySlug` method is a unique identifier for a
    * project. It is typically a user-friendly and URL-safe version of the project name.
    * @param token A token is a piece of information that is used to access a secure system or
    * resource. It is typically a string of characters that serves as a credential for authentication
    * and authorization purposes. In the context of the `getProjectIdBySlug` method, the token
    * parameter is likely an authentication token that grants
    * @return The method `getProjectIdBySlug` is returning an Integer value, which is the ID of the
    * project corresponding to the provided slug.
    */
    public Integer getProjectIdBySlug(String slug, String token){
        String url = BASE_URL + "/projects/by_slug?slug=" + slug;
        HttpHeaders headers = createAuthHeaders(token);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        return (Integer) response.getBody().get("id");
    }


    /**
     * This function retrieves user stories for a specific project from a Taiga API using a provided
     * token.
     * 
     * @param projetId The `projetId` parameter is an Integer representing the ID of the project for
     * which you want to retrieve user stories.
     * @param token A token is a piece of data that is used to authenticate a user or provide access to
     * a system or service. In this context, the token is likely an authentication token that grants
     * access to the Taiga API for retrieving user stories. It serves as a form of authorization to
     * ensure that only authorized users
     * @return A list of TaigaUserStory objects is being returned.
     */
    public List<TaigaUserStory> getUserStories(Integer projetId, String token){
        String url = BASE_URL + "/userstories?project=" + projetId;
        HttpHeaders headers = createAuthHeaders(token);

        ResponseEntity<TaigaUserStory[]> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), TaigaUserStory[].class);
        return Arrays.asList(response.getBody());

    }

    /**
     * The function creates HttpHeaders with an Authorization header containing a Bearer token.
     * 
     * @param token The `token` parameter in the `createAuthHeaders` method is a string that represents
     * the authentication token used for authorization. This token is typically provided by the server
     * or authentication service and is used to authenticate the user's identity when making requests
     * to protected resources.
     * @return The method `createAuthHeaders` returns an instance of `HttpHeaders` with the
     * "Authorization" header set to "Bearer " followed by the provided token.
     */
    private HttpHeaders createAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }
    








}
