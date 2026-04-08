package com.example.backend.modules.projects.acc.dto;

import com.example.backend.modules.projects.core.dto.BaseProjectRequestDTO;

import java.util.ArrayList;
import java.util.List;

public class SupportProjectRequestDTO extends BaseProjectRequestDTO {

    private String status;
    private String bpmnXml;
    private List<DictionaryEntryRequestDTO> dictionaryEntries = new ArrayList<>();

    public List<DictionaryEntryRequestDTO> getDictionaryEntries() {
        return dictionaryEntries;
    }

    public void setDictionaryEntries(List<DictionaryEntryRequestDTO> dictionaryEntries) {
        this.dictionaryEntries = dictionaryEntries;
    }

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



}
