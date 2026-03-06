package com.example.backend.modules.projects.audit.taiga.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TaigaAuthResponse {

    @JsonProperty("auth_token")
    private String authtoken;

    public TaigaAuthResponse() {}

    public String getAuthtoken() {
        return authtoken;
    }

    public void setAuthtoken(String authtoken) {
        this.authtoken = authtoken;
    }
}
