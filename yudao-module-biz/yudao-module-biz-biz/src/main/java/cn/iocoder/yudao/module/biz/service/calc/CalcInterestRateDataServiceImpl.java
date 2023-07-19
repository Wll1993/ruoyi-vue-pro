package cn.iocoder.yudao.module.biz.service.calc;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.biz.controller.admin.calc.dto.ExecProcessDataDTO;
import cn.iocoder.yudao.module.biz.controller.admin.calc.dto.YearInfo;
import cn.iocoder.yudao.module.biz.controller.admin.calc.vo.*;
import cn.iocoder.yudao.module.biz.convert.calc.CalcInterestRateDataConvert;
import cn.iocoder.yudao.module.biz.dal.dataobject.calc.CalcInterestRateDataDO;
import cn.iocoder.yudao.module.biz.dal.mysql.calc.CalcInterestRateDataMapper;
import cn.iocoder.yudao.module.biz.util.CodeUtil;
import cn.iocoder.yudao.module.biz.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;


/**
 * 利率数据 Service 实现类
 *
 * @author 芋道源码
 */
@Slf4j
@Service
@Validated
public class CalcInterestRateDataServiceImpl implements CalcInterestRateDataService {

    /**
     * LPR开始时间
     */
    private final static Date LPR_START_DATE = DateUtil.paseDate("2019-10-08", DateUtil.DATE_FORMAT_NORMAL);
    /**
     * 罚息利率规定变更时间
     */
    private final static Date FX_DATE = DateUtil.paseDate("2014-08-01", DateUtil.DATE_FORMAT_NORMAL);
    private final static BigDecimal FX_RATE = new BigDecimal("0.000175");

    @Resource
    private CalcInterestRateDataMapper calcInterestRateDataMapper;

    @Override
    public CalcInterestRateExecResVO execCalcInterestData(CalcInterestRateExecParamVO execVO) {
        CalcInterestRateExecResVO vo = new CalcInterestRateExecResVO();

        if (execVO.getCalcType() == 1) {
            execLx(execVO);
        } else if (execVO.getCalcType() == 2) {
            execFx(execVO);
        } else if (execVO.getCalcType() == 3) {
            execZxf(execVO);
        }

        return vo;
    }

    private List<YearInfo> getYearsList(Date startDate, Date endDate) {
        List<YearInfo> yearList = new ArrayList<>();
        List<Integer> yearVList = new ArrayList<>();
        while (startDate.compareTo(endDate) <= 0) {
            if (yearVList.contains(cn.hutool.core.date.DateUtil.year(startDate))) {
                startDate = DateUtil.addDays(startDate, 1);
                continue;
            }
            boolean isLeapYear = cn.hutool.core.date.DateUtil.isLeapYear(cn.hutool.core.date.DateUtil.year(startDate));
            yearList.add(new YearInfo(cn.hutool.core.date.DateUtil.year(startDate), isLeapYear));
            yearVList.add(cn.hutool.core.date.DateUtil.year(startDate));
            startDate = DateUtil.addDays(startDate, 1);
        }

        return yearList;
    }

    //获取所在年度天数
    private Integer getDaysThisYear() {

        return 0;
    }


    private CalcInterestRateExecResVO execLx(CalcInterestRateExecParamVO execVO) {
        List<CalcInterestRateDataDO> allRate = calcInterestRateDataMapper.selectAll();

        //计算开始结束时间差
        if (execVO.getLxRateType() == 1) {//1约定利率
            //计算日期差
            int days = DateUtil.dateIntervalDay(execVO.getLxStartDate(), execVO.getLxEndDate()) + 1;
            BigDecimal lxAmount =
                    execVO.getLxFixRate()
                            .divide(new BigDecimal(365), 8, RoundingMode.HALF_DOWN)
                            .divide(new BigDecimal(100))
                            .multiply(new BigDecimal(days))
                            .multiply(execVO.getLxAmount())
                            .setScale(2, RoundingMode.HALF_DOWN);
            log.info(lxAmount.toString());
        } else if (execVO.getLxRateType() == 2) {//2中国人民银行同期贷款基准利率与LPR自动分段
            List<YearInfo> yearList = getYearsList(execVO.getLxStartDate(), execVO.getLxEndDate());
            //计算日期区间,选择适用区间
            String processId = CodeUtil.getUUID();
            Integer yearType = getYearType(execVO.getLxStartDate(), execVO.getLxEndDate());
            Date startDate = execVO.getLxStartDate();
            Date endDate = execVO.getLxEndDate();
            while (startDate.compareTo(endDate) <= 0) {
                CalcInterestRateDataDO suiteRate = calcInterestRateDataMapper.selectSuitableRate(startDate);
                BigDecimal suiteRateValue = null;
                Integer yearDays = 365;
                if (yearType == 1) {
                    suiteRateValue = suiteRate.getRateHalfYear();
                } else if (yearType == 2) {
                    suiteRateValue = suiteRate.getRateOneYear();
                } else if (yearType == 3) {
                    suiteRateValue = suiteRate.getRateThreeYear();
                } else if (yearType == 4) {
                    suiteRateValue = suiteRate.getRateFiveYear();
                } else if (yearType == 5) {
                    suiteRateValue = suiteRate.getRateOverFiveYear();
                }
                suiteRateValue = suiteRateValue.divide(new BigDecimal(100), 3, RoundingMode.HALF_DOWN).divide(new BigDecimal(yearDays), 8, RoundingMode.HALF_DOWN);
                ExecProcessDataDTO execDataIndex = new ExecProcessDataDTO(CodeUtil.getUUID(), processId, suiteRate.getId(), startDate, suiteRateValue, suiteRateValue.multiply(execVO.getLxAmount()));
                calcInterestRateDataMapper.insertExecProcessData(execDataIndex);
                startDate = DateUtil.addDays(startDate, 1);
            }


        } else if (execVO.getLxRateType() == 3) {//3全国银行间同业拆借中心公布的贷款市场报价利率(LPR)

        }
        return null;
    }

