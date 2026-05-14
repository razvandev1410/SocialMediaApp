import { Link, useNavigate } from 'react-router-dom';
import {useAuth} from '../context/useAuth';
import { useEffect } from 'react';

export default function Navbar() {
    const { user, logout, isLoggedIn, refreshUser } = useAuth();
    const navigate = useNavigate();

    useEffect(() => {
        if(isLoggedIn) {
            refreshUser();
        }
    }, []);

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <nav className="navbar navbar-expand navbar-dark bg-primary sticky-top px-3">
            <Link className="navbar-brand fw-bold" to="/">Facebook Clone</Link>

            <div className="navbar-nav ms-auto d-flex align-items-center gap-2">
                {isLoggedIn ? (
                    <>
                        <Link className="nav-link text-white" to="/">Posts</Link>
                        <Link className="nav-link text-white" to="/create-post">+ New Post</Link>
                        <Link className="nav-link text-white" to={`/profile/${user?.userId}`}>
                            {user?.username} <span className="badge bg-light text-primary">{user?.score?.toFixed(1)} pts</span>
                        </Link>
                        {user?.isModerator && (
                            <Link className="nav-link text-warning fw-bold" to="/admin">Admin</Link>
                        )}
                        <button onClick={handleLogout} className="btn btn-outline-light btn-sm ms-2">
                            Logout
                        </button>
                    </>
                ) : (
                    <>
                        <Link className="nav-link text-white" to="/login">Login</Link>
                        <Link className="nav-link text-white" to="/register">Register</Link>
                    </>
                )}
            </div>
        </nav>
    );
}