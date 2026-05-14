import { useState } from 'react';
import type { Comment } from '../types';
import { useAuth } from '../context/useAuth';
import VoteButtons from './VoteButtons';
import commentService from '../services/commentService';

interface Props {
    comments: Comment[];
    onCommentDeleted: (commentId: number) => void;
}

export default function CommentList({ comments, onCommentDeleted }: Props) {
    const { user } = useAuth();
    const [editingId, setEditingId] = useState<number | null>(null);
    const [editText, setEditText] = useState('');

    const formatDate = (dateStr: string) => new Date(dateStr).toLocaleString();

    const handleEdit = (comment: Comment) => {
        setEditingId(comment.commentId);
        setEditText(comment.text);
    };

    const handleSaveEdit = async (comment: Comment) => {
        if (!user) return;
        try {
            await commentService.update(comment.commentId, {
                text: editText,
                authorId: comment.author.userId,
                postId: comment.post.postId,
            }, user.userId);
            comment.text = editText;
            setEditingId(null);
        } catch (err) {
            console.error('Failed to update comment', err);
        }
    };

    const handleDelete = async (commentId: number) => {
        if (!user || !window.confirm('Delete this comment?')) return;
        try {
            await commentService.delete(commentId, user.userId);
            onCommentDeleted(commentId);
        } catch (err) {
            console.error('Failed to delete comment', err);
        }
    };

    if (comments.length === 0) {
        return <p className="text-muted text-center py-3">No comments yet. Be the first!</p>;
    }

    return (
        <div className="d-flex flex-column gap-2">
            {comments.map(comment => {
                const isAuthor = user?.userId === comment.author.userId;
                const isMod = user?.isModerator;

                return (
                    <div key={comment.commentId} className="card bg-light">
                        <div className="card-body py-2 px-3">
                            {/* Header */}
                            <div className="d-flex justify-content-between mb-1">
                <span>
                  <strong className="small">{comment.author.username}</strong>
                  <small className="text-muted ms-1">({comment.author.score?.toFixed(1)} pts)</small>
                </span>
                                <small className="text-muted">{formatDate(comment.creationDate)}</small>
                            </div>

                            {/* Content */}
                            {editingId === comment.commentId ? (
                                <div className="mb-2">
                  <textarea
                      className="form-control form-control-sm mb-1"
                      value={editText}
                      onChange={(e) => setEditText(e.target.value)}
                      rows={2}
                  />
                                    <div className="d-flex gap-1">
                                        <button className="btn btn-primary btn-sm" onClick={() => handleSaveEdit(comment)}>Save</button>
                                        <button className="btn btn-secondary btn-sm" onClick={() => setEditingId(null)}>Cancel</button>
                                    </div>
                                </div>
                            ) : (
                                <p className="small mb-2" style={{ whiteSpace: 'pre-wrap' }}>{comment.text}</p>
                            )}

                            {comment.imageUrl && (
                                <img src={comment.imageUrl} alt="comment" className="img-fluid rounded mb-2" style={{ maxHeight: 200 }} />
                            )}

                            {/* Footer */}
                            <div className="d-flex justify-content-between align-items-center">
                                <VoteButtons
                                    type="comment"
                                    targetId={comment.commentId}
                                    initialVoteCount={comment.voteCount}
                                    authorId={comment.author.userId}
                                />

                                {(isAuthor || isMod) && editingId !== comment.commentId && (
                                    <div className="d-flex gap-1">
                                        {isAuthor && (
                                            <button className="btn btn-outline-primary btn-sm" onClick={() => handleEdit(comment)} style={{ fontSize: 12 }}>
                                                Edit
                                            </button>
                                        )}
                                        <button className="btn btn-outline-danger btn-sm" onClick={() => handleDelete(comment.commentId)} style={{ fontSize: 12 }}>
                                            Delete
                                        </button>
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>
                );
            })}
        </div>
    );
}