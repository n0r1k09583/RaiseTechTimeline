package com.raisetech.timeline.web;

import com.raisetech.timeline.config.OpenApiConfig;
import com.raisetech.timeline.dto.ErrorResponse;
import com.raisetech.timeline.dto.PostListResponse;
import com.raisetech.timeline.dto.PostResponse;
import com.raisetech.timeline.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/posts")
@Tag(name = "投稿", description = "タイムラインの一覧・作成・編集・削除。画像は任意1枚（いまはローカル uploads/）。")
@SecurityRequirement(name = OpenApiConfig.BEARER)
@ApiResponses({
  @ApiResponse(
      responseCode = "401",
      description = "未ログイン、またはトークン無効",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ErrorResponse.class)))
})
public class PostController {

  private static final String ERROR_JSON = MediaType.APPLICATION_JSON_VALUE;

  private final PostService posts;

  public PostController(PostService posts) {
    this.posts = posts;
  }

  @GetMapping
  @Operation(
      summary = "タイムライン",
      description =
          """
          新しい順。続き（無限スクロール）は beforeCreatedAt + beforeId。
          新しい差分は afterCreatedAt + afterId。
          tab=following はフォロー表がまだ無いので空案内になる。
          """)
  public PostListResponse list(
      HttpServletRequest request,
      @Parameter(description = "all=全投稿。following=フォロー中（未実装のため空）")
          @RequestParam(defaultValue = "all")
          String tab,
      @Parameter(description = "件数。省略時20、最大50") @RequestParam(required = false) Integer limit,
      @Parameter(description = "これより古い投稿を取る（続き）") @RequestParam(required = false)
          String beforeCreatedAt,
      @Parameter(description = "同じ時刻のときのタイブレーク") @RequestParam(required = false) Long beforeId,
      @Parameter(description = "これより新しい投稿を取る（静かな取り直し）") @RequestParam(required = false)
          String afterCreatedAt,
      @Parameter(description = "同じ時刻のときのタイブレーク") @RequestParam(required = false) Long afterId) {
    long userId = userId(request);
    return posts.list(userId, tab, limit, beforeCreatedAt, beforeId, afterCreatedAt, afterId);
  }

  @GetMapping("/{id}")
  @Operation(summary = "投稿1件", description = "編集画面・詳細用。無いと 404。")
  @ApiResponse(
      responseCode = "404",
      description = "投稿が無い",
      content = @Content(mediaType = ERROR_JSON, schema = @Schema(implementation = ErrorResponse.class)))
  public PostResponse get(
      HttpServletRequest request, @Parameter(description = "投稿ID") @PathVariable long id) {
    return posts.get(userId(request), id);
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = "投稿を作成",
      description =
          """
          multipart/form-data。body 必須（1〜280文字）。image 任意。
          画像は JPEG / PNG / WebP、5MB。実体は uploads/、DB にはファイル名。
          応答の imageUrl は /uploads/ファイル名。S3 に出すときはこの URL だけ差し替える。
          """)
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "作成した投稿。画面は先頭へすぐ出す"),
    @ApiResponse(
        responseCode = "400",
        description = "本文不正、または画像が JPEG/PNG/WebP でない",
        content = @Content(mediaType = ERROR_JSON, schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "413",
        description = "画像が5MB超。`画像は5MBまでです`",
        content = @Content(mediaType = ERROR_JSON, schema = @Schema(implementation = ErrorResponse.class)))
  })
  public PostResponse create(
      HttpServletRequest request,
      @Parameter(description = "本文 1〜280文字", required = true) @RequestParam String body,
      @Parameter(description = "任意。JPEG / PNG / WebP、5MBまで") @RequestParam(required = false)
          MultipartFile image) {
    return posts.create(userId(request), body, image);
  }

  @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "自分の投稿を編集", description = "他人の投稿は 403。無いと 404。multipart。")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "更新後の投稿"),
    @ApiResponse(
        responseCode = "400",
        description = "本文不正、または画像形式不正",
        content = @Content(mediaType = ERROR_JSON, schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "他人の投稿は編集できない",
        content = @Content(mediaType = ERROR_JSON, schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "投稿が無い",
        content = @Content(mediaType = ERROR_JSON, schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "413",
        description = "画像が5MB超",
        content = @Content(mediaType = ERROR_JSON, schema = @Schema(implementation = ErrorResponse.class)))
  })
  public PostResponse update(
      HttpServletRequest request,
      @Parameter(description = "投稿ID") @PathVariable long id,
      @Parameter(description = "本文 1〜280文字", required = true) @RequestParam String body,
      @Parameter(description = "任意。省略時は既存画像のまま") @RequestParam(required = false)
          MultipartFile image) {
    return posts.update(userId(request), id, body, image);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "自分の投稿を削除", description = "他人は 403。204。画像ファイルも消す。")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "削除した。本文なし"),
    @ApiResponse(
        responseCode = "403",
        description = "他人の投稿は削除できない",
        content = @Content(mediaType = ERROR_JSON, schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "投稿が無い",
        content = @Content(mediaType = ERROR_JSON, schema = @Schema(implementation = ErrorResponse.class)))
  })
  public void delete(
      HttpServletRequest request, @Parameter(description = "投稿ID") @PathVariable long id) {
    posts.delete(userId(request), id);
  }

  private static long userId(HttpServletRequest request) {
    return (Long) request.getAttribute(AuthInterceptor.USER_ID_ATTR);
  }
}
