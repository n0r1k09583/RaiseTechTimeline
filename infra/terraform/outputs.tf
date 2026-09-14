output "cloudfront_url" {
  value = "https://${aws_cloudfront_distribution.cdn.domain_name}"
}

output "alb_dns" {
  value = aws_lb.api.dns_name
}

output "ecr_url" {
  value = aws_ecr_repository.api.repository_url
}

output "web_bucket" {
  value = aws_s3_bucket.web.bucket
}

output "images_bucket" {
  value = aws_s3_bucket.images.bucket
}
