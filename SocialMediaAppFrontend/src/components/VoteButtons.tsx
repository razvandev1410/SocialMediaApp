import {useState} from 'react';
import {useAuth} from '../context/useAuth';
import voteService from '../services/voteService.ts';

interface Props {
    type: 'post' | 'comment';
    targetId: number;
    initialVoteCount: number;
    authorId: number;
}

export default function VoteButtons({ type, targetId, initialVoteCount, authorId }: Props) {
    const { user } = useAuth();
    const [voteCount, setVoteCount] = useState(initialVoteCount);
    const [voted, setVoted] = useState<'UPVOTE' | 'DOWNVOTE' | null>(null);
    const [error, setError] = useState('');

    const isOwnContent = user?.userId === authorId;

    const handleVote = async (voteType: 'UPVOTE' | 'DOWNVOTE') => {
            if(!user || isOwnContent || voted) return;
            setError('');

            try {
                const request = {
                    userId: user.userId,
                    voteType,
                    ...(type === 'post' ? {postId: targetId} : {commentId: targetId}),
                };

                if(type === 'post') {
                    await voteService.voteOnPost(request);
                }
                else {
                    await voteService.voteOnComment(request);
                }

                setVoteCount(prev => voteType === 'UPVOTE' ? prev + 1 : prev - 1);
                setVoted(voteType);
            }
            catch (err: unknown) {
                const errorResponse = err as { response?: {data?: {message?: string}}};
                setError(errorResponse.response?.data?.message || 'Vote failed');
            }
    };

    return (
        <div className="d-flex align-items-center gap-1">
            <button
                className={`btn btn-sm ${voted === 'UPVOTE' ? 'btn-primary' : 'btn-outline-secondary'}`}
                onClick={() => handleVote('UPVOTE')}
                disabled={!user || isOwnContent || voted !== null}
                title={isOwnContent ? "Can't vote on own content" : 'Upvote'}
            >
                ▲
            </button>
            <span className={`fw-bold mx-1 ${voteCount > 0 ? 'text-primary' : voteCount < 0 ? 'text-danger' : 'text-muted'}`}>
        {voteCount}
      </span>
            <button
                className={`btn btn-sm ${voted === 'DOWNVOTE' ? 'btn-danger' : 'btn-outline-secondary'}`}
                onClick={() => handleVote('DOWNVOTE')}
                disabled={!user || isOwnContent || voted !== null}
                title={isOwnContent ? "Can't vote on own content" : 'Downvote'}
            >
                ▼
            </button>
            {error && <small className="text-danger ms-2">{error}</small>}
        </div>
    );
}