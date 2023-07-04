package cn.iocoder.yudao.module.promotion.service.combination;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity.CombinationActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity.CombinationActivityExportReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity.CombinationActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity.CombinationActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.combinationactivity.CombinationActivityDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 拼团活动 Service 接口
 *
 * @author HUIHUI
 */
public interface CombinationActivityService {

    /**
     * 创建拼团活动
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCombinationActivity(@Valid CombinationActivityCreateReqVO createReqVO);

    /**
     * 更新拼团活动
     *
     * @param updateReqVO 更新信息
     */
    void updateCombinationActivity(@Valid CombinationActivityUpdateReqVO updateReqVO);

    /**
     * 删除拼团活动
     *
     * @param id 编号
     */
    void deleteCombinationActivity(Long id);

    /**
     * 获得拼团活动
     *
     * @param id 编号
     * @return 拼团活动
     */
    CombinationActivityDO getCombinationActivity(Long id);

    /**
     * 获得拼团活动列表
     *
     * @param ids 编号
     * @return 拼团活动列表
     */
    List<CombinationActivityDO> getCombinationActivityList(Collection<Long> ids);

    /**
     * 获得拼团活动分页
     *
     * @param pageReqVO 分页查询
     * @return 拼团活动分页
     */
    PageResult<CombinationActivityDO> getCombinationActivityPage(CombinationActivityPageReqVO pageReqVO);

    /**
     * 获得拼团活动列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 拼团活动列表
     */
    List<CombinationActivityDO> getCombinationActivityList(CombinationActivityExportReqVO exportReqVO);

}