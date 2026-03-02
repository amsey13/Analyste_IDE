import axios from 'axios';

// Abstraction réseau : on centralise l'URL de base et la sécurité
const apiClient = axios.create({
    baseURL: 'http://localhost:8080/api',
    // L'adresse de ton Spring Boot
    timeout: 5000,
    // LA LIGNE CRITIQUE POUR L'AUTHENTIFICATION OIDC :
    // Cela force le navigateur à envoyer le cookie JSESSIONID à chaque requête API
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    }
});

export default apiClient;