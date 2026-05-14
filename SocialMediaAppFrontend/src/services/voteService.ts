import api from './api';
import type { VoteRequest, Vote } from '../types';

const voteService = {
    voteOnPost: async (data: VoteRequest): Promise<Vote> => {
        const response = await api.post('/votes/post', data);
        return response.data;
    },
    voteOnComment: async (data: VoteRequest): Promise<Vote> => {
        const response = await api.post('/votes/comment', data);
        return response.data;
    },
};

export default voteService;