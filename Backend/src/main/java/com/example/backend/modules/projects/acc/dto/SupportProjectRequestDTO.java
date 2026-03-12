package com.example.backend.modules.projects.acc.dto;

import com.example.backend.modules.projects.core.dto.BaseProjectRequestDTO;

public class SupportProjectRequestDTO extends BaseProjectRequestDTO {

    private String status;
    private String bpmnXml;

    public String getBpmnXml() {
        return bpmnXml;
    }

    public void setBpmnXml(String bpmnXml) {
        this.bpmnXml = bpmnXml;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }


    //Maybe one day they'll be something there ...
}
