package com.example.backend.modules.projects.acc.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "dictionary_associations")

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DictionaryAssociation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // L'entité de départ (ex: Client)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_entry_id", nullable = false)
    private DictionaryEntry source;

    // L'entité d'arrivée (ex: Commande)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_entry_id", nullable = false)
    private DictionaryEntry target;

    // Le verbe de l'association (ex: "Passe", "Contient")
    @Column(nullable = false)
    private String name;

    // Cardinalité côté source (ex: "0..N", "1..1")
    @Column(nullable = false, length = 10)
    private String sourceMultiplicity;

    // Cardinalité côté cible (ex: "1..1", "1..N")
    @Column(nullable = false, length = 10)
    private String targetMultiplicity;

    @Column(name = "is_relative", nullable = false)
    private Boolean isRelative = false;

    @Column(name = "is_cif", nullable = false)
    private Boolean isCif = false;

    @Column(name = "is_inheritance", nullable = false)
    private Boolean isInheritance = false;





    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public DictionaryEntry getSource() {
        return source;
    }

    public void setSource(DictionaryEntry source) {
        this.source = source;
    }

    public DictionaryEntry getTarget() {
        return target;
    }

    public void setTarget(DictionaryEntry target) {
        this.target = target;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSourceMultiplicity() {
        return sourceMultiplicity;
    }

    public void setSourceMultiplicity(String sourceMultiplicity) {
        this.sourceMultiplicity = sourceMultiplicity;
    }

    public String getTargetMultiplicity() {
        return targetMultiplicity;
    }

    public void setTargetMultiplicity(String targetMultiplicity) {
        this.targetMultiplicity = targetMultiplicity;
    }

    public Boolean getRelative() {
        return isRelative;
    }

    public void setRelative(Boolean relative) {
        isRelative = relative;
    }

    public Boolean getCif() {
        return isCif;
    }

    public void setCif(Boolean cif) {
        isCif = cif;
    }
    public Boolean getIsInheritance() {
        return isInheritance;
    }
    public void setIsInheritance(Boolean isInheritance) {
        this.isInheritance = isInheritance;
    }
}