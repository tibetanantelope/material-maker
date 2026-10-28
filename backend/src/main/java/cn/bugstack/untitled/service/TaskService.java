package cn.bugstack.untitled.service;

import cn.bugstack.untitled.api.BusinessException;
import cn.bugstack.untitled.mapper.TaskMapper;
import cn.bugstack.untitled.model.TaskRecord;
import cn.bugstack.untitled.model.TaskStep;
import cn.bugstack.untitled.model.Template;
import cn.bugstack.untitled.model.Asset;
import cn.bugstack.untitled.web.TaskCreateRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class TaskService {
    private final TaskMapper taskMapper;
    private final CurrentUserService currentUserService;
    private final TemplateService templateService;
    private final TaskProcessor taskProcessor;
    private final AssetService assetService;

    public TaskService(TaskMapper taskMapper,
                       CurrentUserService currentUserService,
                       TemplateService templateService,
                       TaskProcessor taskProcessor,
                       AssetService assetService) {
        this.taskMapper = taskMapper;
        this.currentUserService = currentUserService;
        this.templateService = templateService;
        this.taskProcessor = taskProcessor;
        this.assetService = assetService;
    }

    @Transactional
    public TaskRecord create(String externalUserKey, TaskCreateRequest request) {
        long userId = currentUserService.requireUserId(externalUserKey);
        Template template = templateService.get(request.templateId());
        if (request.assetIds().size() != template.getRequiredImageCount()) {
            throw new BusinessException("ASSET_COUNT_INVALID",
                    "该模板需要 " + template.getRequiredImageCount() + " 个素材",
                    HttpStatus.BAD_REQUEST);
        }
        List<Long> ownedAssetIds = taskMapper.findOwnedAssetIds(userId, request.assetIds());
        if (ownedAssetIds.size() != request.assetIds().size()) {
            throw new BusinessException("ASSET_NOT_OWNED", "存在不可用或不属于当前用户的素材", HttpStatus.BAD_REQUEST);
        }
        validateAssets(template, assetService.findOwnedByIds(userId, request.assetIds()));

        String taskId = UUID.randomUUID().toString();
        TaskRecord task = new TaskRecord();
        task.setId(taskId);
        task.setUserId(userId);
        task.setTemplateId(template.getId());
        task.setStatus("WAITING");
        taskMapper.insertTask(task);
        for (int i = 0; i < request.assetIds().size(); i++) {
            taskMapper.insertTaskAsset(taskId, request.assetIds().get(i), i + 1);
        }
        List<String> stages = List.of(TaskProcessor.ASSET_CHECK, TaskProcessor.GENERATION, TaskProcessor.RESULT_ASSEMBLY);
        for (int i = 0; i < stages.size(); i++) {
            TaskStep step = new TaskStep();
            step.setTaskId(taskId);
            step.setStepOrder(i + 1);
            step.setStepName(stages.get(i));
            step.setStatus("WAITING");
            taskMapper.insertStep(step);
        }
        task.setSteps(taskMapper.findSteps(taskId));
        int delay = request.stepDelayMs() == null ? 1500 : request.stepDelayMs();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                taskProcessor.process(taskId, request.failStage(), delay);
            }
        });
        return task;
    }

    private void validateAssets(Template template, List<Asset> assets) {
        List<String> acceptedMimeTypes = java.util.Arrays.stream(template.getAcceptedMimeTypes().split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .toList();
        for (Asset asset : assets) {
            if (!acceptedMimeTypes.contains(asset.getMimeType().toLowerCase())) {
                throw new BusinessException("ASSET_TEMPLATE_TYPE_NOT_ALLOWED",
                        "素材 " + asset.getOriginalFilename() + " 不符合该模板的格式要求", HttpStatus.BAD_REQUEST);
            }
            if (asset.getFileSizeBytes() > template.getMaxFileSizeBytes()) {
                throw new BusinessException("ASSET_TEMPLATE_TOO_LARGE",
                        "素材 " + asset.getOriginalFilename() + " 超过该模板大小限制", HttpStatus.BAD_REQUEST);
            }
        }
    }

    public TaskRecord get(String externalUserKey, String taskId) {
        long userId = currentUserService.requireUserId(externalUserKey);
        TaskRecord task = taskMapper.findByIdForUser(taskId, userId);
        if (task == null) {
            throw new BusinessException("TASK_NOT_FOUND", "任务不存在", HttpStatus.NOT_FOUND);
        }
        task.setSteps(taskMapper.findSteps(taskId));
        return task;
    }

    public List<TaskRecord> history(String externalUserKey) {
        long userId = currentUserService.requireUserId(externalUserKey);
        List<TaskRecord> tasks = taskMapper.findHistory(userId);
        tasks.forEach(task -> task.setSteps(taskMapper.findSteps(task.getId())));
        return tasks;
    }
}
