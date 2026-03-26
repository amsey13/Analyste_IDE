package com.example.backend.modules.projects.acc.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "dictionary_attribute")
public class DictionaryAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name; // ex: "email", "prixTTC"

    @Column(nullable = false, name = "data_type")
    private String dataType; // ex: "VARCHAR", "INT", "DATE"

    private String size; // ex: "255" pour un VARCHAR

    @Column(name = "is_primary_key")
    private Boolean isPrimaryKey = false;

    @Column(name = "is_not_null")
    private Boolean isNotNull = false;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_id", nullable = true)
    private DictionaryEntry dictionaryEntry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "association_id", nullable = true)
    private DictionaryAssociation dictionaryAssociation;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Boolean getPrimaryKey() {
        return isPrimaryKey;
    }

    public void setPrimaryKey(Boolean primaryKey) {
        isPrimaryKey = primaryKey;
    }

    public Boolean getNotNull() {
        return isNotNull;
    }

    public void setNotNull(Boolean notNull) {
        isNotNull = notNull;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DictionaryEntry getDictionaryEntry() {
        return dictionaryEntry;
    }

    public void setDictionaryEntry(DictionaryEntry dictionaryEntry) {
        this.dictionaryEntry = dictionaryEntry;
    }

    public DictionaryAssociation getDictionaryAssociation() {
        return dictionaryAssociation;
    }

    public void setDictionaryAssociation(DictionaryAssociation dictionaryAssociation) {
        this.dictionaryAssociation = dictionaryAssociation;
    }
}