package cn.iocoder.yudao.module.steam.service.invorder;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pay.dal.dataobject.demo.PayDemoOrderDO;
import cn.iocoder.yudao.module.steam.controller.admin.invorder.vo.InvOrderPageReqVO;
import cn.iocoder.yudao.module.steam.controller.app.vo.PaySteamOrderCreateReqVO;
import cn.iocoder.yudao.module.steam.dal.dataobject.invorder.InvOrderDO;

import javax.validation.Valid;

/**
 * 示例订单 Service 接口
 *
 * @author 芋道源码
 */
public interface PaySteamOrderService {

    /**
     * 创建示例订单
     *
     * @param userId      用户编号
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDemoOrder(Long userId, @Valid PaySteamOrderCreateReqVO createReqVO);

    /**
     * 获得示例订单
     *
     * @param id 编号
     * @return 示例订单
     */
    InvOrderDO getDemoOrder(Long id);

    /**
     * 获得示例订单分页
     *
     * @param pageReqVO 分页查询
     * @return 示例订单分页
     */
    PageResult<InvOrderDO> getDemoOrderPage(InvOrderPageReqVO pageReqVO);

    /**
     * 更新示例订单为已支付
     *
     * @param id 编号
     * @param payOrderId 支付订单号
     */
    void updateDemoOrderPaid(Long id, Long payOrderId);

    /**
     * 发起示例订单的退款
     *
     * @param id 编号
     * @param userIp 用户编号
     */
    void refundDemoOrder(Long id, String userIp);

    /**
     * 更新示例订单为已退款
     *
     * @param id 编号
     * @param payRefundId 退款订单号
     */
    void updateDemoOrderRefunded(Long id, Long payRefundId);

}