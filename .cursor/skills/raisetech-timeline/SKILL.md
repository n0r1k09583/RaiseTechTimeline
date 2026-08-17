---
name: raisetech-timeline
description: >-
  課題提出（中級編）。入口は課題提出のみ。タスク・受付・タイムラインを同じシンプル画面で結合。
  プロトタイプ prototype/ :5178。本実装の認証は frontend/ と backend/（Spring Boot + MyBatis + JWT）。
  タイトルは「タスク」「受付」。にゃんこ・おうちは画面に出さない。
  Use when editing 課題提出, home.html, task.html, reception.html, timeline,
  JWT, ログイン, RaiseTech, 課題提出2.
---

# 課題提出 Skill

中級編の作品。おうち受付フォルダには書かない。提出保管は `C:\Users\user\課題提出2`。

## 入口（正）

- 表示名は **課題提出**（「課題提出2」と出さない）
- 入口: `prototype/home.html` → http://127.0.0.1:5178/home.html
- 上メニューは「課題提出」だけ。タスク／受付／タイムラインはホームのカードから入る

## 結合（見た目を揃えた3画面）

同じシンプルデザイン（白背景、細い枠、角2px、黒ボタン）。ピンク・金・影・丸ボタンは使わない。

| 画面 | ファイル | 元 |
|------|----------|-----|
| タスク | `prototype/task.html` | にゃんこタスク。画面上の名前は **タスク**（にゃんこは出さない） |
| 受付 | `prototype/reception.html` | おうち受付。画面上の名前は **受付**（おうちは出さない） |
| タイムライン | `prototype/login.html` 以降 | SNS。投稿・コメント・いいね・フォロー・検索 |

## 本実装（認証まで）

- 画面: `frontend/` React 19.2.8 / Vite 6.4.3 / TypeScript 5.7.3（ポート 5173）
- API: `backend/` Spring Boot + MyBatis + SQLite + JWT（ポート 8080）。**jOOQ 禁止。Express に戻さない**
- ログイン後は Hello World。タイムライン API はまだ
- 参考はタスクマネジメント。プラットフォームバックエンドは見ない

## やってはいけないこと

- おうち受付の Next.js をこのSNS画面に使わない
- 案内猫・にゃんこ・おうち を画面タイトルに戻さない
- `.env` / `*.db` / `backend/target/` を Git に入れない
- AWS 常時稼働・有料リソースを作らない
