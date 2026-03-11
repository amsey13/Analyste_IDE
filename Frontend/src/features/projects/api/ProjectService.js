import apiClient from '../../../api/HttpClient.js';


export const ProjectService = {

    async getProjects(){
        const response = await apiClient.get('/projects');
        return response.data;
    },





    /**
     * 
     * @param {Object} projetData data to send to the backend to create a new projet
     * @returns 
     */
    async createProjet(projetData) {
        if (projetData.project_type === 'audit' || projetData.project_type === 'AUDIT') {
            const response = await apiClient.post('/projects/audit', projetData);
            return response.data;
        } else {
            const response = await apiClient.post('/projects', projetData);
            return response.data;
        }
    },


    async deleteProjet(projetId) {
        
        return await apiClient.delete(`/projects/${projetId}`);
    }

}