    private Integer getYearType(Date startDate, Date endDate) {
        Integer yearType = 1;
        int diffDays = DateUtil.dateIntervalDay(startDate, endDate);
        //判断所属区间
        BigDecimal yt = new BigDecimal(diffDays).divide(new BigDecimal(365), 2, RoundingMode.HALF_DOWN);
        if (new BigDecimal("0.5").compareTo(yt) >= 0) {
            //半年期
            yearType = 1;
        } else if (new BigDecimal("0.5").compareTo(yt) < 0 && BigDecimal.ONE.compareTo(yt) >= 0) {
            //一年期
            yearType = 2;
        } else if (BigDecimal.ONE.compareTo(yt) < 0 && new BigDecimal("3").compareTo(yt) >= 0) {
            //三年期
            yearType = 3;
        } else if (new BigDecimal("3").compareTo(yt) < 0 && new BigDecimal("5").compareTo(yt) >= 0) {
            //三年到五年期
            yearType = 4;
        } else if (new BigDecimal("5").compareTo(yt) < 0) {
            //五年以上
            yearType = 5;
        }
        return yearType;
    }

    private CalcInterestRateExecResVO execFx(CalcInterestRateExecParamVO execVO) {

        return null;
    }

    private CalcInterestRateExecResVO execZxf(CalcInterestRateExecParamVO execVO) {

        return null;
    }


    @Override
    public Integer createCalcInterestRateData(CalcInterestRateDataCreateReqVO createReqVO) {
        // 插入
        CalcInterestRateDataDO calcInterestRateData = CalcInterestRateDataConvert.INSTANCE.convert(createReqVO);
        calcInterestRateDataMapper.insert(calcInterestRateData);
        // 返回
        return calcInterestRateData.getId();
    }


    @Override
    public void updateCalcInterestRateData(CalcInterestRateDataUpdateReqVO updateReqVO) {
        // 校验存在
        validateCalcInterestRateDataExists(updateReqVO.getId());
        // 更新
        CalcInterestRateDataDO updateObj = CalcInterestRateDataConvert.INSTANCE.convert(updateReqVO);
        calcInterestRateDataMapper.updateById(updateObj);
    }

    @Override
    public void deleteCalcInterestRateData(Integer id) {
        // 校验存在
        validateCalcInterestRateDataExists(id);
        // 删除
        calcInterestRateDataMapper.deleteById(id);
    }

    private void validateCalcInterestRateDataExists(Integer id) {
        if (calcInterestRateDataMapper.selectById(id) == null) {
//            throw exception(CALC_INTEREST_RATE_DATA_NOT_EXISTS);
        }
    }

    @Override
    public CalcInterestRateDataDO getCalcInterestRateData(Integer id) {
        return calcInterestRateDataMapper.selectById(id);
    }

    @Override
    public List<CalcInterestRateDataDO> getCalcInterestRateDataList(Collection<Integer> ids) {
        return calcInterestRateDataMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<CalcInterestRateDataDO> getCalcInterestRateDataPage(CalcInterestRateDataPageReqVO pageReqVO) {
        return calcInterestRateDataMapper.selectPage(pageReqVO);
    }

    @Override
    public List<CalcInterestRateDataDO> getCalcInterestRateDataList(CalcInterestRateDataExportReqVO exportReqVO) {
        return calcInterestRateDataMapper.selectList(exportReqVO);
    }

}
