package cn.iocoder.yudao.module.steam.service.uu;

import cn.iocoder.yudao.module.steam.controller.app.vo.ApiResult;
import cn.iocoder.yudao.module.steam.controller.app.vo.OpenApiReqVo;
import cn.iocoder.yudao.module.steam.service.uu.vo.CreateCommodityOrderReqVo;
import cn.iocoder.yudao.module.steam.service.steam.YouPingOrder;
import cn.iocoder.yudao.module.steam.service.uu.vo.ApiCheckTradeUrlReSpVo;
import cn.iocoder.yudao.module.steam.service.uu.vo.ApiCheckTradeUrlReqVo;
import cn.iocoder.yudao.module.steam.service.uu.vo.ApiPayWalletRespVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * 封装所有平台调用uu的接口
 */
@Service
@Slf4j
public class UUService {
    private OpenApiService openApiService;

    @Autowired
    public void setOpenApiService(OpenApiService openApiService) {
        this.openApiService = openApiService;
    }

    /**
     * 余额查询
     * @return
     */
    public ApiResult<ApiPayWalletRespVO> getAssetsInfo() {
        ApiResult<ApiPayWalletRespVO> apiPayWalletRespVOApiResult = openApiService.requestUU("https://gw-openapi.youpin898.com/open/v1/api/getAssetsInfo", new OpenApiReqVo<Serializable>(), ApiPayWalletRespVO.class);
        return apiPayWalletRespVOApiResult;
    }
    /**
     * 验证交易链接
     * https://www.yuque.com/yyyoupin/ckahux/gy03nhuvbkg62odk
     * 1：正常交易 2:交易链接格式错误 3:请稍后重试 4:账号交易权限被封禁，无法交易 5:该交易链接已不再可用 6:该账户库存私密无法交易 7:该账号个人资料私密无法交易
     * @return
     */
    public ApiResult<ApiCheckTradeUrlReSpVo> checkTradeUrl(ApiCheckTradeUrlReqVo reqVo) {
        return openApiService.requestUU("https://gw-openapi.youpin898.com/open/v1/api/checkTradeUrl",new OpenApiReqVo<ApiCheckTradeUrlReqVo>().setData(reqVo), ApiCheckTradeUrlReSpVo.class);
    }

//    /**
//     * 指定模板购买
//     * @return
//     */
//    public YouPingOrder byTemplateCreateOrder(CreateCommodityOrderReqVo reqVo) {
//        return openApiService.requestUU("https://gw-openapi.youpin898.com/open/v1/api/byTemplateCreateOrder",new OpenApiReqVo<CreateCommodityOrderReqVo>().setData(reqVo), YouPingOrder.class);
//    }
    /**
     * 指定商品购买ID
     * @return
     */
    public YouPingOrder byGoodsIdCreateOrder(CreateCommodityOrderReqVo reqVo) {
        return null;
//        return openApiService.requestUU("https://gw-openapi.youpin898.com/open/v1/api/byGoodsIdCreateOrder",new OpenApiReqVo<CreateCommodityOrderReqVo>().setData(reqVo), YouPingOrder.class);
    }
}
