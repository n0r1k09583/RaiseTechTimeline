---
name: raisetech-timeline-school
description: >-
  課題提出の学校提出ワークフロー。Skill保存、ファイル上書き、git commit、GitHub push。
  タイムライン、要件定義書、FEATURES、コマンドプッシュの話で使う。
---

# 課題提出 — 学校提出（Skill・ファイル・push）

ユーザーが「スキルして」「コマンドプッシュして」「ファイルに保管して」「ファイルに上書き保存」と言ったら、次を行う。

## 絶対ルール

- 画面の入口名は **課題提出**。おうち受付のフォルダには書かない
- 提出用フォルダ名は **タイムライン**（「課題提出2」は使わない）
- GitHub は岡田法子アカウント `n0r1k09583` の **private**
- force push しない。git config は変えない
- `.env` / `*.db` / `backend/target/` / `node_modules/` / `frontend/e2e/` は Git に入れない
- 機能定義書のディレクトリは `FEATURES`（`FUTURES` ではない）

## リポジトリ

- 作業ルート: `C:\Users\user\おうち受付\raisetech-timeline`
- リポジトリ: https://github.com/n0r1k09583/RaiseTechTimeline

## ファイルに保管する場所（上書き）

プロジェクト内を正とし、ユーザー側 Skill も同じ内容で上書きする。

| 内容 | プロジェクト | コピー先 |
|------|----------------|----------|
| アプリ本体Skill | `.cursor/skills/raisetech-timeline/SKILL.md` | `~/.cursor/skills/raisetech-timeline/SKILL.md` |
| 本Skill | `.cursor/skills/raisetech-timeline-school/SKILL.md` | `~/.cursor/skills/raisetech-timeline-school/SKILL.md` |
| 提出用HTML | `docs/要件定義書.html` | `~/タイムライン/` と `~/Desktop/タイムライン/` |
| 要件・機能・画面・ER・技術 | `docs/` | 同上 |
| 機能定義書 | `docs/FEATURES/機能定義書.md` | `~/タイムライン/FEATURES/` |
| 結合プロトタイプ | `prototype/` | `~/タイムライン/prototype/` |
| 開発開始後の仕様 | `docs/dev-changes.md` | `~/タイムライン/` と `~/Desktop/タイムライン/`（`docs/` 配下にも） |
| 次回の授業 | `docs/next-lesson.md` | 同上 |

## コマンド（push）

```powershell
git add -A
git status
git commit -m "学校提出用: 課題提出のソースと要件定義を保管"
git push -u origin HEAD
```

リモートが無いときは:

```powershell
gh repo create RaiseTechTimeline --private --source=. --remote=origin --push
```

## 再開用（ここまでの確定）

- 提出用フォルダ名は **タイムライン**（`C:\Users\user\タイムライン` とデスクトップの `タイムライン`）
- 入口はタイムラインのログイン（login.html）。`/` も login.html へ
- タスク・受付、3カードのホームは置かない。`home.html` / `task.html` / `reception.html` は作らない
- 見た目は和風・書道風（和紙・墨・明朝）。機能は変えない
- 7機能（ログイン・タイムライン・コメント・いいね・画像・フォロー・コメント数）はプロトタイプ `node server.js` の 5178 で表示する
- 本実装は Spring Boot + MyBatis + Flyway + JWT（アクセス＋リフレッシュ）。画面は 5173。ログイン／新規登録／ログアウト／リフレッシュあり
- 起動は `backend` で `.\mvnw.cmd -Dmaven.test.skip=true spring-boot:run`（絶対パスやシステム `mvn` に頼らない）。`.env` は任意。古い `data/timeline.db` で Flyway が落ちたら消して再起動
- ログイン後は **タイムライン**（`SuccessPage.tsx` のソースは残すが、成功後の画面には使わない）
- 投稿の作成・編集・削除。自分の操作は直後に反映。続きは無限スクロール。「もっと見る」は置かない
- コメントの作成・一覧・自分の削除。件数は投稿と同じ SELECT のサブクエリ（N+1 にしない）。`docs/n-plus-one.md` は提出してよい
- 他人の変更は約30秒おき（またはタブに戻ったとき）に静かに取り直す。WebSocket の一件通知は使わない
- 機能定義書の F-03〜F-07（コメント・いいね・画像・フォロー・検索）は本文から外さない。プログラムは認証・投稿・コメントまで。いいね操作はまだ（件数は 0）。フォロー・プロフィール・検索は次回
- フォルダを開いたら `docs/next-lesson.md` から再開する。次回はプロフィール表示・編集とフォロー・フォロワー
- MyBatis は SQL を XML に分ける。複数行アノテーションは使わない（コードレビューの指摘）
- プロフィール画像は投稿画像と同じ穴（ファイルダイアログ、DevTools の制限、8080 停止、Content-Type、`/uploads` プロキシ）を先に見る
- フォローは `follows` の1行（片方向）。プロフィールは他人にトグル、自分には編集。N+1 にしない（`docs/er.md` の派生項目）
