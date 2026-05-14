import { useState, useEffect } from 'react';
import { useAuth } from '../context/useAuth';
import postService from '../services/postService';
import PostCard from '../components/PostCard';
import SearchBar from '../components/SearchBar';
import type { Post } from '../types';

export default function PostListPage() {
    const { user } = useAuth();
    const [posts, setPosts] = useState<Post[]>([]);
    const [loading, setLoading] = useState(true);
    const [filterLabel, setFilterLabel] = useState('All Posts');

    useEffect(() => {
        let cancelled = false;
        postService.getAll()
            .then(data => {
                if (!cancelled) {
                    setPosts(data);
                    setFilterLabel('All Posts');
                }
            })
            .catch(err => console.error('Failed to load posts', err))
            .finally(() => { if (!cancelled) setLoading(false); });
        return () => { cancelled = true; };
    }, []);

    const loadPosts = async () => {
        setLoading(true);
        try {
            const data = await postService.getAll();
            setPosts(data);
            setFilterLabel('All Posts');
        } catch (err) {
            console.error('Failed to load posts', err);
        } finally {
            setLoading(false);
        }
    };

    const handleSearch = async (keyword: string) => {
        setLoading(true);
        try {
            const data = await postService.searchByTitle(keyword);
            setPosts(data);
            setFilterLabel(`Search: "${keyword}"`);
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleFilterByTag = async (tag: string) => {
        setLoading(true);
        try {
            const data = await postService.filterByTag(tag);
            setPosts(data);
            setFilterLabel(`Tag: #${tag}`);
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleFilterByUser = async () => {
        if (!user) return;
        setLoading(true);
        try {
            const data = await postService.getByUserId(user.userId);
            setPosts(data);
            setFilterLabel('My Posts');
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (postId: number) => {
        if (!user || !window.confirm('Delete this post?')) return;
        try {
            await postService.delete(postId, user.userId);
            setPosts(prev => prev.filter(p => p.postId !== postId));
        } catch (err) {
            console.error(err);
        }
    };

    return (
        <div>
            <SearchBar
                onSearch={handleSearch}
                onFilterByTag={handleFilterByTag}
                onFilterByUser={handleFilterByUser}
                onClearFilters={loadPosts}
            />

            <h5 className="text-muted mb-3">{filterLabel}</h5>

            {loading ? (
                <div className="text-center py-5">
                    <div className="spinner-border text-primary" role="status">
                        <span className="visually-hidden">Loading...</span>
                    </div>
                </div>
            ) : posts.length === 0 ? (
                <div className="text-center py-5 text-muted">No posts found.</div>
            ) : (
                posts.map(post => (
                    <PostCard
                        key={post.postId}
                        post={post}
                        onDelete={handleDelete}
                        onTagClick={handleFilterByTag}
                    />
                ))
            )}
        </div>
    );
}