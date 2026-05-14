import api from './api';
import type { User } from '../types';

const userService = {
    getAll: async (): Promise<User[]> => {
        const response = await api.get('/users');
        return response.data;
    },
    getById: async (id: number): Promise<User> => {
        const response = await api.get(`/users/${id}`);
        return response.data;
    },
    ban: async (userId: number, moderatorId: number): Promise<User> => {
        const response = await api.patch(`/users/${userId}/ban?moderatorId=${moderatorId}`);
        return response.data;
    },
    unban: async (userId: number, moderatorId: number): Promise<User> => {
        const response = await api.patch(`/users/${userId}/unban?moderatorId=${moderatorId}`);
        return response.data;
    },
    delete: async (userId: number): Promise<void> => {
        await api.delete(`/users/${userId}`);
    },
};

export default userService;