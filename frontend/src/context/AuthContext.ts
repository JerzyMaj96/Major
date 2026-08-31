import { createContext } from "react";
import type { User, UserLogin } from "../types/types";

export interface AuthContextType {
  user: User | null;
  login: (credentials: UserLogin) => Promise<void>;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextType | undefined>(
  undefined,
);
