import { useState, type FormEvent } from "react";
import { login, type User } from "./api";

type AuthOk = { accessToken?: string; refreshToken?: string; token?: string; user: User };

type Props = {
  onSuccess: (res: AuthOk) => void;
  onGoSignup: () => void;
};

const DEMOS = [
  { email: "yamada@example.com", label: "@yamada" },
  { email: "hanako@example.com", label: "@hanako" },
  { email: "ichiro@example.com", label: "@ichiro" },
];

export function LoginPage({ onSuccess, onGoSignup }: Props) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  function fill(demoEmail: string) {
    setEmail(demoEmail);
    setPassword("password123");
    setError("");
    void submit(demoEmail, "password123");
  }

  async function submit(nextEmail: string, nextPassword: string) {
    if (!nextEmail || !nextPassword) {
      setError("メールアドレスとパスワードを入力してください");
      return;
    }
    setBusy(true);
    setError("");
    try {
      const res = await login(nextEmail, nextPassword);
      onSuccess(res);
    } catch (err) {
      setPassword("");
      setError(err instanceof Error ? err.message : "ログインに失敗しました");
    } finally {
      setBusy(false);
    }
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    await submit(email, password);
  }

  return (
    <main className="wrap">
      <p className="brand">
        課題提出
        <span className="brand-sub">タイムライン</span>
      </p>
      <section className="card">
        <h1>ログイン</h1>
        <p className="demo">
          試すアカウント（パスワードは password123）
          <br />
          {DEMOS.map((demo) => (
            <button key={demo.email} type="button" className="btn ghost" onClick={() => fill(demo.email)}>
              {demo.label}
            </button>
          ))}
        </p>
        <form onSubmit={onSubmit}>
          <label htmlFor="email">メールアドレス</label>
          <input
            id="email"
            type="email"
            autoComplete="username"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
          <label htmlFor="password">パスワード</label>
          <input
            id="password"
            type="password"
            autoComplete="current-password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
          <div className="err">{error}</div>
          <div className="row-actions">
            <button className="btn" type="submit" disabled={busy}>
              ログイン
            </button>
          </div>
        </form>
        <p className="hint">
          初めての人は{" "}
          <button type="button" className="btn link" onClick={onGoSignup}>
            新規登録
          </button>
        </p>
      </section>
    </main>
  );
}
