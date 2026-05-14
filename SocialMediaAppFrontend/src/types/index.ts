export interface User {
    userId: number;
    username: string;
    email: string;
    phoneNumber: string;
    score: number;
    isBanned: boolean;
    isModerator: boolean;
}

export interface Tag {
    tagId: number;
    name: string;
}

export interface Post {
    postId: number;
    title: string;
    text: string;
    imageUrl: string | null;
    author: User;
    creationDate: string;
    status: 'JUST_POSTED' | 'FIRST_REACTIONS' | 'OUTDATED';
    voteCount: number;
    tags: Tag[];
}

export interface Comment {
    commentId: number;
    text: string;
    imageUrl: string | null;
    creationDate: string;
    voteCount: number;
    author: User;
    post: Post;
}

export interface Vote {
    voteId: number;
    user: User;
    post: Post | null;
    comment: Comment | null;
    voteType: 'UPVOTE' | 'DOWNVOTE';
}

export interface LoginRequest {
    username: string;
    password: string;
}

export interface RegisterRequest {
    username: string;
    password: string;
    email: string;
    phoneNumber: string;
}

export interface PostDTO {
    title: string;
    text: string;
    imageUrl?: string;
    authorId: number;
    tags?: string[];
}

export interface CommentDTO {
    text: string;
    imageUrl?: string;
    authorId: number;
    postId: number;
}

export interface VoteRequest {
    userId: number;
    postId?: number;
    commentId?: number;
    voteType: 'UPVOTE' | 'DOWNVOTE';
}