import Link from "next/link";

export default function HomePage() {
  return (
    <main>
      <p className="eyebrow">MATERIAL MAKER</p>
      <h1>素材制作工作台</h1>
      <p>浏览模板、上传图片并提交一个可观察的异步制作任务。</p>
      <Link href="/templates">进入模板工作台 →</Link>
    </main>
  );
}
