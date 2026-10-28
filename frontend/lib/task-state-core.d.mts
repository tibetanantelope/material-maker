import type { Task } from "./api";
export declare function isTaskActive(task: Task): boolean;
export declare function mergeTask(tasks: Task[], updated: Task): Task[];
export declare function clearQueryError(errors: Record<string, string>, taskId: string): Record<string, string>;
export declare function setQueryError(errors: Record<string, string>, taskId: string, message: string): Record<string, string>;
