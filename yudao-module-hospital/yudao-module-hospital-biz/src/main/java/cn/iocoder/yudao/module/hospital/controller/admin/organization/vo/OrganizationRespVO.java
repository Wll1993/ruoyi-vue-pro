package cn.iocoder.yudao.module.hospital.controller.admin.organization.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 组织机构 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OrganizationRespVO extends OrganizationBaseVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "28439")
    private Long id;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
