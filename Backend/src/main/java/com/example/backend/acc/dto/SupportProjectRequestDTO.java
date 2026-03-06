package com.example.backend.acc.dto;

import com.example.backend.core.modules.projects.dto.BaseProjectRequestDTO;

public class SupportProjectRequestDTO extends BaseProjectRequestDTO {

    private String status;

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }


    //Maybe one day they'll be something there ...
}
