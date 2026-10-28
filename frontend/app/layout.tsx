import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "素材制作工作台",
  description: "模板、素材和异步制作任务管理工作台",
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="zh-CN">
      <body>{children}</body>
    </html>
  );
}
