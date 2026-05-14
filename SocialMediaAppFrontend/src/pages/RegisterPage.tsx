import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/useAuth';
import authService from '../services/authService';

export default function RegisterPage() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [email, setEmail] = useState('');
    const [phoneNumber, setPhoneNumber] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const { login } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            const user = await authService.register({ username, password, email, phoneNumber });
            login(user);
            navigate('/');
        } catch (err: unknown) {
            const errorResponse = err as { response?: {data?: {message?: string}}};
            setError(errorResponse.response?.data?.message || 'Registration failed');
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
                        <h3 className="text-center mb-4">Register</h3>
                        {error && <div className="alert alert-danger py-2">{error}</div>}

                        <form onSubmit={handleSubmit}>
                            <div className="mb-3">
                                <label htmlFor="username" className="form-label">Username</label>
                                <input id="username" type="text" className="form-control"
                                       value={username} onChange={(e) => setUsername(e.target.value)} required minLength={3} />
                            </div>
                            <div className="mb-3">
                                <label htmlFor="email" className="form-label">Email</label>
                                <input id="email" type="email" className="form-control"
                                       value={email} onChange={(e) => setEmail(e.target.value)} required />
                            </div>
                            <div className="mb-3">
                                <label htmlFor="phone" className="form-label">Phone Number</label>
                                <input id="phone" type="text" className="form-control"
                                       value={phoneNumber} onChange={(e) => setPhoneNumber(e.target.value)}
                                       required placeholder="+40712345678" />
                            </div>
                            <div className="mb-3">
                                <label htmlFor="password" className="form-label">Password</label>
                                <input id="password" type="password" className="form-control"
                                       value={password} onChange={(e) => setPassword(e.target.value)} required minLength={6} />
                            </div>
                            <button type="submit" className="btn btn-primary w-100" disabled={loading}>
                                {loading ? 'Creating account...' : 'Register'}
                            </button>
                        </form>

                        <p className="text-center mt-3 text-muted small">
                            Already have an account? <Link to="/login">Login</Link>
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
}