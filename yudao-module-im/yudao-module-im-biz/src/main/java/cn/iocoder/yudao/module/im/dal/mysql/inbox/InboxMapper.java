package cn.iocoder.yudao.module.im.dal.mysql.inbox;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.im.dal.dataobject.inbox.InboxDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IM 收件箱 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface InboxMapper extends BaseMapperX<InboxDO> {

    default List<Long> selectMessageIdsByUserIdAndSequence(Long userId, Long sequence, Integer size) {
        return selectList(new LambdaQueryWrapperX<InboxDO>()
                .gt(InboxDO::getUserId, userId)
                .gt(InboxDO::getSequence, sequence)
                .orderByAsc(InboxDO::getSequence)
                .last("limit 0," + size))
                .stream()
                .map(InboxDO::getMessageId)
                .toList();
    }

//    default PageResult<InboxDO> selectPage(ImInboxPageReqVO reqVO) {
//        return selectPage(reqVO, new LambdaQueryWrapperX<InboxDO>()
//                .eqIfPresent(InboxDO::getUserId, reqVO.getUserId())
//                .eqIfPresent(InboxDO::getMessageId, reqVO.getMessageId())
//                .eqIfPresent(InboxDO::getSequence, reqVO.getSequence())
//                .betweenIfPresent(InboxDO::getCreateTime, reqVO.getCreateTime())
//                .orderByDesc(InboxDO::getId));
//    }

}