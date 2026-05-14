import api from './api';
import type { Comment, CommentDTO } from '../types';

const commentService = {
    getByPostId: async (postId: number): Promise<Comment[]> => {
        const response = await api.get(`/comments/post/${postId}`);
        return response.data;
    },
    create: async (data: CommentDTO): Promise<Comment> => {
        const response = await api.post('/comments', data);
        return response.data;
    },
    update: async (id: number, data: CommentDTO, requestingUserId: number): Promise<Comment> => {
        const response = await api.put(`/comments/${id}?requestingUserId=${requestingUserId}`, data);
        return response.data;
    },
    delete: async (id: number, requestingUserId: number): Promise<void> => {
        await api.delete(`/comments/${id}?requestingUserId=${requestingUserId}`);
    },
};

export default commentService;