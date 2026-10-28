package cn.bugstack.untitled.web;

import cn.bugstack.untitled.api.ApiResponse;
import cn.bugstack.untitled.model.TaskRecord;
import cn.bugstack.untitled.service.TaskService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ApiResponse<TaskRecord> create(
            @RequestHeader(value = "X-Demo-User", defaultValue = "demo-user") String demoUser,
            @Valid @RequestBody TaskCreateRequest request) {
        return ApiResponse.ok(taskService.create(demoUser, request));
    }

    @GetMapping("/{id}")
    public ApiResponse<TaskRecord> get(
            @RequestHeader(value = "X-Demo-User", defaultValue = "demo-user") String demoUser,
            @PathVariable String id) {
        return ApiResponse.ok(taskService.get(demoUser, id));
    }

    @GetMapping
    public ApiResponse<List<TaskRecord>> history(
            @RequestHeader(value = "X-Demo-User", defaultValue = "demo-user") String demoUser) {
        return ApiResponse.ok(taskService.history(demoUser));
    }
}
