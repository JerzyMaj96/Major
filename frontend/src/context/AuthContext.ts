import { createContext } from "react";
import type { User } from "../types/types";

export interface AuthContextType {
  user: User | null;
  login: (name: string, password: string) => Promise<void>;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextType | undefined>(
  undefined,
);
