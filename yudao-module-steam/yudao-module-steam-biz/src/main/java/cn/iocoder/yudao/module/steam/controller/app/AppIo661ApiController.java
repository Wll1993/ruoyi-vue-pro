package cn.iocoder.yudao.module.steam.controller.app;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.framework.pay.core.enums.channel.PayChannelEnum;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.module.infra.controller.admin.config.vo.ConfigSaveReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.config.ConfigDO;
import cn.iocoder.yudao.module.infra.service.config.ConfigService;
import cn.iocoder.yudao.module.pay.controller.admin.order.vo.PayOrderSubmitRespVO;
import cn.iocoder.yudao.module.pay.controller.app.order.vo.AppPayOrderSubmitReqVO;
import cn.iocoder.yudao.module.pay.controller.app.order.vo.AppPayOrderSubmitRespVO;
import cn.iocoder.yudao.module.pay.convert.order.PayOrderConvert;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletDO;
import cn.iocoder.yudao.module.pay.framework.pay.core.WalletPayClient;
import cn.iocoder.yudao.module.pay.service.order.PayOrderService;
import cn.iocoder.yudao.module.pay.service.wallet.PayWalletService;
import cn.iocoder.yudao.module.steam.controller.admin.invpreview.vo.InvPreviewPageReqVO;
import cn.iocoder.yudao.module.steam.controller.app.droplist.vo.AppInvPreviewReqVO;
import cn.iocoder.yudao.module.steam.controller.app.droplist.vo.AppSellingPageReqVO;
import cn.iocoder.yudao.module.steam.controller.app.droplist.vo.ItemResp;
import cn.iocoder.yudao.module.steam.controller.app.droplist.vo.SellListItemResp;
import cn.iocoder.yudao.module.steam.controller.app.vo.ApiResult;
import cn.iocoder.yudao.module.steam.controller.app.vo.OpenApiReqVo;
import cn.iocoder.yudao.module.steam.controller.app.vo.api.AppBatchGetOnSaleCommodityInfoReqVO;
import cn.iocoder.yudao.module.steam.controller.app.vo.api.AppBatchGetOnSaleCommodityInfoRespVO;
import cn.iocoder.yudao.module.steam.controller.app.vo.order.Io661OrderInfoResp;
import cn.iocoder.yudao.module.steam.controller.app.vo.order.QueryOrderReqVo;
import cn.iocoder.yudao.module.steam.controller.app.wallet.vo.PaySteamOrderCreateReqVO;
import cn.iocoder.yudao.module.steam.dal.dataobject.bindipaddress.BindIpaddressDO;
import cn.iocoder.yudao.module.steam.dal.dataobject.binduser.BindUserDO;
import cn.iocoder.yudao.module.steam.dal.dataobject.devaccount.DevAccountDO;
import cn.iocoder.yudao.module.steam.dal.dataobject.invorder.InvOrderDO;
import cn.iocoder.yudao.module.steam.dal.mysql.binduser.BindUserMapper;
import cn.iocoder.yudao.module.steam.dal.mysql.selling.SellingHashSummary;
import cn.iocoder.yudao.module.steam.enums.OpenApiCode;
import cn.iocoder.yudao.module.steam.enums.PlatFormEnum;
import cn.iocoder.yudao.module.steam.service.SteamService;
import cn.iocoder.yudao.module.steam.service.SteamWeb;
import cn.iocoder.yudao.module.steam.service.binduser.BindUserService;
import cn.iocoder.yudao.module.steam.service.fin.PaySteamOrderService;
import cn.iocoder.yudao.module.steam.service.invpreview.InvPreviewExtService;
import cn.iocoder.yudao.module.steam.service.selling.SellingsearchService;
import cn.iocoder.yudao.module.steam.service.steam.CreateOrderResult;
import cn.iocoder.yudao.module.steam.service.steam.SteamMaFile;
import cn.iocoder.yudao.module.steam.service.steam.TradeUrlStatus;
import cn.iocoder.yudao.module.steam.service.uu.OpenApiService;
import cn.iocoder.yudao.module.steam.service.uu.vo.ApiCheckTradeUrlReSpVo;
import cn.iocoder.yudao.module.steam.service.uu.vo.ApiCheckTradeUrlReqVo;
import cn.iocoder.yudao.module.steam.service.uu.vo.ApiPayWalletRespVO;
import cn.iocoder.yudao.module.steam.utils.DevAccountUtils;
import cn.iocoder.yudao.module.steam.utils.JacksonUtils;
import com.google.common.collect.Maps;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 兼容有品的开放平台接口
 * 地址基准
 * https://gw-openapi.youpin898.com/open/
 */
