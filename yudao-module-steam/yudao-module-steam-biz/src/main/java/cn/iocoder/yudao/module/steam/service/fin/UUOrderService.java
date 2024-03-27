package cn.iocoder.yudao.module.steam.service.fin;

import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.module.steam.controller.app.vo.order.OrderCancelVo;
import cn.iocoder.yudao.module.steam.controller.app.vo.order.OrderInfoResp;
import cn.iocoder.yudao.module.steam.controller.app.vo.order.QueryOrderReqVo;
import cn.iocoder.yudao.module.steam.controller.app.wallet.vo.PayWithdrawalOrderCreateReqVO;
import cn.iocoder.yudao.module.steam.dal.dataobject.youyouorder.YouyouOrderDO;
import cn.iocoder.yudao.module.steam.service.steam.CreateOrderResult;
import cn.iocoder.yudao.module.steam.service.uu.vo.CreateCommodityOrderReqVo;
import cn.iocoder.yudao.module.steam.service.uu.vo.notify.NotifyReq;

import javax.validation.Valid;

/**
 * 示例订单 Service 接口
 *
 * @author 芋道源码
 */
public interface UUOrderService {
    /**
     * 创建示例订单
     *
     * @param loginUser      用户
     * @param createReqVO 创建信息
     * @return 编号
     */
    YouyouOrderDO createInvOrder(LoginUser loginUser, @Valid CreateCommodityOrderReqVo createReqVO);

    /**
     * 释放库存
     * @param invOrderId
     */
    void releaseInvOrder(Long invOrderId);
    /**
     * 支付订单
     * @param loginUser 前端用户
     * @param invOrderId 订单号
     * @return
     */
    YouyouOrderDO payInvOrder(LoginUser loginUser, @Valid Long invOrderId);
    /**
     * 获得订单详情
     * 订单是以买家身份进行查询
     * @param loginUser 订单用户
     * @param queryOrderReqVo 订单号
     * @return 示例订单
     */
    YouyouOrderDO getUUOrder(LoginUser loginUser, QueryOrderReqVo queryOrderReqVo);
    OrderInfoResp orderInfo(YouyouOrderDO youyouOrderDO);
    /**
     * 获得示例订单列表
     * @param youyouOrderPageReqVO
     * @return
     */
//    PageResult<YouyouOrderDO> getInvOrderPageOrder(YouyouOrderPageReqVO youyouOrderPageReqVO);

    /**
     * 发起示例订单的退款
     *
     * @param id 编号
     * @param userIp 用户编号
     */
    Integer refundInvOrder(LoginUser loginUser, OrderCancelVo id, String userIp);

    /**
     * 买家取消订单
     * @param loginUser
     * @param id
     * @param userIp
     * @param cancelReason 取消原因
     * @return
     */
    Integer orderCancel(LoginUser loginUser, OrderCancelVo id, String userIp,String cancelReason);


    void processNotify(NotifyReq notifyReq);

}
