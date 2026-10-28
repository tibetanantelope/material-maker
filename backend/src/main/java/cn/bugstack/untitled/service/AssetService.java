package cn.bugstack.untitled.service;

import cn.bugstack.untitled.api.BusinessException;
import cn.bugstack.untitled.mapper.AssetMapper;
import cn.bugstack.untitled.model.Asset;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AssetService {
    private static final long DEFAULT_MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/jpeg", "image/png");

    private final AssetMapper assetMapper;
    private final CurrentUserService currentUserService;
    private final TemplateService templateService;
    private final Path uploadRoot;

    public AssetService(AssetMapper assetMapper,
                        CurrentUserService currentUserService,
                        TemplateService templateService,
                        @Value("${app.upload-dir:./runtime/uploads}") String uploadDir) {
        this.assetMapper = assetMapper;
        this.currentUserService = currentUserService;
        this.templateService = templateService;
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public AssetView upload(String externalUserKey, MultipartFile file, String templateId) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("ASSET_EMPTY", "请选择要上传的图片", HttpStatus.BAD_REQUEST);
        }
        if (file.getSize() > DEFAULT_MAX_FILE_SIZE) {
            throw new BusinessException("ASSET_TOO_LARGE", "图片大小不能超过 10 MB", HttpStatus.BAD_REQUEST);
        }

        String mimeType = file.getContentType() == null
                ? ""
                : file.getContentType().toLowerCase(Locale.ROOT);
        if (!ALLOWED_MIME_TYPES.contains(mimeType)) {
            throw new BusinessException("ASSET_TYPE_NOT_ALLOWED", "仅支持 JPG 或 PNG 图片", HttpStatus.BAD_REQUEST);
        }
        if (templateId != null && !templateId.isBlank()) {
            var template = templateService.get(templateId);
            validateAgainstTemplate(template, mimeType, file.getSize());
        }
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null || image.getWidth() <= 0 || image.getHeight() <= 0) {
                throw new BusinessException("ASSET_CONTENT_INVALID", "上传文件不是有效图片", HttpStatus.BAD_REQUEST);
            }
        } catch (IOException exception) {
            throw new BusinessException("ASSET_CONTENT_INVALID", "图片内容无法读取", HttpStatus.BAD_REQUEST);
        }

        long userId = currentUserService.requireUserId(externalUserKey);
        String originalFilename = file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename();
        String extension = mimeType.equals("image/png") ? ".png" : ".jpg";
        String storedFilename = UUID.randomUUID() + extension;
        Path target = uploadRoot.resolve(storedFilename).normalize();
        if (!target.startsWith(uploadRoot)) {
            throw new BusinessException("ASSET_PATH_INVALID", "素材存储路径无效", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        try {
            Files.createDirectories(uploadRoot);
            file.transferTo(target);
            Asset asset = new Asset();
            asset.setUserId(userId);
            asset.setOriginalFilename(originalFilename);
            asset.setStoredFilename(storedFilename);
            asset.setStoragePath(target.toString());
            asset.setMimeType(mimeType);
            asset.setFileSizeBytes(file.getSize());
            asset.setUploadStatus("READY");
            assetMapper.insert(asset);
            return new AssetView(asset.getId(), originalFilename, mimeType, file.getSize(), "/uploads/" + storedFilename);
        } catch (IOException | RuntimeException exception) {
            try {
                Files.deleteIfExists(target);
            } catch (IOException ignored) {
                // Preserve the original upload error; cleanup is best effort.
            }
            if (exception instanceof BusinessException businessException) {
                throw businessException;
            }
            throw new BusinessException("ASSET_UPLOAD_FAILED", "素材保存失败，请稍后重试", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<Asset> findOwnedByIds(long userId, List<Long> assetIds) {
        return assetMapper.findOwnedByIds(userId, assetIds);
    }

    private void validateAgainstTemplate(cn.bugstack.untitled.model.Template template,
                                         String mimeType,
                                         long fileSize) {
        boolean mimeAllowed = java.util.Arrays.stream(template.getAcceptedMimeTypes().split(","))
                .map(String::trim)
                .anyMatch(mimeType::equalsIgnoreCase);
        if (!mimeAllowed) {
            throw new BusinessException("ASSET_TEMPLATE_TYPE_NOT_ALLOWED",
                    "该模板不支持 " + mimeType + " 格式", HttpStatus.BAD_REQUEST);
        }
        if (fileSize > template.getMaxFileSizeBytes()) {
            throw new BusinessException("ASSET_TEMPLATE_TOO_LARGE",
                    "该模板要求单张图片不超过 " + (template.getMaxFileSizeBytes() / 1024 / 1024) + " MB",
                    HttpStatus.BAD_REQUEST);
        }
    }

    public record AssetView(long id, String originalFilename, String mimeType,
                            long sizeBytes, String url) {
    }
}
