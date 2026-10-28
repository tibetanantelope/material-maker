export function isTaskActive(task) {
  return task.status !== "SUCCEEDED" && task.status !== "FAILED";
}

export function mergeTask(tasks, updated) {
  return tasks.map((task) => task.id === updated.id ? updated : task);
}

export function clearQueryError(errors, taskId) {
  const next = { ...errors };
  delete next[taskId];
  return next;
}

export function setQueryError(errors, taskId, message) {
  return { ...errors, [taskId]: message };
}
