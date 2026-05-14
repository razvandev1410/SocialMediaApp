import { Link } from 'react-router-dom';
import type { Post } from '../types';
import { useAuth } from '../context/useAuth';
import VoteButtons from './VoteButtons.tsx';
import TagBadge from './TagBadge';

interface Props {
    post: Post;
    onDelete?: (postId: number) => void;
    onTagClick?: (tagName: string) => void;
}

export default function PostCard({ post, onDelete, onTagClick }: Props) {
    const {user} = useAuth();
    if(!post.author) return null;
    const isAuthor = user?.userId === post.author.userId;
    const isMod = user?.isModerator;

    const formatDate = (dateStr: string) => new Date(dateStr).toLocaleString();

    const statusMap: Record<string, { label: string; className: string }> = {
        JUST_POSTED: {label: 'Just Posted', className: 'bg-primary'},
        FIRST_REACTIONS: {label: 'First Reactions', className: 'bg-warning text-dark'},
        OUTDATED: {label: 'Outdated', className: 'bg-secondary'},
    };

    const status = statusMap[post.status] || { label: 'Unknown', className: 'bg-secondary' };

    return (
        <div className="card mb-3 post-card">
            <div className="card-body">
                {/* Header */}
                <div className="d-flex justify-content-between align-items-start mb-2">
                    <div>
                        <Link to={`/profile/${post.author.userId}`} className="fw-bold text-decoration-none">
                            {post.author.username}
                        </Link>
                        <small className="text-muted ms-2">({post.author.score?.toFixed(1)} pts)</small>
                    </div>
                    <div className="d-flex align-items-center gap-2">
                        <span className={`badge ${status.className}`}>{status.label}</span>
                        <small className="text-muted">{formatDate(post.creationDate)}</small>
                    </div>
                </div>

                {/* Title */}
                <Link to={`/posts/${post.postId}`} className="text-decoration-none text-dark">
                    <h5 className="card-title">{post.title}</h5>
                </Link>

                {/* Text */}
                <p className="card-text" style={{whiteSpace: 'pre-wrap'}}>
                    {post.text.length > 300 ? post.text.substring(0, 300) + '...' : post.text}
                </p>

                {/* Image */}
                {post.imageUrl && (
                    <img src={post.imageUrl} alt={post.title} className="img-fluid rounded mb-3"/>
                )}

                {/* Tags */}
                {post.tags && post.tags.length > 0 && (
                    <div className="mb-3">
                        {post.tags.map(tag => (
                            <TagBadge key={tag.tagId} tag={tag} onClick={onTagClick}/>
                        ))}
                    </div>
                )}

                {/* Footer */}
                <div className="d-flex justify-content-between align-items-center border-top pt-2">
                    <VoteButtons
                        type="post"
                        targetId={post.postId}
                        initialVoteCount={post.voteCount}
                        authorId={post.author.userId}
                    />

                    <div className="d-flex gap-2">
                        {isAuthor && (
                            <Link to={`/posts/${post.postId}/edit`} className="btn btn-outline-primary btn-sm">
                                Edit
                            </Link>
                        )}
                        {(isAuthor || isMod) && onDelete && (
                            <button onClick={() => onDelete(post.postId)} className="btn btn-outline-danger btn-sm">
                                Delete
                            </button>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}