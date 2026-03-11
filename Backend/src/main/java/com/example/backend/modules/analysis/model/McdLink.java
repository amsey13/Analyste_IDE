package com.example.backend.modules.analysis.model;

public class McdLink {

    private String entity;
    private String relationship;
    private String cardinality;

    public McdLink(String cardinality, String entity, String relationship) {
        this.cardinality = cardinality;
        this.entity = entity;
        this.relationship = relationship;
    }

    public String getCardinality() {
        return cardinality;
    }

    public void setCardinality(String cardinality) {
        this.cardinality = cardinality;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    @Override
    public String toString() {
        return entity + " -> " + relationship + " [" + cardinality + "]";
    }
}
