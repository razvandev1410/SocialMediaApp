import { useState, useEffect } from 'react';
import { useAuth } from '../context/useAuth';
import postService from '../services/postService';
import tagService from '../services/tagService';
import ImageUpload from './ImageUpload';
import type { Post, Tag } from '../types';
import { useNavigate } from 'react-router-dom';

interface Props {
    existingPost?: Post;
}

export default function PostForm({ existingPost }: Props) {
    const { user } = useAuth();
    const navigate = useNavigate();

    const [title, setTitle] = useState(existingPost?.title || '');
    const [text, setText] = useState(existingPost?.text || '');
    const [imageUrl, setImageUrl] = useState<string | null>(existingPost?.imageUrl || null);
    const [tagInput, setTagInput] = useState('');
    const [selectedTags, setSelectedTags] = useState<string[]>(
        existingPost?.tags.map(t => t.name) || []
    );
    const [allTags, setAllTags] = useState<Tag[]>([]);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        tagService.getAll().then(setAllTags).catch(console.error);
    }, []);

    const addTag = () => {
        const tag = tagInput.trim().toLowerCase();
        if (tag && !selectedTags.includes(tag)) {
            setSelectedTags([...selectedTags, tag]);
        }
        setTagInput('');
    };

    const removeTag = (tagName: string) => {
        setSelectedTags(selectedTags.filter(t => t !== tagName));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!user) return;

        setLoading(true);
        setError('');

        try {
            const dto = {
                title,
                text,
                imageUrl: imageUrl || undefined,
                authorId: user.userId,
                tags: selectedTags.length > 0 ? selectedTags : undefined,
            };

            if(existingPost) {
                await postService.update(existingPost.postId, dto, user.userId);
            }
            else {
                await postService.create(dto);
            }

            navigate('/');
        }
        catch(err: unknown) {
            if(err instanceof Error && 'response' in err) {
                const axiosErr = err as { response?: { data?: { message?: string } } };
                setError(axiosErr.response?.data?.message || 'Failed to save post');
            }
            else {
                setError('Failed to save post');
            }
        }
        finally {
            setLoading(false);
        }
    };

    return (
        <div className="card">
            <div className="card-body">
                <h4 className="card-title mb-3">{existingPost ? 'Edit Post' : 'Create New Post'}</h4>

                {error && <div className="alert alert-danger">{error}</div>}

                <form onSubmit={handleSubmit}>
                    <div className="mb-3">
                        <label htmlFor="title" className="form-label">Title</label>
                        <input
                            id="title" type="text" className="form-control"
                            value={title} onChange={(e) => setTitle(e.target.value)}
                            required placeholder="Post title"
                        />
                    </div>

                    <div className="mb-3">
                        <label htmlFor="text" className="form-label">Content</label>
                        <textarea
                            id="text" className="form-control"
                            value={text} onChange={(e) => setText(e.target.value)}
                            required rows={6} placeholder="What's on your mind?"
                        />
                    </div>

                    <ImageUpload
                        currentImageUrl={existingPost?.imageUrl}
                        onImageUploaded={(url) => setImageUrl(url)}
                    />

                    <div className="mb-3">
                        <label className="form-label">Tags</label>
                        <div className="input-group mb-2">
                            <input
                                type="text" className="form-control"
                                value={tagInput} onChange={(e) => setTagInput(e.target.value)}
                                placeholder="Add a tag..."
                                onKeyDown={(e) => { if (e.key === 'Enter') { e.preventDefault(); addTag(); } }}
                            />
                            <button className="btn btn-outline-secondary" type="button" onClick={addTag}>Add</button>
                        </div>

                        {allTags.length > 0 && (
                            <div className="mb-2">
                                <small className="text-muted me-2">Existing:</small>
                                {allTags.map(tag => (
                                    <span
                                        key={tag.tagId}
                                        className={`badge me-1 ${selectedTags.includes(tag.name) ? 'bg-primary' : 'bg-light text-dark border'}`}
                                        style={{ cursor: 'pointer' }}
                                        onClick={() => {
                                            if (!selectedTags.includes(tag.name)) setSelectedTags([...selectedTags, tag.name]);
                                        }}
                                    >
                                        #{tag.name}
                                    </span>
                                ))}
                            </div>
                        )}

                        {selectedTags.length > 0 && (
                            <div>
                                {selectedTags.map(tag => (
                                    <span key={tag} className="badge bg-info me-1">
                                        #{tag}
                                        <button
                                            type="button" className="btn-close btn-close-white ms-1"
                                            style={{ fontSize: 8 }} onClick={() => removeTag(tag)}
                                        />
                                    </span>
                                ))}
                            </div>
                        )}
                    </div>

                    <button type="submit" className="btn btn-primary w-100" disabled={loading}>
                        {loading ? 'Saving...' : existingPost ? 'Update Post' : 'Create Post'}
                    </button>
                </form>
            </div>
        </div>
    );
}