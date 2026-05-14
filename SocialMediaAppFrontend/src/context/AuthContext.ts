import { createContext } from 'react';
import type { User } from '../types';

export interface AuthContextType {
    user: User | null;
    login: (user: User) => void;
    logout: () => void;
    refreshUser: () => Promise<void>;
    isLoggedIn: boolean;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);