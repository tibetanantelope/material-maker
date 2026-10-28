"use client";

import type { Task } from "../../lib/api";
import { imageUrl } from "../../lib/api";

const stageLabels: Record<string, string> = {
  ASSET_CHECK: "素材检查",
  GENERATION: "模拟生成",
  RESULT_ASSEMBLY: "结果整理",
};

const statusLabels: Record<string, string> = {
  WAITING: "等待处理",
  PROCESSING: "处理中",
  SUCCEEDED: "已完成",
  FAILED: "执行失败",
};

export function TaskCard({ task, queryError }: { task: Task; queryError?: string }) {
  const isFinished = task.status === "SUCCEEDED" || task.status === "FAILED";
  return (
    <article className={`task-card status-${task.status.toLowerCase()}`}>
      <div className="task-card-header">
        <div>
          <span className="task-label">任务 {task.id.slice(0, 8)}</span>
          <h3>{statusLabels[task.status] ?? task.status}</h3>
        </div>
        <span className="status-pill">{stageLabels[task.currentStage ?? ""] ?? (isFinished ? "已结束" : "排队中")}</span>
      </div>
      <div className="steps">
        {task.steps.map((step) => (
          <div className={`step step-${step.status.toLowerCase()}`} key={step.stepName}>
            <span className="step-dot" />
            <span>{stageLabels[step.stepName] ?? step.stepName}</span>
            <small>{step.status === "SUCCEEDED" ? "完成" : step.status === "PROCESSING" ? "进行中" : step.status === "FAILED" ? "失败" : "等待"}</small>
          </div>
        ))}
      </div>
      {queryError && <p className="inline-error">查询暂时不可用：{queryError}，任务状态未被修改。</p>}
      {task.status === "FAILED" && <p className="inline-error">{task.failureStage}：{task.failureReason}</p>}
      {task.resultUrl && <img className="result-image" src={imageUrl(task.resultUrl)} alt="模拟制作结果" />}
    </article>
  );
}
