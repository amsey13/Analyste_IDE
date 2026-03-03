package com.example.backend.core.modules.projects.taigaAPi.dto;

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
