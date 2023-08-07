package cn.iocoder.yudao.module.member.controller.app.existalarmsettings.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "用户 APP - 人员存在感知雷达设置 Excel 导出 Request VO，参数和 ExistAlarmSettingsPageReqVO 是一致的")
@Data
public class AppExistAlarmSettingsExportReqVO {

    @Schema(description = "设备ID", example = "28742")
    private Long deviceId;

    @Schema(description = "进入告警")
    private Boolean enter;

    @Schema(description = "离开告警")
    private Boolean leave;

    @Schema(description = "人员滞留报警")
    private Boolean stop;

    @Schema(description = "无人报警")
    private Boolean nobody;
    @Schema(description = "通知方式 0-不通知,1-短信,2-电话,3-电话短信,4-微信通知")
    private Byte notice;

    @Schema(description = "滞留时间 单位分钟")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Byte[] stopTime;

    @Schema(description = "无人时间 单位分钟")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private Byte[] nobodyTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}