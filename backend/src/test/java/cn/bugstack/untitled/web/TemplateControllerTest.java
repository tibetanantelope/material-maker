package cn.bugstack.untitled.web;

import cn.bugstack.untitled.api.BusinessException;
import cn.bugstack.untitled.model.Template;
import cn.bugstack.untitled.service.TemplateService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TemplateController.class)
class TemplateControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TemplateService templateService;

    @Test
    void listsTemplatesWithOptionalCategory() throws Exception {
        Template template = new Template();
        template.setId("demo-postcard");
        template.setTitle("旅行明信片");
        when(templateService.list("创意图片")).thenReturn(List.of(template));

        mockMvc.perform(get("/api/templates").param("category", "创意图片"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("demo-postcard"));
    }

    @Test
    void returnsBusinessErrorWhenTemplateDoesNotExist() throws Exception {
        when(templateService.get("missing"))
                .thenThrow(new BusinessException("TEMPLATE_NOT_FOUND", "模板不存在", HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/api/templates/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("TEMPLATE_NOT_FOUND"));
    }
}
