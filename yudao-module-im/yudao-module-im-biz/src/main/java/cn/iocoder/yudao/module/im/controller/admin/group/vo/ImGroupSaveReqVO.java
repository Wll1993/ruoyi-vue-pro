package cn.iocoder.yudao.module.im.controller.admin.group.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 群新增/修改 Request VO")
@Data
public class ImGroupSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1003")
    private Long id;

    @Schema(description = "群名字", example = "芋艿")
    private String groupName;

    @Schema(description = "群主id", requiredMode = Schema.RequiredMode.REQUIRED, example = "31460")
    @NotNull(message = "群主id不能为空")
    private Long ownerId;

    @Schema(description = "群头像")
    private String headImage;

    @Schema(description = "群头像缩略图")
    private String headImageThumb;

    @Schema(description = "群公告")
    private String notice;

    @Schema(description = "群备注", example = "你说的对")
    private String remark;

}