@Tag(name = "用户APP - 开放平台")
@RestController
@RequestMapping("io661api")
@Validated
public class AppIo661ApiController {
    @Resource
    private OpenApiService openApiService;

    @Resource
    private PayWalletService payWalletService;

    private ConfigService configService;
    @Autowired
    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }


    @Resource
    private PaySteamOrderService paySteamOrderService;
    @Resource
    private PayOrderService payOrderService;

    @Autowired
    private SellingsearchService sellingsearchService;
    private InvPreviewExtService invPreviewExtService;
    @Autowired
    public void setInvPreviewExtService(InvPreviewExtService invPreviewExtService) {
        this.invPreviewExtService = invPreviewExtService;
    }
    @Resource
    private SteamService steamService;

    /**
     * api余额接口
     * @return
     */
    @PostMapping("v1/api/getAssetsInfo")
    @Operation(summary = "余额查询")
    @PermitAll
    public ApiResult<ApiPayWalletRespVO> getAssetsInfo(@RequestBody OpenApiReqVo<Serializable> openApiReqVo) {
        try {
            return DevAccountUtils.tenantExecute(1L, () -> {
                DevAccountDO devAccount = openApiService.apiCheck(openApiReqVo);
                PayWalletDO wallet = payWalletService.getOrCreateWallet(devAccount.getUserId(), devAccount.getUserType());
                ApiPayWalletRespVO apiPayWalletRespVO=new ApiPayWalletRespVO();
                apiPayWalletRespVO.setAmount(new BigDecimal(wallet.getBalance().toString()).divide(new BigDecimal("100"),2, RoundingMode.HALF_UP));
                apiPayWalletRespVO.setUserId(devAccount.getUserId().intValue());
                return ApiResult.success(apiPayWalletRespVO);
            });
        } catch (ServiceException e) {
            return ApiResult.error(e.getCode(),  e.getMessage(),ApiPayWalletRespVO.class);
        }
    }
    /**
     * 检查交易链接
     * @return
     */
    @PostMapping("v1/api/checkTradeUrl")
    @Operation(summary = "验证交易链接")
    @PermitAll
    public ApiResult<ApiCheckTradeUrlReSpVo> checkTradeUrl(@RequestBody OpenApiReqVo<ApiCheckTradeUrlReqVo> openApiReqVo) {
        try {
            return DevAccountUtils.tenantExecute(1L, () -> {
                DevAccountDO devAccount = openApiService.apiCheck(openApiReqVo);
                BindUserDO bindUserDO = new BindUserDO();
                ConfigDO configMa = configService.getConfigByKey("steam.bot.ma");
                ConfigDO configCookie = configService.getConfigByKey("steam.bot.cookie");
                ConfigDO configPasswd = configService.getConfigByKey("steam.bot.passwd");

                bindUserDO.setSteamPassword(configPasswd.getValue());
                SteamMaFile steamMaFile = JacksonUtils.readValue(Objects.requireNonNull(configMa.getValue(),()->""), SteamMaFile.class);
                bindUserDO.setMaFile(steamMaFile);
                if(Objects.nonNull(configCookie)){
                    bindUserDO.setLoginCookie(Objects.requireNonNull(configCookie.getValue(),()->"登录密码不能为空"));
                }
                Optional<BindIpaddressDO> bindUserIp = steamService.getBindUserIp(bindUserDO);
                SteamWeb steamWeb=new SteamWeb(configService,bindUserIp);
                if(steamWeb.checkLogin(bindUserDO)){
                    ConfigSaveReqVO configSaveReqVO=new ConfigSaveReqVO();
                    if(Objects.isNull(configCookie)){
                        configSaveReqVO.setCategory("steam.bot");
                        configSaveReqVO.setName("steam.bot 机器人cookie");
                        configSaveReqVO.setKey("steam.bot.cookie");
                        configSaveReqVO.setValue(steamWeb.getCookieString());
                        configSaveReqVO.setVisible(false);
                        configService.createConfig(configSaveReqVO);
                    }else{
                        configSaveReqVO.setId(configCookie.getId());
                        configSaveReqVO.setCategory("steam.bot");
                        configSaveReqVO.setName("steam.bot 机器人cookie");
                        configSaveReqVO.setKey("steam.bot.cookie");
                        configSaveReqVO.setValue(steamWeb.getCookieString());
                        configSaveReqVO.setVisible(false);
                        configService.updateConfig(configSaveReqVO);
                    }
                }
//                steamWeb.initTradeUrl();
                TradeUrlStatus tradeUrlStatus = steamWeb.checkTradeUrl(openApiReqVo.getData().getTradeLinks());
                URI uri = URI.create(openApiReqVo.getData().getTradeLinks());
                String query = uri.getQuery();
                Map<String, String> stringStringMap = steamWeb.parseQuery(query);
                String partner = steamWeb.toCommunityID(stringStringMap.get("partner"));

                ApiCheckTradeUrlReSpVo tradeUrlReSpVo=new ApiCheckTradeUrlReSpVo();
                tradeUrlReSpVo.setSteamId(partner);
                tradeUrlReSpVo.setMsg(tradeUrlStatus.getMessage());
                tradeUrlReSpVo.setStatus(tradeUrlStatus.getStatus());
                return ApiResult.success(tradeUrlReSpVo);
            });
        } catch (ServiceException e) {
            return ApiResult.error(e.getCode(),  e.getMessage(),ApiCheckTradeUrlReSpVo.class);
        }
    }
    /**
     * 根据模板hash查询在售商品
     * @return
     */
    @PostMapping("v1/api/batchGetPriceByTemplate")
    @Operation(summary = "根据模板hash查询在售商品")
    @PermitAll
    public ApiResult<List<AppBatchGetOnSaleCommodityInfoRespVO>> batchGetPriceByTemplate(@RequestBody OpenApiReqVo<AppBatchGetOnSaleCommodityInfoReqVO> openApiReqVo) {
        List<AppBatchGetOnSaleCommodityInfoRespVO> ret=new ArrayList<>();
        try {
            return DevAccountUtils.tenantExecute(1L, () -> {
                DevAccountDO devAccount = openApiService.apiCheck(openApiReqVo);
                if(Objects.isNull(openApiReqVo.getData().getRequestList()) || openApiReqVo.getData().getRequestList().size()<=0){
                    throw new ServiceException(OpenApiCode.JACKSON_EXCEPTION);
                }
                List<AppBatchGetOnSaleCommodityInfoReqVO.RequestListDTO> requestList = openApiReqVo.getData().getRequestList();
                if(requestList.size()>=20){
                    throw new ServiceException(OpenApiCode.TO_MANY_ITEM);
                }
                List<String> collect = openApiReqVo.getData().getRequestList().stream().map(AppBatchGetOnSaleCommodityInfoReqVO.RequestListDTO::getMarketHashName).collect(Collectors.toList());
                return ApiResult.success(sellingsearchService.summaryByHashName(collect));
            });
        } catch (ServiceException e) {
            return ApiResult.error(e.getCode(),  e.getMessage(), ret);
        }
    }
    /**
     * 查询在售的模板
     * @return
     */
    @PostMapping("v1/api/batchGetOnTemplate")
    @Operation(summary = "查询在售的模板")
    @PermitAll
    public ApiResult<PageResult> batchGetOnTemplate(@RequestBody OpenApiReqVo<AppInvPreviewReqVO> openApiReqVo) {
        try {
            return DevAccountUtils.tenantExecute(1L, () -> {
                DevAccountDO devAccount = openApiService.apiCheck(openApiReqVo);
                InvPreviewPageReqVO reqVO=new InvPreviewPageReqVO();
                reqVO.setExistInv(true);
                reqVO.setItemName(openApiReqVo.getData().getItemName());
                reqVO.setSelRarity(openApiReqVo.getData().getSelRarity());
                reqVO.setSelQuality(openApiReqVo.getData().getSelQuality());

                reqVO.setSelExterior(openApiReqVo.getData().getSelExterior());
                reqVO.setSelItemset(openApiReqVo.getData().getSelItemset());
                reqVO.setPageNo(openApiReqVo.getData().getPageNo());
                reqVO.setPageSize(openApiReqVo.getData().getPageSize());
                if(reqVO.getPageSize()>200){
                    reqVO.setPageSize(200);
                }
                PageResult<ItemResp> invPreviewPage = invPreviewExtService.getInvPreviewPage(reqVO);
                return ApiResult.success(invPreviewPage);
            });
        } catch (ServiceException e) {
            return ApiResult.error(e.getCode(),  e.getMessage(),PageResult.class);
        }
    }
    /**
     * 根据模板hash查询在售商品
     * @return
     */
    @PostMapping("v1/api/batchGetOnSaleCommodityInfo")
    @Operation(summary = "根据模板hash查询在售商品")
    @PermitAll
    public ApiResult<PageResult> batchGetOnSaleCommodityInfo(@RequestBody OpenApiReqVo<AppSellingPageReqVO> openApiReqVo) {
        try {
            return DevAccountUtils.tenantExecute(1L, () -> {
                DevAccountDO devAccount = openApiService.apiCheck(openApiReqVo);

                PageResult<SellListItemResp> sellingDOPageResult = sellingsearchService.sellList(openApiReqVo.getData());
                return ApiResult.success(sellingDOPageResult);
            });
        } catch (ServiceException e) {
            return ApiResult.error(e.getCode(),  e.getMessage(),PageResult.class);
        }
    }
    @Operation(summary = "创建库存订单")
    @PostMapping("v1/api/createInvOrder")
    @PermitAll
    public ApiResult<AppPayOrderSubmitRespVO> createInvOrder(@RequestBody OpenApiReqVo<PaySteamOrderCreateReqVO> openApiReqVo) {
        try {
            return DevAccountUtils.tenantExecute(1L, () -> {
                DevAccountDO devAccount = openApiService.apiCheck(openApiReqVo);


                LoginUser loginUser = new LoginUser().setUserType(devAccount.getUserType()).setId(devAccount.getUserId()).setTenantId(1L);
                openApiReqVo.getData().setPlatform(PlatFormEnum.API);
                CreateOrderResult invOrder = paySteamOrderService.createInvOrder(loginUser, openApiReqVo.getData());

                //付款
                AppPayOrderSubmitReqVO reqVO=new AppPayOrderSubmitReqVO();
                reqVO.setChannelCode(PayChannelEnum.WALLET.getCode());
                reqVO.setId(invOrder.getPayOrderId());
                if (Objects.equals(reqVO.getChannelCode(), PayChannelEnum.WALLET.getCode())) {
                    Map<String, String> channelExtras = reqVO.getChannelExtras() == null ?
                            Maps.newHashMapWithExpectedSize(2) : reqVO.getChannelExtras();
                    channelExtras.put(WalletPayClient.USER_ID_KEY, String.valueOf(devAccount.getUserId()));
                    channelExtras.put(WalletPayClient.USER_TYPE_KEY, String.valueOf(devAccount.getUserType()));
                    reqVO.setChannelExtras(channelExtras);
                }
                // 2. 提交支付
                PayOrderSubmitRespVO respVO = payOrderService.submitOrder(reqVO, ServletUtils.getClientIP());
                AppPayOrderSubmitRespVO appPayOrderSubmitRespVO = PayOrderConvert.INSTANCE.convert3(respVO);
                InvOrderDO invOrder1 = paySteamOrderService.getInvOrder(invOrder.getBizOrderId());
                appPayOrderSubmitRespVO.setOrderNo(invOrder1.getOrderNo());
                appPayOrderSubmitRespVO.setMerchantNo(invOrder1.getMerchantNo());
                return ApiResult.success(appPayOrderSubmitRespVO);
            });
        } catch (ServiceException e) {
            e.printStackTrace();
            return ApiResult.error(e.getCode(),  e.getMessage(),AppPayOrderSubmitRespVO.class);
        }

    }
    /**
     * 查询订单详情
     * @param openApiReqVo
     * @return
     */
    @Operation(summary = "查询订单详情")
    @PostMapping("v1/api/orderStatus")
    @PermitAll
    public ApiResult<Io661OrderInfoResp> orderStatus(@RequestBody OpenApiReqVo<QueryOrderReqVo> openApiReqVo) {
        try {
            return DevAccountUtils.tenantExecute(1L, () -> {
                DevAccountDO devAccount = openApiService.apiCheck(openApiReqVo);
                LoginUser loginUser = new LoginUser().setUserType(devAccount.getUserType()).setId(devAccount.getUserId()).setTenantId(1L);
                return ApiResult.success(paySteamOrderService.getOrderInfo(openApiReqVo.getData(), loginUser));
            });
        } catch (ServiceException e) {

            return ApiResult.error(e.getCode(),  e.getMessage(),Io661OrderInfoResp.class);
        }
    }
}