import apiClient from '../../../api/HttpClient.js';

const ENDPOINTS_BY_TYPE = {
    'audit': '/projects/audit',
    'accompagnement': '/projects/support',
};

const DEFAULT_ENDPOINT = '/projects';
export const ProjectService = {

    async getProjects(){
        const response = await apiClient.get('/projects');
        return response.data;
    },


    async getProjectById(projectId) {
        const response = await apiClient.get(`/projects/${projectId}`);
        return response.data;
    },


    /**
     * 
     * @param {Object} projectData data to send to the backend to create a new project
     * @returns 
     */
    async createProject(projectData) {
        const type = projectData.project_type?.toLowerCase();
        const endpoint = ENDPOINTS_BY_TYPE[type] || DEFAULT_ENDPOINT;
        const response = await apiClient.post(endpoint, projectData);
        return response.data;
    },


    async deleteProjet(idProject) {

        if (!idProject) {
            console.error("projectId est undefined !")
            return
        }
        
        return await apiClient.delete(`/projects/${idProject}`);
    }

}