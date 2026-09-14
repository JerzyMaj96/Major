import type {
  CreateTask,
  Task,
  User,
  UserLogin,
  UserRegister,
} from "../types/types";
import { authFetch, baseUrl } from "./api_helper";

export const authService = {
  login: async (credentials: UserLogin): Promise<string> => {
    const response = await fetch(`${baseUrl}/major/api/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(credentials),
    });
    if (!response.ok) throw new Error(await response.text());
    return response.text();
  },
};

export const userService = {
  register: async (userData: UserRegister): Promise<User> => {
    const response = await fetch(`${baseUrl}/major/api/users/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(userData),
    });
    if (!response.ok) throw new Error(await response.text());
    return response.json() as Promise<User>;
  },
  getCurrentUser: async (): Promise<User> => {
    const response = await authFetch("GET", `/major/api/users/me`);
    if (!response.ok) throw new Error("Failed to get user");
    return response.json() as Promise<User>;
  },
  deleteAccount: async (): Promise<void> => {
    const response = await authFetch("DELETE", `/major/api/users/delete-me`);
    if (!response.ok) throw new Error("Failed to delete account");
  },
};

export const taskService = {
  getTasks: async (): Promise<Task[]> => {
    const response = await authFetch("GET", `/major/api/tasks`);
    if (!response.ok) throw new Error("Failed to fetch tasks");
    return response.json() as Promise<Task[]>;
  },
  createTask: async (taskData: CreateTask): Promise<Task> => {
    const response = await authFetch(
      "POST",
      `/major/api/tasks`,
      JSON.stringify(taskData),
    );
    if (!response.ok) throw new Error("Failed to create task");
    return response.json() as Promise<Task>;
  },
};

export const gptService = {

  generateDescription: async (title: string): Promise<string> => {
    const response = await authFetch(
      "POST",
      `/major/api/gpt/generate-description`,
      JSON.stringify({ title }),
    );
    if (!response.ok) throw new Error("Failed to generate description");
    return response.text();
  }
};
