const ACCESS_KEY = "rtl-jwt";
const REFRESH_KEY = "rtl-refresh";

export type User = {
  id: number;
  email: string;
  username: string;
  displayName: string;
};

type AuthPayload = {
  accessToken?: string;
  refreshToken?: string;
  token?: string;
  user: User;
};

export function getToken(): string | null {
  return sessionStorage.getItem(ACCESS_KEY);
}

export function getRefreshToken(): string | null {
  return sessionStorage.getItem(REFRESH_KEY);
}

export function setSession(accessToken: string | null, refreshToken: string | null = null) {
  if (accessToken) sessionStorage.setItem(ACCESS_KEY, accessToken);
  else sessionStorage.removeItem(ACCESS_KEY);
  if (refreshToken) sessionStorage.setItem(REFRESH_KEY, refreshToken);
  else sessionStorage.removeItem(REFRESH_KEY);
}

export function setToken(token: string | null) {
  setSession(token, token ? getRefreshToken() : null);
}

export function accessOf(res: AuthPayload) {
  return res.accessToken || res.token || "";
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers);
  headers.set("Content-Type", "application/json");
  const token = getToken();
  if (token) headers.set("Authorization", `Bearer ${token}`);
  const res = await fetch(path, { ...init, headers });
  if (res.status === 204) return undefined as T;
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
  return request<AuthPayload>("/api/signup", {
    method: "POST",
    body: JSON.stringify(body),
  });
}

export function login(email: string, password: string) {
  return request<AuthPayload>("/api/login", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });
}

export function me() {
  return request<{ user: User }>("/api/me");
}

export async function logout() {
  const refreshToken = getRefreshToken();
  try {
    await request("/api/logout", {
      method: "POST",
      body: JSON.stringify({ refreshToken }),
    });
  } catch {
    // 画面はトークンを消してログインへ戻す。API が古いときも同じ
  } finally {
    setSession(null, null);
  }
}
