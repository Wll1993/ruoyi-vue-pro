package cn.iocoder.yudao.module.trade.service.delivery;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.express.DeliveryExpressCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.express.DeliveryExpressExportReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.express.DeliveryExpressPageReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.express.DeliveryExpressUpdateReqVO;
import cn.iocoder.yudao.module.trade.convert.delivery.DeliveryExpressConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressDO;
import cn.iocoder.yudao.module.trade.dal.mysql.delivery.DeliveryExpressMapper;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;


import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.*;

/**
 * 快递公司 Service 实现类
 *
 * @author jason
 */
@Service
@Validated
public class DeliveryExpressServiceImpl implements DeliveryExpressService {

    @Resource
    private DeliveryExpressMapper deliveryExpressMapper;

    @Override
    public Long createDeliveryExpress(DeliveryExpressCreateReqVO createReqVO) {
        //校验编码是否唯一
        validateExpressCodeUnique(createReqVO.getCode(), null);
        // 插入
        DeliveryExpressDO deliveryExpress = DeliveryExpressConvert.INSTANCE.convert(createReqVO);
        deliveryExpressMapper.insert(deliveryExpress);
        // 返回
        return deliveryExpress.getId();
    }

    @Override
    public void updateDeliveryExpress(DeliveryExpressUpdateReqVO updateReqVO) {
        // 校验存在
        validateDeliveryExpressExists(updateReqVO.getId());
        //校验编码是否唯一
        validateExpressCodeUnique(updateReqVO.getCode(), updateReqVO.getId());
        // 更新
        DeliveryExpressDO updateObj = DeliveryExpressConvert.INSTANCE.convert(updateReqVO);
        deliveryExpressMapper.updateById(updateObj);
    }

    @Override
    public void deleteDeliveryExpress(Long id) {
        // 校验存在
        validateDeliveryExpressExists(id);
        // 删除
        deliveryExpressMapper.deleteById(id);
    }

    private void validateExpressCodeUnique(String code, Long id) {
        DeliveryExpressDO express = deliveryExpressMapper.selectByCode(code);
        if (express == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的快递公司
        if (id == null) {
            throw exception(EXPRESS_CODE_DUPLICATE);
        }
        if (!express.getId().equals(id)) {
            throw exception(EXPRESS_CODE_DUPLICATE);
        }
    }
    private void validateDeliveryExpressExists(Long id) {
        if (deliveryExpressMapper.selectById(id) == null) {
            throw exception(EXPRESS_NOT_EXISTS);
        }
    }

    @Override
    public DeliveryExpressDO getDeliveryExpress(Long id) {
        return deliveryExpressMapper.selectById(id);
    }

    @Override
    public List<DeliveryExpressDO> getDeliveryExpressList(Collection<Long> ids) {
        return deliveryExpressMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<DeliveryExpressDO> getDeliveryExpressPage(DeliveryExpressPageReqVO pageReqVO) {
        return deliveryExpressMapper.selectPage(pageReqVO);
    }

    @Override
    public List<DeliveryExpressDO> getDeliveryExpressList(DeliveryExpressExportReqVO exportReqVO) {
        return deliveryExpressMapper.selectList(exportReqVO);
    }

}
