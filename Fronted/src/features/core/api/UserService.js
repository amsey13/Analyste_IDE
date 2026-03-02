import apiClient from '../../../services/HttpClient';

// Ce service a l'unique responsabilité de gérer les données Utilisateur
export const UserService = {

    /**
     * Récupère le profil de l'utilisateur actuellement connecté
     * @returns {Promise<Object>} Les données de l'utilisateur
     */
    async getCurrentUser() {
        try {
            const response = await apiClient.get('/users/me');
            return response.data;
        } catch (error) {
            //  On loggue l'erreur techniquement, mais on peut la propager
            // pour que l'interface graphique affiche un message (ex: "Non connecté")
            console.error('[UserService] Erreur lors de la récupération du profil', error);
            throw error;
        }
    }
};