import apiClient from '../../../api/HttpClient.js';

const BASE_URL = '/support';

export const SupportFeatureService = {

    // --- Gestion du BPMN ---

    async saveBpmnDiagram(projectId, bpmnXml) {
        // Envoie le XML en texte brut (ou JSON selon comment tu as configuré ton Controller)
        const response = await apiClient.put(`${BASE_URL}/projects/${projectId}/bpmn`, bpmnXml, {
            headers: { 'Content-Type': 'text/plain' } // Assure-toi que le Content-Type correspond
        });
        return response.data;
    },

    // --- Gestion des Acteurs ---

    async addActor(projectId, actorData) {
        // actorData doit correspondre à ActorDTO { name: "..." }
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

    // --- Gestion des User Stories ---

    async addUserStory(projectId, actorId, userStoryData) {
        // userStoryData doit correspondre à UserStoryDTO { identifier: "...", description: "..." }
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

    // --- Gestion du Dictionnaire de Données ---

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
};