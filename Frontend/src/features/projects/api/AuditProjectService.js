import apiClient from "../../../api/HttpClient.js";


export default{


    /**
     * @param {String} idProject
     * @param {File} bpmn
     * @param {File} mcd
     * @param {File} mfc
     */
    async analyzeProject(idProject,bpmn,mcd,mfc){
        const formData = new FormData();

        if (bpmn) formData.append('bpmn', bpmn);
        if (mcd) formData.append('mcd', mcd);
        if (mfc) formData.append('mfc', mfc);

        try {
            const response = await apiClient.post(`/audit/${idProject}/analyze`, formData);
            return response.data;
        } catch (error) {
            console.error("Erreur lors de l'appel à l'Audit API", error);
            throw error;
        }


    }





}