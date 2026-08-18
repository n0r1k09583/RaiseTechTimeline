import { useEffect, useState } from "react";
import { accessOf, getToken, logout, me, setSession, type User } from "./api";
import { LoginPage } from "./LoginPage";
import { SignupPage } from "./SignupPage";
import { SuccessPage } from "./SuccessPage";

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
      .catch(() => setSession(null, null))
      .finally(() => setBoot(false));
  }, []);

  function onAuthed(res: { accessToken?: string; refreshToken?: string; token?: string; user: User }) {
    setSession(accessOf(res), res.refreshToken ?? null);
    setUser(res.user);
  }

  async function onLogout() {
    await logout();
    setUser(null);
    setScreen("login");
  }

  if (boot) return <p className="wrap">読み込み中…</p>;

  if (!user) {
    return screen === "signup" ? (
      <SignupPage onSuccess={onAuthed} onGoLogin={() => setScreen("login")} />
    ) : (
      <LoginPage onSuccess={onAuthed} onGoSignup={() => setScreen("signup")} />
    );
  }

  return <SuccessPage user={user} onLogout={onLogout} />;
}
