package com.example.backend.modules.analysis.exporter;

import com.example.backend.modules.projects.acc.dto.DictionaryEntryRequestDTO;
import com.example.backend.modules.projects.acc.dto.McdSuggestionDTO;
import com.example.backend.modules.projects.audit.dto.AnomalyDTO;
import com.example.backend.modules.projects.audit.entity.Report;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.Math.max;

@Service
public class MistralService {


    private final ObjectMapper mapper =  new ObjectMapper();
    private final String apiKey ;
    private final ClientHttp client;

    private static final String MISTRAL_URL = "https://api.mistral.ai/v1/conversations";

    @Autowired
    public MistralService(@Value("${API_KEY_MISTRAL:test_key}") String apiKey, ClientHttp client) {
        if (apiKey == null || apiKey.isEmpty() || "test_key".equals(apiKey)) {

        }
        this.apiKey = apiKey;
        this.client = client;
    }



    /**
     * The function creates a map of headers with authorization, accept, and content type for an HTTP
     * request.
     * 
     * @return A Map containing headers is being returned. The headers include "Authorization" with a
     * value of "Bearer " concatenated with the apiKey, "Accept" with a value of "application/json",
     * and "Content-Type" with a value of "application/json".
     */
    private Map<String, String> createHeaders() {
        return Map.of(
                "Authorization", "Bearer " + apiKey,
                "Accept", "application/json",
                "Content-Type", "application/json"
        );
    }

    /**
     * The function prepares a request body in JSON format with a specified prompt.
     * 
     * @param prompt The `prepareRequestBody` method takes a `prompt` as input and creates a request
     * body in the form of a JSON string. The request body includes a model name and a list of inputs
     * where each input contains a role (e.g., "user") and the content of the prompt provided.
     * @return The method `prepareRequestBody` returns a JSON string representing a request body with a
     * specific structure. The JSON object includes a "model" key with the value
     * "mistral-medium-latest" and an "inputs" key with a list containing a map with keys "role" and
     * "content" along with their corresponding values.
     */
    private String prepareRequestBody(String prompt) throws IOException {
        Map<String, Object> requestBody = Map.of(
                "model", "mistral-medium-latest",
                "inputs", List.of(Map.of("role", "user", "content", prompt))

        );
        return mapper.writeValueAsString(requestBody);
    }

    /**
     * The function `parseResponse` reads a JSON response, extracts content from it, and returns the
     * extracted content as a string.
     * 
     * @param response The `parseResponse` method takes a JSON response as input and extracts the
     * content from it. The content is retrieved from the "outputs" array, the first element of that
     * array, and then the "content" field within that element.
     * @return The method `parseResponse` is returning the content extracted from the JSON response.
     */
    private String parseReponse(String response) throws IOException {
        JsonNode responseNode = mapper.readTree(response);
        return responseNode.path("outputs").get(0).path("content").asText();


    }


    /**
     * This Java function sends a question to an API, handles the response, and returns the parsed
     * response.
     * 
     * @param prompt The `prompt` parameter in the `askQuestion` method is a String that represents the
     * question or prompt that you want to ask. This prompt will be used to prepare the request body
     * before sending a request to the Mistral API.
     * @return The method `askQuestion` is returning the response body after parsing it.
     */
    public String askQuestion(String prompt) throws IOException {

        Map<String,String> headers = createHeaders();

        String body = this.prepareRequestBody(prompt);


        this.client.setHeaders(headers);
        this.client.setBody(body);
        HttpResponse response = client.execute(MISTRAL_URL);

        if (!response.isSuccess()) {
            throw new IOException("Erreur API Mistral: " + response.getCode() + " - " + response.getBody());
        }


        return parseReponse(response.getBody());
    }

    /**
     * The function calculates a score based on the severity of anomalies in a report.
     * 
     * @param report The `calculScore` method takes a `Report` object as a parameter. The method
     * calculates a score based on the anomalies present in the report. It calculates the total malus
     * (penalty points) by summing up the malus values of each anomaly severity in the report. The
     * final score
     * @return The method `calculScore` is returning a double value, which represents the calculated
     * score based on the anomalies severity in the given `Report` object. The score is calculated by
     * summing up the malus values of each anomaly severity and subtracting it from 100. The final
     * score is then returned, ensuring it is at least 0.
     */
    public double calculScore(Report report){

        double totalMalus = report.getAnomalies().stream()
                .mapToInt(res -> res.getSeverity().getMalus())
                .sum();
        return max(0,100.0-totalMalus);

    }

