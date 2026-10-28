package cn.bugstack.untitled.service;

import cn.bugstack.untitled.mapper.TaskMapper;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class TaskProcessor {
    private static final Logger log = LoggerFactory.getLogger(TaskProcessor.class);
    public static final String ASSET_CHECK = "ASSET_CHECK";
    public static final String GENERATION = "GENERATION";
    public static final String RESULT_ASSEMBLY = "RESULT_ASSEMBLY";
    private static final List<String> STAGES = List.of(ASSET_CHECK, GENERATION, RESULT_ASSEMBLY);

    private final TaskMapper taskMapper;

    public TaskProcessor(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    @Async("taskExecutor")
    public void process(String taskId, String failStage, int stepDelayMs) {
        String normalizedFailStage = normalizeFailStage(failStage);
        int safeDelay = Math.max(0, Math.min(stepDelayMs, 60_000));
        String currentStage = ASSET_CHECK;
        try {
            for (String stage : STAGES) {
                currentStage = stage;
                taskMapper.updateTaskRunning(taskId, stage);
                taskMapper.updateStepRunning(taskId, stage);
                try {
                    if (safeDelay > 0) {
                        TimeUnit.MILLISECONDS.sleep(safeDelay);
                    }
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    fail(taskId, stage, "任务线程被中断");
                    return;
                }
                if (stage.equals(normalizedFailStage)) {
                    fail(taskId, stage, "按演示配置在该阶段失败");
                    return;
                }
                taskMapper.updateStepSucceeded(taskId, stage);
            }
            taskMapper.updateTaskSucceeded(taskId, "/demo/generated-result.svg");
        } catch (Exception exception) {
            String reason = exception.getMessage() == null || exception.getMessage().isBlank()
                    ? "任务处理发生未预期异常"
                    : "任务处理发生未预期异常：" + exception.getMessage();
            log.error("异步任务处理失败，taskId={}, stage={}", taskId, currentStage, exception);
            try {
                fail(taskId, currentStage, reason);
            } catch (Exception updateException) {
                log.error("异步任务失败状态写回失败，taskId={}, stage={}", taskId, currentStage, updateException);
            }
        }
    }

    private void fail(String taskId, String stage, String reason) {
        taskMapper.updateStepFailed(taskId, stage, reason);
        taskMapper.updateTaskFailed(taskId, stage, reason);
    }

    private String normalizeFailStage(String failStage) {
        if (failStage == null || failStage.isBlank()) {
            return "";
        }
        String normalized = failStage.trim().toUpperCase(Locale.ROOT);
        return STAGES.contains(normalized) ? normalized : "";
    }
}
