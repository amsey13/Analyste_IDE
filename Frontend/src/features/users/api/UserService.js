import apiClient from '../../../api/HttpClient.js';

// Ce service a l'unique responsabilité de gérer les données Utilisateur
export const UserService = {

    /**
     * Récupère le profil de l'utilisateur actuellement connecté
     * @returns {Promise<Object>} Les données de l'utilisateur
     */
    async getCurrentUser() {
        try {
            console.log('[UserService] Tentative de récupération du profil sur /api/users/me...');
            const response = await apiClient.get('/users/me');
            console.log('[UserService] Succès ! Données reçues :', response.data);
            return response.data;
        } catch (error) {
            console.error('[UserService] Le serveur a renvoyé une erreur :', error.response.status);
            console.error('[UserService] Détails du message serveur :', error.response.data);

            if (error.response.status === 401) {
                console.warn('[UserService] Raison : Utilisateur non authentifié (Session expirée ou absente).');
            } else if (error.response.status === 403) {
                console.warn('[UserService] Raison : Accès interdit (Problème de CSRF ou de droits).');
            } else if (error.response.status === 500) {
                console.error('[UserService] Raison : Crash interne du Backend (Vérifier les logs Java).');
            } else if (error.request) {
                // La requête a été faite mais aucune réponse n'a été reçue (ex: ECONNREFUSED)
                console.error('[UserService] Aucune réponse du serveur. Le Backend est-il lancé sur le port 8080 ?');
                console.error('[UserService] Détails techniques de la requête :', error.request);
            } else {
                // Erreur lors de la configuration de la requête
                console.error('[UserService] Erreur de configuration Axios :', error.message);
            }
            throw error;
        }
    }


}