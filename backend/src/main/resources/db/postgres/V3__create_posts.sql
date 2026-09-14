CREATE TABLE posts (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  body VARCHAR(280) NOT NULL,
  image_path VARCHAR(255),
  created_at VARCHAR(32) NOT NULL DEFAULT TO_CHAR(CURRENT_TIMESTAMP, 'YYYY-MM-DD HH24:MI:SS'),
  updated_at VARCHAR(32) NOT NULL DEFAULT TO_CHAR(CURRENT_TIMESTAMP, 'YYYY-MM-DD HH24:MI:SS'),
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_posts_created_at ON posts (created_at DESC, id DESC);
CREATE INDEX idx_posts_user_id ON posts (user_id);
