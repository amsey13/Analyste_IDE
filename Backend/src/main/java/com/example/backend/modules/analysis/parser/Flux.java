package com.example.backend.modules.analysis.parser;

public class Flux {

    private String name;
    private String recipient;
    private String sender;


    public  Flux(String name, String recipient, String sender) {
        this.name = name;
        this.recipient = recipient;
        this.sender = sender;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
