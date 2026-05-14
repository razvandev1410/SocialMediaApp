import api from './api';
import type { Post, PostDTO } from '../types';

const postService = {
    getAll: async (): Promise<Post[]> => {
        const response = await api.get('/posts');
        return response.data;
    },
    getById: async (id: number): Promise<Post> => {
        const response = await api.get(`/posts/${id}`);
        return response.data;
    },
    getByUserId: async (userId: number): Promise<Post[]> => {
        const response = await api.get(`/posts/user/${userId}`);
        return response.data;
    },
    create: async (data: PostDTO): Promise<Post> => {
        const response = await api.post('/posts', data);
        return response.data;
    },
    update: async (id: number, data: PostDTO, requestingUserId: number): Promise<Post> => {
        const response = await api.put(`/posts/${id}?requestingUserId=${requestingUserId}`, data);
        return response.data;
    },
    delete: async (id: number, requestingUserId: number): Promise<void> => {
        await api.delete(`/posts/${id}?requestingUserId=${requestingUserId}`);
    },
    markOutdated: async (id: number, requestingUserId: number): Promise<Post> => {
        const response = await api.patch(`/posts/${id}/outdated?requestingUserId=${requestingUserId}`);
        return response.data;
    },
    searchByTitle: async (keyword: string): Promise<Post[]> => {
        const response = await api.get(`/posts/search?keyword=${keyword}`);
        return response.data;
    },
    filterByTag: async (tag: string, userId?: number): Promise<Post[]> => {
        let url = `/posts/filter/tag?tag=${tag}`;
        if (userId) url += `&userId=${userId}`;
        const response = await api.get(url);
        return response.data;
    },
};

export default postService;