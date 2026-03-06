package com.example.backend.modules.projects.audit.dto;

import com.example.backend.modules.projects.core.dto.BaseProjectRequestDTO;

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
