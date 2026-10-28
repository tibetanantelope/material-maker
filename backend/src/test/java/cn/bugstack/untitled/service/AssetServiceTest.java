package cn.bugstack.untitled.service;

import cn.bugstack.untitled.api.BusinessException;
import cn.bugstack.untitled.mapper.AssetMapper;
import cn.bugstack.untitled.model.Template;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {
    @Mock
    private AssetMapper assetMapper;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private TemplateService templateService;

    @Test
    void rejectsMimeTypeNotAllowedByTemplate() throws Exception {
        Template template = template("image/png", 5 * 1024 * 1024L);
        when(templateService.get("png-only")).thenReturn(template);
        AssetService service = new AssetService(assetMapper, currentUserService, templateService,
                Files.createTempDirectory("asset-service-test").toString());
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[]{1});

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.upload("demo-user", file, "png-only"));

        assertEquals("ASSET_TEMPLATE_TYPE_NOT_ALLOWED", exception.getCode());
    }

    @Test
    void rejectsFileLargerThanTemplateLimit() throws Exception {
        Template template = template("image/png", 1);
        when(templateService.get("tiny")).thenReturn(template);
        AssetService service = new AssetService(assetMapper, currentUserService, templateService,
                Files.createTempDirectory("asset-service-test").toString());
        MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", new byte[]{1, 2});

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.upload("demo-user", file, "tiny"));

        assertEquals("ASSET_TEMPLATE_TOO_LARGE", exception.getCode());
    }

    private Template template(String mimeTypes, long maxFileSize) {
        Template template = new Template();
        template.setId("template");
        template.setAcceptedMimeTypes(mimeTypes);
        template.setMaxFileSizeBytes(maxFileSize);
        return template;
    }
}
