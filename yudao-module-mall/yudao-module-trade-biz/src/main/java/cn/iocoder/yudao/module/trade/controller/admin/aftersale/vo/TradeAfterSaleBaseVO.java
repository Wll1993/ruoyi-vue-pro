package cn.iocoder.yudao.module.trade.controller.admin.aftersale.vo;

import cn.iocoder.yudao.module.trade.controller.admin.base.product.property.ProductPropertyValueDetailRespVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
* 交易售后 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class TradeAfterSaleBaseVO {

    @ApiModelProperty(value = "售后流水号", required = true, example = "202211190847450020500077")
    @NotNull(message = "售后流水号不能为空")
    private String no;

    @ApiModelProperty(value = "售后状态", required = true, example = "10", notes = "参见 TradeAfterSaleStatusEnum 枚举")
    @NotNull(message = "售后状态不能为空")
    private Integer status;

    @ApiModelProperty(value = "售后类型", required = true, example = "20", notes = "参见 TradeAfterSaleTypeEnum 枚举")
    @NotNull(message = "售后类型不能为空")
    private Integer type;

    @ApiModelProperty(value = "售后方式", required = true, example = "10", notes = "参见 TradeAfterSaleWayEnum 枚举")
    @NotNull(message = "售后方式不能为空")
    private Integer way;

    @ApiModelProperty(value = "用户编号", required = true, example = "30337")
    @NotNull(message = "用户编号不能为空")
    private Long userId;

    @ApiModelProperty(value = "申请原因", required = true, example = "不喜欢")
    @NotNull(message = "申请原因不能为空")
    private String applyReason;

    @ApiModelProperty(value = "补充描述", example = "你说的对")
    private String applyDescription;

    @ApiModelProperty(value = "补充凭证图片", example = "https://www.iocoder.cn/1.png")
    private List<String> applyPicUrls;

    @ApiModelProperty(value = "订单编号", required = true, example = "18078")
    @NotNull(message = "订单编号不能为空")
    private Long orderId;

    @ApiModelProperty(value = "订单流水号", required = true, example = "2022111917190001")
    @NotNull(message = "订单流水号不能为空")
    private Long orderNo;

    @ApiModelProperty(value = "订单项编号", required = true, example = "572")
    @NotNull(message = "订单项编号不能为空")
    private Long orderItemId;

    @ApiModelProperty(value = "商品 SPU 编号", required = true, example = "2888")
    @NotNull(message = "商品 SPU 编号不能为空")
    private Long spuId;

    @ApiModelProperty(value = "商品 SPU 名称", required = true, example = "李四")
    @NotNull(message = "商品 SPU 名称不能为空")
    private String spuName;

    @ApiModelProperty(value = "商品 SKU 编号", required = true, example = "15657")
    @NotNull(message = "商品 SKU 编号不能为空")
    private Long skuId;

    @ApiModelProperty(value = "商品属性数组")
    private List<ProductPropertyValueDetailRespVO> properties;

    @ApiModelProperty(value = "商品图片", example = "https://www.iocoder.cn/2.png")
    private String picUrl;

    @ApiModelProperty(value = "购买数量", required = true, example = "20012")
    @NotNull(message = "购买数量不能为空")
    private Integer count;

    @ApiModelProperty(value = "审批时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime auditTime;

    @ApiModelProperty(value = "审批人", example = "30835")
    private Long auditUserId;

    @ApiModelProperty(value = "审批备注", example = "不香")
    private String auditReason;

    @ApiModelProperty(value = "退款金额，单位：分", required = true, example = "18077")
    @NotNull(message = "退款金额，单位：分不能为空")
    private Integer refundPrice;

    @ApiModelProperty(value = "支付退款编号", example = "10271")
    private Long payRefundId;

    @ApiModelProperty(value = "退款时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime refundTime;

    @ApiModelProperty(value = "退货物流公司编号", example = "10")
    private Long logisticsId;

    @ApiModelProperty(value = "退货物流单号", example = "610003952009")
    private String logisticsNo;

    @ApiModelProperty(value = "退货时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime deliveryTime;

    @ApiModelProperty(value = "收货时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime receiveTime;

    @ApiModelProperty(value = "收货备注", example = "不喜欢")
    private String receiveReason;

}
