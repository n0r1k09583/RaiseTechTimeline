# RaiseTechタイムライン — エージェント向けルール

このリポジトリは中級編の SNS。おうち受付とは別。提出保管は **課題提出2**。

## いま実装している範囲

**認証・認可（ユーザー登録・ログイン・JWT）だけ。** タイムライン・コメント・いいね・フォローはまだ。

1. 機能定義書 `docs/FEATURES/機能定義書.md`（FEATURES。FUTURES ではない）
2. バックエンド `backend/` … Express + SQLite + JWT。ポート 8080
3. 画面 `frontend/` … にゃんこタスクと同じ React 19.2.8 / Vite 6.4.3 / TypeScript 5.7.3。ポート 5173
4. プロトタイプ `prototype/` は HTML/CSS/JS の確認用。本実装の画面は `frontend/` に書く
5. コメント・いいねを足すときは `docs/n-plus-one.md`（N+1 を避ける）

起動: `backend` で `npm run dev`、`frontend` で `npm run dev`。画面は http://localhost:5173

## やってはいけないこと

- おうち受付の Next.js を画面に使わない
- JWT 秘密鍵を Git に書かない（`.env`）
- パスワードをレスポンスやログに出さない
- 投稿一覧でコメント・いいねを 1 件ずつ取る（N+1）
- AWS の常時稼働・有料リソースを作らない
