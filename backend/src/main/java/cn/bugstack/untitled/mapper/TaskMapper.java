package cn.bugstack.untitled.mapper;

import cn.bugstack.untitled.model.TaskRecord;
import cn.bugstack.untitled.model.TaskStep;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TaskMapper {
    int insertTask(TaskRecord task);
    int insertTaskAsset(@Param("taskId") String taskId, @Param("assetId") long assetId, @Param("assetOrder") int assetOrder);
    int insertStep(TaskStep step);
    int updateTaskRunning(@Param("taskId") String taskId, @Param("stage") String stage);
    int updateStepRunning(@Param("taskId") String taskId, @Param("stepName") String stepName);
    int updateStepSucceeded(@Param("taskId") String taskId, @Param("stepName") String stepName);
    int updateStepFailed(@Param("taskId") String taskId, @Param("stepName") String stepName, @Param("reason") String reason);
    int updateTaskSucceeded(@Param("taskId") String taskId, @Param("resultUrl") String resultUrl);
    int updateTaskFailed(@Param("taskId") String taskId, @Param("stage") String stage, @Param("reason") String reason);
    TaskRecord findByIdForUser(@Param("taskId") String taskId, @Param("userId") long userId);
    List<TaskRecord> findHistory(@Param("userId") long userId);
    List<TaskStep> findSteps(@Param("taskId") String taskId);
    List<Long> findOwnedAssetIds(@Param("userId") long userId, @Param("assetIds") List<Long> assetIds);
}