    /**
     * The function isNotBlank checks if a given string is not null and not empty.
     * 
     * @param string The `isNotBlank` method takes a `String` parameter named `string` and checks if it
     * is not `null` and not empty. It returns `true` if the `string` is not `null` and not empty,
     * otherwise it returns `false`.
     * @return The method isNotBlank returns a boolean value indicating whether the input string is not
     * null and not empty.
     */
    private boolean isNotBlank(String string) {
        return string != null && !string.isEmpty();
    }


    /**
     * The function `buildAuditPrompt` generates an audit prompt with specific rules and instructions
     * based on provided BPMN, MCD, MFC, and user stories content.
     * 
     * @param bpmnContent The `buildAuditPrompt` method you provided seems to be generating an audit
     * prompt message based on the content of different models (BPMN, MCD, MFC, User Stories). The
     * method constructs a message instructing an expert on how to perform a coherence audit across
     * these models.
     * @param mcdContent The `buildAuditPrompt` method you provided seems to be generating an audit
     * prompt for a multi-model coherence audit. The method takes four inputs: `bpmnContent`,
     * `mcdContent`, `mfcContent`, and `usContent`, which represent the content of BPMN, MCD
     * @param mfcContent The `mfcContent` parameter likely contains content related to the MFC model.
     * This content will be included in the audit prompt that is generated by the `buildAuditPrompt`
     * method. The method constructs a message that guides an expert in auditing the coherence of
     * multiple models (BPMN, M
     * @param usContent The `buildAuditPrompt` method you provided seems to be generating an audit
     * prompt for a multi-model coherence audit. The method takes four inputs: `bpmnContent`,
     * `mcdContent`, `mfcContent`, and `usContent`, which represent the content of BPMN, MCD
     * @return The method `buildAuditPrompt` returns a formatted string that includes instructions and
     * guidelines for an audit of coherence across multiple models (BPMN, MCD, MFC, and User Stories).
     * The content of the BPMN, MCD, MFC, and User Stories is included in the returned string, along
     * with severity rules, allowed anomaly types, and specific instructions for the audit process. The
     */
    private String buildAuditPrompt(String bpmnContent, String mcdContent, String mfcContent, String usContent){

        StringBuilder sb = new StringBuilder();

        if(isNotBlank(bpmnContent)) sb.append("\n--- MODÈLE BPMN ---\n").append(bpmnContent);
        if(isNotBlank(mcdContent))  sb.append("\n--- MODÈLE MCD ---\n").append(mcdContent);
        if(isNotBlank(mfcContent))  sb.append("\n--- MODÈLE MFC ---\n").append(mfcContent);
        if(isNotBlank(usContent))   sb.append("\n--- USER STORIES ---\n").append(usContent);

        return """
        Tu es un expert en audit de cohérence multi-modèles (BPMN, MCD, MFC, US).
        Ton but est de vérifier que ces différents modèles sont alignés et ne se contredisent pas.
        
        [RÈGLES DE SÉVÉRITÉ]
        Utilise impérativement l'une de ces valeurs pour 'severity' :
        - CRITICAL (Erreur bloquante, incohérence majeure)
        - HIGH (Manque important)
        - MEDIUM
        - LOW (Optimisation ou libellé mal nommé)
        
        [TYPES D'ANOMALIES AUTORISÉS]
        Utilise l'un de ces libellés pour 'type' :
        ACTEUR_PASSIF, DONNEE_NON_MODELISE, OBJECT_SANS_ATTRIBUT, TACHE_SANS_US, 
        LIBELLE_NON_CONFORME, INCOHERENCE_LOGIQUE, REDONDANCE_SÉMANTIQUE, IMPASSE_LOGIQUE.
        
        [LIEN ENTRE SEVERITE ET ANOMALIE]
        IMPASSE_LOGIQUE - CRITICAL
        INCOHERENCE_LOGIQUE - CRITICAL
        TACHE_SANS_US - CRITICAL
        REDONDANCE_SÉMANTIQUE - HIGH
        OBJECT_SANS_ATTRIBUT - HIGH
        LIBELLE_NON_CONFORME - LOW
        ACTEUR_PASSIF - LOW
        
        
        
        
        
        
        
        
        [CONTENU DES MODÈLES]
        %s
        
        [CONSIGNES]
        1. Analyse UNIQUEMENT les modèles fournis.
        2. Vérifie la cohérence croisée (ex: une donnée citée dans une US doit être dans le MCD).
        2,5. Pour chaque anomalie detecté propose une solution concrete pour la resoudre
        3. Réponds UNIQUEMENT au format JSON :
        {"anomalies": [
                         {
                           "description": "...",\s
                           "type": "...",\s
                           "severity": "...",\s
                           "suggestion": "Texte expliquant comment corriger l'anomalie"
                         }
                       ]
                     }
        IMPORTANT : Ta réponse doit être un JSON compact. Ne mets pas de vrais sauts de ligne à l'intérieur des valeurs de texte (descriptions ou suggestions)
        utilise '\\n' si nécessaire."
        """.formatted(sb.toString());

    }


