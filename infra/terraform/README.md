# AWS 構成（定義のみ）

授業で示した形です。

| 層 | サービス |
|----|----------|
| フロント | S3 + CloudFront（Nginx / EC2 は置かない） |
| API | ECS Fargate（Spring Boot） |
| 前段 | Application Load Balancer |
| DB | RDS PostgreSQL 17（プライベートサブネット） |
| 画像 | S3。CloudFront の `/uploads/*` |

NAT Gateway は使わない。Fargate はパブリックサブネット + パブリック IP で ECR を取る。

## やってはいけないこと

- **`terraform apply` して作りっぱなしにしない。** ALB / RDS / Fargate / CloudFront は課金する。
- 誤って作ったらすぐ `terraform destroy`。
- キーを Git に書かない。Datadog は入れない。

提出の動作確認は今どおりローカル `5173` と `8080`（SQLite）で足りる。

## もし短時間だけ確認する場合

`desired_count` の既定は **0**（タスクを起こさない）。上げるときだけ:

```powershell
cd infra/terraform
terraform apply -var "desired_count=1" -var "jwt_secret=長いランダムな値"
```

終わったら:

```powershell
terraform apply -var "desired_count=0"
terraform destroy
```

画面のアップロード:

```powershell
cd frontend
npm run build
aws s3 sync dist/ s3://（web_bucket の出力）
```

API イメージ:

```powershell
cd backend
docker build -t timeline-api .
docker tag timeline-api:latest （ecr_url）:latest
aws ecr get-login-password | docker login --username AWS --password-stdin （ECR ホスト）
docker push （ecr_url）:latest
```
