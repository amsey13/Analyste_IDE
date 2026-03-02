import apiClient from '../../../services/HttpClient';


export const ProjetService = {

    async getProjet(){
        const response = await apiClient.get('/projets');
        return response.data;
    },



    /**
     * 
     * @param {Object} projetData data to send to the backend to create a new projet
     * @returns 
     */
    async createProjet(projetData) {
        const response = await apiClient.post('/projets', projetData);
        return response.data;
    }

}