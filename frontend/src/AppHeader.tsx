import type { User } from "./api";

type Props = {
  user: User;
  onLogout: () => void | Promise<void>;
  onHome?: () => void;
};

export function AppHeader({ user, onLogout, onHome }: Props) {
  return (
    <header className="topbar">
      <button type="button" className="brand brand-btn" onClick={onHome}>
        課題提出
        <span className="brand-sub">タイムライン</span>
      </button>
      <div className="topbar-right">
        <span className="chip">{user.displayName}</span>
        <button type="button" className="btn ghost" id="logout-btn" onClick={onLogout}>
          ログアウト
        </button>
      </div>
    </header>
  );
}
