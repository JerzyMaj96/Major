export const getAuthToken = () => {
  return sessionStorage.getItem("jwtToken");
};

export const setAuthToken = (token: string | null) => {
  if (token) {
    sessionStorage.setItem("jwtToken", token);
  } else {
    sessionStorage.removeItem("jwtToken");
  }
};

export const authFetch = (
  method: string,
  url: string,
  body?: string,
): Promise<Response> => {
  const token = getAuthToken();
  const headers: Record<string, string> = {};

  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  if (body) {
    headers["Content-Type"] = "application/json";
  }

  const options: RequestInit = { method, headers, body };

  const finalUrl = url.startsWith("http") ? url : `${baseUrl}${url}`;
  return fetch(finalUrl, options);
};

export const baseUrl = import.meta.env.VITE_API_BASE_URL;
