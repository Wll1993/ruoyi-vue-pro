package cn.iocoder.yudao.module.steam.controller.admin.youyoucommodity.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 悠悠商品列表分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class YouyouCommodityPageReqVO extends PageParam {

    @Schema(description = "商品id", example = "13196")
    private Integer id;

    @Schema(description = "商品模板id", example = "15581")
    private Integer templateId;

    @Schema(description = "商品名称", example = "王五")
    private String commodityName;

    @Schema(description = "商品价格（单位元）", example = "13598")
    private String commodityPrice;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "印花")
    private String commodityStickers;

    @Schema(description = "多普勒属性")
    private String commodityDoppler;

    @Schema(description = "渐变色属性")
    private String commodityFade;

    @Schema(description = "表面淬火属性")
    private String commodityHardened;

    @Schema(description = "发货状态", example = "2")
    private Integer transferStatus;

    @Schema(description = "悠悠商品是否有效0开启1关闭", example = "1")
    private Integer status;

}