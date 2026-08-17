import { useState, type FormEvent } from "react";
import { login } from "./api";

type Props = {
  onSuccess: (token: string) => void;
  onGoSignup: () => void;
};

export function LoginPage({ onSuccess, onGoSignup }: Props) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  function fill(demoEmail: string) {
    setEmail(demoEmail);
    setPassword("password123");
    setError("");
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setError("");
    if (!email || !password) {
      setError("メールアドレスとパスワードを入力してください");
      return;
    }
    try {
      const res = await login(email, password);
      onSuccess(res.token);
    } catch (err) {
      setPassword("");
      setError(err instanceof Error ? err.message : "ログインに失敗しました");
    }
  }

  return (
    <div className="wrap">
      <p className="site">RaiseTechタイムライン</p>
      <h1>ログイン</h1>
      <p className="demo">
        試すアカウント（パスワードは password123）
        <br />
        <button type="button" className="btn ghost" onClick={() => fill("yamada@example.com")}>
          yamada
        </button>
        <button type="button" className="btn ghost" onClick={() => fill("hanako@example.com")}>
          hanako
        </button>
        <button type="button" className="btn ghost" onClick={() => fill("ichiro@example.com")}>
          ichiro
        </button>
      </p>
      <form onSubmit={onSubmit}>
        <label htmlFor="email">メールアドレス</label>
        <input id="email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
        <label htmlFor="password">パスワード</label>
        <input
          id="password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        <div className="err">{error}</div>
        <div className="row">
          <button className="btn" type="submit">
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
    </div>
  );
}
