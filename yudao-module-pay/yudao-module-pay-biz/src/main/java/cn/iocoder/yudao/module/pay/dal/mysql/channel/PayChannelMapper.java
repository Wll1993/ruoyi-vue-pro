package cn.iocoder.yudao.module.pay.dal.mysql.channel;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.pay.controller.admin.channel.vo.PayChannelPageReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.channel.PayChannelDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface PayChannelMapper extends BaseMapperX<PayChannelDO> {

    default PayChannelDO selectByAppIdAndCode(Long appId, String code) {
        return selectOne(PayChannelDO::getAppId, appId, PayChannelDO::getCode, code);
    }

    default PageResult<PayChannelDO> selectPage(PayChannelPageReqVO reqVO) {
        return selectPage(reqVO, new QueryWrapperX<PayChannelDO>()
                .eqIfPresent("code", reqVO.getCode())
                .eqIfPresent("status", reqVO.getStatus())
                .eqIfPresent("remark", reqVO.getRemark())
                .eqIfPresent("fee_rate", reqVO.getFeeRate())
                .eqIfPresent("app_id", reqVO.getAppId())
                .betweenIfPresent("create_time", reqVO.getCreateTime())
                .orderByDesc("id"));
    }

    /**
     * 根据条件获取渠道
     *
     * @param appId      应用编号
     * @param code       渠道编码
     * @return 数量
     */
    default PayChannelDO selectOne(Long appId, String code) {
        return this.selectOne((new QueryWrapper<PayChannelDO>().lambda()
                .eq(PayChannelDO::getAppId, appId)
                .eq(PayChannelDO::getCode, code)
        ));
    }

    // TODO @aquan：select 命名
    /**
     * 根据支付应用ID集合获得支付渠道列表
     *
     * @param appIds 应用编号集合
     * @return 支付渠道列表
     */
    default List<PayChannelDO> getChannelListByAppIds(Collection<Long> appIds){
        return this.selectList(new QueryWrapper<PayChannelDO>().lambda()
                .in(PayChannelDO::getAppId, appIds));
    }

    default List<PayChannelDO> selectListByAppId(Long appId, Integer status) {
        return selectList(new LambdaQueryWrapperX<PayChannelDO>()
                .eq(PayChannelDO::getAppId, appId)
                .eq(PayChannelDO::getStatus, status));
    }

}