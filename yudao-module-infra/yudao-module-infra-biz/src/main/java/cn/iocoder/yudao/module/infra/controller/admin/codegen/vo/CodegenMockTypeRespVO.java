package cn.iocoder.yudao.module.infra.controller.admin.codegen.vo;

import cn.hutool.core.util.NumberUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Schema(description = "管理后台 - 模拟类型 Response VO")
@Data
public class CodegenMockTypeRespVO implements Comparable<CodegenMockTypeRespVO> {

    @Schema(description = "类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

    @Schema(description = "标签", requiredMode = Schema.RequiredMode.REQUIRED, example = "不模拟")
    private String lable;

    @Override
    public int compareTo(@NotNull CodegenMockTypeRespVO o) {
        return NumberUtil.compare(type, o.getType());
    }
}
