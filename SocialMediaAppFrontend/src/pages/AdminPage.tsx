import { useState, useEffect } from 'react';
import { useAuth } from '../context/useAuth';
import userService from '../services/userService';
import type { User } from '../types';
import { Navigate } from 'react-router-dom';

export default function AdminPage() {
    const { user } = useAuth();
    const [users, setUsers] = useState<User[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        userService.getAll().then(setUsers).catch(console.error).finally(() => setLoading(false));
    }, []);

    if (!user?.isModerator) return <Navigate to="/" replace />;

    const handleBan = async (userId: number) => {
        if (!window.confirm('Ban this user?')) return;
        try {
            const updated = await userService.ban(userId, user.userId);
            setUsers(users.map(u => u.userId === userId ? updated : u));
        } catch (err) { console.error(err); }
    };

    const handleUnban = async (userId: number) => {
        try {
            const updated = await userService.unban(userId, user.userId);
            setUsers(users.map(u => u.userId === userId ? updated : u));
        } catch (err) { console.error(err); }
    };

    if (loading) return <div className="text-center py-5"><div className="spinner-border text-primary" /></div>;

    return (
        <div>
            <h3 className="mb-4">Admin Panel — User Management</h3>

            <div className="card">
                <div className="table-responsive">
                    <table className="table table-hover mb-0">
                        <thead className="table-light">
                        <tr>
                            <th>ID</th>
                            <th>Username</th>
                            <th>Email</th>
                            <th>Score</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        {users.map(u => (
                            <tr key={u.userId} className={u.isBanned ? 'table-danger' : ''}>
                                <td>{u.userId}</td>
                                <td>
                                    {u.username}
                                    {u.isModerator && <span className="badge bg-primary ms-1" style={{ fontSize: 10 }}>MOD</span>}
                                </td>
                                <td>{u.email}</td>
                                <td>{u.score?.toFixed(1)}</td>
                                <td>
                                    {u.isBanned
                                        ? <span className="badge bg-danger">Banned</span>
                                        : <span className="badge bg-success">Active</span>
                                    }
                                </td>
                                <td>
                                    {u.userId !== user.userId && (
                                        u.isBanned ? (
                                            <button className="btn btn-success btn-sm" onClick={() => handleUnban(u.userId)}>Unban</button>
                                        ) : (
                                            <button className="btn btn-danger btn-sm" onClick={() => handleBan(u.userId)}>Ban</button>
                                        )
                                    )}
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
}