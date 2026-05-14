import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/useAuth';
import authService from '../services/authService.ts';
import * as React from "react";
import axios from "axios";

export default function LoginPage() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const { login } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            const user = await authService.login({ username, password });
            login(user);
            navigate('/');
        }
        catch (err: unknown) {
            let errorMessage = 'Login failed';

            if(axios.isAxiosError(err)) {
                errorMessage = err.response?.data?.message || errorMessage;
            }

            setError(errorMessage);
        }
        finally {
            setLoading(false);
        }
    };

    return (
        <div className="row justify-content-center mt-5">
            <div className="col-md-5 col-lg-4">
                <div className="card shadow-sm">
                    <div className="card-body p-4">
                        <h3 className="text-center mb-4">Login</h3>
                        {error && <div className="alert alert-danger py-2">{error}</div>}

                        <form onSubmit={handleSubmit}>
                            <div className="mb-3">
                                <label htmlFor="username" className="form-label">Username</label>
                                <input id="username" type="text" className="form-control"
                                       value={username} onChange={(e) => setUsername(e.target.value)} required />
                            </div>
                            <div className="mb-3">
                                <label htmlFor="password" className="form-label">Password</label>
                                <input id="password" type="password" className="form-control"
                                       value={password} onChange={(e) => setPassword(e.target.value)} required />
                            </div>
                            <button type="submit" className="btn btn-primary w-100" disabled={loading}>
                                {loading ? 'Logging in...' : 'Login'}
                            </button>
                        </form>

                        <p className="text-center mt-3 text-muted small">
                            Don't have an account? <Link to="/register">Register</Link>
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
}