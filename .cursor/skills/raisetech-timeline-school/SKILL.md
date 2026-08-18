---
name: raisetech-timeline-school
description: >-
  課題提出の学校提出ワークフロー。Skill保存、ファイル上書き、git commit、GitHub push。
  課題提出2、要件定義書、FEATURES、コマンドプッシュの話で使う。
---

# 課題提出 — 学校提出（Skill・ファイル・push）

ユーザーが「スキルして」「コマンドプッシュして」「ファイルに保管して」「ファイルに上書き保存」と言ったら、次を行う。

## 絶対ルール

- 画面の入口名は **課題提出**。おうち受付のフォルダには書かない
- GitHub は岡田法子アカウント `n0r1k09583` の **private**
- force push しない。git config は変えない
- `.env` / `*.db` / `backend/target/` / `node_modules/` / `frontend/e2e/` は Git に入れない
- 機能定義書のディレクトリは `FEATURES`（`FUTURES` ではない）

## リポジトリ

- 作業ルート: `C:\Users\user\課題提出\raisetech-timeline`
- リポジトリ: https://github.com/n0r1k09583/RaiseTechTimeline

## ファイルに保管する場所（上書き）

プロジェクト内を正とし、ユーザー側 Skill も同じ内容で上書きする。

| 内容 | プロジェクト | コピー先 |
|------|----------------|----------|
| アプリ本体Skill | `.cursor/skills/raisetech-timeline/SKILL.md` | `~/.cursor/skills/raisetech-timeline/SKILL.md` |
| 本Skill | `.cursor/skills/raisetech-timeline-school/SKILL.md` | `~/.cursor/skills/raisetech-timeline-school/SKILL.md` |
| 提出用HTML | `docs/要件定義書.html` | `~/課題提出2/` と `~/Desktop/課題提出2/` |
| 要件・機能・画面・ER・技術 | `docs/` | 同上 |
| 機能定義書 | `docs/FEATURES/機能定義書.md` | `~/課題提出2/FEATURES/` |
| 結合プロトタイプ | `prototype/` | `~/課題提出2/prototype/` |

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

- 入口は課題提出のみ（home.html）。見出しの下に「タイムライン」は出さない
- 説明文: タスク、受付、タイムラインを、課題ごとにボタンを分けて同じシンプルな画面に揃えています
- 3カードでタスク／受付／タイムラインへ。デザインはシンプル（白・細い枠・黒ボタン）
- 7機能（ログイン・タイムライン・コメント・いいね・画像・フォロー・コメント数）はプロトタイプ `node server.js` の 5178 で表示する
- 本実装は Spring Boot + MyBatis + Flyway + JWT（アクセス＋リフレッシュ）。画面は 5173。ログイン後は「ログイン成功」。タイムライン API はまだ
