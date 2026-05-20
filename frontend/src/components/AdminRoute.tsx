import { useEffect, useState, type ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { fetchCurrentUser } from '../api/auth';
import type { User } from '../types';

export default function AdminRoute({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null | undefined>(undefined);

  useEffect(() => {
    fetchCurrentUser().then(setUser).catch(() => setUser(null));
  }, []);

  if (user === undefined) return null;
  if (!Array.isArray(user?.roles) || !user.roles.includes('ADMIN')) return <Navigate to="/" replace />;
  return <>{children}</>;
}