   /**
    * The `executeAuditAnalysis` function processes an API response, extracts anomalies data, and
    * returns a list of `AnomalyDTO` objects, handling exceptions along the way.
    * 
    * @param bpmn The `executeAuditAnalysis` method takes four parameters: `bpmn`, `mcd`, `mfc`, and
    * `us`. In the provided code snippet, the `bpmn` parameter is used to build an audit prompt, which
    * is then sent as a question to receive a response
    * @param mcd The `mcd` parameter in the `executeAuditAnalysis` method likely stands for
    * "Model-Checking Description". This parameter is used as input for building an audit prompt and
    * conducting an audit analysis. It seems to be a part of a larger process involving BPMN (Business
    * Process Model and Notation
    * @param mfc The `mfc` parameter in the `executeAuditAnalysis` method likely stands for "Model Flow
    * Chart". It seems to be used as input for building an audit prompt and performing an audit
    * analysis. If you have any specific questions or need further assistance with this code snippet or
    * any related tasks, feel
    * @param us The `us` parameter in the `executeAuditAnalysis` method likely stands for User Story.
    * It seems to be one of the inputs used to build the audit prompt for the analysis. If you have any
    * specific questions or need further assistance with the code, feel free to ask!
    * @return A List of AnomalyDTO objects is being returned from the executeAuditAnalysis method.
    */
    public List<AnomalyDTO> executeAuditAnalysis(String bpmn, String mcd, String mfc, String us) throws IOException{

        String prompt = this.buildAuditPrompt(bpmn,mcd,mfc,us);
        String response = this.askQuestion(prompt);

        String cleanedResponse = response.trim();
        if (cleanedResponse.contains("```json")) {
            cleanedResponse = cleanedResponse.substring(cleanedResponse.indexOf("```json") + 7);
            cleanedResponse = cleanedResponse.substring(0, cleanedResponse.lastIndexOf("```"));
        } else if (cleanedResponse.contains("```")) {
            cleanedResponse = cleanedResponse.substring(cleanedResponse.indexOf("```") + 3);
            cleanedResponse = cleanedResponse.substring(0, cleanedResponse.lastIndexOf("```"));
        }
        cleanedResponse = cleanedResponse.trim();
        cleanedResponse = cleanedResponse
                .replace("‘", "'")
                .replace("’", "'")
                .replace("“", "\"")
                .replace("”", "\"")
                .replace("\\'", "'");


        try{
            JsonNode root = mapper.readTree(cleanedResponse);
            JsonNode anomalies = root.path("anomalies");

            if (anomalies.isMissingNode() || !anomalies.isArray()) {
                root.fieldNames().forEachRemaining(name -> System.out.println(" -> " + name));
                throw new IOException("La clé 'anomalies' est absente ou n'est pas un tableau. Réponse brute : " + response );
            }
            this.mapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
            this.mapper.configure(JsonReadFeature.ALLOW_SINGLE_QUOTES.mappedFeature(), true);
            this.mapper.configure(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER.mappedFeature(), true);




            return mapper.readValue(
                    anomalies.toString(),
                    new TypeReference<List<AnomalyDTO>>(){}
            );
        } catch(Exception e){
            System.err.println("JSON défectueux : " + cleanedResponse);
            throw new IOException("Erreur parsing JSON: " + e.getMessage());
        }
    }

    private String buildDictionaryPrompt(String usContent) {
        return """
        Tu es un architecte logiciel expert en modélisation de bases de données.
        Voici les User Stories d'un projet :
        
        %s
        
        Ton objectif est d'extraire les entités métiers principales et leurs attributs probables pour créer un Dictionnaire de Données.
        
        CONSIGNES STRICTES :
        1. Ne génère que les entités pertinentes pour une base de données (ignore les acteurs qui ne sont pas stockés).
        2. Réponds UNIQUEMENT au format JSON strict avec la structure exacte ci-dessous.
        3. Ne mets aucun texte d'introduction ou de conclusion.
        
        STRUCTURE JSON ATTENDUE :
        {
          "entries": [
            {
              "name": "Nom de l'entité (ex: Client)",
              "description": "Rôle de l'entité dans le système",
              "attributes": [
                {
                  "name": "nom_attribut (en snake_case ou camelCase)",
                  "dataType": "VARCHAR ou INT ou BOOLEAN ou DATE ou DATETIME ou DECIMAL ou TEXT",
                  "size": "255 (ou vide si non applicable)",
                  "primaryKey": true ou false,
                  "notNull": true ou false,
                  "description": "Description de ce que stocke cet attribut"
                }
              ]
            }
          ]
        }
        """.formatted(usContent);
    }

