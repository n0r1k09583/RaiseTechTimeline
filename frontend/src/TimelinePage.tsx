import { useEffect, useRef, useState, type FormEvent } from "react";
import { createPost, deletePost, listPosts, type Post, type User } from "./api";
import { AppHeader } from "./AppHeader";

const PAGE = 20;
const REFRESH_MS = 30_000;
const IMAGE_MAX = 5 * 1024 * 1024;
const IMAGE_TYPES = ["image/jpeg", "image/png", "image/webp"];

type Tab = "all" | "following";

type Props = {
  user: User;
  onLogout: () => void | Promise<void>;
  onEdit: (id: number) => void;
  onOpen: (id: number) => void;
};

export function TimelinePage({ user, onLogout, onEdit, onOpen }: Props) {
  const [tab, setTab] = useState<Tab>("all");
  const [posts, setPosts] = useState<Post[]>([]);
  const [hasMore, setHasMore] = useState(false);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState("");
  const [body, setBody] = useState("");
  const [image, setImage] = useState<File | null>(null);
  const [busy, setBusy] = useState(false);
  const [confirm, setConfirm] = useState<Post | null>(null);
  const [fresh, setFresh] = useState<Post[]>([]);
  const [notice, setNotice] = useState("");
  const sentinelRef = useRef<HTMLDivElement>(null);
  const loadingMoreRef = useRef(false);
  const postsRef = useRef<Post[]>([]);
  const hasMoreRef = useRef(false);
  const tabRef = useRef(tab);
  const bodyRef = useRef<HTMLTextAreaElement>(null);
  const fileRef = useRef<HTMLInputElement>(null);
  const noticeTimerRef = useRef(0);

  postsRef.current = posts;
  hasMoreRef.current = hasMore;
  tabRef.current = tab;
  loadingMoreRef.current = loadingMore;

  useEffect(() => {
    let cancelled = false;
    setFresh([]);
    if (tab === "following") {
      setPosts([]);
      setHasMore(false);
      setLoading(false);
      setError("");
      return;
    }
    setLoading(true);
    setError("");
    listPosts({ tab: "all", limit: PAGE })
      .then((res) => {
        if (cancelled) return;
        setPosts(res.posts);
        setHasMore(res.hasMore);
      })
      .catch((err) => {
        if (cancelled) return;
        const message = err instanceof Error ? err.message : "読み込みに失敗しました";
        if (message.includes("ログイン")) {
          void onLogout();
          return;
        }
        setError(message);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [tab, onLogout]);

  useEffect(() => {
    return () => window.clearTimeout(noticeTimerRef.current);
  }, []);

  function showPostedNotice(message: string) {
    window.clearTimeout(noticeTimerRef.current);
    setNotice(message);
    noticeTimerRef.current = window.setTimeout(() => setNotice(""), 6000);
  }

  useEffect(() => {
    async function refreshQuietly() {
      if (document.visibilityState !== "visible") return;
      if (tabRef.current !== "all") return;
      const head = postsRef.current[0];
      if (!head) return;
      try {
        const res = await listPosts({
          tab: "all",
          limit: PAGE,
          afterCreatedAt: head.createdAt,
          afterId: head.id,
        });
        if (res.posts.length === 0) return;
        if (window.scrollY < 80) {
          applyFresh(res.posts);
        } else {
          setFresh((prev) => mergeById(res.posts, prev));
        }
      } catch {
        // 一定間隔の取り直しは失敗しても画面に出さない
      }
    }

    const timer = window.setInterval(() => {
      void refreshQuietly();
    }, REFRESH_MS);

    function onVisible() {
      if (document.visibilityState === "visible") void refreshQuietly();
    }
    document.addEventListener("visibilitychange", onVisible);
    return () => {
      window.clearInterval(timer);
      document.removeEventListener("visibilitychange", onVisible);
    };
  }, []);

  useEffect(() => {
    const el = sentinelRef.current;
    if (!el || tab !== "all") return;
    const io = new IntersectionObserver(
      (entries) => {
        if (!entries[0]?.isIntersecting) return;
        void loadOlder();
      },
      { rootMargin: "240px" },
    );
    io.observe(el);
    return () => io.disconnect();
  }, [tab, posts.length, hasMore, loading]);

  function applyFresh(incoming: Post[]) {
    setPosts((prev) => mergeById(incoming, prev));
    setFresh([]);
  }

  function showFresh() {
    applyFresh(fresh);
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  async function loadOlder() {
    if (tabRef.current !== "all") return;
    if (loading || loadingMoreRef.current || !hasMoreRef.current) return;
    const last = postsRef.current[postsRef.current.length - 1];
    if (!last) return;
    loadingMoreRef.current = true;
    setLoadingMore(true);
    try {
      const res = await listPosts({
        tab: "all",
        limit: PAGE,
        beforeCreatedAt: last.createdAt,
        beforeId: last.id,
      });
      const existing = new Set(postsRef.current.map((p) => p.id));
      const added = res.posts.filter((p) => !existing.has(p.id));
      setPosts((prev) => {
        const ids = new Set(prev.map((p) => p.id));
        return [...prev, ...res.posts.filter((p) => !ids.has(p.id))];
      });
      setHasMore(res.hasMore);
      if (added.length > 0) {
        showPostedNotice(`投稿されました（続きを${added.length}件読み込みました）`);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "続きの読み込みに失敗しました");
    } finally {
      loadingMoreRef.current = false;
      setLoadingMore(false);
    }
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    const text = body.trim();
    setError("");
    if (!text || text.length > 280) {
      setError("本文は1〜280文字です");
      return;
    }
    if (image && !IMAGE_TYPES.includes(image.type)) {
      setError("JPEG / PNG / WebP のみです");
      return;
    }
    if (image && image.size > IMAGE_MAX) {
      setError("画像は5MBまでです");
      return;
    }
    setBusy(true);
    try {
      const created = await createPost(text, image);
      setPosts((prev) => [created, ...prev.filter((p) => p.id !== created.id)]);
      setFresh((prev) => prev.filter((p) => p.id !== created.id));
      setBody("");
      setImage(null);
      if (fileRef.current) fileRef.current.value = "";
      setTab("all");
      showPostedNotice("投稿されました");
    } catch (err) {
      setError(err instanceof Error ? err.message : "投稿に失敗しました");
    } finally {
      setBusy(false);
    }
  }

  async function onConfirmDelete() {
    if (!confirm) return;
    const target = confirm;
    setConfirm(null);
    try {
      await deletePost(target.id);
      setPosts((prev) => prev.filter((p) => p.id !== target.id));
    } catch (err) {
      setError(err instanceof Error ? err.message : "削除に失敗しました");
    }
  }

  return (
    <main className="page">
      <AppHeader user={user} onLogout={onLogout} onHome={() => window.scrollTo({ top: 0 })} />
      {notice ? (
        <p className="notice-bar" role="status">
          {notice}
        </p>
      ) : null}
      {fresh.length > 0 ? (
        <button type="button" className="fresh-bar" onClick={showFresh}>
          新着の投稿が{fresh.length}件あります
        </button>
      ) : null}
      <section className="card feed-card">
        <div className="feed-head">
          <h1>タイムライン</h1>
          <p className="lead">
            投稿はすぐ先頭に出ます。続きは下へスクロールします。
            <button type="button" className="btn link" onClick={() => bodyRef.current?.focus()}>
              投稿する
            </button>
          </p>
          <form onSubmit={(e) => void onSubmit(e)}>
            <label htmlFor="body">本文</label>
            <textarea
              id="body"
              ref={bodyRef}
              placeholder="いまどうしてる？"
              value={body}
              maxLength={280}
              onChange={(e) => setBody(e.target.value)}
            />
            <p className={`counter${body.length > 280 ? " over" : ""}`}>{body.length} / 280</p>
            <label htmlFor="image">画像（任意）</label>
            <input
              id="image"
              ref={fileRef}
              type="file"
              accept="image/jpeg,image/png,image/webp"
              onChange={(e) => setImage(e.target.files?.[0] ?? null)}
            />
            <p className="hint">JPEG / PNG / WebP、5MBまで</p>
            <div className="err">{error}</div>
            <div className="row-actions">
              <button className="btn" type="submit" disabled={busy}>
                投稿する
              </button>
            </div>
          </form>
        </div>
        <div className="tabs" role="tablist">
          <button type="button" className={`tab${tab === "all" ? " active" : ""}`} onClick={() => setTab("all")}>
            すべて
          </button>
          <button
            type="button"
            className={`tab${tab === "following" ? " active" : ""}`}
            onClick={() => setTab("following")}
          >
            フォロー中
          </button>
        </div>
        <div>
          {tab === "following" ? (
            <p className="empty">フォロー中の一覧は後続です。「すべて」で全投稿を見られます。</p>
          ) : null}
          {tab === "all" && loading && posts.length === 0 ? <p className="empty">読み込み中…</p> : null}
          {tab === "all" && !loading && posts.length === 0 ? (
            <p className="empty">まだ投稿はありません。</p>
          ) : null}
          {tab === "all"
            ? posts.map((post) => (
                <article className="post" key={post.id}>
                  <div className="avatar">{initial(post)}</div>
                  <div>
                    <div>
                      <span className="name">{post.displayName}</span>
                      <span className="handle">@{post.username}</span>
                      <span className="meta">
                        {" "}
                        · {fmt(post.createdAt)}
                        {post.mine ? (
                          <>
                            {" "}
                            ·{" "}
                            <button type="button" className="btn link" onClick={() => onEdit(post.id)}>
                              編集
                            </button>
                            {" · "}
                            <button type="button" className="btn link" onClick={() => setConfirm(post)}>
                              削除
                            </button>
                          </>
                        ) : null}
                      </span>
                    </div>
                    <button type="button" className="post-main" onClick={() => onOpen(post.id)}>
                      <p className="body">{post.body}</p>
                      {post.imageUrl ? <img className="thumb" src={post.imageUrl} alt="投稿画像" /> : null}
                    </button>
                    <div className="stats">
                      <span>♡ {post.likeCount}</span>
                      <button type="button" className="btn link" onClick={() => onOpen(post.id)}>
                        コメント {post.commentCount}件
                      </button>
                    </div>
                  </div>
                </article>
              ))
            : null}
          <div ref={sentinelRef} className="scroll-sentinel" />
          {tab === "all" && loadingMore ? <p className="empty">続きを読み込み中…</p> : null}
        </div>
      </section>
      {confirm ? (
        <div className="modal-bg show">
          <div className="modal">
            <h2>この投稿を削除しますか？</h2>
            <p className="lead">削除すると元に戻せません。</p>
            <div className="row-actions">
              <button type="button" className="btn ghost" onClick={() => setConfirm(null)}>
                キャンセル
              </button>
              <button type="button" className="btn danger" onClick={() => void onConfirmDelete()}>
                削除する
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </main>
  );
}

function mergeById(incoming: Post[], prev: Post[]): Post[] {
  const ids = new Set(incoming.map((p) => p.id));
  return [...incoming, ...prev.filter((p) => !ids.has(p.id))];
}

function initial(post: Post) {
  return (post.displayName || post.username).slice(0, 1);
}

function fmt(value: string) {
  const d = new Date(value.includes("T") ? value : value.replace(" ", "T"));
  if (Number.isNaN(d.getTime())) return value;
  const p = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`;
}
