const TOKEN_KEY = "rtl-jwt";

export type User = {
  id: number;
  email: string;
  username: string;
  displayName: string;
};

export function getToken(): string | null {
  return sessionStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string | null) {
  if (token) sessionStorage.setItem(TOKEN_KEY, token);
  else sessionStorage.removeItem(TOKEN_KEY);
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers);
  headers.set("Content-Type", "application/json");
  const token = getToken();
  if (token) headers.set("Authorization", `Bearer ${token}`);
  const res = await fetch(path, { ...init, headers });
  const data = (await res.json().catch(() => ({}))) as T & { error?: string };
  if (!res.ok) throw new Error(data.error || "リクエストに失敗しました");
  return data;
}

export function signup(body: {
  username: string;
  displayName: string;
  email: string;
  password: string;
  confirm: string;
}) {
  return request<{ token: string; user: User }>("/api/signup", {
    method: "POST",
    body: JSON.stringify(body),
  });
}

export function login(email: string, password: string) {
  return request<{ token: string; user: User }>("/api/login", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });
}

export function me() {
  return request<{ user: User }>("/api/me");
}
