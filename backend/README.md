# バックエンド（認証と投稿）

いま実装しているのは **ユーザー登録・ログイン・JWT** と **投稿（一覧・作成・編集・削除）**。コメント・いいね・フォローの API はまだ無い。開発開始後の判断は `docs/dev-changes.md`。

## 起動

```powershell
cd backend
mvn spring-boot:run
```

http://127.0.0.1:8080/api/health

JWT の秘密鍵は `.env` の `JWT_SECRET`。Git に書かない。

## API

| 方法   | パス              | 認証   | 内容                                                                    |
| ------ | ----------------- | ------ | ----------------------------------------------------------------------- |
| POST   | `/api/signup`     | 不要   | 登録して JWT を返す                                                     |
| POST   | `/api/login`      | 不要   | ログインして JWT を返す                                                 |
| POST   | `/api/refresh`    | 不要   | リフレッシュトークンを回す                                              |
| POST   | `/api/logout`     | 不要   | リフレッシュを破棄。204                                                 |
| GET    | `/api/me`         | Bearer | 今のユーザー。トークンが無い／壊れていると 401                          |
| GET    | `/api/health`     | 不要   | 起動確認                                                                |
| GET    | `/api/posts`      | Bearer | タイムライン。新しい順。無限スクロール用の `before*`、差分用の `after*` |
| GET    | `/api/posts/{id}` | Bearer | 1件                                                                     |
| POST   | `/api/posts`      | Bearer | 作成（multipart。本文必須、画像任意）                                   |
| PATCH  | `/api/posts/{id}` | Bearer | 自分の投稿だけ編集                                                      |
| DELETE | `/api/posts/{id}` | Bearer | 自分の投稿だけ削除。204                                                 |

ログイン・登録の成功レスポンス:

```json
{
  "accessToken": "eyJ...",
  "refreshToken": "...",
  "token": "eyJ...",
  "user": {
    "id": 1,
    "email": "...",
    "username": "yamada",
    "displayName": "山田"
  }
}
```

画面は `Authorization: Bearer <accessToken>` を付ける。パスワードは `password_digest` としてハッシュ保存し、レスポンスに出さない。

デモユーザー（空の DB のときだけ入れる）: `yamada@example.com` / `hanako@example.com` / `ichiro@example.com`、パスワード `password123`。

## 認証と認可

- **認証**: メール＋パスワードが正しいか。正しければ JWT を発行する
- **認可**: トークンがある人だけ投稿できる。他人の投稿の編集・削除は 403

最新の取り方: 自分の操作は直後に画面へ出す。他人の変更は WebSocket で一件通知せず、画面が約30秒おき（またはタブ復帰時）に取り直す。

## コメント・いいねと N+1（後で実装するとき）

N+1 問題: 親を 1 回取ったあと、各レコードごとに子を取る非効率なアクセス。

投稿一覧 50 件のあと、1 件ずつ「いいね数」「コメント数」を取るとクエリが 100 回増える。

対処:

1. **サブクエリ / JOIN で一括** … 投稿と件数を 1 本の SQL で取る
2. **バッチ** … `WHERE post_id IN (...)` で件数をまとめて取り、メモリでくっつける

いまの一覧は同じ SELECT に `0 AS comment_count` / `0 AS like_count` を乗せている。テーブルを足すときも投稿ごとに問い合わせない。
