package cn.iocoder.yudao.module.member.controller.app.homepage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Schema(description = "用户 APP - 首页配置 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppHomePageRespVO extends AppHomePageBaseVO {

    @Schema(description = "自增编号", required = true, example = "5597")
    private Long id;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

}