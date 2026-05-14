import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import postService from '../services/postService';
import PostForm from '../components/PostForm';
import type { Post } from '../types';

export default function EditPostPage() {
    const { id } = useParams<{ id: string }>();
    const [post, setPost] = useState<Post | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (!id) return;
        postService.getById(Number(id))
            .then(setPost)
            .catch(console.error)
            .finally(() => setLoading(false));
    }, [id]);

    if (loading) return <div className="text-center py-5"><div className="spinner-border text-primary" /></div>;
    if (!post) return <div className="alert alert-warning">Post not found.</div>;

    return <PostForm existingPost={post} />;
}