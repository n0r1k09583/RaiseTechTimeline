import { useEffect, useState } from "react";
import { getToken, me, setToken, type User } from "./api";
import { LoginPage } from "./LoginPage";
import { SignupPage } from "./SignupPage";

type Screen = "login" | "signup";

export function App() {
  const [screen, setScreen] = useState<Screen>("login");
  const [user, setUser] = useState<User | null>(null);
  const [boot, setBoot] = useState(true);

  useEffect(() => {
    const token = getToken();
    if (!token) {
      setBoot(false);
      return;
    }
    me()
      .then((res) => setUser(res.user))
      .catch(() => setToken(null))
      .finally(() => setBoot(false));
  }, []);

  function onAuthed(token: string) {
    setToken(token);
    me().then((res) => setUser(res.user));
  }

  if (boot) return <p className="wrap">読み込み中…</p>;

  if (!user) {
    return screen === "signup" ? (
      <SignupPage onSuccess={onAuthed} onGoLogin={() => setScreen("login")} />
    ) : (
      <LoginPage onSuccess={onAuthed} onGoSignup={() => setScreen("signup")} />
    );
  }

  return (
    <div className="wrap">
      <p className="site">RaiseTechタイムライン</p>
      <h1>Hello World</h1>
      <p className="lead">ログイン後の仮画面です。</p>
      <div className="row">
        <button
          className="btn ghost"
          type="button"
          onClick={() => {
            setToken(null);
            setUser(null);
            setScreen("login");
          }}
        >
          ログアウト
        </button>
      </div>
    </div>
  );
}
