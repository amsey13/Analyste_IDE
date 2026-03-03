package com.example.backend.core.modules.projects.taigaAPi.dto;

public class TaigaUserStory {

    private Long idUser;
    private String subject;

    public TaigaUserStory() {}

    public Long getIdUser() {
        return idUser;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
