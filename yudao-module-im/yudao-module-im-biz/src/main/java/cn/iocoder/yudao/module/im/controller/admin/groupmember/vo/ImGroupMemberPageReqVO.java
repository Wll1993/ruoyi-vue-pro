package cn.iocoder.yudao.module.im.controller.admin.groupmember.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 群成员分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ImGroupMemberPageReqVO extends PageParam {

    @Schema(description = "群 id", example = "13279")
    private Long groupId;

    @Schema(description = "用户id", example = "21730")
    private Long userId;

    @Schema(description = "昵称", example = "芋艿")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "组内显示名称", example = "芋艿")
    private String aliasName;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}