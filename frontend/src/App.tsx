import { useCallback, useEffect, useState } from "react";
import { accessOf, getToken, logout, me, setSession, type User } from "./api";
import { EditPage } from "./EditPage";
import { LoginPage } from "./LoginPage";
import { PostDetailPage } from "./PostDetailPage";
import { SignupPage } from "./SignupPage";
import { TimelinePage } from "./TimelinePage";
import { applyPrivateSeo, applyPublicSeo, publicScreenFromPath } from "./seo";

type Screen = "login" | "signup" | "timeline" | "edit" | "post";

function goPublic(path: "/login" | "/signup") {
  if (window.location.pathname !== path) {
    window.history.pushState(null, "", path);
  }
}

export function App() {
  const [screen, setScreen] = useState<Screen>(() => publicScreenFromPath(window.location.pathname));
  const [user, setUser] = useState<User | null>(null);
  const [editId, setEditId] = useState<number | null>(null);
  const [postId, setPostId] = useState<number | null>(null);
  const [boot, setBoot] = useState(true);

  const onLogout = useCallback(async () => {
    await logout();
    setUser(null);
    setEditId(null);
    setPostId(null);
    goPublic("/login");
    setScreen("login");
  }, []);

  useEffect(() => {
    const token = getToken();
    if (!token) {
      const next = publicScreenFromPath(window.location.pathname);
      setScreen(next);
      if (window.location.pathname === "/" || window.location.pathname === "") {
        window.history.replaceState(null, "", "/login");
      }
      setBoot(false);
      return;
    }
    me()
      .then((res) => {
        setUser(res.user);
        setScreen("timeline");
        window.history.replaceState(null, "", "/");
      })
      .catch(() => setSession(null, null))
      .finally(() => setBoot(false));
  }, []);

  useEffect(() => {
    function onPop() {
      if (getToken()) {
        return;
      }
      setScreen(publicScreenFromPath(window.location.pathname));
    }
    window.addEventListener("popstate", onPop);
    return () => window.removeEventListener("popstate", onPop);
  }, []);

  useEffect(() => {
    if (boot) {
      return;
    }
    if (!user) {
      applyPublicSeo(screen === "signup" ? "signup" : "login");
      return;
    }
    applyPrivateSeo();
  }, [boot, user, screen]);

  function onAuthed(res: { accessToken?: string; refreshToken?: string; token?: string; user: User }) {
    setSession(accessOf(res), res.refreshToken ?? null);
    setUser(res.user);
    setScreen("timeline");
    window.history.replaceState(null, "", "/");
  }

  const goTimeline = useCallback(() => {
    setEditId(null);
    setPostId(null);
    setScreen("timeline");
  }, []);

  if (boot) return <p className="wrap">読み込み中…</p>;

  if (!user) {
    return screen === "signup" ? (
      <SignupPage
        onSuccess={onAuthed}
        onGoLogin={() => {
          goPublic("/login");
          setScreen("login");
        }}
      />
    ) : (
      <LoginPage
        onSuccess={onAuthed}
        onGoSignup={() => {
          goPublic("/signup");
          setScreen("signup");
        }}
      />
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
