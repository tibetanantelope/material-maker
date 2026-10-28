"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { apiRequest, type Task } from "../../lib/api";
import { TaskCard } from "../components/TaskCard";

export default function HistoryPage() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [queryErrors, setQueryErrors] = useState<Record<string, string>>({});
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;
    apiRequest<Task[]>("/tasks").then((data) => active && setTasks(data)).catch((reason: Error) => active && setError(reason.message));
    return () => { active = false; };
  }, []);

  useEffect(() => {
    const unfinished = tasks.filter((task) => task.status !== "SUCCEEDED" && task.status !== "FAILED");
    if (!unfinished.length) return;
    const timer = window.setInterval(() => {
      unfinished.forEach((task) => apiRequest<Task>(`/tasks/${task.id}`).then((updated) => {
        setTasks((current) => current.map((item) => item.id === updated.id ? updated : item));
        setQueryErrors((current) => { const next = { ...current }; delete next[task.id]; return next; });
      }).catch((reason: Error) => setQueryErrors((current) => ({ ...current, [task.id]: reason.message }))));
    }, 1500);
    return () => window.clearInterval(timer);
  }, [tasks]);

  return <main className="shell"><header className="topbar"><Link href="/templates" className="brand">素材制作工作台</Link><nav><Link href="/templates">模板</Link><Link href="/history">历史记录</Link></nav></header><section className="page-heading"><div><span className="eyebrow">HISTORY</span><h1>我的制作记录</h1><p>刷新页面后，记录从 Java 服务恢复，未完成任务会继续查询。</p></div><Link className="primary-button" href="/templates">新建任务</Link></section>{error && <p className="error-panel">{error}</p>}{!error && !tasks.length && <p className="empty-panel">还没有制作记录，先去创建一个任务吧。</p>}<section className="task-list">{tasks.map((task) => <TaskCard key={task.id} task={task} queryError={queryErrors[task.id]} />)}</section></main>;
}
