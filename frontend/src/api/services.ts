import type { User, UserLogin, UserRegister } from "../types/types";
import { authFetch, baseUrl } from "./api_helper";

export const authService = {
  register: async (userData: UserRegister): Promise<User> => {
    const response = await fetch(`${baseUrl}/major/api/users/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(userData),
    });
    if (!response.ok) throw new Error(await response.text());
    return response.json() as Promise<User>;
  },
  login: async (credentials: UserLogin): Promise<string> => {
    const response = await fetch(`${baseUrl}/major/api/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(credentials),
    });
    if (!response.ok) throw new Error(await response.text());
    return response.text();
  },
  getCurrentUser: async (): Promise<User> => {
    const response = await authFetch("GET", `/major/api/users/me`);
    if (!response.ok) throw new Error("Failed to get user");
    return response.json() as Promise<User>;
  },
};
