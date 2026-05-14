import { useState, ReactNode } from 'react';
import { AuthContext } from './AuthContext';
import type { User } from '../types';
import userService from "../services/userService.ts";

export function AuthProvider({ children }: { children: ReactNode }) {
    const [user, setUser] = useState<User | null>(() => {
        const stored = localStorage.getItem('user');
        if (stored) {
            try {
                return JSON.parse(stored);
            } catch {
                localStorage.removeItem('user');
            }
        }
        return null;
    });

    const login = (userData: User) => {
        setUser(userData);
        localStorage.setItem('user', JSON.stringify(userData));
    };

    const logout = () => {
        setUser(null);
        localStorage.removeItem('user');
    };

    const refreshUser = async() =>  {
      if(!user) return;
      try {
          const updated = await userService.getById(user.userId);
          setUser(updated);
          localStorage.setItem('user', JSON.stringify(updated));
      }
      catch {
          console.error('Failed to refresh user ', user);
      }
    };

    return (
        <AuthContext.Provider value={{ user, login, logout, refreshUser, isLoggedIn: !!user }}>
            {children}
        </AuthContext.Provider>
    );
}