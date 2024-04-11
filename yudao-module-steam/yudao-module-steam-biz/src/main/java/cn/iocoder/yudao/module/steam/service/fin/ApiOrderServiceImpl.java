package cn.iocoder.yudao.module.steam.service.fin;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.module.infra.dal.dataobject.config.ConfigDO;
import cn.iocoder.yudao.module.infra.service.config.ConfigService;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletTransactionDO;
import cn.iocoder.yudao.module.pay.dal.redis.no.PayNoRedisDAO;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderStatusEnum;
import cn.iocoder.yudao.module.pay.enums.wallet.PayWalletBizTypeEnum;
import cn.iocoder.yudao.module.pay.service.wallet.PayWalletService;
import cn.iocoder.yudao.module.steam.controller.app.vo.ApiResult;
import cn.iocoder.yudao.module.steam.controller.app.vo.OpenApiReqVo;
import cn.iocoder.yudao.module.steam.controller.app.vo.order.OrderCancelVo;
import cn.iocoder.yudao.module.steam.controller.app.vo.order.OrderInfoResp;
import cn.iocoder.yudao.module.steam.controller.app.vo.order.QueryOrderReqVo;
import cn.iocoder.yudao.module.steam.dal.dataobject.apiorder.ApiOrderDO;
import cn.iocoder.yudao.module.steam.dal.dataobject.binduser.BindUserDO;
import cn.iocoder.yudao.module.steam.dal.dataobject.devaccount.DevAccountDO;
import cn.iocoder.yudao.module.steam.dal.dataobject.youyounotify.YouyouNotifyDO;
import cn.iocoder.yudao.module.steam.dal.dataobject.youyouorder.YouyouOrderDO;
import cn.iocoder.yudao.module.steam.dal.mysql.apiorder.ApiOrderMapper;
import cn.iocoder.yudao.module.steam.dal.mysql.devaccount.DevAccountMapper;
import cn.iocoder.yudao.module.steam.dal.mysql.youyounotify.YouyouNotifyMapper;
import cn.iocoder.yudao.module.steam.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.steam.enums.OpenApiCode;
import cn.iocoder.yudao.module.steam.enums.PlatCodeEnum;
import cn.iocoder.yudao.module.steam.enums.PlatFormEnum;
import cn.iocoder.yudao.module.steam.service.SteamService;
import cn.iocoder.yudao.module.steam.service.fin.vo.ApiBuyItemRespVo;
import cn.iocoder.yudao.module.steam.service.fin.vo.ApiCommodityRespVo;
import cn.iocoder.yudao.module.steam.service.fin.vo.ApiOrderCancelRespVo;
import cn.iocoder.yudao.module.steam.service.fin.vo.ApiQueryCommodityReqVo;
import cn.iocoder.yudao.module.steam.service.steam.InvSellCashStatusEnum;
import cn.iocoder.yudao.module.steam.service.steam.InvTransferStatusEnum;
import cn.iocoder.yudao.module.steam.service.uu.UUService;
import cn.iocoder.yudao.module.steam.service.uu.vo.ApiCheckTradeUrlReSpVo;
import cn.iocoder.yudao.module.steam.service.uu.vo.ApiCheckTradeUrlReqVo;
import cn.iocoder.yudao.module.steam.service.uu.vo.notify.NotifyReq;
import cn.iocoder.yudao.module.steam.service.uu.vo.notify.NotifyVo;
import cn.iocoder.yudao.module.steam.utils.HttpUtil;
import cn.iocoder.yudao.module.steam.utils.JacksonUtils;
import cn.iocoder.yudao.module.steam.utils.RSAUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 多平台下单接口
 * 因需要两服务费和商品费用分开收取，所以这里不再使用以前的订单，直接走钱包支付
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class ApiOrderServiceImpl implements ApiOrderService {

    /**
     * 接入的实力应用编号
     *
     * 从 [支付管理 -> 应用信息] 里添加
     */
    private static final Long UU_CASH_ACCOUNT_ID = 250L;//UU收款账号ID
    /**
     * 服务费收费帐号
     *
     * 从 [支付管理 -> 应用信息] 里添加
     */
    private static final Long UU_CASH_SERVICE_ID = 251L;//UU收款账号ID
    /**
     * 支付单流水的 no 前缀
     */
    private static final String PAY_NO_PREFIX = "YY";


    @Resource
    private PayNoRedisDAO noRedisDAO;



    private SteamService steamService;

    @Autowired
    public void setSteamService(SteamService steamService) {
        this.steamService = steamService;
    }


    @Resource
    private UUService uuService;



    private PayWalletService payWalletService;
    @Autowired
    public void setPayWalletService(PayWalletService payWalletService) {
        this.payWalletService = payWalletService;
    }

    @Resource
    private ApiOrderMapper apiOrderMapper;

    @Resource
    private YouyouNotifyMapper youyouNotifyMapper;

    @Resource
    private ConfigService configService;


    @Resource
    private DevAccountMapper devAccountMapper;

    List<ApiThreeOrderService> apiThreeOrderServiceList;

    @Autowired
    public void setApiThreeOrderServiceList(List<ApiThreeOrderService> apiThreeOrderServiceList) {
        this.apiThreeOrderServiceList = apiThreeOrderServiceList;
    }


    public ApiOrderServiceImpl() {
    }
    private Optional<ApiThreeOrderService> getApiThreeByOrder(ApiOrderDO apiOrderDO){
        return apiThreeOrderServiceList.stream().filter(item->item.getPlatCode().getCode().equals(apiOrderDO.getPlatCode())).findFirst();
    }
    private Optional<ApiThreeOrderService> getApiThreeByPlatCode(PlatCodeEnum platCodeEnum){
        return apiThreeOrderServiceList.stream().filter(item->item.getPlatCode().equals(platCodeEnum)).findFirst();
    }

    @Override
    public ApiOrderDO createInvOrder(LoginUser loginUser, ApiQueryCommodityReqVo reqVo) {
        if(Objects.isNull(loginUser)){
            throw new ServiceException(OpenApiCode.ID_ERROR);
        }
        //买家身份检测
        BindUserDO buyBindUserDO=null;
        if(StringUtils.hasText(reqVo.getTradeLinks())){
            buyBindUserDO = steamService.getTempBindUserByLogin(loginUser, reqVo.getTradeLinks(),true);
        }
        if(Objects.isNull(buyBindUserDO)){
            throw new ServiceException(OpenApiCode.ERR_5201);
        }

        ApiOrderDO orderDO=new ApiOrderDO()
                //设置买家
                .setBuyBindUserId(loginUser.getId()).setBuyUserType(loginUser.getUserType())
                .setBuyBindUserId(buyBindUserDO.getId()).setBuySteamId(buyBindUserDO.getSteamId()).setBuyTradeLinks(buyBindUserDO.getTradeUrl())
                //设置卖家信息
                //服务费账号
                .setServiceFeeUserId(UU_CASH_SERVICE_ID).setServiceFeeUserType(UserTypeEnum.MEMBER.getValue())
                .setCashStatus(InvSellCashStatusEnum.INIT.getStatus())
                //订单信息
                .setOrderNo(noRedisDAO.generate(PAY_NO_PREFIX)).setMerchantOrderNo(reqVo.getMerchantOrderNo())
                //设置支付信息
                .setPayOrderStatus(PayOrderStatusEnum.WAITING.getStatus())
                //设置购买信息
                .setBuyInfo(reqVo);
        validateInvOrderCanCreate(loginUser,orderDO);
        apiOrderMapper.insert(orderDO);
        return orderDO;
    }

    /**
     * 未支付订单取消支付并释放库存
     * @param invOrderId 订单号
     */
    public void closeUnPayInvOrder(Long invOrderId) {
        ApiOrderDO uuOrder = getUUOrderById(invOrderId);
        if (PayOrderStatusEnum.isSuccess(uuOrder.getPayOrderStatus())) {
            throw new ServiceException(-1,"订单已支付不支持关闭");
        }

        apiOrderMapper.updateById(new ApiOrderDO().setId(invOrderId).setTransferStatus(InvTransferStatusEnum.CLOSE.getStatus())
                    .setPayOrderStatus(PayOrderStatusEnum.CLOSED.getStatus()));
        //释放库存
        if(Objects.nonNull(uuOrder.getThreeOrderNo())){
            Optional<ApiThreeOrderService> apiThreeByOrder = getApiThreeByOrder(uuOrder);
            if(apiThreeByOrder.isPresent()){
                ApiThreeOrderService apiThreeOrderService = apiThreeByOrder.get();
                LoginUser loginUser = new LoginUser().setId(uuOrder.getBuyUserId()).setUserType(uuOrder.getBuyUserType());
                ApiOrderCancelRespVo apiOrderCancelRespVo = apiThreeOrderService.releaseIvn(loginUser, uuOrder.getThreeOrderNo(), uuOrder.getId());
                if(!apiOrderCancelRespVo.getIsSuccess()) {
                    throw new ServiceException(apiOrderCancelRespVo.getErrorCode());
                }
            }
        }
    }
    /**
     * 释放库存
     * 已经支付的订单退还库存
     * 请不要直接调用
     */
    @Override
    public void releaseInvOrder(Long invOrderId) {
        ApiOrderDO uuOrder = getUUOrderById(invOrderId);
        if(uuOrder.getCashStatus().equals(InvSellCashStatusEnum.CASHED.getStatus())){
            throw new ServiceException(-1,"订单已经支付给卖家，不支持操作");
        }
        if(uuOrder.getCashStatus().equals(InvSellCashStatusEnum.DAMAGES.getStatus())){
            throw new ServiceException(-1,"订单已经支付违约金，不支持操作");
        }
        if (!PayOrderStatusEnum.isSuccess(uuOrder.getPayOrderStatus())) {
            throw new ServiceException(-1,"不支持未支付的订单关闭");
        }
        apiOrderMapper.updateById(new ApiOrderDO().setId(invOrderId).setTransferStatus(InvTransferStatusEnum.CLOSE.getStatus()));
        //释放库存
        if(Objects.nonNull(uuOrder.getThreeOrderNo())){
            Optional<ApiThreeOrderService> apiThreeByOrder = getApiThreeByOrder(uuOrder);
            if(apiThreeByOrder.isPresent()){
                ApiThreeOrderService apiThreeOrderService = apiThreeByOrder.get();
                LoginUser loginUser = new LoginUser().setId(uuOrder.getBuyUserId()).setUserType(uuOrder.getBuyUserType());
                ApiOrderCancelRespVo apiOrderCancelRespVo = apiThreeOrderService.releaseIvn(loginUser, uuOrder.getThreeOrderNo(), uuOrder.getId());
                if(!apiOrderCancelRespVo.getIsSuccess()) {
                    throw new ServiceException(apiOrderCancelRespVo.getErrorCode());
                }
            }
        }
    }


    /**
     * 订单失败后退款
     * 扣除商品的2%后退还买家
     * @param invOrderId InvOrderId
     * @param reason 本次原因
     */
    @Transactional(rollbackFor = ServiceException.class)
    public void damagesCloseInvOrder(Long invOrderId,String reason) {
        ApiOrderDO uuOrderById = getUUOrderById(invOrderId);
        if(Objects.isNull(uuOrderById)){
            throw new ServiceException(OpenApiCode.JACKSON_EXCEPTION);
        }
        if (!PayOrderStatusEnum.isSuccess(uuOrderById.getPayOrderStatus())) {
            throw new ServiceException(-1,"订单未支付不支持退款");
        }
        releaseInvOrder(invOrderId);
        if (PayOrderStatusEnum.isSuccess(uuOrderById.getPayOrderStatus())) {
//            Integer paymentAmount = uuOrderById.getPayAmount();
//            BigDecimal divide = new BigDecimal("2").divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
//            int transferDamagesAmount = divide.multiply(new BigDecimal(paymentAmount.toString())).intValue();
//            int transferRefundAmount = paymentAmount - transferDamagesAmount;
//            //打款违约金-打款到服务费用户,取消订单后卖家应收的违约金由uu代扣，这里只需要将金额扣给平台
//            PayWalletDO orCreateWallet = payWalletService.getOrCreateWallet(uuOrderById.getServiceFeeUserId(), uuOrderById.getServiceFeeUserType());
//            PayWalletTransactionDO payWalletTransactionDO = payWalletService.addWalletBalance(orCreateWallet.getId(), String.valueOf(uuOrderById.getId()),
//                    PayWalletBizTypeEnum.INV_DAMAGES, transferDamagesAmount);
//            //获取买家钱包并进行退款
//            PayWalletDO orCreateWallet2 = payWalletService.getOrCreateWallet(uuOrderById.getBuyUserId(), uuOrderById.getBuyUserType());
//            PayWalletTransactionDO payWalletTransactionDO1 = payWalletService.addWalletBalance(orCreateWallet2.getId(), String.valueOf(uuOrderById.getId()),
//                    PayWalletBizTypeEnum.STEAM_REFUND, transferRefundAmount);

            //不收违约金
            PayWalletDO orCreateWallet = payWalletService.getOrCreateWallet(uuOrderById.getBuyUserId(), uuOrderById.getBuyUserType());
            PayWalletTransactionDO payWalletTransactionDO = payWalletService.addWalletBalance(orCreateWallet.getId(), String.valueOf(uuOrderById.getId()),
                    PayWalletBizTypeEnum.INV_SERVICE_FEE_REFUND, uuOrderById.getServiceFee());
            //获取买家钱包并进行退款
//            PayWalletDO orCreateWallet2 = payWalletService.getOrCreateWallet(uuOrderById.getBuyUserId(), uuOrderById.getBuyUserType());
            PayWalletTransactionDO payWalletTransactionDO1 = payWalletService.addWalletBalance(orCreateWallet.getId(), String.valueOf(uuOrderById.getId()),
                    PayWalletBizTypeEnum.STEAM_CASH_REFUND, uuOrderById.getCommodityAmount());

            List<PayWalletTransactionDO> payWalletTransactionDOS = Arrays.asList(payWalletTransactionDO, payWalletTransactionDO1);
            apiOrderMapper.updateById(new ApiOrderDO().setId(uuOrderById.getId())
//                    .setTransferDamagesAmount(transferDamagesAmount)
//                    .setTransferRefundAmount(transferRefundAmount)
//                    .setTransferRefundAmount(uuOrderById.getPayAmount())//不收手续费
//                    .setTransferDamagesTime(LocalDateTime.now())
//                    .setTransferDamagesRet(JacksonUtils.writeValueAsString(payWalletTransactionDOS))
                    .setCashStatus(InvSellCashStatusEnum.DAMAGES.getStatus())
                    .setCancelReason(reason)
            );
        }
    }

    /**
     * 订单完成后打款
     * @param invOrderId 订单号
     */
    @Transactional(rollbackFor = ServiceException.class)
    public void cashInvOrder(Long invOrderId) {
        ApiOrderDO uuOrderById = getUUOrderById(invOrderId);
        if(Objects.isNull(uuOrderById)){
            throw new ServiceException(OpenApiCode.JACKSON_EXCEPTION);
        }
        if (PayOrderStatusEnum.isSuccess(uuOrderById.getPayOrderStatus())) {
            //打款服务费
            PayWalletDO orCreateWallet = payWalletService.getOrCreateWallet(uuOrderById.getServiceFeeUserId(), uuOrderById.getServiceFeeUserType());
            PayWalletTransactionDO payWalletTransactionDO = payWalletService.addWalletBalance(orCreateWallet.getId(), String.valueOf(uuOrderById.getId()),
                    PayWalletBizTypeEnum.INV_SERVICE_FEE, uuOrderById.getServiceFee());

            apiOrderMapper.updateById(new ApiOrderDO().setId(invOrderId).setServiceFeeRet(JacksonUtils.writeValueAsString(payWalletTransactionDO)));
            //获取卖家家钱包并进行打款
            PayWalletDO orCreateWallet2 = payWalletService.getOrCreateWallet(uuOrderById.getSellUserId(), uuOrderById.getSellUserType());
            PayWalletTransactionDO payWalletTransactionDO1 = payWalletService.addWalletBalance(orCreateWallet2.getId(), String.valueOf(uuOrderById.getId()),
                    PayWalletBizTypeEnum.STEAM_CASH, uuOrderById.getCommodityAmount());
            apiOrderMapper.updateById(new ApiOrderDO().setId(invOrderId).setCashRet(JacksonUtils.writeValueAsString(payWalletTransactionDO1)).setCashStatus(InvSellCashStatusEnum.CASHED.getStatus()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiOrderDO payInvOrder(LoginUser loginUser, Long invOrderId) {
        QueryOrderReqVo queryOrderReqVo=new QueryOrderReqVo();
        queryOrderReqVo.setId(invOrderId);
        ApiOrderDO uuOrder = getOrder(loginUser, queryOrderReqVo);
        if(Objects.isNull(uuOrder)){
            log.error("订单不存在{}",JacksonUtils.writeValueAsString(invOrderId));
            throw new ServiceException(OpenApiCode.CONCAT_ADMIN);
        }
        PayWalletDO orCreateWallet = payWalletService.getOrCreateWallet(uuOrder.getBuyUserId(), uuOrder.getBuyUserType());
        if(Objects.isNull(orCreateWallet) || orCreateWallet.getBalance()<uuOrder.getPayAmount()){
            throw exception(OpenApiCode.ERR_5212);
        }
        //生成支付流水
        //扣除服务费到平台
        PayWalletTransactionDO payWalletTransactionDO = payWalletService.reduceWalletBalance(orCreateWallet.getId(), uuOrder.getId(),
                PayWalletBizTypeEnum.SUB_INV_SERVICE_FEE, uuOrder.getServiceFee());
        //获取卖家家钱包并进行打款
        PayWalletTransactionDO payWalletTransactionDO1 = payWalletService.reduceWalletBalance(orCreateWallet.getId(),uuOrder.getId(),
                PayWalletBizTypeEnum.SUB_STEAM_CASH, uuOrder.getCommodityAmount());
        List<PayWalletTransactionDO> payWalletTransactionDOS = Arrays.asList(payWalletTransactionDO, payWalletTransactionDO1);
        apiOrderMapper.updateById(new ApiOrderDO().setId(uuOrder.getId()).setPayPayRet(JacksonUtils.writeValueAsString(payWalletTransactionDOS))
                .setPayOrderStatus(PayOrderStatusEnum.SUCCESS.getStatus()));
        ApiOrderDO uuOrder1 = getOrder(loginUser, queryOrderReqVo);
        Optional<ApiThreeOrderService> apiThreeByOrder = getApiThreeByOrder(uuOrder1);
        if(!apiThreeByOrder.isPresent()){
            throw exception(OpenApiCode.ERR_5213);
        }
        //购买
        ApiThreeOrderService apiThreeOrderService = apiThreeByOrder.get();
        ApiBuyItemRespVo apiBuyItemRespVo = apiThreeOrderService.buyItem(loginUser, uuOrder1.getBuyInfo());
        if(!apiBuyItemRespVo.getIsSuccess()){
            throw new ServiceException(apiBuyItemRespVo.getErrorCode());
        }

        apiOrderMapper.updateById(new ApiOrderDO().setId(uuOrder1.getId())
                .setThreeOrderNo(apiBuyItemRespVo.getOrderNo())
                .setPayOrderStatus(PayOrderStatusEnum.SUCCESS.getStatus())
                .setTransferStatus(InvTransferStatusEnum.TransferING.getStatus())
        );
        return getOrder(loginUser, queryOrderReqVo);
    }

    private void validateInvOrderCanCreate(LoginUser loginUser,ApiOrderDO orderDO) {
        //检测交易链接
        try{
            ApiResult<ApiCheckTradeUrlReSpVo> apiCheckTradeUrlReSpVoApiResult = uuService.checkTradeUrl(new ApiCheckTradeUrlReqVo().setTradeLinks(orderDO.getBuyTradeLinks()));
            if(apiCheckTradeUrlReSpVoApiResult.getCode()!=0){
                throw exception(OpenApiCode.CONCAT_ADMIN);
            }
            switch (apiCheckTradeUrlReSpVoApiResult.getData().getStatus()){
                case 1://1：正常交易   6:该账户库存私密无法交易 7:该账号个人资料私密无法交易
                    break;
                case 2://2:交易链接格式错误
                    throw exception(OpenApiCode.ERR_5408);
                case 3://3:请稍后重试
                    throw exception(OpenApiCode.ERR_5402);
                case 4://4:账号交易权限被封禁，无法交易
                    throw exception(OpenApiCode.ERR_5406);
                case 5://5:该交易链接已不再可用
                    throw exception(OpenApiCode.ERR_5405);
                case 6:// 6:该账户库存私密无法交易
                    throw exception(OpenApiCode.ERR_5403);
                case 7://7:该账号个人资料私密无法交易
                    throw exception(OpenApiCode.ERR_5403);
                default:
                    throw exception(OpenApiCode.CONCAT_ADMIN);
            }
        }catch (ServiceException e){
            throw exception(OpenApiCode.ERR_5408);
        }
        ApiCommodityRespVo query=null;
        for (PlatCodeEnum value : PlatCodeEnum.values()) {
            Optional<ApiThreeOrderService> apiThreeByOrder = getApiThreeByPlatCode(value);
            if(!apiThreeByOrder.isPresent()){
                continue;
            }
            ApiThreeOrderService apiThreeOrderService = apiThreeByOrder.get();
            query = apiThreeOrderService.query(loginUser, orderDO.getBuyInfo());
            if(Objects.nonNull(query)){
                break;
            }
        }
        if(Objects.isNull(query)){
            throw new ServiceException(OpenApiCode.ERR_5301);
        }
        //检测交易链接是否是自己
//        Long aLong = bindUserMapper.selectCount(new LambdaQueryWrapperX<BindUserDO>()
//                .eq(BindUserDO::getUserId, youyouOrderDO.getBuyUserId())
//                .eqIfPresent(BindUserDO::getUserType, youyouOrderDO.getBuyUserType())
//                .eqIfPresent(BindUserDO::getTradeUrl, youyouOrderDO.getBuyTradeLinks())
//        );
//        if(aLong>0){
//            throw exception(OpenApiCode.ERR_5407);
//        }
        //todo 查询 一个商品
        ApiCommodityRespVo buyItem = null;
        for (ApiThreeOrderService apiThreeOrderService : apiThreeOrderServiceList) {
            buyItem = apiThreeOrderService.query(loginUser, orderDO.getBuyInfo());
            if(buyItem.getPrice()<=orderDO.getBuyInfo().getPurchasePrice()){
                break;
            }
        }
        //校验商品是否存在
        if (Objects.isNull(buyItem)) {
            throw exception(ErrorCodeConstants.UU_GOODS_NOT_FOUND);
        }
        if(PayOrderStatusEnum.isSuccess(orderDO.getPayOrderStatus())){
            throw exception(OpenApiCode.ERR_5299);
        }
        orderDO.setPlatCode(buyItem.getPlatCode().getCode());
        orderDO.setCommodityAmount(buyItem.getPrice());
        ConfigDO serviceFeeLimit = configService.getConfigByKey("steam.inv.serviceFeeLimit");
        ConfigDO serviceFeeRateConfigByKey = configService.getConfigByKey("steam.inv.serviceFeeRate");
        if(PlatFormEnum.WEB.getCode().equals(orderDO.getBuyMethod()) || Objects.isNull(serviceFeeRateConfigByKey)){
            orderDO.setServiceFeeRate("0");
            orderDO.setServiceFee(0);
            orderDO.setPayAmount(orderDO.getCommodityAmount());
        }else{
            orderDO.setServiceFeeRate(serviceFeeRateConfigByKey.getValue());
            BigDecimal rate = new BigDecimal(serviceFeeRateConfigByKey.getValue()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            //四舍五入后金额
            BigDecimal serviceFee = new BigDecimal(orderDO.getCommodityAmount()).multiply(rate).setScale(0, RoundingMode.HALF_UP);


            if(Objects.nonNull(serviceFeeLimit.getValue())){
                int compareResult = serviceFee.compareTo(new BigDecimal(serviceFeeLimit.getValue()));
                if (compareResult < 0) { // 如果bigDemical1小于bigDemical2
                    orderDO.setServiceFee(serviceFee.intValue());
                } else if (compareResult == 0) { // 如果两者相等
                    orderDO.setServiceFee(serviceFee.intValue());
                } else { // 如果bigDemical1大于bigDemical2
                    orderDO.setServiceFee(new BigDecimal(serviceFeeLimit.getValue()).intValue());
                }
            }else{
                orderDO.setServiceFee(serviceFee.intValue());
            }
            BigDecimal bigDecimal2 = new BigDecimal(orderDO.getCommodityAmount());
            BigDecimal add = bigDecimal2.add(new BigDecimal(orderDO.getServiceFee()));
            orderDO.setPayAmount(add.intValue());
        }
        //判断用户钱包是否有足够的钱
        PayWalletDO orCreateWallet = payWalletService.getOrCreateWallet(orderDO.getBuyUserId(), orderDO.getBuyUserType());
        if(Objects.isNull(orCreateWallet) || orCreateWallet.getBalance()<orderDO.getPayAmount()){
            throw exception(OpenApiCode.ERR_5212);
        }

        //检查是否已经下过单
//        List<YouyouOrderDO> expOrder = getExpOrder(youyouOrderDO.getRealCommodityId());
//        if(expOrder.size()>0){
//            throw exception(OpenApiCode.ERR_5301);
//        }
        // 校验订单是否支付
        if (Objects.isNull(orderDO.getPayAmount()) || orderDO.getPayAmount()<=0) {
            throw exception(ErrorCodeConstants.UU_GOODS_AMOUNT_EXCEPT);
        }
        // 校验订单是否支付
        if (!PayOrderStatusEnum.WAITING.getStatus().equals(orderDO.getPayOrderStatus())) {
            throw exception(ErrorCodeConstants.UU_GOODS_ORDER_UPDATE_PAID_STATUS_NOT_UNPAID);
        }
    }

    public ApiOrderDO getUUOrderById(Long orderId) {
        ApiOrderDO orderDO = apiOrderMapper.selectById(orderId);
        if(Objects.isNull(orderDO)){
            throw exception(OpenApiCode.JACKSON_EXCEPTION);
        }else{
            return orderDO;
        }
    }
    /**
     * 检查是否有有效订单有魔兽的时候不能再次下单
     * 条件支付订单未关闭且发货状态不是关闭都是有效订单
     *
     * @param commodityId 商品ID
     * @return
     */
    @Deprecated
    public List<ApiOrderDO> getExpOrder(String commodityId){
        return apiOrderMapper.selectList(new LambdaQueryWrapperX<ApiOrderDO>()
//                .eq(YouyouOrderDO::getCommodityId, commodityId)
//                .neIfPresent(YouyouOrderDO::getPayOrderStatus,PayOrderStatusEnum.CLOSED.getStatus())
//                .ne(YouyouOrderDO::getTransferStatus, InvTransferStatusEnum.CLOSE.getStatus())
        );
    }
    @Override
    public ApiOrderDO getOrder(LoginUser loginUser, QueryOrderReqVo queryOrderReqVo) {
        LambdaQueryWrapperX<ApiOrderDO> uuOrderDOLambdaQueryWrapperX = new LambdaQueryWrapperX<ApiOrderDO>()
                .eqIfPresent(ApiOrderDO::getBuyUserId, loginUser.getId())
                .eqIfPresent(ApiOrderDO::getBuyUserType, loginUser.getUserType());
        if (Objects.isNull(queryOrderReqVo.getOrderNo()) && Objects.isNull(queryOrderReqVo.getMerchantNo()) && Objects.isNull(queryOrderReqVo.getId())){
            throw exception(OpenApiCode.JACKSON_EXCEPTION);
        }
        uuOrderDOLambdaQueryWrapperX.eqIfPresent(ApiOrderDO::getOrderNo, queryOrderReqVo.getOrderNo());
        uuOrderDOLambdaQueryWrapperX.eqIfPresent(ApiOrderDO::getMerchantOrderNo, queryOrderReqVo.getMerchantNo());
        uuOrderDOLambdaQueryWrapperX.eqIfPresent(ApiOrderDO::getId, queryOrderReqVo.getId());
        if(Objects.nonNull(queryOrderReqVo.getId())){
            uuOrderDOLambdaQueryWrapperX.eqIfPresent(ApiOrderDO::getId, queryOrderReqVo.getId());
        }
        ApiOrderDO orderDO = apiOrderMapper.selectOne(uuOrderDOLambdaQueryWrapperX);
        if(Objects.isNull(orderDO)){
            throw exception(OpenApiCode.JACKSON_EXCEPTION);
        }else{
            return orderDO;
        }
    }

    @Override
    public OrderInfoResp orderInfo(YouyouOrderDO uuOrder, OpenApiReqVo<QueryOrderReqVo> openApiReqVo)  {
        return null;
//        ObjectMapper objectMapper = new ObjectMapper();
//        if (uuOrder != null && uuOrder.getOrderInformReturnJason() == null){
//            openApiReqVo.getData().setOrderNo(uuOrder.getUuOrderNo());
//            ApiResult<OrderInfoResp> orderInfoRespApiResult = uuService.orderInfo(openApiReqVo.getData());
//            String json = null;
//            try {
//                json = objectMapper.writeValueAsString(orderInfoRespApiResult.getData());
//            } catch (Exception e) {
//                throw exception(ErrorCodeConstants.YOUYOU_DETAILS_NOT_EXISTS);
//            }
//            if (json != null){
//                YouyouOrderDO youyouOrderDO = new YouyouOrderDO();
//                youyouOrderDO.setId(uuOrder.getId());
//                youyouOrderDO.setOrderInformReturnJason(json);
//                youyouOrderMapper.updateById(youyouOrderDO);
//            }
//        }
//        YouyouCommodityDO youyouCommodity = null;
//        if (uuOrder != null) {
//            youyouCommodity = youyouCommodityService.getYouyouCommodity(Integer.valueOf(uuOrder.getRealCommodityId()));
//        }
//        if (youyouCommodity == null){
//            throw new ServiceException(OpenApiCode.JACKSON_EXCEPTION);
//        }
//        Optional<YouyouTemplateDO> first = uuTemplateService.getYouyouTemplatePage(new YouyouTemplatePageReqVO().setTemplateId(youyouCommodity.getTemplateId())).getList().stream().findFirst();
//        PayOrderDO payOrder = payOrderService.getOrder(uuOrder.getPayOrderId());
//        //买家
//        MemberUserRespDTO buyUser = memberUserApi.getUser(uuOrder.getBuyUserId());
//        //卖家
//        MemberUserRespDTO sellUser = memberUserApi.getUser(uuOrder.getSellUserId());
//
//        YouyouTemplateDO youyouTemplateDO;
//        if(first.isPresent()){
//            youyouTemplateDO = first.get();
//        }else{
//            throw new ServiceException(OpenApiCode.CONCAT_ADMIN);
//        }
//
//        OrderInfoResp ret = new OrderInfoResp();
//        ret.setId(uuOrder.getOrderNo());
//        ret.setOrderId(uuOrder.getId());
//        ret.setOrderNo(uuOrder.getOrderNo());
//
//        OrderInfoResp orderInfoResp = null;
//        try {
//            orderInfoResp = objectMapper.readValue(uuOrder.getOrderInformReturnJason(), OrderInfoResp.class);
//        } catch (Exception e) {
//            throw exception(ErrorCodeConstants.YOUYOU_DETAILS_NOT_EXISTS);
//        }
//        ret.setProcessStatus(orderInfoResp.getProcessStatus());
//        ret.setOrderSubStatus(orderInfoResp.getOrderSubStatus());
//        ret.setOrderSubStatusName(orderInfoResp.getOrderSubStatusName());
//        ret.setOrderType(orderInfoResp.getOrderType());
//        ret.setOrderSubType(orderInfoResp.getOrderSubType());
//        ret.setTimeType(orderInfoResp.getTimeType());
////        ret.setTime(null);// TODO 待确认
//        ret.setReturnAmount(orderInfoResp.getReturnAmount());
//        ret.setServiceFee(uuOrder.getServiceFee().toString());
////        ret.setServiceFeeRate(uuOrder.getServiceFeeRate());
//        ret.setCommodityAmount(youyouCommodity.getCommodityPrice());
//        if(Objects.nonNull(uuOrder.getPayAmount())){
//            ret.setPaymentAmount(new BigDecimal(uuOrder.getPayAmount()).divide(new BigDecimal("100")).toString());
//        }
//        ret.setSellerSteamRegTime(orderInfoResp.getSellerSteamRegTime());
////        ret.setTradeOfferId(null);// TODO 待确认
//        ret.setCancelOrderTime(orderInfoResp.getCancelOrderTime());
////        ret.setOfferSendResult(null);// TODO 待确认
//        ret.setTradeUrl(uuOrder.getBuyTradeLinks());
//        ret.setFinishOrderTime(orderInfoResp.getFinishOrderTime());
//        ret.setPaySuccessTime(orderInfoResp.getPaySuccessTime());
//        ret.setPayEndTime(orderInfoResp.getPayEndTime());
////        ret.setSendOfferSuccessTime(null);
//        ret.setSendOfferEndTime(orderInfoResp.getSendOfferEndTime());
//        ret.setOrderStatusColor(orderInfoResp.getOrderStatusColor());
//        ret.setOrderSubStatus(orderInfoResp.getOrderSubStatus());
//
////        ret.setShippingMode(uuOrder.getUuShippingMode());
//        ret.setOrderStatus(uuOrder.getUuOrderStatus());
//        ret.setBuyerUserId(uuOrder.getBuyUserId());
//        ret.setBuyerUserName(buyUser.getNickname());
//        if(StringUtils.hasText(buyUser.getAvatar())){
//            ret.setBuyerUserIcon(buyUser.getAvatar());
//        }else{
//            ret.setBuyerUserIcon("https://img.zcool.cn/community/01a3865ab91314a8012062e3c38ff6.png@1280w_1l_2o_100sh.png");
//        }
//        ret.setSellerUserId(uuOrder.getSellUserId());
//        ret.setSellerUserName(sellUser.getNickname());
//        if(StringUtils.hasText(sellUser.getAvatar())){
//            ret.setSellerUserIcon(sellUser.getAvatar());
//        }else{
//            ret.setSellerUserIcon("https://img.zcool.cn/community/01a3865ab91314a8012062e3c38ff6.png@1280w_1l_2o_100sh.png");
//        }
//        ret.setCreateOrderTime(uuOrder.getCreateTime().toInstant(ZoneOffset.of("+8")).toEpochMilli());
//        if(Objects.nonNull(uuOrder.getPayTime())){
//            ret.setPaySuccessTime(uuOrder.getPayTime().toInstant(ZoneOffset.of("+8")).toEpochMilli());
//        }
//        if(Objects.nonNull(payOrder)){
//            ret.setPayEndTime(payOrder.getExpireTime().toInstant(ZoneOffset.of("+8")).toEpochMilli());
//        }
//
////        ret.setConfirmOfferEndTime(null);
////        ret.setPendingEndTime(null);
////        ret.setDelayedTransferEndTime(null);
////        ret.setPrice(new BigDecimal(youyouCommodity.getCommodityPrice()).multiply(new BigDecimal("100")).longValue());//TODO 待确认
//        ret.setTotalAmount(String.valueOf(uuOrder.getPayAmount()));
//        ret.setCancelReason(uuOrder.getCancelReason());
//        if(Objects.nonNull(ret.getOrderStatus())){
//            ret.setOrderStatusName(UUOrderStatus.valueOf(ret.getOrderStatus()).getMsg());
//        }
//        if (Objects.nonNull(ret.getOrderStatusName())){
//            String updatedOrderStatusName = ret.getOrderStatusName().replace("-s", "time");
//            ret.setOrderStatusDesc(updatedOrderStatusName);
//        }
//
//        OrderInfoResp.ProductDetailDTO productDetailDTO = new OrderInfoResp.ProductDetailDTO();
//        productDetailDTO.setCommodityId(youyouCommodity.getId());
//        productDetailDTO.setCommodityName(youyouCommodity.getCommodityName());
//        productDetailDTO.setCommodityHashName(youyouTemplateDO.getHashName());
//
//        productDetailDTO.setCommodityTemplateId(youyouCommodity.getTemplateId());
//        productDetailDTO.setAssertId(null);// TODO 待确认
//        productDetailDTO.setAbrade(youyouCommodity.getCommodityAbrade());
//
//        productDetailDTO.setIsDoppler(youyouCommodity.getTemplateisDoppler());
//        productDetailDTO.setDopplerColor(null);// TODO 待确认
//        productDetailDTO.setIsFade(youyouCommodity.getTemplateisFade());
////        productDetailDTO.setFadeName(youyouCommodity.getCommodityFade());
//        productDetailDTO.setDopplerColor(youyouCommodity.getCommodityDoppler());
//
//        //TODO 以下属性先这样返回， 目前用不上
//        productDetailDTO.setPrice(new BigDecimal(youyouCommodity.getCommodityPrice()).multiply(new BigDecimal("100")).intValue());
////                productDetailDTO.setNum(new BigDecimal(youyouCommodity.get()).multiply(new BigDecimal("100")).intValue());
//        productDetailDTO.setPaintIndex(youyouCommodity.getCommodityPaintIndex());
//        productDetailDTO.setPaintSeed(youyouCommodity.getCommodityPaintSeed());
//        productDetailDTO.setHaveNameTag(youyouCommodity.getCommodityHaveNameTag());
////                productDetailDTO.setNameTag(youyouCommodity.getn());
//        productDetailDTO.setHaveClothSeal(youyouCommodity.getCommodityHaveBuzhang());
////                productDetailDTO.setDopplerColor(youyouCommodity.getd());
////                productDetailDTO.setd(youyouCommodity.get());
//        productDetailDTO.setHaveSticker(String.valueOf(youyouCommodity.getCommodityHaveSticker()));
//
//        productDetailDTO.setTypeId(youyouTemplateDO.getTypeId());
//        productDetailDTO.setTypeHashName(youyouTemplateDO.getTypeHashName());
////                productDetailDTO.setRarityName(youyouTemplateDO.get());
//
//        productDetailDTO.setTypeId(youyouTemplateDO.getTypeId());
//        productDetailDTO.setTypeHashName(youyouTemplateDO.getTypeHashName());
//        productDetailDTO.setTypeName(youyouTemplateDO.getTypeName());
//        productDetailDTO.setWeaponName(youyouTemplateDO.getWeaponName());
//        productDetailDTO.setWeaponId(youyouTemplateDO.getWeaponId());
//        productDetailDTO.setWeaponHashName(youyouTemplateDO.getWeaponHashName());
//        productDetailDTO.setCommodityAbrade(youyouCommodity.getCommodityAbrade());
//
//        ret.setProductDetail(productDetailDTO);
//        return ret;
    }
    @Override
    public Integer orderCancel(LoginUser loginUser, OrderCancelVo orderCancelVo, String userIp,String cancelReason) {
        ApiOrderDO uuOrder = getOrder(loginUser, new QueryOrderReqVo().setOrderNo(orderCancelVo.getOrderNo()));

        // 1. 校验订单是否可以退款
        ApiOrderDO orderDO = validateInvOrderCanRefund(uuOrder, loginUser);
        if(Objects.isNull(orderDO.getThreeOrderNo())){
            refundAction(orderDO,loginUser);
            return 1;
        }
        Optional<ApiThreeOrderService> apiThreeByOrder = getApiThreeByOrder(orderDO);
        if(apiThreeByOrder.isPresent()){
            ApiThreeOrderService apiThreeOrderService = apiThreeByOrder.get();
            ApiOrderCancelRespVo apiOrderCancelRespVo = apiThreeOrderService.orderCancel(loginUser, uuOrder.getThreeOrderNo(), uuOrder.getId());
            if(apiOrderCancelRespVo.getIsSuccess()){
                refundAction(orderDO,loginUser,cancelReason);
                return 1;
            }
            return 3;
        }else{
            return 3;
        }
    }
    /**
     * 执行退款
     * 退款不正走此方法，
     * @param apiOrderDO
     */
    private void refundAction(ApiOrderDO apiOrderDO,LoginUser loginUser,String reason) {
        validateInvOrderCanRefund(apiOrderDO,loginUser);
        damagesCloseInvOrder(apiOrderDO.getId(),reason);
    }
    private void refundAction(ApiOrderDO apiOrderDO,LoginUser loginUser) {
        refundAction(apiOrderDO,loginUser,"用户不想要了主动退款");
    }

    private ApiOrderDO validateInvOrderCanRefund(ApiOrderDO apiOrderDO,LoginUser loginUser) {
        // 校验订单是否存在
        if (Objects.isNull(apiOrderDO)) {
            throw exception(ErrorCodeConstants.INVORDER_ORDER_NOT_FOUND);
        }
        if(apiOrderDO.getCashStatus().equals(InvSellCashStatusEnum.CASHED.getStatus())){
            throw exception(ErrorCodeConstants.INVORDER_ORDER_CASHED_CANNOTREFUND);
        }
        if(!apiOrderDO.getBuyUserId().equals(loginUser.getId())){
            throw exception(ErrorCodeConstants.INVORDER_ORDER_REFUND_USER_ERROR);
        }
        if(!apiOrderDO.getBuyUserType().equals(loginUser.getUserType())){
            throw exception(ErrorCodeConstants.INVORDER_ORDER_REFUND_USER_ERROR);
        }
        // 校验订单是否支付
        if (!PayOrderStatusEnum.isSuccess(apiOrderDO.getPayOrderStatus())) {
            throw exception(ErrorCodeConstants.INVORDER_ORDER_REFUND_FAIL_NOT_PAID);
        }
        //检查是否已发货
        if(InvSellCashStatusEnum.DAMAGES.getStatus().equals(apiOrderDO.getCashStatus())){
            throw exception(ErrorCodeConstants.UU_GOODS_ORDER_TRANSFER_CASHED);
        }
        //检查是否已发货
        if(InvSellCashStatusEnum.CASHED.getStatus().equals(apiOrderDO.getCashStatus())){
            throw exception(ErrorCodeConstants.UU_GOODS_ORDER_TRANSFER_CASHED);
        }
        //通过此接口可取消符合取消规则「创单成功后30min后卖家未发送交易报价」的代购订单。
        if(Objects.nonNull(apiOrderDO.getThreeOrderNo())){
            //orderStatus,140的除 1101外其它状态不能取消，不能取消的还有340，280，360
//            if(Arrays.asList(UUOrderStatus.CODE140.getCode(),UUOrderStatus.CODE340.getCode(),UUOrderStatus.CODE280.getCode(),UUOrderStatus.CODE360.getCode()).contains(youyouOrderDO.getUuOrderStatus())){
//                if(!uuOrder.getUuOrderStatus().equals(UUOrderSubStatus.SUB_CODE1104.getCode())){
//                    throw exception(ErrorCodeConstants.UU_GOODS_ORDER_CAN_NOT_CANCEL);
//                }
//            }
            Optional<ApiThreeOrderService> apiThreeByOrder = getApiThreeByOrder(apiOrderDO);
            if(apiThreeByOrder.isPresent()) {
                ApiThreeOrderService apiThreeOrderService = apiThreeByOrder.get();
                // 1,进行中，2完成，3作废
                Integer orderSimpleStatus = apiThreeOrderService.getOrderSimpleStatus(loginUser, apiOrderDO.getThreeOrderNo(), apiOrderDO.getId());
                if(orderSimpleStatus.equals(3)){

                }else{
                    throw exception(ErrorCodeConstants.UU_GOODS_ORDER_CAN_NOT_CANCEL);
                }
            }
            Duration between = Duration.between(apiOrderDO.getCreateTime(), LocalDateTime.now());
            if(between.getSeconds()<=30*60){//小于30分钟不能取消
                throw exception(ErrorCodeConstants.UU_GOODS_ORDER_MIN_TIME);
            }
        }
        return apiOrderDO;
    }

    @Override
    public void processNotify(NotifyReq notifyReq) {
        String callBackInfo = notifyReq.getCallBackInfo();
        NotifyVo notifyVo = JacksonUtils.readValue(callBackInfo, NotifyVo.class);
        log.info("回调接收的数据{}",notifyVo);
        List<ApiOrderDO> apiOrderDOS = apiOrderMapper.selectList(new LambdaQueryWrapper<ApiOrderDO>()
                .eq(ApiOrderDO::getThreeOrderNo, notifyVo.getOrderNo()));
        if (!apiOrderDOS.isEmpty()) {
            //生成相关回调数据
//            ApiOrderDO orderDO = apiOrderDOS.get(0);
//            Optional<ApiThreeOrderService> apiThreeByOrder = getApiThreeByOrder(orderDO);
//            if(apiThreeByOrder.isPresent()){
//                ApiThreeOrderService apiThreeOrderService = apiThreeByOrder.get();
//                LoginUser loginUser = new LoginUser().setTenantId(1L).setUserType(orderDO.getBuyUserType()).setId(orderDO.getBuyUserId());
//                // 1,进行中，2完成，3作废
//                Integer orderSimpleStatus = apiThreeOrderService.getOrderSimpleStatus(loginUser,orderDO.getThreeOrderNo(), orderDO.getId());
//                switch (orderSimpleStatus){
//                    case 1://进行中
//                        break;
//                    case 2://完成
////                        apiOrderMapper.updateById(new ApiOrderDO().setId(orderDO.getId())
////                                .setTransferStatus(InvTransferStatusEnum.TransferFINISH.getStatus()));
//                        break;
//                    case 3://作废
//                        refundAction(orderDO,loginUser);
//                        break;
//                    default:
//                }
//            }
        }

    }
    @Override
    public void pushRemote(NotifyReq notifyReq) {
        String callBackInfo = notifyReq.getCallBackInfo();
        NotifyVo notifyVo = JacksonUtils.readValue(callBackInfo, NotifyVo.class);

        log.info("回调接收的数据{}",notifyVo);
        List<ApiOrderDO> apiOrderDOS = apiOrderMapper.selectList(new LambdaQueryWrapper<ApiOrderDO>()
                .eq(ApiOrderDO::getThreeOrderNo, notifyVo.getOrderNo()));
        if (!apiOrderDOS.isEmpty()) {
            ApiOrderDO apiOrderDO = apiOrderDOS.get(0);
            //获取用户的devaccount
            try{
                DevAccountDO devAccountDO = devAccountMapper.selectOne(new LambdaQueryWrapperX<DevAccountDO>()
                        .eq(DevAccountDO::getUserId, apiOrderDO.getBuyUserId())
                        .eq(DevAccountDO::getUserType, apiOrderDO.getBuyUserType())
                );
                if(StringUtils.hasText(devAccountDO.getCallbackUrl()) && StringUtils.hasText(devAccountDO.getCallbackPrivateKey()) && StringUtils.hasText(devAccountDO.getCallbackPublicKey())){
                    HttpUtil.HttpRequest.HttpRequestBuilder builder = HttpUtil.HttpRequest.builder();
                    builder.url(devAccountDO.getCallbackUrl());
                    builder.method(HttpUtil.Method.JSON);
                    Map<String, String> params = new HashMap<>();
                    params.put("messageNo",notifyReq.getMessageNo());
                    //注意接收到的callBackInfo是含有双引号转译符"\" 文档上无法体现只需要在验证签名是直接把callBackInfo值当成字符串即可以
                    params.put("callBackInfo",notifyReq.getCallBackInfo());

                    // 第一步：检查参数是否已经排序
                    String[] keys = params.keySet().toArray(new String[0]);
                    Arrays.sort(keys);
                    // 第二步：把所有参数名和参数值串在一起
                    StringBuilder stringBuilder = new StringBuilder();
                    for (String key : keys) {
                        String value = params.get(key);
                        if (!StringUtils.hasText(value)) {
                            stringBuilder.append(key).append(value);
                        }
                    }
                    log.info("stringBuilder:{}",stringBuilder);
                    String s = RSAUtils.signByPrivateKey(stringBuilder.toString().getBytes(), devAccountDO.getCallbackPrivateKey());
                    notifyReq.setSign(s);
                    builder.postObject(notifyReq);
                    HttpUtil.HttpResponse sent = HttpUtil.sent(builder.build());
                    YouyouNotifyDO youyouNotifyDO = youyouNotifyMapper.selectOne(new LambdaQueryWrapperX<YouyouNotifyDO>()
                            .eq(YouyouNotifyDO::getMessageNo, notifyReq.getMessageNo()));
                    youyouNotifyMapper.updateById(new YouyouNotifyDO().setId(youyouNotifyDO.getId()).setPushRemote(true)
                            .setPushRemoteUrl(devAccountDO.getCallbackUrl())
                            .setPushRemoteResult(sent.html()));
                }
            }catch (Exception e){
                log.error("消费消息出错{}",e.getMessage());
            }
        }

    }
    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void checkTransfer(Long invOrderId) {
        ApiOrderDO uuOrderById = getUUOrderById(invOrderId);
        if(Objects.isNull(uuOrderById)){
            throw new ServiceException(OpenApiCode.JACKSON_EXCEPTION);
        }
        if(!PayOrderStatusEnum.SUCCESS.getStatus().equals(uuOrderById.getPayOrderStatus())){
            throw new ServiceException(-1,"订单未支付不支持打款");
        }
        if(InvTransferStatusEnum.TransferING.getStatus().equals(uuOrderById.getTransferStatus())){
            //发货完成时
            Optional<ApiThreeOrderService> apiThreeByOrder = getApiThreeByOrder(uuOrderById);
            if(apiThreeByOrder.isPresent()) {
                ApiThreeOrderService apiThreeOrderService = apiThreeByOrder.get();
                LoginUser loginUser = new LoginUser().setTenantId(1L).setUserType(uuOrderById.getBuyUserType()).setId(uuOrderById.getBuyUserId());
                // 1,进行中，2完成，3作废
                Integer orderSimpleStatus = apiThreeOrderService.getOrderSimpleStatus(loginUser,uuOrderById.getThreeOrderNo(), uuOrderById.getId());
                switch (orderSimpleStatus){
                    case 1://进行中
                        break;
                    case 2://完成
                        apiOrderMapper.updateById(new ApiOrderDO().setId(uuOrderById.getId())
                                .setTransferStatus(InvTransferStatusEnum.TransferFINISH.getStatus()));
                        cashInvOrder(invOrderId);
                        apiOrderMapper.updateById(new ApiOrderDO().setId(invOrderId).setTransferStatus(InvTransferStatusEnum.TransferFINISH.getStatus()));
                        break;
                    case 3://作废
                        damagesCloseInvOrder(invOrderId,"订单被第三方取消");
                        apiOrderMapper.updateById(new ApiOrderDO().setId(invOrderId).setTransferStatus(InvTransferStatusEnum.CLOSE.getStatus()));
                        break;
                    default:
                }
            }
        }
    }
}
