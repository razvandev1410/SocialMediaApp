import api from './api';

const fileService = {
    upload: async (file: File): Promise<string> => {
        const formData = new FormData();
        formData.append('file', file);

        const response = await api.post('/files/upload', formData, {
            headers: { 'Content-Type': 'multipart/form-data' },
        });

        return `http://localhost:8080${response.data.url}`;
    },
};

export default fileService;