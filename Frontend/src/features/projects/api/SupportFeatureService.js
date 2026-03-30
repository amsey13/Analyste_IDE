import apiClient from '../../../api/HttpClient.js';

const BASE_URL = '/support';

export const SupportFeatureService = {



    async saveBpmnDiagram(projectId, bpmnXml) {
        const response = await apiClient.put(`${BASE_URL}/projects/${projectId}/bpmn`, bpmnXml, {
            headers: { 'Content-Type': 'text/plain' }
        });
        return response.data;
    },



    async addActor(projectId, actorData) {

        const response = await apiClient.post(`${BASE_URL}/projects/${projectId}/actors`, actorData);
        return response.data;
    },

    async updateActor(actorId, actorData) {
        const response = await apiClient.put(`${BASE_URL}/actors/${actorId}`, actorData);
        return response.data;
    },

    async deleteActor(actorId) {
        const response = await apiClient.delete(`${BASE_URL}/actors/${actorId}`);
        return response.data;
    },



    async addUserStory(projectId, actorId, userStoryData) {
        const response = await apiClient.post(`${BASE_URL}/projects/${projectId}/actors/${actorId}/user-stories`, userStoryData);
        return response.data;
    },

    async updateUserStory(userStoryId, userStoryData) {
        const response = await apiClient.put(`${BASE_URL}/user-stories/${userStoryId}`, userStoryData);
        return response.data;
    },

    async deleteUserStory(userStoryId) {
        const response = await apiClient.delete(`${BASE_URL}/user-stories/${userStoryId}`);
        return response.data;
    },


    async addDictionaryEntry(projectId, entryData) {
        const response = await apiClient.post(`/support/projects/${projectId}/dictionary-entries`, entryData);
        return response.data;
    },

    async updateDictionaryEntry(entryId, entryData) {
        const response = await apiClient.put(`/support/dictionary-entries/${entryId}`, entryData);
        return response.data;
    },

    async deleteDictionaryEntry(entryId) {
        await apiClient.delete(`/support/dictionary-entries/${entryId}`);
    },

    async addDictionaryAttribute(entryId, attrData) {
        const response = await apiClient.post(`/support/dictionary-entries/${entryId}/attributes`, attrData);
        return response.data;
    },

    async updateDictionaryAttribute(attrId, attrData) {
        const response = await apiClient.put(`/support/dictionary-attributes/${attrId}`, attrData);
        return response.data;
    },

    async deleteDictionaryAttribute(attrId) {
        await apiClient.delete(`/support/dictionary-attributes/${attrId}`);
    },

    async suggestDictionary(projectId) {
        const response = await apiClient.get(`/support/projects/${projectId}/dictionary-suggestions`);
        return response.data;
    },

    async getAssociations(projectId) {
        const response = await apiClient.get(`/support/projects/${projectId}/associations`);
        return response.data;
    },

    async addAssociation(projectId, association) {
        const response = await apiClient.post(`/support/projects/${projectId}/associations`, association);
        return response.data;
    },

    async updateAssociation(associationId, association) {
        const response = await apiClient.put(`/support/associations/${associationId}`, association);
        return response.data;
    },

    async deleteAssociation(associationId) {
        await apiClient.delete(`/support/associations/${associationId}`);
    },



    async getBusinessRules(projectId) {
        const response = await apiClient.get(`${BASE_URL}/projects/${projectId}/business-rules`);
        return response.data;
    },

    async addBusinessRule(projectId, ruleData) {
        const response = await apiClient.post(`${BASE_URL}/projects/${projectId}/business-rules`, ruleData);
        return response.data;
    },

    async deleteBusinessRule(ruleId) {
        await apiClient.delete(`${BASE_URL}/business-rules/${ruleId}`);
    },

    async generateMcdWithAi(projectId) {
        const response = await apiClient.post(`${BASE_URL}/projects/${projectId}/generate-mcd`);
        return response.data;
    },

    async generateAudit(projectId) {
        const response = await apiClient.post(`${BASE_URL}/projects/${projectId}/audit`);
        return response.data;
    },

    async getAudit(projectId) {
        const response = await apiClient.get(`${BASE_URL}/projects/${projectId}/audit`);
        return response.data;
    },

    async exportMcdFile(projectId) {
        const response = await apiClient.get(`${BASE_URL}/projects/${projectId}/export/mcd`, {
            responseType: 'arraybuffer',
            headers: {
                'Accept': 'application/octet-stream'
            }
        });
        return response.data;
    },
};