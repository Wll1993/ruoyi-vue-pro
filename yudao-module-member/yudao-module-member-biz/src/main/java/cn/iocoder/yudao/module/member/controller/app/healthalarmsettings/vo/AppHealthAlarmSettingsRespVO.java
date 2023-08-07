package cn.iocoder.yudao.module.member.controller.app.healthalarmsettings.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Schema(description = "用户 APP - 体征检测雷达设置 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppHealthAlarmSettingsRespVO extends AppHealthAlarmSettingsBaseVO {

    @Schema(description = "自增编号", required = true, example = "28225")
    private Long id;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

}
