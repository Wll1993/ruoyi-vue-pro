package cn.iocoder.yudao.framework.pay.core.client.impl;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.pay.core.client.PayClient;
import cn.iocoder.yudao.framework.pay.core.client.PayClientConfig;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.order.PayOrderUnifiedRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundRespDTO;
import cn.iocoder.yudao.framework.pay.core.client.dto.refund.PayRefundUnifiedReqDTO;
import cn.iocoder.yudao.framework.pay.core.client.exception.PayException;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Validation;

import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;

/**
 * 支付客户端的抽象类，提供模板方法，减少子类的冗余代码
 *
 * @author 芋道源码
 */
@Slf4j
public abstract class AbstractPayClient<Config extends PayClientConfig> implements PayClient {

    /**
     * 渠道编号
     */
    private final Long channelId;
    /**
     * 渠道编码
     */
    private final String channelCode;
    /**
     * 支付配置
     */
    protected Config config;

    public AbstractPayClient(Long channelId, String channelCode, Config config) {
        this.channelId = channelId;
        this.channelCode = channelCode;
        this.config = config;
    }

    /**
     * 初始化
     */
    public final void init() {
        doInit();
        log.info("[init][配置({}) 初始化完成]", config);
    }

    /**
     * 自定义初始化
     */
    protected abstract void doInit();

    public final void refresh(Config config) {
        // 判断是否更新
        if (config.equals(this.config)) {
            return;
        }
        log.info("[refresh][配置({})发生变化，重新初始化]", config);
        this.config = config;
        // 初始化
        this.init();
    }

    @Override
    public Long getId() {
        return channelId;
    }

    @Override
    public final PayOrderUnifiedRespDTO unifiedOrder(PayOrderUnifiedReqDTO reqDTO) {
        Validation.buildDefaultValidatorFactory().getValidator().validate(reqDTO);
        // 执行统一下单
        PayOrderUnifiedRespDTO resp;
        try {
            resp = doUnifiedOrder(reqDTO);
        } catch (ServiceException ex) {
            // 业务异常，都是实现类已经翻译，所以直接抛出即可
            throw ex;
        } catch (Throwable ex) {
            // 系统异常，则包装成 PayException 异常抛出
            log.error("[unifiedRefund][request({}) 发起支付异常]", toJsonString(reqDTO), ex);
            throw buildException(ex);
        }
        return resp;
    }

    protected abstract PayOrderUnifiedRespDTO doUnifiedOrder(PayOrderUnifiedReqDTO reqDTO)
            throws Throwable;

    @Override
    public PayRefundRespDTO unifiedRefund(PayRefundUnifiedReqDTO reqDTO) {
        Validation.buildDefaultValidatorFactory().getValidator().validate(reqDTO);
        // 执行统一退款
        PayRefundRespDTO resp;
        try {
            resp = doUnifiedRefund(reqDTO);
        } catch (ServiceException ex) {
            // 业务异常，都是实现类已经翻译，所以直接抛出即可
            throw ex;
        } catch (Throwable ex) {
            // 系统异常，则包装成 PayException 异常抛出
            log.error("[unifiedRefund][request({}) 发起退款异常]", toJsonString(reqDTO), ex);
            throw buildException(ex);
        }
        return resp;
    }

    protected abstract PayRefundRespDTO doUnifiedRefund(PayRefundUnifiedReqDTO reqDTO) throws Throwable;

    // ========== 各种工具方法 ==========

    private PayException buildException(Throwable ex) {
        if (ex instanceof PayException) {
            return (PayException) ex;
        }
        throw new PayException(ex);
    }

}
