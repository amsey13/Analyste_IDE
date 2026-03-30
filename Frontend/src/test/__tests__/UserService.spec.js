import { describe, it, expect, vi, beforeEach } from 'vitest';
import { UserService } from '../UserService';
import apiClient from '../../api/HttpClient.js';

vi.mock('../HttpClient');

describe('UserService', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('doit récupérer le profil utilisateur depuis /users/me', async () => {

        const mockData = { email: 'test@miage.fr', fullName: 'Mamady' };
        apiClient.get.mockResolvedValue({ data: mockData });
        const result = await UserService.getCurrentUser();
        expect(apiClient.get).toHaveBeenCalledWith('/users/me');
        expect(result).toEqual(mockData);
    });
});