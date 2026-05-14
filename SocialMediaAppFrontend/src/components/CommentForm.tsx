import { useState, useRef } from 'react';
import { useAuth } from '../context/useAuth';
import commentService from '../services/commentService';
import fileService from '../services/fileService';
import type { Comment } from '../types';

interface Props {
    postId: number;
    onCommentAdded: (comment: Comment) => void;
    disabled?: boolean;
}

export default function CommentForm({ postId, onCommentAdded, disabled }: Props) {
    const { user } = useAuth();
    const [text, setText] = useState('');
    const [imageFile, setImageFile] = useState<File | null>(null);
    const [imagePreview, setImagePreview] = useState<string | null>(null);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const fileInputRef = useRef<HTMLInputElement>(null);

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if(!file) return;

        if(!file.type.startsWith('image/')) {
            setError('Please select an image file');
            return;
        }

        setImageFile(file);
        const reader = new FileReader();
        reader.onload = () => setImagePreview(reader.result as string);
        reader.readAsDataURL(file);
    };

    const removeImage = () => {
        setImageFile(null);
        setImagePreview(null);
        if (fileInputRef.current) fileInputRef.current.value = '';
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if(!user || !text.trim()) return;

        setLoading(true);
        setError('');

        try {
            let imageUrl: string | undefined;

            if (imageFile) {
                imageUrl = await fileService.upload(imageFile);
            }

            const comment = await commentService.create({
                text: text.trim(),
                authorId: user.userId,
                postId,
                imageUrl,
            });

            setText('');
            removeImage();
            onCommentAdded(comment);
        }
        catch (err: unknown) {
            if(err instanceof Error && 'response' in err) {
                const axiosErr = err as { response?: { data?: { message?: string } } };
                setError(axiosErr.response?.data?.message || 'Failed to add comment');
            }
            else {
                setError('Failed to add comment');
            }
        }
        finally {
            setLoading(false);
        }
    };

    if (disabled) {
        return (
            <div className="alert alert-secondary text-center">
                Comments are closed for this post.
            </div>
        );
    }

    return (
        <form onSubmit={handleSubmit} className="mb-4">
            <textarea
                className="form-control mb-2"
                value={text}
                onChange={(e) => setText(e.target.value)}
                placeholder="Write a comment..."
                rows={3}
                required
            />

            {imagePreview && (
                <div className="position-relative mb-2" style={{ display: 'inline-block' }}>
                    <img src={imagePreview} alt="Preview" className="rounded" style={{ maxHeight: 150 }} />
                    <button
                        type="button"
                        className="btn btn-sm btn-danger position-absolute top-0 end-0"
                        onClick={removeImage}
                    >✕</button>
                </div>
            )}

            <div className="d-flex gap-2 align-items-center">
                <input
                    ref={fileInputRef}
                    type="file"
                    className="form-control form-control-sm"
                    accept="image/*"
                    onChange={handleFileChange}
                    style={{ maxWidth: 250 }}
                />
                <button className="btn btn-primary btn-sm" type="submit" disabled={loading || !text.trim()}>
                    {loading ? 'Posting...' : 'Post Comment'}
                </button>
            </div>

            {error && <div className="alert alert-danger py-1 small mt-2">{error}</div>}
        </form>
    );
}