    /**
     * Analyse les User Stories via Mistral et suggère un dictionnaire de données.
     */
    public List<DictionaryEntryRequestDTO> suggestDictionaryFromUserStories(String usContent) throws IOException {
        if (usContent == null || usContent.isBlank()) {
            return Collections.emptyList();
        }

        String prompt = this.buildDictionaryPrompt(usContent);
        String response = this.askQuestion(prompt);

        // Nettoyage ultra-propre avec Regex (adieu les if/else)
        String cleanedResponse = response.replaceFirst("^```(json)?\\s*", "")
                .replaceFirst("\\s*```$", "")
                .trim();

        try {
            JsonNode root = mapper.readTree(cleanedResponse);
            JsonNode entries = root.path("entries");

            if (entries.isMissingNode() || !entries.isArray()) {
                throw new IOException("La clé 'entries' est absente de la réponse de l'IA.");
            }

            this.mapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);

            // On convertit directement le JSON en liste de DTOs
            return mapper.readValue(
                    entries.toString(),
                    new TypeReference<List<DictionaryEntryRequestDTO>>() {}
            );
        } catch (Exception e) {
            throw new IOException("Erreur lors du parsing des suggestions IA: " + e.getMessage());
        }
    }
    private String buildMcdPrompt(String rulesContent) {
        return """
        Tu es un architecte logiciel expert en méthode Merise.
        Voici les Règles de Gestion métier d'un projet :
        
        %s
        
        Ton objectif est de concevoir le Modèle Conceptuel de Données (MCD) complet. 
        Tu dois extraire les entités pertinentes (avec leurs attributs) ET les associations entre ces entités.
        
        CONSIGNES STRICTES :
        1. Les cardinalités (multiplicités) doivent OBLIGATOIREMENT être choisies parmi ces 4 valeurs : "0..N", "1..N", "0..1", "1..1".
        2. Le champ "ruleCode" doit contenir le code de la règle de gestion qui justifie cette association (ex: "RG-01").
        3. Si une association porte des données (ex: une quantité, une date d'achat dans une relation 0..N / 0..N), ajoute ces attributs dans le tableau "attributes" de l'association.
        4. Réponds UNIQUEMENT au format JSON strict avec la structure exacte ci-dessous. Ne rajoute AUCUN texte d'introduction ou de conclusion.
        
        STRUCTURE JSON ATTENDUE :
        {
          "entries": [
            {
              "name": "Nom de l'entité (ex: Client)",
              "description": "...",
              "attributes": [
                { "name": "...", "dataType": "VARCHAR", "size": "255", "primaryKey": true, "notNull": true, "description": "..." }
              ]
            }
          ],
          "associations": [
            {
              "sourceName": "Nom exact de l'entité source",
              "targetName": "Nom exact de l'entité cible",
              "name": "verbe de relation (ex: passe)",
              "sourceMultiplicity": "0..N",
              "targetMultiplicity": "1..1",
              "ruleCode": "RG-01",
              "attributes": [
                 { "name": "quantite", "dataType": "INT", "size": "", "primaryKey": false, "notNull": true, "description": "Quantité commandée" }
              ]
            }
          ]
        }
        """.formatted(rulesContent);
    }
    public McdSuggestionDTO suggestMcdFromBusinessRules(String rulesContent) throws IOException {
        if (rulesContent == null || rulesContent.isBlank()) {
            return new McdSuggestionDTO(); // On retourne un objet vide si pas de règles
        }

        String prompt = this.buildMcdPrompt(rulesContent);
        String response = this.askQuestion(prompt);

        // Nettoyage de la réponse (retirer les balises Markdown ```json si Mistral en a mis)
        String cleanedResponse = response.replaceFirst("^```(json)?\\s*", "")
                .replaceFirst("\\s*```$", "")
                .trim();

        try {
            JsonNode root = mapper.readTree(cleanedResponse);

            // Vérification de sécurité
            if (!root.has("entries") || !root.has("associations")) {
                throw new IOException("La réponse de l'IA ne contient pas les clés attendues ('entries' et 'associations').");
            }

            this.mapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);

            // On mappe directement le JSON dans notre super objet DTO !
            return mapper.readValue(cleanedResponse, McdSuggestionDTO.class);

        } catch (Exception e) {
            throw new IOException("Erreur lors du parsing de la suggestion MCD IA: " + e.getMessage());
        }
    }
}



