package cn.iocoder.yudao.module.pay.convert.wallet;

import cn.iocoder.yudao.module.pay.controller.app.wallet.vo.recharge.AppPayWalletRechargeCreateReqVO;
import cn.iocoder.yudao.module.pay.controller.app.wallet.vo.recharge.AppPayWalletRechargeCreateRespVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletRechargeDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author jason
 */
@Mapper
public interface PayWalletRechargeConvert {

    PayWalletRechargeConvert INSTANCE = Mappers.getMapper(PayWalletRechargeConvert.class);

    PayWalletRechargeDO convert(AppPayWalletRechargeCreateReqVO vo);

    // TODO @jason：好像 price 相加，可以写个表达式的，通过 @Mapping
    default PayWalletRechargeDO convert(Long walletId, AppPayWalletRechargeCreateReqVO vo) {
        PayWalletRechargeDO walletRecharge = convert(vo);
        return walletRecharge.setWalletId(walletId)
                .setPrice(walletRecharge.getPayPrice() + walletRecharge.getWalletBonus());
    }

    AppPayWalletRechargeCreateRespVO convert(PayWalletRechargeDO bean);

}
