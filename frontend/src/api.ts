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
  const isForm = init.body instanceof FormData;
  if (!isForm) headers.set("Content-Type", "application/json");
  const token = getToken();
  if (token) headers.set("Authorization", `Bearer ${token}`);
  let res: Response;
  try {
    res = await fetch(path, { ...init, headers });
  } catch {
    throw new Error("サーバーに接続できません。バックエンドを起動してください");
  }
  if (res.status === 204) return undefined as T;
  const data = (await res.json().catch(() => ({}))) as T & { error?: string };
  if (!res.ok) {
    throw new Error(
      data.error ||
        (res.status >= 500
          ? "サーバーに接続できません。バックエンドを起動してください"
          : "リクエストに失敗しました"),
    );
  }
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

export type Post = {
  id: number;
  userId: number;
  username: string;
  displayName: string;
  body: string;
  imageUrl: string | null;
  createdAt: string;
  updatedAt: string;
  mine: boolean;
  commentCount: number;
  likeCount: number;
};

export type PostList = {
  posts: Post[];
  hasMore: boolean;
};

type ListQuery = {
  tab?: "all" | "following";
  limit?: number;
  beforeCreatedAt?: string;
  beforeId?: number;
  afterCreatedAt?: string;
  afterId?: number;
};

export function listPosts(query: ListQuery = {}) {
  const params = new URLSearchParams();
  if (query.tab) params.set("tab", query.tab);
  if (query.limit != null) params.set("limit", String(query.limit));
  if (query.beforeCreatedAt) params.set("beforeCreatedAt", query.beforeCreatedAt);
  if (query.beforeId != null) params.set("beforeId", String(query.beforeId));
  if (query.afterCreatedAt) params.set("afterCreatedAt", query.afterCreatedAt);
  if (query.afterId != null) params.set("afterId", String(query.afterId));
  const qs = params.toString();
  return request<PostList>(`/api/posts${qs ? `?${qs}` : ""}`);
}

export function getPost(id: number) {
  return request<Post>(`/api/posts/${id}`);
}

export function createPost(body: string, image?: File | null) {
  const form = new FormData();
  form.append("body", body);
  if (image) form.append("image", image);
  return request<Post>("/api/posts", { method: "POST", body: form });
}

export function updatePost(id: number, body: string, image?: File | null) {
  const form = new FormData();
  form.append("body", body);
  if (image) form.append("image", image);
  return request<Post>(`/api/posts/${id}`, { method: "PATCH", body: form });
}

export function deletePost(id: number) {
  return request<void>(`/api/posts/${id}`, { method: "DELETE" });
}

export type Comment = {
  id: number;
  postId: number;
  userId: number;
  username: string;
  displayName: string;
  body: string;
  createdAt: string;
  mine: boolean;
};

export function listComments(postId: number) {
  return request<{ comments: Comment[] }>(`/api/posts/${postId}/comments`);
}

export function createComment(postId: number, body: string) {
  return request<Comment>(`/api/posts/${postId}/comments`, {
    method: "POST",
    body: JSON.stringify({ body }),
  });
}

export function deleteComment(id: number) {
  return request<void>(`/api/comments/${id}`, { method: "DELETE" });
}
