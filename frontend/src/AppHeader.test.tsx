import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { AppHeader } from "./AppHeader";
import { user } from "./test/fixtures";

describe("AppHeader", () => {
  it("shows the display name and brand", () => {
    render(<AppHeader user={user} onLogout={vi.fn()} />);
    expect(screen.getByText("山田")).toBeInTheDocument();
    expect(screen.getByText("課題提出")).toBeInTheDocument();
    expect(screen.getByText("タイムライン")).toBeInTheDocument();
  });

  it("calls logout", async () => {
    const onLogout = vi.fn();
    const events = userEvent.setup();
    render(<AppHeader user={user} onLogout={onLogout} />);
    await events.click(screen.getByRole("button", { name: "ログアウト" }));
    expect(onLogout).toHaveBeenCalledOnce();
  });

  it("calls onHome from the brand button", async () => {
    const onHome = vi.fn();
    const events = userEvent.setup();
    render(<AppHeader user={user} onLogout={vi.fn()} onHome={onHome} />);
    await events.click(screen.getByRole("button", { name: /課題提出/ }));
    expect(onHome).toHaveBeenCalledOnce();
  });
});
