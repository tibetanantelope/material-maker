package cn.bugstack.untitled.web;

import cn.bugstack.untitled.api.ApiResponse;
import cn.bugstack.untitled.service.AssetService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/assets")
public class AssetController {
    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<AssetService.AssetView> upload(
            @RequestHeader(value = "X-Demo-User", defaultValue = "demo-user") String demoUser,
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "templateId", required = false) String templateId) {
        return ApiResponse.ok(assetService.upload(demoUser, file, templateId));
    }
}
