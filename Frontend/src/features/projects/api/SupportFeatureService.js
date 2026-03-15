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

    async getBpmnSkeleton(projectId) {
        const response = await apiClient.get(`${BASE_URL}/projects/${projectId}/bpmn-skeleton`);
        return response.data;
    },
};