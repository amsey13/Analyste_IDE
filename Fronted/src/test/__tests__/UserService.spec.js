import { describe, it, expect, vi, beforeEach } from 'vitest';
import { UserService } from '../UserService';
import apiClient from '../HttpClient';

// On simule (mock) le client HTTP pour isoler le test de l'environnement physique
vi.mock('../HttpClient');

describe('UserService', () => {
    beforeEach(() => {
        vi.clearAllMocks(); //  on réinitialise entre chaque test
    });

    it('doit récupérer le profil utilisateur depuis /users/me', async () => {
        // Arrange : On prépare la fausse réponse du serveur
        const mockData = { email: 'test@miage.fr', fullName: 'Mamady' };
        apiClient.get.mockResolvedValue({ data: mockData });

        // Act : On appelle notre service
        const result = await UserService.getCurrentUser();

        // Assert : On vérifie que les principes sont respectés
        expect(apiClient.get).toHaveBeenCalledWith('/users/me'); // A-t-on tapé la bonne route ?
        expect(result).toEqual(mockData); // A-t-on reçu la bonne donnée ?
    });
});