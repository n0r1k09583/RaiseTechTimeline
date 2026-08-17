import { useState, type FormEvent } from "react";
import { signup } from "./api";

type Props = {
  onSuccess: (token: string) => void;
  onGoLogin: () => void;
};

export function SignupPage({ onSuccess, onGoLogin }: Props) {
  const [username, setUsername] = useState("");
  const [displayName, setDisplayName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [error, setError] = useState("");

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setError("");
    try {
      const res = await signup({ username, displayName, email, password, confirm });
      onSuccess(res.token);
    } catch (err) {
      setError(err instanceof Error ? err.message : "登録に失敗しました");
    }
  }

  return (
    <div className="wrap">
      <p className="site">RaiseTechタイムライン</p>
      <h1>新規登録</h1>
      <form onSubmit={onSubmit}>
        <label htmlFor="username">ユーザー名</label>
        <input id="username" value={username} onChange={(e) => setUsername(e.target.value)} />
        <p className="hint">3〜20文字。半角英小文字・数字・_</p>
        <label htmlFor="displayName">表示名</label>
        <input
          id="displayName"
          value={displayName}
          onChange={(e) => setDisplayName(e.target.value)}
          maxLength={20}
        />
        <label htmlFor="email">メールアドレス</label>
        <input id="email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
        <label htmlFor="password">パスワード</label>
        <input
          id="password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        <label htmlFor="confirm">パスワード確認</label>
        <input
          id="confirm"
          type="password"
          value={confirm}
          onChange={(e) => setConfirm(e.target.value)}
        />
        <div className="err">{error}</div>
        <div className="row">
          <button className="btn" type="submit">
            登録する
          </button>
        </div>
      </form>
      <p className="hint">
        すでにアカウントがある人は{" "}
        <button type="button" className="btn link" onClick={onGoLogin}>
          ログイン
        </button>
      </p>
    </div>
  );
}
