export type ApiResponse<T> = {
  success: boolean;
  data: T | null;
  error: { code: string; message: string } | null;
};

export type Template = {
  id: string;
  title: string;
  category: string;
  coverUrl: string;
  description: string;
  requiredImageCount: number;
  acceptedMimeTypes: string;
  maxFileSizeBytes: number;
  enabled: boolean;
};

export type Asset = {
  id: number;
  originalFilename: string;
  mimeType: string;
  sizeBytes: number;
  url: string;
};

export type TaskStep = {
  id: number;
  taskId: string;
  stepOrder: number;
  stepName: string;
  status: string;
  startedAt?: string;
  finishedAt?: string;
  errorMessage?: string;
};

export type Task = {
  id: string;
  userId: number;
  templateId: string;
  status: string;
  currentStage?: string;
  failureStage?: string;
  failureReason?: string;
  resultUrl?: string;
  createdAt: string;
  steps: TaskStep[];
};

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080/api";

export async function apiRequest<T>(path: string, init: RequestInit = {}): Promise<T> {
  const isMultipart = typeof FormData !== "undefined" && init.body instanceof FormData;
  const response = await fetch(`${API_BASE}${path}`, {
    ...init,
    headers: { ...(isMultipart ? {} : { "Content-Type": "application/json" }), "X-Demo-User": "demo-user", ...(init.headers ?? {}) },
  });
  const payload = (await response.json()) as ApiResponse<T>;
  if (!response.ok || !payload.success || payload.data === null) {
    throw new Error(payload.error?.message ?? "请求失败");
  }
  return payload.data;
}

export async function uploadAsset(file: File, templateId?: string): Promise<Asset> {
  const formData = new FormData();
  formData.append("file", file);
  const query = templateId ? `?templateId=${encodeURIComponent(templateId)}` : "";
  return apiRequest<Asset>(`/assets${query}`, { method: "POST", body: formData, headers: {} });
}

export function imageUrl(url: string) {
  if (url.startsWith("http")) return url;
  return url.startsWith("/") ? `${API_BASE.replace(/\/api$/, "")}${url}` : url;
}
