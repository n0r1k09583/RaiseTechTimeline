import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { TimelinePage } from "./TimelinePage";
import { createPost, deletePost, listPosts } from "./api";
import { post, user } from "./test/fixtures";

vi.mock("./api", async (importOriginal) => {
  const actual = await importOriginal<typeof import("./api")>();
  return {
    ...actual,
    listPosts: vi.fn(),
    createPost: vi.fn(),
    deletePost: vi.fn(),
  };
});

describe("TimelinePage", () => {
  beforeEach(() => {
    vi.mocked(listPosts).mockReset();
    vi.mocked(createPost).mockReset();
    vi.mocked(deletePost).mockReset();
    vi.mocked(listPosts).mockResolvedValue({ posts: [post()], hasMore: false });
  });

  it("shows a load error from the API", async () => {
    vi.mocked(listPosts).mockRejectedValue(new Error("サーバーに接続できません。バックエンドを起動してください"));
    render(<TimelinePage user={user} onLogout={vi.fn()} onEdit={vi.fn()} onOpen={vi.fn()} />);
    expect(
      await screen.findByText("サーバーに接続できません。バックエンドを起動してください"),
    ).toBeInTheDocument();
  });

  it("rejects gif on the client without calling createPost", async () => {
    const events = userEvent.setup();
    render(<TimelinePage user={user} onLogout={vi.fn()} onEdit={vi.fn()} onOpen={vi.fn()} />);
    await screen.findByText("本文です");
    await events.type(screen.getByLabelText("本文"), "画像つき");
    const gif = new File(["gif"], "x.gif", { type: "image/gif" });
    await events.upload(screen.getByLabelText("画像（任意）"), gif);
    await events.click(screen.getAllByRole("button", { name: "投稿する" }).find((el) => el.getAttribute("type") === "submit")!);
    expect(await screen.findByText("JPEG / PNG / WebP のみです")).toBeInTheDocument();
    expect(createPost).not.toHaveBeenCalled();
  });

  it("rejects an empty body without calling createPost", async () => {
    const events = userEvent.setup();
    render(<TimelinePage user={user} onLogout={vi.fn()} onEdit={vi.fn()} onOpen={vi.fn()} />);
    await screen.findByText("本文です");
    await events.click(screen.getAllByRole("button", { name: "投稿する" }).find((el) => el.getAttribute("type") === "submit")!);
    expect(await screen.findByText("本文は1〜280文字です")).toBeInTheDocument();
    expect(createPost).not.toHaveBeenCalled();
  });

  it("shows the following-tab placeholder", async () => {
    const events = userEvent.setup();
    render(<TimelinePage user={user} onLogout={vi.fn()} onEdit={vi.fn()} onOpen={vi.fn()} />);
    await screen.findByText("本文です");
    await events.click(screen.getByRole("button", { name: "フォロー中" }));
    expect(screen.getByText(/フォロー中の一覧は後続です/)).toBeInTheDocument();
  });

  it("logs out when the list API says the session expired", async () => {
    const onLogout = vi.fn();
    vi.mocked(listPosts).mockRejectedValue(new Error("ログインしてください"));
    render(<TimelinePage user={user} onLogout={onLogout} onEdit={vi.fn()} onOpen={vi.fn()} />);
    await vi.waitFor(() => expect(onLogout).toHaveBeenCalledOnce());
  });

  it("keeps the post when delete API fails", async () => {
    const events = userEvent.setup();
    vi.mocked(deletePost).mockRejectedValue(new Error("自分の投稿だけ削除できます"));
    render(<TimelinePage user={user} onLogout={vi.fn()} onEdit={vi.fn()} onOpen={vi.fn()} />);
    await screen.findByText("本文です");
    await events.click(screen.getByRole("button", { name: "削除" }));
    await events.click(screen.getByRole("button", { name: "削除する" }));
    expect(await screen.findByText("自分の投稿だけ削除できます")).toBeInTheDocument();
    expect(screen.getByText("本文です")).toBeInTheDocument();
  });
});
