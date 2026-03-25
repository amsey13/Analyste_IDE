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
    },

    async downloadPdf(reportId) {
        try {
            const response = await apiClient.get(`/audit/${reportId}/export/pdf`, {
                responseType: 'blob'
            });

            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement("a");
            link.href = url;
            link.setAttribute("download", `Rapport_Audit.pdf`);
            document.body.appendChild(link);
            link.click();

            link.remove();
            window.URL.revokeObjectURL(url);

        } catch (error) {
            console.error(error);
            throw error;
        }
    }







}