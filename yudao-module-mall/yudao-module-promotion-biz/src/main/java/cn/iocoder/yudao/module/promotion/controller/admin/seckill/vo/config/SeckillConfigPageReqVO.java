package cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.config;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

// TODO @puhui：VO 上不写注释，已经有注解啦。
/**
 * 管理后台 - 秒杀时段分页 Request VO
 *
 * @author HUIHUI
 */
@Schema(description = "管理后台 - 秒杀时段分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SeckillConfigPageReqVO extends PageParam {

    @Schema(description = "秒杀时段名称", example = "上午场")
    private String name;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
