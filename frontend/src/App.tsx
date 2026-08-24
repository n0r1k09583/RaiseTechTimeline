import { useCallback, useEffect, useState } from "react";
import { accessOf, getToken, logout, me, setSession, type User } from "./api";
import { EditPage } from "./EditPage";
import { LoginPage } from "./LoginPage";
import { PostDetailPage } from "./PostDetailPage";
import { SignupPage } from "./SignupPage";
import { TimelinePage } from "./TimelinePage";

type Screen = "login" | "signup" | "timeline" | "edit" | "post";

export function App() {
  const [screen, setScreen] = useState<Screen>("login");
  const [user, setUser] = useState<User | null>(null);
  const [editId, setEditId] = useState<number | null>(null);
  const [postId, setPostId] = useState<number | null>(null);
  const [boot, setBoot] = useState(true);

  const onLogout = useCallback(async () => {
    await logout();
    setUser(null);
    setEditId(null);
    setPostId(null);
    setScreen("login");
  }, []);

  useEffect(() => {
    const token = getToken();
    if (!token) {
      setBoot(false);
      return;
    }
    me()
      .then((res) => {
        setUser(res.user);
        setScreen("timeline");
      })
      .catch(() => setSession(null, null))
      .finally(() => setBoot(false));
  }, []);

  function onAuthed(res: { accessToken?: string; refreshToken?: string; token?: string; user: User }) {
    setSession(accessOf(res), res.refreshToken ?? null);
    setUser(res.user);
    setScreen("timeline");
  }

  const goTimeline = useCallback(() => {
    setEditId(null);
    setPostId(null);
    setScreen("timeline");
  }, []);

  if (boot) return <p className="wrap">読み込み中…</p>;

  if (!user) {
    return screen === "signup" ? (
      <SignupPage onSuccess={onAuthed} onGoLogin={() => setScreen("login")} />
    ) : (
      <LoginPage onSuccess={onAuthed} onGoSignup={() => setScreen("signup")} />
    );
  }

  if (screen === "edit" && editId != null) {
    return (
      <EditPage
        user={user}
        postId={editId}
        onLogout={onLogout}
        onDone={goTimeline}
      />
    );
  }

  if (screen === "post" && postId != null) {
    return (
      <PostDetailPage
        user={user}
        postId={postId}
        onLogout={onLogout}
        onBack={goTimeline}
        onEdit={(id) => {
          setEditId(id);
          setScreen("edit");
        }}
      />
    );
  }

  return (
    <TimelinePage
      user={user}
      onLogout={onLogout}
      onEdit={(id) => {
        setEditId(id);
        setScreen("edit");
      }}
      onOpen={(id) => {
        setPostId(id);
        setScreen("post");
      }}
    />
  );
}
