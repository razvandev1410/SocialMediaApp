import api from './api';
import { LoginRequest, RegisterRequest, User } from '../types';

const authService = {
    login: async (data: LoginRequest): Promise<User> => {
        const response = await api.post('/auth/login', data);
        return response.data;
    },
    register: async (data: RegisterRequest): Promise<User> => {
        const response = await api.post('/auth/register', data);
        return response.data;
    },
};

export default authService;