import { useEffect, useState, type FormEvent } from "react";
import { getPost, updatePost, type User } from "./api";
import { AppHeader } from "./AppHeader";

const IMAGE_MAX = 5 * 1024 * 1024;
const IMAGE_TYPES = ["image/jpeg", "image/png", "image/webp"];

type Props = {
  user: User;
  postId: number;
  onLogout: () => void | Promise<void>;
  onDone: () => void;
};

export function EditPage({ user, postId, onLogout, onDone }: Props) {
  const [body, setBody] = useState("");
  const [image, setImage] = useState<File | null>(null);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    getPost(postId)
      .then((post) => {
        if (cancelled) return;
        if (!post.mine) {
          onDone();
          return;
        }
        setBody(post.body);
      })
      .catch((err) => {
        if (cancelled) return;
        setError(err instanceof Error ? err.message : "投稿を開けません");
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [postId, onDone]);

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
      await updatePost(postId, text, image);
      onDone();
    } catch (err) {
      setError(err instanceof Error ? err.message : "保存に失敗しました");
    } finally {
      setBusy(false);
    }
  }

  return (
    <main className="page">
      <AppHeader user={user} onLogout={onLogout} onHome={onDone} />
      <section className="card">
        <h1>投稿を編集</h1>
        <p className="lead">自分の投稿だけ編集できます。新しい画像を選ぶと差し替わります。</p>
        {loading ? (
          <p className="empty">読み込み中…</p>
        ) : (
          <form onSubmit={onSubmit}>
            <label htmlFor="edit-body">本文</label>
            <textarea
              id="edit-body"
              value={body}
              maxLength={280}
              onChange={(e) => setBody(e.target.value)}
            />
            <p className="counter">{body.length} / 280</p>
            <label htmlFor="edit-image">画像を差し替え（任意）</label>
            <input
              id="edit-image"
              type="file"
              accept="image/jpeg,image/png,image/webp"
              onChange={(e) => setImage(e.target.files?.[0] ?? null)}
            />
            <div className="err">{error}</div>
            <div className="row-actions">
              <button type="button" className="btn ghost" onClick={onDone}>
                キャンセル
              </button>
              <button className="btn" type="submit" disabled={busy}>
                保存する
              </button>
            </div>
          </form>
        )}
      </section>
    </main>
  );
}
