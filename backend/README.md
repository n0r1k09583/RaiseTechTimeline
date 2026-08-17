# バックエンド（認証）

いま実装しているのは **ユーザー登録・ログイン・JWT・認可（/api/me）** だけ。コメント・いいね・タイムラインの API はまだ無い。

## 起動

```powershell
cd backend
copy .env.example .env
npm install
npm run dev
```

http://127.0.0.1:8080/api/health

JWT の秘密鍵は `.env` の `JWT_SECRET`。Git に書かない。

## API

| 方法 | パス | 認証 | 内容 |
|------|------|------|------|
| POST | `/api/signup` | 不要 | 登録して JWT を返す |
| POST | `/api/login` | 不要 | ログインして JWT を返す |
| GET | `/api/me` | Bearer | 今のユーザー。トークンが無い／壊れていると 401 |
| GET | `/api/health` | 不要 | 起動確認 |

ログイン・登録の成功レスポンス:

```json
{ "token": "eyJ...", "user": { "id": 1, "email": "...", "username": "yamada", "displayName": "山田" } }
```

画面は `Authorization: Bearer <token>` を付ける。パスワードは `password_digest` としてハッシュ保存し、レスポンスに出さない。

デモユーザー（空の DB のときだけ入れる）: `yamada@example.com` / `hanako@example.com` / `ichiro@example.com`、パスワード `password123`。

## 認証と認可

- **認証**: メール＋パスワードが正しいか。正しければ JWT を発行する
- **認可**: トークンがある人だけ `/api/me` を見られる。投稿・いいねは後から同じ `requireAuth` を付ける

## コメント・いいねと N+1（後で実装するとき）

N+1 問題: 親を 1 回取ったあと、各レコードごとに子を取る非効率なアクセス。

投稿一覧 50 件のあと、1 件ずつ「いいね数」「コメント数」を取るとクエリが 100 回増える。

対処:

1. **サブクエリ / JOIN で一括** … 投稿と件数を 1 本の SQL で取る
2. **バッチ** … `WHERE post_id IN (...)` で件数をまとめて取り、メモリでくっつける

コード上のメモ: `src/n-plus-one.example.ts`
