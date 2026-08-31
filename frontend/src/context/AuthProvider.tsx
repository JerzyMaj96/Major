import { useEffect, useState, type ReactNode } from "react";
import { getAuthToken, setAuthToken } from "../api/api_helper";
import { authService } from "../api/services";
import type { User, UserLogin } from "../types/types";
import { AuthContext } from "./AuthContext";

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const initAuth = async () => {
      const token = getAuthToken();
      if (token) {
        try {
          const userData: User = await authService.getCurrentUser();
          setUser(userData);
        } catch (ex) {
          console.error("Auth check failed", ex);
          setAuthToken(null);
        }
      }
      setLoading(false);
    };
    initAuth();
  }, []);

  const login = async (credentials: UserLogin) => {
    const token = await authService.login(credentials);
    setAuthToken(token);
    const userData: User = await authService.getCurrentUser();
    setUser(userData);
  };

  const logout = () => {
    setAuthToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {!loading && children}
    </AuthContext.Provider>
  );
};
