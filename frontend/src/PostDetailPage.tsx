import { useEffect, useState, type FormEvent } from "react";
import {
  createComment,
  deleteComment,
  getPost,
  listComments,
  type Comment,
  type Post,
  type User,
} from "./api";
import { AppHeader } from "./AppHeader";

type Props = {
  user: User;
  postId: number;
  onLogout: () => void | Promise<void>;
  onBack: () => void;
  onEdit: (id: number) => void;
};

export function PostDetailPage({ user, postId, onLogout, onBack, onEdit }: Props) {
  const [post, setPost] = useState<Post | null>(null);
  const [comments, setComments] = useState<Comment[]>([]);
  const [body, setBody] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [confirm, setConfirm] = useState<Comment | null>(null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError("");
    Promise.all([getPost(postId), listComments(postId)])
      .then(([nextPost, listed]) => {
        if (cancelled) return;
        setPost(nextPost);
        setComments(listed.comments);
      })
      .catch((err) => {
        if (cancelled) return;
        setError(err instanceof Error ? err.message : "読み込みに失敗しました");
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [postId]);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    const text = body.trim();
    setError("");
    if (!text || text.length > 140) {
      setError("コメントは1〜140文字です");
      return;
    }
    setBusy(true);
    try {
      const created = await createComment(postId, text);
      setComments((prev) => [...prev, created]);
      setPost((prev) => (prev ? { ...prev, commentCount: prev.commentCount + 1 } : prev));
      setBody("");
    } catch (err) {
      setError(err instanceof Error ? err.message : "送信に失敗しました");
    } finally {
      setBusy(false);
    }
  }

  async function onDelete() {
    if (!confirm) return;
    const target = confirm;
    setConfirm(null);
    try {
      await deleteComment(target.id);
      setComments((prev) => prev.filter((item) => item.id !== target.id));
      setPost((prev) => (prev ? { ...prev, commentCount: Math.max(0, prev.commentCount - 1) } : prev));
    } catch (err) {
      setError(err instanceof Error ? err.message : "削除に失敗しました");
    }
  }

  return (
    <main className="page">
      <AppHeader user={user} onLogout={onLogout} onHome={onBack} />
      <p className="back-row">
        <button type="button" className="btn link" onClick={onBack}>
          ← タイムライン
        </button>
      </p>
      {loading ? <p className="empty">読み込み中…</p> : null}
      {!loading && !post ? <p className="empty">{error || "投稿が見つかりません"}</p> : null}
      {post ? (
        <article className="card post-detail">
          <div className="post">
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
                    </>
                  ) : null}
                </span>
              </div>
              <p className="body">{post.body}</p>
              {post.imageUrl ? <img className="thumb" src={post.imageUrl} alt="投稿画像" /> : null}
              <div className="stats">
                <span>♡ {post.likeCount}</span>
                <span>コメント {post.commentCount}件</span>
              </div>
            </div>
          </div>
        </article>
      ) : null}

      {post ? (
        <section className="card">
          <h1>コメント</h1>
          {comments.length === 0 ? (
            <p className="empty">まだコメントはありません（コメント 0件）</p>
          ) : (
            comments.map((comment) => (
              <article className="comment" key={comment.id}>
                <div className="avatar">{(comment.displayName || comment.username).slice(0, 1)}</div>
                <div>
                  <div>
                    <span className="name">{comment.displayName}</span>
                    <span className="handle">@{comment.username}</span>
                    <span className="meta"> · {fmt(comment.createdAt)}</span>
                    {comment.mine ? (
                      <>
                        {" "}
                        <button type="button" className="btn link" onClick={() => setConfirm(comment)}>
                          削除
                        </button>
                      </>
                    ) : null}
                  </div>
                  <p className="body">{comment.body}</p>
                </div>
              </article>
            ))
          )}
          <form onSubmit={onSubmit}>
            <label htmlFor="comment-body">コメントを書く</label>
            <textarea
              id="comment-body"
              value={body}
              maxLength={140}
              onChange={(e) => setBody(e.target.value)}
            />
            <p className={`counter${body.length > 140 ? " over" : ""}`}>{body.length} / 140</p>
            <div className="err">{error}</div>
            <div className="row-actions">
              <button className="btn" type="submit" disabled={busy}>
                送信
              </button>
            </div>
          </form>
        </section>
      ) : null}

      {confirm ? (
        <div className="modal-bg show">
          <div className="modal">
            <h2>このコメントを削除しますか？</h2>
            <p className="lead">削除すると元に戻せません。</p>
            <div className="row-actions">
              <button type="button" className="btn ghost" onClick={() => setConfirm(null)}>
                キャンセル
              </button>
              <button type="button" className="btn danger" onClick={() => void onDelete()}>
                削除する
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </main>
  );
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
