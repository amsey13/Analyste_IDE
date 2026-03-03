import apiClient from '../../../test/HttpClient';


export const ProjetService = {

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
        const response = await apiClient.post('/projects', projetData);
        return response.data;
    }

}