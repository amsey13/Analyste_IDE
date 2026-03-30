package com.example.backend.modules.projects.acc.dto;

import java.util.List;

public class McdSuggestionDTO {

    private List<DictionaryEntryRequestDTO> entries;
    private List<AiAssociationDTO> associations;

    public List<DictionaryEntryRequestDTO> getEntries() { return entries; }
    public void setEntries(List<DictionaryEntryRequestDTO> entries) { this.entries = entries; }

    public List<AiAssociationDTO> getAssociations() { return associations; }
    public void setAssociations(List<AiAssociationDTO> associations) { this.associations = associations; }


    public static class AiAssociationDTO {
        private String sourceName;
        private String targetName;
        private String name;
        private String sourceMultiplicity;
        private String targetMultiplicity;
        private String ruleCode;
        private List<DictionaryAttributeRequestDTO> attributes;


        public String getSourceName() { return sourceName; }
        public void setSourceName(String sourceName) { this.sourceName = sourceName; }

        public String getTargetName() { return targetName; }
        public void setTargetName(String targetName) { this.targetName = targetName; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getSourceMultiplicity() { return sourceMultiplicity; }
        public void setSourceMultiplicity(String sourceMultiplicity) { this.sourceMultiplicity = sourceMultiplicity; }

        public String getTargetMultiplicity() { return targetMultiplicity; }
        public void setTargetMultiplicity(String targetMultiplicity) { this.targetMultiplicity = targetMultiplicity; }

        public String getRuleCode() { return ruleCode; }
        public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }

        public List<DictionaryAttributeRequestDTO> getAttributes() { return attributes; }
        public void setAttributes(List<DictionaryAttributeRequestDTO> attributes) { this.attributes = attributes; }
    }
}