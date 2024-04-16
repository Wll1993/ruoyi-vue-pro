package cn.iocoder.yudao.module.steam.service.fin.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 APP - 支付订单提交 Response VO")
@Data
public class ApiOrderSubmitRespVO  {
    /**
     * UU订单号
     */
    @JsonProperty("orderNo")
    private String orderNo;

    /**
     * 商户订单号
     * orderNo 和 merchantNo 二选一
     * 同时传入以 orderNo 为准
     */
    @JsonProperty("merchantNo")
    private String merchantNo;
    /**
     * 最终成交价格（单位：元）
     */
    @JsonProperty("dealPrice")
    private String dealPrice;
    /**
     * 最终成交价格（单位：分）
     */
    @JsonProperty("dealPriceFen")
    private Integer dealPriceFen;
}