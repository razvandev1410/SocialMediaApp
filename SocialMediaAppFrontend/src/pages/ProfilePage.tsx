import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import userService from '../services/userService';
import postService from '../services/postService';
import PostCard from '../components/PostCard';
import type { User, Post } from '../types';
import { useAuth } from '../context/useAuth';

export default function ProfilePage() {
    const { id } = useParams<{ id: string }>();
    const { user: currentUser } = useAuth();
    const [profileUser, setProfileUser] = useState<User | null>(null);
    const [posts, setPosts] = useState<Post[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (!id) return;
        Promise.all([
            userService.getById(Number(id)),
            postService.getByUserId(Number(id)),
        ]).then(([u, p]) => { setProfileUser(u); setPosts(p); })
            .catch(console.error)
            .finally(() => setLoading(false));
    }, [id]);

    const handleDelete = async (postId: number) => {
        if (!currentUser || !window.confirm('Delete this post?')) return;
        try {
            await postService.delete(postId, currentUser.userId);
            setPosts(posts.filter(p => p.postId !== postId));
        } catch (err) { console.error(err); }
    };

    if (loading) return <div className="text-center py-5"><div className="spinner-border text-primary" /></div>;
    if (!profileUser) return <div className="alert alert-warning">User not found.</div>;

    return (
        <div>
            {/* Profile card */}
            <div className="card mb-4">
                <div className="card-body d-flex align-items-center gap-4">
                    <div className="profile-avatar">
                        {profileUser.username.charAt(0).toUpperCase()}
                    </div>
                    <div>
                        <h4 className="mb-1">
                            {profileUser.username}
                            {profileUser.isModerator && (
                                <span className="badge bg-primary ms-2" style={{ fontSize: 12 }}>Moderator</span>
                            )}
                            {profileUser.isBanned && (
                                <span className="badge bg-danger ms-2" style={{ fontSize: 12 }}>Banned</span>
                            )}
                        </h4>
                        <p className="text-muted mb-0">{profileUser.email}</p>
                        <p className="text-primary fw-bold mb-0">Score: {profileUser.score?.toFixed(1)} points</p>
                    </div>
                </div>
            </div>

            {/* Posts */}
            <h5 className="mb-3">Posts by {profileUser.username} ({posts.length})</h5>
            {posts.map(post => (
                <PostCard key={post.postId} post={post} onDelete={handleDelete} />
            ))}
            {posts.length === 0 && <p className="text-muted text-center">No posts yet.</p>}
        </div>
    );
}