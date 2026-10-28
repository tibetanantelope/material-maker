"use client";

import Link from "next/link";
import { use, useEffect, useMemo, useState, type ChangeEvent } from "react";
import { apiRequest, imageUrl, type Asset, type Task, type Template, uploadAsset } from "../../../lib/api";
import { TaskCard } from "../../components/TaskCard";

export default function TemplateDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const [template, setTemplate] = useState<Template | null>(null);
  const [assets, setAssets] = useState<Asset[]>([]);
  const [task, setTask] = useState<Task | null>(null);
  const [error, setError] = useState("");
  const [uploading, setUploading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [failStage, setFailStage] = useState("");

  useEffect(() => { apiRequest<Template>(`/templates/${id}`).then(setTemplate).catch((reason: Error) => setError(reason.message)); }, [id]);

  useEffect(() => {
    if (!task || task.status === "SUCCEEDED" || task.status === "FAILED") return;
    let active = true;
    const timer = window.setInterval(() => apiRequest<Task>(`/tasks/${task.id}`).then((data) => active && setTask(data)).catch((reason: Error) => active && setError(`任务查询失败：${reason.message}`)), 1500);
    return () => { active = false; window.clearInterval(timer); };
  }, [task]);

  const acceptedTypes = useMemo(() => template?.acceptedMimeTypes.split(",") ?? [], [template]);

  async function handleFiles(event: ChangeEvent<HTMLInputElement>) {
    const files = Array.from(event.target.files ?? []);
    if (!files.length) return;
    setUploading(true); setError("");
    try {
      const uploaded: Asset[] = [];
      for (const file of files) {
        if (!acceptedTypes.includes(file.type)) throw new Error(`不支持的文件类型：${file.name}`);
        if (template && file.size > template.maxFileSizeBytes) throw new Error(`${file.name} 超过大小限制`);
        uploaded.push(await uploadAsset(file, template?.id));
      }
      setAssets((current) => [...current, ...uploaded].slice(0, template?.requiredImageCount ?? 10));
    } catch (reason) { setError(reason instanceof Error ? reason.message : "上传失败"); }
    finally { setUploading(false); event.target.value = ""; }
  }

  async function submitTask() {
    if (!template || assets.length !== template.requiredImageCount) return;
    setSubmitting(true); setError("");
    try {
      const created = await apiRequest<Task>("/tasks", { method: "POST", body: JSON.stringify({ templateId: template.id, assetIds: assets.map((asset) => asset.id), failStage: failStage || null, stepDelayMs: 1400 }) });
      setTask(created);
    } catch (reason) { setError(reason instanceof Error ? reason.message : "任务提交失败"); }
    finally { setSubmitting(false); }
  }

  return <main className="shell"><header className="topbar"><Link href="/templates" className="brand">← 返回模板</Link><nav><Link href="/history">历史记录</Link></nav></header>
    {error && <p className="error-panel">{error}</p>}
    {!template && !error && <p className="empty-panel">正在加载模板…</p>}
    {template && <>
      <section className="detail-hero"><div className="cover-placeholder large"><img src={imageUrl(template.coverUrl)} alt={`${template.title} 示例封面`} /><span>{template.category}</span></div><div><span className="eyebrow">{template.category}</span><h1>{template.title}</h1><p>{template.description}</p><p className="requirements">需要 {template.requiredImageCount} 张图片 · 支持 {template.acceptedMimeTypes.replaceAll(",", " / ")} · 单张不超过 {Math.round(template.maxFileSizeBytes / 1024 / 1024)} MB</p></div></section>
      <section className="upload-panel"><div className="section-title"><div><span className="eyebrow">UPLOAD</span><h2>准备制作素材</h2></div><span>{assets.length} / {template.requiredImageCount}</span></div><label className="upload-drop"><input type="file" accept={template.acceptedMimeTypes} multiple onChange={handleFiles} disabled={uploading || assets.length >= template.requiredImageCount} />{uploading ? "正在上传…" : "选择图片上传"}<small>图片会保存到 Java 服务端，上传成功后才能提交制作。</small></label><div className="asset-list">{assets.map((asset) => <div className="asset-item" key={asset.id}><img src={imageUrl(asset.url)} alt={asset.originalFilename} /><span>{asset.originalFilename}</span><button type="button" onClick={() => setAssets((current) => current.filter((item) => item.id !== asset.id))}>移除</button></div>)}</div><div className="submit-row"><select aria-label="演示失败阶段" value={failStage} onChange={(event) => setFailStage(event.target.value)}><option value="">正常完成</option><option value="ASSET_CHECK">素材检查失败</option><option value="GENERATION">模拟生成失败</option><option value="RESULT_ASSEMBLY">结果整理失败</option></select><button className="primary-button" type="button" disabled={submitting || uploading || assets.length !== template.requiredImageCount || Boolean(task)} onClick={submitTask}>{submitting ? "正在受理…" : task ? "任务已受理" : "提交制作任务"}</button></div></section>
      {task && <section className="task-section"><div className="section-title"><div><span className="eyebrow">LIVE TASK</span><h2>制作进度</h2></div><Link href="/history">查看全部历史 →</Link></div><TaskCard task={task} /></section>}
    </>}
  </main>;
}
