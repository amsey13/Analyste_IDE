package com.example.backend.audit.dto;

import com.example.backend.core.modules.projects.dto.BaseProjectRequestDTO;

public class AuditProjectRequestDTO extends BaseProjectRequestDTO {

    private String taigaUserName;
    private String taigaPassword;
    private String taigaProjectUrl;

    public String getTaigaPassword() {
        return taigaPassword;
    }

    public void setTaigaPassword(String taigaPassword) {
        this.taigaPassword = taigaPassword;
    }

    public String getTaigaProjectUrl() {
        return taigaProjectUrl;
    }

    public void setTaigaProjectUrl(String taigaProjectUrl) {
        this.taigaProjectUrl = taigaProjectUrl;
    }

    public String getTaigaUserName() {
        return taigaUserName;
    }

    public void setTaigaUserName(String taigaUserName) {
        this.taigaUserName = taigaUserName;
    }
}
