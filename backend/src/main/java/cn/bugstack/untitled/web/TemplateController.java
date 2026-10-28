package cn.bugstack.untitled.web;

import cn.bugstack.untitled.api.ApiResponse;
import cn.bugstack.untitled.model.Template;
import cn.bugstack.untitled.service.TemplateService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {
    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    public ApiResponse<List<Template>> list(@RequestParam(required = false) String category) {
        return ApiResponse.ok(templateService.list(category));
    }

    @GetMapping("/{id}")
    public ApiResponse<Template> detail(@PathVariable String id) {
        return ApiResponse.ok(templateService.get(id));
    }
}
