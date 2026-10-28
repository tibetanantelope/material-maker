"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { apiRequest, imageUrl, type Template } from "../../lib/api";

export default function TemplatesPage() {
  const [templates, setTemplates] = useState<Template[]>([]);
  const [category, setCategory] = useState("全部");
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;
    apiRequest<Template[]>("/templates")
      .then((data) => active && setTemplates(data))
      .catch((reason: Error) => active && setError(reason.message));
    return () => { active = false; };
  }, []);

  const categories = useMemo(() => ["全部", ...Array.from(new Set(templates.map((item) => item.category)))], [templates]);
  const visibleTemplates = category === "全部" ? templates : templates.filter((item) => item.category === category);

  return (
    <main className="shell">
      <header className="topbar"><Link href="/" className="brand">素材制作工作台</Link><nav><Link href="/templates">模板</Link><Link href="/history">历史记录</Link></nav></header>
      <section className="page-heading"><div><span className="eyebrow">TEMPLATES</span><h1>选择一个模板开始制作</h1><p>模板和素材要求由 Java 服务提供，选择后即可进入上传流程。</p></div></section>
      <div className="filters" aria-label="模板分类">
        {categories.map((item) => <button className={category === item ? "filter active" : "filter"} key={item} onClick={() => setCategory(item)}>{item}</button>)}
      </div>
      {error && <p className="error-panel">{error}</p>}
      {!error && templates.length === 0 && <p className="empty-panel">正在加载模板…</p>}
      {!error && templates.length > 0 && visibleTemplates.length === 0 && <p className="empty-panel">这个分类暂无模板。</p>}
      <section className="template-grid">
        {visibleTemplates.map((template) => <Link className="template-card" href={`/templates/${template.id}`} key={template.id}>
          <div className="cover-placeholder"><img src={imageUrl(template.coverUrl)} alt={`${template.title} 示例封面`} /><span>{template.category}</span></div>
          <div className="card-body"><div className="card-kicker">{template.requiredImageCount} 张素材 · {template.acceptedMimeTypes.replaceAll(",", " / ")}</div><h2>{template.title}</h2><p>{template.description}</p><span className="text-link">查看详情 →</span></div>
        </Link>)}
      </section>
    </main>
  );
}
