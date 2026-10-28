package cn.bugstack.untitled.service;

import static org.mockito.Mockito.inOrder;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import cn.bugstack.untitled.mapper.TaskMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskProcessorTest {
    @Mock
    private TaskMapper taskMapper;

    @Test
    void processesThreeStagesInOrderAndStoresResult() {
        new TaskProcessor(taskMapper).process("task-1", null, 0);

        InOrder order = inOrder(taskMapper);
        verifyStageSucceeded(order, "task-1", TaskProcessor.ASSET_CHECK);
        verifyStageSucceeded(order, "task-1", TaskProcessor.GENERATION);
        verifyStageSucceeded(order, "task-1", TaskProcessor.RESULT_ASSEMBLY);
        order.verify(taskMapper).updateTaskSucceeded("task-1", "/demo/generated-result.svg");
    }

    @Test
    void stopsLaterStagesWhenConfiguredStageFails() {
        new TaskProcessor(taskMapper).process("task-2", TaskProcessor.GENERATION, 0);

        InOrder order = inOrder(taskMapper);
        verifyStageSucceeded(order, "task-2", TaskProcessor.ASSET_CHECK);
        order.verify(taskMapper).updateTaskRunning("task-2", TaskProcessor.GENERATION);
        order.verify(taskMapper).updateStepRunning("task-2", TaskProcessor.GENERATION);
        order.verify(taskMapper).updateStepFailed("task-2", TaskProcessor.GENERATION, "按演示配置在该阶段失败");
        order.verify(taskMapper).updateTaskFailed("task-2", TaskProcessor.GENERATION, "按演示配置在该阶段失败");
        verify(taskMapper, never()).updateTaskRunning("task-2", TaskProcessor.RESULT_ASSEMBLY);
        verify(taskMapper, never()).updateTaskSucceeded("task-2", "/demo/generated-result.svg");
    }

    @Test
    void marksTaskFailedWhenUnexpectedStageExceptionOccurs() {
        doAnswer(invocation -> {
            if (TaskProcessor.GENERATION.equals(invocation.getArgument(1))) {
                throw new IllegalStateException("模拟异常");
            }
            return 1;
        }).when(taskMapper).updateStepSucceeded(anyString(), anyString());

        new TaskProcessor(taskMapper).process("task-3", null, 0);

        verify(taskMapper).updateStepFailed("task-3", TaskProcessor.GENERATION,
                "任务处理发生未预期异常：模拟异常");
        verify(taskMapper).updateTaskFailed("task-3", TaskProcessor.GENERATION,
                "任务处理发生未预期异常：模拟异常");
    }

    private void verifyStageSucceeded(InOrder order, String taskId, String stage) {
        order.verify(taskMapper).updateTaskRunning(taskId, stage);
        order.verify(taskMapper).updateStepRunning(taskId, stage);
        order.verify(taskMapper).updateStepSucceeded(taskId, stage);
    }
}
