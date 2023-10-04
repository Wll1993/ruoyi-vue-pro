package cn.iocoder.yudao.module.promotion.dal.mysql.bargain;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainHelpDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Mapper
public interface BargainHelpMapper extends BaseMapperX<BargainHelpDO> {

    default Long selectCountByUserIdAndActivityId(Long userId, Long activityId) {
        return selectCount(new LambdaQueryWrapper<>(BargainHelpDO.class)
                .eq(BargainHelpDO::getUserId, userId)
                .eq(BargainHelpDO::getActivityId, activityId));
    }

    default Long selectCountByRecordId(Long recordId) {
        return selectCount(BargainHelpDO::getRecordId, recordId);
    }

    default BargainHelpDO selectByUserIdAndRecordId(Long userId, Long recordId) {
        return selectOne(new LambdaQueryWrapper<>(BargainHelpDO.class)
                .eq(BargainHelpDO::getUserId, userId)
                .eq(BargainHelpDO::getRecordId, recordId));
    }


    default Map<Long, Integer> selectCountByActivityId(Collection<Long> activityIds) {
        // SQL count 查询
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<BargainHelpDO>()
                .select("COUNT(DISTINCT(user_id)) AS userCount, activity_id AS activityId")
                .in("activity_id", activityIds)
                .groupBy("activity_id"));
        if (CollUtil.isEmpty(result)) {
            return Collections.emptyMap();
        }
        // 转换数据
        return CollectionUtils.convertMap(result,
                record -> MapUtil.getLong(record, "activityId"),
                record -> MapUtil.getInt(record, "userCount" ));
    }

}
