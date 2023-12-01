package cn.iocoder.yudao.module.system.controller.admin.helpcenter.vo;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理后台 - 常见问题创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HelpCenterCreateReqVO extends HelpCenterBaseVO {

}
