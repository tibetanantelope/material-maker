import assert from "node:assert/strict";
import test from "node:test";
import { clearQueryError, isTaskActive, mergeTask, setQueryError } from "../lib/task-state-core.mjs";

const task = (id, status) => ({ id, status });

test("updates one task without changing another task", () => {
  const first = task("first", "PROCESSING");
  const second = task("second", "WAITING");
  const updated = { ...first, status: "SUCCEEDED" };
  assert.deepEqual(mergeTask([first, second], updated), [updated, second]);
});

test("stops tracking terminal tasks", () => {
  assert.equal(isTaskActive(task("waiting", "WAITING")), true);
  assert.equal(isTaskActive(task("done", "SUCCEEDED")), false);
  assert.equal(isTaskActive(task("failed", "FAILED")), false);
});

test("stores query errors independently from task status", () => {
  const errors = setQueryError({}, "first", "network unavailable");
  assert.deepEqual(errors, { first: "network unavailable" });
  assert.deepEqual(clearQueryError(errors, "first"), {});
});

test("merges only the matching task and clears its query error", () => {
  const tasks = [task("first", "PROCESSING"), task("second", "WAITING")];
  const updated = { ...tasks[0], status: "SUCCEEDED" };
  const merged = mergeTask(tasks, updated);

  assert.equal(merged[0].status, "SUCCEEDED");
  assert.equal(merged[1].status, "WAITING");
  assert.deepEqual(clearQueryError({ first: "timeout", second: "offline" }, "first"), { second: "offline" });
});
