import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/useAuth';
import postService from '../services/postService';
import commentService from '../services/commentService';
import VoteButtons from '../components/VoteButtons';
import TagBadge from '../components/TagBadge';
import CommentForm from '../components/CommentForm';
import CommentList from '../components/CommentList';
import type { Post, Comment } from '../types';

export default function PostDetailPage() {
    const { id } = useParams<{ id: string }>();
    const { user } = useAuth();
    const navigate = useNavigate();

    const [post, setPost] = useState<Post | null>(null);
    const [comments, setComments] = useState<Comment[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (!id) return;
        Promise.all([
            postService.getById(Number(id)),
            commentService.getByPostId(Number(id)),
        ]).then(([p, c]) => { setPost(p); setComments(c); })
            .catch(console.error)
            .finally(() => setLoading(false));
    }, [id]);

    const handleCommentAdded = (comment: Comment) => {
        setComments([comment, ...comments]);
        if (post?.status === 'JUST_POSTED') setPost({ ...post, status: 'FIRST_REACTIONS' });
    };

    const handleCommentDeleted = (commentId: number) => {
        setComments(comments.filter(c => c.commentId !== commentId));
    };

    const handleMarkOutdated = async () => {
        if (!post || !user) return;
        try { setPost(await postService.markOutdated(post.postId, user.userId)); }
        catch (err) { console.error(err); }
    };

    const handleDelete = async () => {
        if (!post || !user || !window.confirm('Delete this post?')) return;
        try { await postService.delete(post.postId, user.userId); navigate('/'); }
        catch (err) { console.error(err); }
    };

    if (loading) return <div className="text-center py-5"><div className="spinner-border text-primary" /></div>;
    if (!post) return <div className="alert alert-warning">Post not found.</div>;

    const isAuthor = user?.userId === post.author.userId;
    const isMod = user?.isModerator;

    const statusMap: Record<string, { label: string; cls: string }> = {
        JUST_POSTED: { label: 'Just Posted', cls: 'bg-primary' },
        FIRST_REACTIONS: { label: 'First Reactions', cls: 'bg-warning text-dark' },
        OUTDATED: { label: 'Outdated', cls: 'bg-secondary' },
    };

    return (
        <div>
            {/* Post */}
            <div className="card mb-4">
                <div className="card-body">
                    <div className="d-flex justify-content-between mb-2">
                        <div>
                            <strong>{post.author.username}</strong>
                            <small className="text-muted ms-2">({post.author.score?.toFixed(1)} pts)</small>
                        </div>
                        <div className="d-flex align-items-center gap-2">
                            <span className={`badge ${(statusMap[post.status] || { cls: 'bg-secondary', label: post.status || 'Unknown' }).cls}`}>{(statusMap[post.status] || { cls: 'bg-secondary', label: post.status || 'Unknown' }).label}</span>
                            <small className="text-muted">{new Date(post.creationDate).toLocaleString()}</small>
                        </div>
                    </div>

                    <h3>{post.title}</h3>
                    <p style={{ whiteSpace: 'pre-wrap', lineHeight: 1.7 }}>{post.text}</p>

                    {post.imageUrl && <img src={post.imageUrl} alt={post.title} className="img-fluid rounded mb-3" />}

                    {post.tags?.length > 0 && (
                        <div className="mb-3">
                            {post.tags.map(tag => <TagBadge key={tag.tagId} tag={tag} />)}
                        </div>
                    )}

                    <div className="d-flex justify-content-between align-items-center border-top pt-3">
                        <VoteButtons type="post" targetId={post.postId}
                                     initialVoteCount={post.voteCount} authorId={post.author.userId} />

                        {(isAuthor || isMod) && (
                            <div className="d-flex gap-2">
                                {isAuthor && post.status !== 'OUTDATED' && (
                                    <button className="btn btn-outline-secondary btn-sm" onClick={handleMarkOutdated}>
                                        Close Comments
                                    </button>
                                )}
                                {isAuthor && (
                                    <button className="btn btn-outline-primary btn-sm"
                                            onClick={() => navigate(`/posts/${post.postId}/edit`)}>Edit</button>
                                )}
                                <button className="btn btn-outline-danger btn-sm" onClick={handleDelete}>Delete</button>
                            </div>
                        )}
                    </div>
                </div>
            </div>

            {/* Comments */}
            <div className="card">
                <div className="card-body">
                    <h5 className="mb-3">Comments ({comments.length})</h5>
                    {user && (
                        <CommentForm postId={post.postId} onCommentAdded={handleCommentAdded}
                                     disabled={post.status === 'OUTDATED'} />
                    )}
                    <CommentList comments={comments} onCommentDeleted={handleCommentDeleted} />
                </div>
            </div>
        </div>
    );
}