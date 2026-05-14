import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthProvider';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import PostListPage from './pages/PostListPage';
import PostDetailPage from './pages/PostDetailPage';
import CreatePostPage from './pages/CreatePostPage';
import ProfilePage from './pages/ProfilePage';
import AdminPage from './pages/AdminPage';
import EditPostPage from './pages/EditPostPage';
import './App.css';

export default function App() {
  return (
      <AuthProvider>
        <BrowserRouter>
          <Navbar />
          <main className="container py-4" style={{ maxWidth: 720 }}>
            <Routes>
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />
              <Route path="/" element={<ProtectedRoute><PostListPage /></ProtectedRoute>} />
              <Route path="/posts/:id" element={<ProtectedRoute><PostDetailPage /></ProtectedRoute>} />
              <Route path="/create-post" element={<ProtectedRoute><CreatePostPage /></ProtectedRoute>} />
              <Route path="/profile/:id" element={<ProtectedRoute><ProfilePage /></ProtectedRoute>} />
              <Route path="/admin" element={<ProtectedRoute><AdminPage /></ProtectedRoute>} />
              <Route path="/posts/:id/edit" element={
                <ProtectedRoute><EditPostPage /></ProtectedRoute>
              } />
            </Routes>
          </main>
        </BrowserRouter>
      </AuthProvider>
  );
}