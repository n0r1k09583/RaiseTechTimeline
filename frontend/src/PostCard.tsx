import { toggleLike, type Post } from "./api";
import { formatTime } from "./formatTime";

type Props = {
  post: Post;
  onOpen?: (id: number) => void;
  onEdit?: (id: number) => void;
  onDelete?: (post: Post) => void;
  onProfile: (username: string) => void;
  onLiked: (post: Post) => void;
  onError: (message: string) => void;
  large?: boolean;
};

export function PostCard({
  post,
  onOpen,
  onEdit,
  onDelete,
  onProfile,
  onLiked,
  onError,
  large,
}: Props) {
  async function onLike() {
    try {
      onLiked(await toggleLike(post.id));
    } catch (err) {
      onError(err instanceof Error ? err.message : "いいねに失敗しました");
    }
  }

  return (
    <article className="post">
      <div className="avatar">{(post.displayName || post.username).slice(0, 1)}</div>
      <div>
        <div>
          <button type="button" className="btn link name-btn" onClick={() => onProfile(post.username)}>
            <span className="name">{post.displayName}</span>
            <span className="handle">@{post.username}</span>
          </button>
          <span className="meta">
            {" "}
            · {formatTime(post.createdAt)}
            {post.mine && onEdit ? (
              <>
                {" "}
                ·{" "}
                <button type="button" className="btn link" onClick={() => onEdit(post.id)}>
                  編集
                </button>
              </>
            ) : null}
            {post.mine && onDelete ? (
              <>
                {" · "}
                <button type="button" className="btn link" onClick={() => onDelete(post)}>
                  削除
                </button>
              </>
            ) : null}
          </span>
        </div>
        {onOpen ? (
          <button type="button" className="post-main" onClick={() => onOpen(post.id)}>
            <p className="body">{post.body}</p>
            {post.imageUrl ? <img className="thumb" src={post.imageUrl} alt="投稿画像" /> : null}
          </button>
        ) : (
          <>
            <p className="body">{post.body}</p>
            {post.imageUrl ? (
              <img className={`thumb${large ? " large" : ""}`} src={post.imageUrl} alt="投稿画像" />
            ) : null}
          </>
        )}
        <div className="stats">
          <button
            type="button"
            className={`btn like${post.likedByMe ? " on" : ""}`}
            aria-pressed={post.likedByMe}
            onClick={() => void onLike()}
          >
            {post.likedByMe ? "♥" : "♡"} {post.likeCount}
          </button>
          {onOpen ? (
            <button type="button" className="btn link" onClick={() => onOpen(post.id)}>
              コメント {post.commentCount}件
            </button>
          ) : (
            <span>コメント {post.commentCount}件</span>
          )}
        </div>
      </div>
    </article>
  );
}
