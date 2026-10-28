package cn.bugstack.untitled.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record TaskCreateRequest(
        @NotBlank(message = "templateId 不能为空") String templateId,
        @NotEmpty(message = "至少上传一个素材") @Size(max = 10, message = "素材数量不能超过 10 个") List<Long> assetIds,
        String failStage,
        Integer stepDelayMs
) {
}
