package cn.bugstack.untitled.service;

import cn.bugstack.untitled.api.BusinessException;
import cn.bugstack.untitled.mapper.TemplateMapper;
import cn.bugstack.untitled.model.Template;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class TemplateService {
    private final TemplateMapper templateMapper;

    public TemplateService(TemplateMapper templateMapper) {
        this.templateMapper = templateMapper;
    }

    public List<Template> list(String category) {
        return templateMapper.findEnabled(category);
    }

    public Template get(String id) {
        Template template = templateMapper.findEnabledById(id);
        if (template == null) {
            throw new BusinessException("TEMPLATE_NOT_FOUND", "模板不存在或已停用", HttpStatus.NOT_FOUND);
        }
        return template;
    }
}
