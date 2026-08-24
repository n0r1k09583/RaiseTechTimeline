# RaiseTechタイムライン — エージェント向けルール

このリポジトリは中級編の SNS。おうち受付とは別。提出保管は **タイムライン**。

## いま実装している範囲

**認証と投稿（タイムライン表示・作成・編集・削除）、コメント。** いいね操作・フォロー・プロフィールはまだ。コメント数は投稿一覧の同じ SELECT で取る（N+1 にしない）。次回は `docs/next-lesson.md`（プロフィール表示・編集、フォロー・フォロワー）。

1. 機能定義書 `docs/FEATURES/機能定義書.md`（FEATURES。FUTURES ではない）
2. バックエンド `backend/` … Spring Boot + MyBatis + SQLite + JWT + Flyway。ポート 8080
3. 画面 `frontend/` … にゃんこタスクと同じ React 19.2.8 / Vite 6.4.3 / TypeScript 5.7.3。ポート 5173
4. プロトタイプ `prototype/` は HTML/CSS/JS の確認用。本実装の画面は `frontend/` に書く
5. ログイン後はタイムライン。自分の投稿・編集・削除は直後に反映。他人の変更は約30秒おきに静かに取り直す（WebSocket の一件通知は使わない）。続きは無限スクロール
6. コメント・いいねを足すときは `docs/n-plus-one.md`（N+1 を避ける）

起動: `backend` で `.\mvnw.cmd -Dmaven.test.skip=true spring-boot:run`（このフォルダで実行）、`frontend` で `npm run dev`。画面は http://127.0.0.1:5173。`.env` は無くても起動する。

## やってはいけないこと

- おうち受付の Next.js を画面に使わない
- JWT 秘密鍵を Git に書かない（`.env`）
- パスワードをレスポンスやログに出さない
- 投稿一覧でコメント・いいねを 1 件ずつ取る（N+1）
- AWS の常時稼働・有料リソースを作らない
