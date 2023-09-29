package cn.iocoder.yudao.module.biz.service.calc;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.biz.controller.admin.calc.dto.ExecProcessDataDTO;
import cn.iocoder.yudao.module.biz.controller.admin.calc.dto.MonthInfoDTO;
import cn.iocoder.yudao.module.biz.controller.admin.calc.dto.YearInfoDTO;
import cn.iocoder.yudao.module.biz.controller.admin.calc.vo.*;
import cn.iocoder.yudao.module.biz.convert.calc.CalcInterestRateDataConvert;
import cn.iocoder.yudao.module.biz.dal.dataobject.calc.CalcInterestRateDataDO;
import cn.iocoder.yudao.module.biz.dal.mysql.calc.CalcInterestRateDataMapper;
import cn.iocoder.yudao.module.biz.util.CodeUtil;
import cn.iocoder.yudao.module.biz.util.DateUtil;
import cn.iocoder.yudao.module.system.enums.ErrorCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;


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
     * 罚息利率规定变更时间
     */
    private final static Date FX_DATE = DateUtil.paseDate("2014-08-01 00:00:00");
    private final static BigDecimal FX_RATE = new BigDecimal("0.000175");

    @Resource
    private CalcInterestRateDataMapper calcInterestRateDataMapper;

    @Override
    public CalcInterestRateExecResVO execCalcInterestLxData(CalcInterestRateExecLxParamVO paramVO) {
        String processId = CodeUtil.getUUID();
        paramVO.setProcessId(processId);
        return execLx(paramVO);
    }

    @Override
    public CalcInterestRateExecResVO execCalcInterestFxData(CalcInterestRateExecFxParamVO paramVO) {
        String processId = CodeUtil.getUUID();
        paramVO.setProcessId(processId);
        return execFx(paramVO);
    }

    /**
     * 执行金额或者价额不超过1万元的，每件交纳50元；超过1万元至50万元的部分，按照1.5％交纳；超过50万元至500万元的部分，按照1％交纳；超过500万元至1000万元的部分，按照0.5％交纳；超过1000万元的部分，按照0.1％交纳
     *
     * @param execVO
     * @return
     */
    @Override
    public CalcInterestRateExecZxfResVO execCalcFeeData(CalcInterestRateExecZxfParamVO execVO) {
        execVO.setTotalAmount(execVO.getTotalAmount());
        CalcInterestRateExecZxfResVO vo = new CalcInterestRateExecZxfResVO();
        BigDecimal zxfAmount = BigDecimal.ZERO;
        BigDecimal leftAmount = BigDecimal.ZERO;
        //一万
        BigDecimal ten_thousand = new BigDecimal("10000");
        //五十万
        BigDecimal five_hundred_thousand = new BigDecimal("500000");
        //五百万
        BigDecimal five_million = new BigDecimal("5000000");
        //一千万
        BigDecimal ten_million = new BigDecimal("10000000");
        if (execVO.getZxfType() == 1) {
            //计算总执行费，即计算zxfAmount
            //计算梯度
            Integer level = 0;
            if (execVO.getTotalAmount() == null || execVO.getTotalAmount().compareTo(ten_thousand) <= 0) {
                //执行金额或者价额不超过1万元的
                level = 1;
            } else if (execVO.getTotalAmount().compareTo(ten_thousand) > 0 && execVO.getTotalAmount().compareTo(five_hundred_thousand) <= 0) {
                //超过1万元至50万元的部分
                level = 2;
            } else if (execVO.getTotalAmount().compareTo(five_hundred_thousand) > 0 && execVO.getTotalAmount().compareTo(five_million) <= 0) {
                //超过50万元至500万元的部分
                level = 3;
            } else if (execVO.getTotalAmount().compareTo(five_million) > 0 && execVO.getTotalAmount().compareTo(ten_million) <= 0) {
                //超过500万元至1000万元的部分
                level = 4;
            } else if (execVO.getTotalAmount().compareTo(ten_million) > 0) {
                //超过1000万元的部分
                level = 5;
            }
            if (level >= 1) {
                //执行金额或者价额不超过1万元的，每件交纳50元；
                zxfAmount = zxfAmount.add(new BigDecimal("50"));
            }
            if (level >= 2) {
                //执行金额或者价额不超过1万元的，每件交纳50元；超过1万元至50万元的部分，按照1.5％交纳
                BigDecimal calcAmount = five_hundred_thousand.compareTo(execVO.getTotalAmount()) >= 0 ? execVO.getTotalAmount() : five_hundred_thousand;
                zxfAmount = zxfAmount.add(calcAmount.subtract(ten_thousand).multiply(new BigDecimal("0.015")));
            }
            if (level >= 3) {
                BigDecimal calcAmount = five_million.compareTo(execVO.getTotalAmount()) >= 0 ? execVO.getTotalAmount() : five_million;
                //执行金额或者价额不超过1万元的，每件交纳50元；超过1万元至50万元的部分，按照1.5％交纳；超过50万元至500万元的部分，按照1％交纳
                zxfAmount = zxfAmount.add(calcAmount.subtract(five_hundred_thousand).multiply(new BigDecimal("0.01")));
            }
            if (level >= 4) {
                BigDecimal calcAmount = ten_million.compareTo(execVO.getTotalAmount()) >= 0 ? execVO.getTotalAmount() : ten_million;
                //执行金额或者价额不超过1万元的，每件交纳50元；超过1万元至50万元的部分，按照1.5％交纳；超过50万元至500万元的部分，按照1％交纳；超过500万元至1000万元的部分，按照0.5％交纳
                zxfAmount = zxfAmount.add(calcAmount.subtract(five_million).multiply(new BigDecimal("0.005")));
            }
            if (level >= 5) {
                //执行金额或者价额不超过1万元的，每件交纳50元；超过1万元至50万元的部分，按照1.5％交纳；超过50万元至500万元的部分，按照1％交纳；超过500万元至1000万元的部分，按照0.5％交纳；超过1000万元的部分，按照0.1％交纳
                zxfAmount = zxfAmount.add(execVO.getTotalAmount().subtract(ten_million).multiply(new BigDecimal("0.001")));
            }
            zxfAmount.setScale(2, RoundingMode.HALF_UP);
            vo.setZxfAmount(zxfAmount);
        } else if (execVO.getZxfType() == 2) {
            BigDecimal leve1 = new BigDecimal("10050");
            //五十万
            BigDecimal leve2 = new BigDecimal("507400");
            //五百万
            BigDecimal leve3 = new BigDecimal("5052400");
            //一千万
            BigDecimal leve4 = new BigDecimal("10077400");
            //计算总执行费，即计算zxfAmount和leftAmount
            //第一梯队的
            if (execVO.getTotalAmount() == null || execVO.getTotalAmount().compareTo(leve1) <= 0) {
                //执行金额或者价额不超过1万元的，每件交纳50元
                zxfAmount = new BigDecimal("50");
                leftAmount = execVO.getTotalAmount().subtract(zxfAmount);
            }
            if (execVO.getTotalAmount().compareTo(leve1) > 0 && execVO.getTotalAmount().compareTo(leve2) <= 0) {
                //超过1万元至50万元的部分，按照1.5％交纳；
                //执行金额或者价额不超过1万元的，每件交纳50元；超过1万元至50万元的部分，按照1.5％交纳
                leftAmount = (execVO.getTotalAmount().add(new BigDecimal("100"))).divide(new BigDecimal("1.015"), 2, RoundingMode.HALF_UP);
                zxfAmount = execVO.getTotalAmount().subtract(leftAmount);
            }
            if (execVO.getTotalAmount().compareTo(leve2) > 0 && execVO.getTotalAmount().compareTo(leve3) <= 0) {
                //超过50万元至500万元的部分，按照1％交纳
                //执行金额或者价额不超过1万元的，每件交纳50元；超过1万元至50万元的部分，按照1.5％交纳；超过50万元至500万元的部分，按照1％交纳
                leftAmount = (execVO.getTotalAmount().subtract(new BigDecimal("2400"))).divide(new BigDecimal("1.01"), 2, RoundingMode.HALF_UP);
                zxfAmount = execVO.getTotalAmount().subtract(leftAmount);
            }
            if (execVO.getTotalAmount().compareTo(leve3) > 0 && execVO.getTotalAmount().compareTo(leve4) <= 0) {
                //执行金额或者价额不超过1万元的，每件交纳50元；超过1万元至50万元的部分，按照1.5％交纳；超过50万元至500万元的部分，按照1％交纳；超过500万元至1000万元的部分，按照0.5％交纳
                //超过500万元至1000万元的部分，按照0.5％交纳
                leftAmount = (execVO.getTotalAmount().subtract(new BigDecimal("27400"))).divide(new BigDecimal("1.005"), 2, RoundingMode.HALF_UP);
                zxfAmount = execVO.getTotalAmount().subtract(leftAmount);
            }
            if (execVO.getTotalAmount().compareTo(leve4) > 0) {
                //超过1000万元的部分，按照0.1％交纳
                //执行金额或者价额不超过1万元的，每件交纳50元；超过1万元至50万元的部分，按照1.5％交纳；超过50万元至500万元的部分，按照1％交纳；超过500万元至1000万元的部分，按照0.5％交纳；超过1000万元的部分，按照0.1％交纳
                leftAmount = (execVO.getTotalAmount().subtract(new BigDecimal("67400"))).divide(new BigDecimal("1.001"), 2, RoundingMode.HALF_UP);
                zxfAmount = execVO.getTotalAmount().subtract(leftAmount);
            }
            vo.setZxfAmount(zxfAmount);
            vo.setLeftAmount(leftAmount);

        }

        return vo;
    }


    //获取所在年度天数
    private Integer getDaysThisYear(Date date) {
        boolean isLeapYear = cn.hutool.core.date.DateUtil.isLeapYear(cn.hutool.core.date.DateUtil.year(date));
        return isLeapYear ? 366 : 365;
    }

    /**
     * 处理利息+罚息
     */
    private CalcInterestRateExecResVO execLx(CalcInterestRateExecLxParamVO execVO) {
        CalcInterestRateExecResVO vo = new CalcInterestRateExecResVO();
        //计算开始结束时间差
        if (execVO.getRateType() == 1) {
            //1约定利率
            vo = getLxType1(execVO);
        } else if (execVO.getRateType() == 2) {
            //2中国人民银行同期贷款基准利率与LPR自动分段
            //计算日期区间,选择适用区间
            vo = getLxType2(execVO);
        } else if (execVO.getRateType() == 3) {
            //3全国银行间同业拆借中心公布的贷款市场报价利率(LPR)

        }
        return vo;
    }

    private CalcInterestRateExecResVO execFx(CalcInterestRateExecFxParamVO execVO) {
        return getFxType2(execVO);
    }


    private CalcInterestRateExecResVO getFxType2(CalcInterestRateExecFxParamVO execVO) {
        CalcInterestRateExecResVO vo = new CalcInterestRateExecResVO();
        //计算日期区间,选择适用区间
        String processId = execVO.getProcessId();
        Integer yearType = getYearType(execVO.getStartDate(), execVO.getEndDate());
        Date startDate = execVO.getStartDate();
        Date endDate = execVO.getEndDate();
        List<CalcInterestRateDataDO> allRateList = calcInterestRateDataMapper.selectAll();
        List<ExecProcessDataDTO> dataList = new ArrayList<>();
        while (startDate.compareTo(endDate) <= 0) {
            BigDecimal suiteDayRateValue = null;
            BigDecimal suiteYearRateValue = null;
            Integer yearDays = getDaysThisYear(startDate);
            if (startDate.compareTo(FX_DATE) < 0) {
                //2014-08-01之前
                CalcInterestRateDataDO suiteRate = getSuiteRate(allRateList, startDate);
                if (yearType == 1) {
                    suiteYearRateValue = suiteRate.getRateHalfYear();
                } else if (yearType == 2) {
                    suiteYearRateValue = suiteRate.getRateOneYear();
                } else if (yearType == 3) {
                    suiteYearRateValue = suiteRate.getRateThreeYear();
                } else if (yearType == 4) {
                    suiteYearRateValue = suiteRate.getRateFiveYear();
                } else if (yearType == 5) {
                    suiteYearRateValue = suiteRate.getRateOverFiveYear();
                }
                suiteDayRateValue = suiteYearRateValue.divide(new BigDecimal(100), 16, RoundingMode.HALF_UP).divide(new BigDecimal(yearDays), 16, RoundingMode.HALF_UP);
                ExecProcessDataDTO execDataIndex = new ExecProcessDataDTO(CodeUtil.getUUID(), processId, suiteRate.getId(), startDate, suiteDayRateValue, suiteDayRateValue.multiply(execVO.getLeftAmount()).multiply(new BigDecimal("2")), suiteYearRateValue.multiply(new BigDecimal("2")));
                dataList.add(execDataIndex);
            } else {
                suiteDayRateValue = FX_RATE;
                ExecProcessDataDTO execDataIndex = new ExecProcessDataDTO(CodeUtil.getUUID(), processId, 0, startDate, suiteDayRateValue, suiteDayRateValue.multiply(execVO.getLeftAmount()), suiteDayRateValue.multiply(new BigDecimal("100")).multiply(new BigDecimal(yearDays)));
                dataList.add(execDataIndex);
            }
            startDate = DateUtil.addDays(startDate, 1);
        }
        insertDataList(dataList);
        vo.setSectionList(calcInterestRateDataMapper.selectSectionListByProcessAndYearType(processId));
        vo.setTotalAmount(calcInterestRateDataMapper.selectTotalAmountByProcessId(processId));
        vo.setProcessId(processId);
        return vo;
    }


    private CalcInterestRateExecResVO getLxType1(CalcInterestRateExecLxParamVO execVO) {
        CalcInterestRateExecResVO vo = new CalcInterestRateExecResVO();
        if (execVO.getFixType() == 1) {
            //固定数值
            if (execVO.getFixSectionType() == 1) {
                //1-年
                Date startDate = execVO.getStartDate();
                Date endDate = execVO.getEndDate();
                List<YearInfoDTO> yearList = getYearList(startDate, endDate);
                List<ExecProcessDataDTO> dataList = new ArrayList<>();
                for (YearInfoDTO yearIndex : yearList) {
                    ExecProcessDataDTO execDataIndex = null;
                    if (yearIndex.getIsFull() == 1) {
                        execDataIndex = new ExecProcessDataDTO(CodeUtil.getUUID(), execVO.getProcessId(), 0, yearIndex.getYearStartDate(), yearIndex.getYearEndDate(), execVO.getFixRate().divide(new BigDecimal(100)), execVO.getFixRate().divide(new BigDecimal(100)).multiply(execVO.getLeftAmount()), yearIndex.getDays(), execVO.getFixRate());
                    } else {
                        //计算日利率
                        BigDecimal dayRateValue = execVO.getFixRate().divide(new BigDecimal(100)).divide(new BigDecimal(yearIndex.getFullDays()), 16, RoundingMode.HALF_UP);
                        //处理非整年金额
                        execDataIndex = new ExecProcessDataDTO(CodeUtil.getUUID(), execVO.getProcessId(), 0, yearIndex.getYearStartDate(), yearIndex.getYearEndDate(), execVO.getFixRate().divide(new BigDecimal(100)), dayRateValue.multiply(execVO.getLeftAmount()).multiply(new BigDecimal(yearIndex.getDays())), yearIndex.getDays(), execVO.getFixRate());
                    }
                    dataList.add(execDataIndex);
                }
                insertDataListYear(dataList);
                vo.setSectionList(calcInterestRateDataMapper.selectSectionListByFixYearRate(execVO.getProcessId()));
                vo.setTotalAmount(calcInterestRateDataMapper.selectTotalAmountByProcessId(execVO.getProcessId()));
                vo.setProcessId(execVO.getProcessId());
            } else if (execVO.getFixSectionType() == 2) {
                //2-月
                //判断有多少个整月，然后最后不足一个月的有多少天
                Date startDate = execVO.getStartDate();
                Date endDate = execVO.getEndDate();
                List<MonthInfoDTO> monthList = getMonthList(startDate, endDate);
                List<ExecProcessDataDTO> dataList = new ArrayList<>();
                for (MonthInfoDTO monthIndex : monthList) {
                    ExecProcessDataDTO execDataIndex = null;
                    if (monthIndex.getIsFull() == 1) {
                        execDataIndex = new ExecProcessDataDTO(CodeUtil.getUUID(), execVO.getProcessId(), 0, monthIndex.getMonthStartDate(), monthIndex.getMonthEndDate(), execVO.getFixRate().divide(new BigDecimal(100)), execVO.getFixRate().divide(new BigDecimal(100)).multiply(execVO.getLeftAmount()), monthIndex.getDays(), execVO.getFixRate().multiply(new BigDecimal("12")), monthIndex.getFullDays(), monthIndex.getIsFull());
                    } else {
                        //计算月利率
                        BigDecimal dayRateValue = execVO.getFixRate().divide(new BigDecimal(100)).divide(new BigDecimal(monthIndex.getFullDays()), 16, RoundingMode.HALF_UP);
                        //处理非整月金额
                        execDataIndex = new ExecProcessDataDTO(CodeUtil.getUUID(), execVO.getProcessId(), 0, monthIndex.getMonthStartDate(), monthIndex.getMonthEndDate(), execVO.getFixRate().divide(new BigDecimal(100)), dayRateValue.multiply(execVO.getLeftAmount()).multiply(new BigDecimal(monthIndex.getDays())), monthIndex.getDays(), execVO.getFixRate().multiply(new BigDecimal("12")), monthIndex.getFullDays(), monthIndex.getIsFull()

                        );
                    }
                    dataList.add(execDataIndex);
                }
                insertDataListMonth(dataList);
                vo.setSectionList(calcInterestRateDataMapper.selectSectionListByFixMonthRate(execVO.getProcessId()));
                vo.setTotalAmount(calcInterestRateDataMapper.selectTotalAmountByProcessId(execVO.getProcessId()));
                vo.setProcessId(execVO.getProcessId());
            } else if (execVO.getFixSectionType() == 3) {
                //3-日
                Date startDate = execVO.getStartDate();
                Date endDate = execVO.getEndDate();
                BigDecimal dayRateValue = execVO.getFixRate().divide(new BigDecimal(100), 16, RoundingMode.HALF_UP);
                List<ExecProcessDataDTO> dataList = new ArrayList<>();
                while (startDate.compareTo(endDate) <= 0) {
                    ExecProcessDataDTO execDataIndex = new ExecProcessDataDTO(CodeUtil.getUUID(), execVO.getProcessId(), 0, startDate, dayRateValue, dayRateValue.multiply(execVO.getLeftAmount()), execVO.getFixRate().multiply(new BigDecimal(getDaysThisYear(startDate))));
                    dataList.add(execDataIndex);
                    startDate = DateUtil.addDays(startDate, 1);
                }
                insertDataList(dataList);
                vo.setSectionList(calcInterestRateDataMapper.selectSectionListByFixRate(execVO.getProcessId()));
                vo.setTotalAmount(calcInterestRateDataMapper.selectTotalAmountByProcessId(execVO.getProcessId()));
                vo.setProcessId(execVO.getProcessId());
            }

        } else if (execVO.getFixType() == 2) {
            List<CalcInterestRateDataDO> allRateList = calcInterestRateDataMapper.selectAll();
            //LPR倍数
            Date startDate = execVO.getStartDate();
            Date endDate = execVO.getEndDate();
            if (startDate.compareTo(DateUtil.paseDate("2019-08-20", DateUtil.DATE_FORMAT_NORMAL)) < 0) {
                //2019年以前不可以选择LPR4倍数据
//                throw new RuntimeException("起始时间是2019年8月20号以前，不允许使用LPR倍数！");
                throw exception(ErrorCodeConstants.LPRS);
            }
            List<ExecProcessDataDTO> dataList = new ArrayList<>();
            while (startDate.compareTo(endDate) <= 0) {
                CalcInterestRateDataDO suiteRate = getSuiteRate(allRateList, startDate);
                Integer yearDays = getDaysThisYear(startDate);
                BigDecimal suiteYearRateValue = suiteRate.getRateOneYear();
                BigDecimal suiteRateValue = suiteRate.getRateOneYear();
                if (execVO.getFixLPRs().compareTo(new BigDecimal("4")) >= 0) {
                    suiteYearRateValue = suiteRateValue.multiply(new BigDecimal("4"));
                    suiteRateValue = suiteRateValue.multiply(new BigDecimal("4")).divide(new BigDecimal(100), 16, RoundingMode.HALF_UP).divide(new BigDecimal(yearDays), 16, RoundingMode.HALF_UP);
                } else {
                    suiteYearRateValue = suiteRateValue.multiply(execVO.getFixLPRs());
                    suiteRateValue = suiteRateValue.multiply(execVO.getFixLPRs()).divide(new BigDecimal(100), 16, RoundingMode.HALF_UP).divide(new BigDecimal(yearDays), 16, RoundingMode.HALF_UP);
                }
                suiteRate.getEndDate();

                Date currentEndDate = endDate.compareTo(suiteRate.getEndDate() == null ? DateUtil.getToday() : suiteRate.getEndDate()) > 0 ? suiteRate.getEndDate() : endDate;
                ExecProcessDataDTO execDataIndex = new ExecProcessDataDTO(CodeUtil.getUUID(), execVO.getProcessId(), suiteRate.getId(), startDate, currentEndDate, suiteRateValue, suiteRateValue.multiply(execVO.getLeftAmount()), DateUtil.dateIntervalDay(startDate, currentEndDate) + 1, suiteYearRateValue, 1);
                dataList.add(execDataIndex);
                startDate = DateUtil.addDays(startDate, 1);
            }
            insertDataList(dataList);
            List<SectionIndexVO> sectionList = calcInterestRateDataMapper.selectSectionListByFixLPRs(execVO.getProcessId());
            vo.setSectionList(mergeList(sectionList));
            vo.setTotalAmount(calcInterestRateDataMapper.selectTotalAmountByProcessId(execVO.getProcessId()));
            vo.setProcessId(execVO.getProcessId());

        }
        return vo;
    }

    private List<SectionIndexVO> mergeList(List<SectionIndexVO> sectionList) {
        List<SectionIndexVO> finalList = new ArrayList<>();
        for (SectionIndexVO index : sectionList) {
            if (CollectionUtils.isEmpty(finalList)) {
                finalList.add(index);
                continue;
            }
            SectionIndexVO last = finalList.get(finalList.size() - 1);

            if (last.getSuiteRate().equals(index.getSuiteRate())) {
                /**
                 * 相同的税率
                 */
                last.setEndDate(index.getEndDate());
                last.setDays(String.valueOf(Integer.parseInt(last.getDays()) + Integer.parseInt(index.getDays())));
                last.setSectionAmount(last.getSectionAmount().add(index.getSectionAmount()));
            } else {
                /**
                 * 不同的税率
                 */
                finalList.add(index);
            }
        }
        return finalList;
    }

    private CalcInterestRateDataDO getSuiteRate(List<CalcInterestRateDataDO> allRateList, Date startDate) {
        CalcInterestRateDataDO suiteRate = null;
        for (CalcInterestRateDataDO index : allRateList) {
            if (index.getStartDate().compareTo(startDate) > 0) {
                break;
            }
            suiteRate = index;
        }
        return suiteRate;
    }

    private List<MonthInfoDTO> getMonthList(Date startDate, Date endDate) {
        //获取startDate 所在的次月1号
        List<MonthInfoDTO> monthList = new ArrayList<>();
        while (startDate.compareTo(endDate) <= 0) {
            Date preStartDate = startDate;
            Date preMonthLastDay = DateUtil.getMonthLastDay(startDate, DateUtil.DATE_FORMAT_NORMAL);
            Date preMonthFirstDay = DateUtil.getMonthFirstDay(preMonthLastDay, DateUtil.DATE_FORMAT_NORMAL);
            Integer fullDays = DateUtil.dateIntervalDay(preMonthFirstDay, preMonthLastDay) + 1;
            Date preEndDate = preMonthLastDay.compareTo(endDate) <= 0 ? preMonthLastDay : endDate;

            Integer isFull = 1;
            Integer days = 0;
            if (startDate.compareTo(preMonthFirstDay) == 0 && preEndDate.compareTo(preMonthLastDay) == 0) {
                days = DateUtil.dateIntervalDay(startDate, preEndDate) + 1;
            } else {
                isFull = 0;
                days = DateUtil.dateIntervalDay(preStartDate, preEndDate) + 1;
            }
            MonthInfoDTO monthInfoDTO = new MonthInfoDTO(startDate, preEndDate, isFull, days, fullDays);
            if (!monthList.contains(monthInfoDTO)) {
                monthList.add(monthInfoDTO);
            }
            //加一个月
            startDate = DateUtil.getMonthFirstDay(cn.hutool.core.date.DateUtil.offsetMonth(preMonthLastDay, 1), DateUtil.DATE_FORMAT_NORMAL);

        }
        return monthList;
    }

    private List<YearInfoDTO> getYearList(Date startDate, Date endDate) {
        List<YearInfoDTO> yearList = new ArrayList<>();
        while (startDate.compareTo(endDate) <= 0) {
            Integer isFull = 0;
            Integer days = 0;
            Integer fullDays = 0;
            Date currentEnd = null;
            if (startDate.compareTo(DateUtil.getYearFirstDay(startDate)) != 0 && endDate.compareTo(DateUtil.getYearLastDay(startDate)) >= 0) {
                //非整年，但是年尾在结束日期之前
                isFull = 0;
                days = DateUtil.dateIntervalDay(startDate, DateUtil.getYearLastDay(startDate)) + 1;
                fullDays = getDaysThisYear(endDate);
                currentEnd = DateUtil.getYearLastDay(startDate);
            } else if (startDate.compareTo(DateUtil.getYearFirstDay(startDate)) == 0 && endDate.compareTo(DateUtil.getYearLastDay(startDate)) >= 0) {
                //一整年
                isFull = 1;
                days = getDaysThisYear(startDate);
                fullDays = getDaysThisYear(startDate);
                currentEnd = DateUtil.getYearLastDay(startDate);
            } else if (startDate.compareTo(DateUtil.getYearFirstDay(startDate)) == 0 && endDate.compareTo(DateUtil.getYearLastDay(startDate)) < 0) {
                //最后一个非整年
                isFull = 0;
                days = DateUtil.dateIntervalDay(startDate, endDate) + 1;
                fullDays = getDaysThisYear(startDate);
                currentEnd = endDate;
            } else if (startDate.compareTo(DateUtil.getYearFirstDay(startDate)) != 0 && endDate.compareTo(DateUtil.getYearLastDay(startDate)) <= 0) {
                //非整年，但是年尾在结束日期之前
                isFull = 0;
                days = DateUtil.dateIntervalDay(startDate, endDate) + 1;
                fullDays = getDaysThisYear(endDate);
                currentEnd = DateUtil.getYearLastDay(startDate);
            }
            YearInfoDTO yearInfoDTO = new YearInfoDTO(startDate, currentEnd, isFull, days, fullDays);
            yearList.add(yearInfoDTO);
            startDate = DateUtil.getYearFirstDay(cn.hutool.core.date.DateUtil.offsetMonth(startDate, 12));
        }
        return yearList;
    }

    private List<YearInfoDTO> getYearListBak(Date startDate, Date endDate) {
        List<YearInfoDTO> yearList = new ArrayList<>();
        while (startDate.compareTo(endDate) <= 0) {
            Date preStartDate = startDate;
            Date preEndDate = null;
            Date end = DateUtil.addDays(cn.hutool.core.date.DateUtil.offsetMonth(startDate, 12), -1);
            Integer isFull = 1;
            Integer days = 0;
            if (end.compareTo(endDate) > 0) {
                isFull = 0;
                days = DateUtil.dateIntervalDay(startDate, end);
                end = endDate;
            } else {
                days = DateUtil.dateIntervalDay(startDate, end);
                days = days + 1;
            }
            YearInfoDTO yearInfoDTO = new YearInfoDTO(startDate, end, isFull, days);
            if (!yearList.contains(yearInfoDTO)) {
                yearInfoDTO.setDays(DateUtil.dateIntervalDay(yearInfoDTO.getYearStartDate(), yearInfoDTO.getYearEndDate()) + 1);
                yearList.add(yearInfoDTO);
            }
            //加12个月
            startDate = cn.hutool.core.date.DateUtil.offsetMonth(startDate, 12);
            preEndDate = DateUtil.addDays(startDate, -1);
            yearInfoDTO.setFullDays(DateUtil.dateIntervalDay(preStartDate, preEndDate) + 1);
        }
        return getYearList(yearList);
    }

    private List<YearInfoDTO> getYearList(List<YearInfoDTO> rawList) {
        List<YearInfoDTO> finalList = new ArrayList<>();
        for (YearInfoDTO yearIndex : rawList) {
            if (1 == yearIndex.getIsFull()) {
                finalList.add(yearIndex);
            } else {
                Date startDate = yearIndex.getYearStartDate();
                Date endDate = yearIndex.getYearEndDate();
                //处理尾年，跨年分段
                if (DateUtil.format(startDate, DateUtil.YEAR_FORMAT_NORMAL).equals(DateUtil.format(endDate, DateUtil.YEAR_FORMAT_NORMAL))) {
                    finalList.add(yearIndex);
                } else {
                    /**
                     * 首段结束时间
                     */
                    Date startYearEndDate = DateUtil.getYearLastDay(startDate);
                    finalList.add(new YearInfoDTO(startDate, startYearEndDate, 0, DateUtil.dateIntervalDay(startDate, startYearEndDate) + 1, getDaysThisYear(startDate)));
                    /**
                     * 次段结束时间
                     */
                    Date endYearFirstDate = DateUtil.getYearFirstDay(endDate);
                    finalList.add(new YearInfoDTO(endYearFirstDate, endDate, 0, DateUtil.dateIntervalDay(endYearFirstDate, endDate) + 1, getDaysThisYear(endDate)));
                }
            }
        }
        return finalList;
    }


    private CalcInterestRateExecResVO getLxType2(CalcInterestRateExecLxParamVO execVO) {
        CalcInterestRateExecResVO vo = new CalcInterestRateExecResVO();
        Integer yearType = getYearType(execVO.getStartDate(), execVO.getEndDate());
        List<CalcInterestRateDataDO> allRateList = calcInterestRateDataMapper.selectAll();
        Date startDate = execVO.getStartDate();
        Date endDate = execVO.getEndDate();
        List<ExecProcessDataDTO> dataList = new ArrayList<>();
        while (startDate.compareTo(endDate) <= 0) {
            CalcInterestRateDataDO suiteRate = getSuiteRate(allRateList, startDate);
            BigDecimal suiteDayRateValue = null;
            BigDecimal suiteYearRateValue = null;
            Integer yearDays = getDaysThisYear(startDate);
            if (yearType == 1) {
                suiteYearRateValue = suiteRate.getRateHalfYear();
            } else if (yearType == 2) {
                suiteYearRateValue = suiteRate.getRateOneYear();
            } else if (yearType == 3) {
                suiteYearRateValue = suiteRate.getRateThreeYear();
            } else if (yearType == 4) {
                suiteYearRateValue = suiteRate.getRateFiveYear();
            } else if (yearType == 5) {
                suiteYearRateValue = suiteRate.getRateOverFiveYear();
            }
            suiteDayRateValue = suiteYearRateValue.divide(new BigDecimal(100), 16, RoundingMode.HALF_UP).divide(new BigDecimal(yearDays), 16, RoundingMode.HALF_UP);
            ExecProcessDataDTO execDataIndex = new ExecProcessDataDTO(CodeUtil.getUUID(), execVO.getProcessId(), suiteRate.getId(), startDate, suiteDayRateValue, suiteDayRateValue.multiply(execVO.getLeftAmount()), suiteYearRateValue, 2);
            dataList.add(execDataIndex);
            startDate = DateUtil.addDays(startDate, 1);
        }
        insertDataList(dataList);
        List<SectionIndexVO> sectionList = calcInterestRateDataMapper.selectSectionListByProcessAndYearType(execVO.getProcessId());
        vo.setSectionList(mergeList(sectionList));
        vo.setTotalAmount(calcInterestRateDataMapper.selectTotalAmountByProcessId(execVO.getProcessId()));
        vo.setProcessId(execVO.getProcessId());
        return vo;
    }

    private void insertDataList(List<ExecProcessDataDTO> dataList) {
        if (!CollectionUtils.isEmpty(dataList)) {
            calcInterestRateDataMapper.insertExecProcessDataBatch(dataList);
        }
    }

    private void insertDataListMonth(List<ExecProcessDataDTO> dataList) {
        if (!CollectionUtils.isEmpty(dataList)) {
            calcInterestRateDataMapper.insertExecProcessDataMonthBatch(dataList);
        }
    }

    private void insertDataListYear(List<ExecProcessDataDTO> dataList) {
        if (!CollectionUtils.isEmpty(dataList)) {
            calcInterestRateDataMapper.insertExecProcessDataYearBatch(dataList);
        }
    }


    private Integer getYearType(Date startDate, Date endDate) {
        Integer yearType = 1;
        int diffDays = DateUtil.dateIntervalDay(startDate, endDate);
        //判断所属区间
        BigDecimal yt = new BigDecimal(diffDays).divide(new BigDecimal(getDaysThisYear(startDate)), 2, RoundingMode.HALF_UP);
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
