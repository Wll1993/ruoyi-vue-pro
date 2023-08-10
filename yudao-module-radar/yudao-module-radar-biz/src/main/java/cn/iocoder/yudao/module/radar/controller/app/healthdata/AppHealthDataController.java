package cn.iocoder.yudao.module.radar.controller.app.healthdata;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.module.radar.controller.admin.healthdata.vo.*;
import cn.iocoder.yudao.module.radar.controller.app.healthdata.vo.AppHealthDataReqVO;
import cn.iocoder.yudao.module.radar.controller.app.healthdata.vo.AppHealthDataResVO;
import cn.iocoder.yudao.module.radar.controller.app.healthdata.vo.BreathAndHeartDataVO;
import cn.iocoder.yudao.module.radar.controller.app.healthdata.vo.BreathAndHeartVO;
import cn.iocoder.yudao.module.radar.convert.healthdata.HealthDataConvert;
import cn.iocoder.yudao.module.radar.dal.dataobject.healthdata.HealthDataDO;
import cn.iocoder.yudao.module.radar.service.healthdata.HealthDataService;
import cn.iocoder.yudao.module.radar.service.healthstatistics.HealthStatisticsService;
import com.google.common.util.concurrent.AtomicDouble;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Tag(name = "用户APP - 体征数据")
@RestController
@RequestMapping("/radar/health-data")
@Validated
public class AppHealthDataController {

    @Resource
    private HealthDataService healthDataService;

    @Resource
    private HealthStatisticsService healthStatisticsService;


    private final Double SLEEP = 70.0;

    @GetMapping("/list")
    @Operation(summary = "获得心率和呼吸数据")
    @PreAuthenticated
    public CommonResult<AppHealthDataResVO> getHealthDataList(AppHealthDataReqVO reqVO) {

        LocalDate startDate = reqVO.getStartDate();
        if(startDate == null){
            startDate = LocalDate.now();
        }
        LocalDate yesterday = startDate.plusDays(-1L);

        // 查询昨天 20点到当前日期
        reqVO.setCreateTime(new String[]{LocalDateTimeUtil.format(yesterday, "yyyy-MM-dd") + " 20:00:00",
                                         LocalDateTimeUtil.format(startDate, "yyyy-MM-dd") + " 20:00:00"});

        List<HealthDataDO> list = healthDataService.getHealthDataList(reqVO);
        if(CollUtil.isEmpty(list)){
            return success(new AppHealthDataResVO());
        }

        AppHealthDataResVO resVO = new AppHealthDataResVO();
        BreathAndHeartVO heartVO = new BreathAndHeartVO();
        BreathAndHeartVO breathVO = new BreathAndHeartVO();
        resVO.setHeart(heartVO).setBreath(breathVO);

        AtomicDouble heart = new AtomicDouble();
        AtomicDouble breath = new AtomicDouble();

        List<BreathAndHeartDataVO> heartList = new ArrayList<>();
        List<BreathAndHeartDataVO> breathList = new ArrayList<>();

        heartVO.setTimeList(heartList);
        breathVO.setTimeList(breathList);

        List<HealthDataDO> lowSleep = new ArrayList<>();
        List<HealthDataDO> wakeList = new ArrayList<>();
        LocalDateTime sleepStart = null;
        LocalDateTime wakeTime = null;

        for(HealthDataDO healthDataDO: list){
            Double heartFreqAverage = healthDataDO.getHeartFreqAverage();
            Integer hasPeople = healthDataDO.getHasPeople();
            LocalDateTime createTime = healthDataDO.getCreateTime();
            if(!Objects.equals(0, hasPeople) && SLEEP.compareTo(heartFreqAverage) > 0){
                // 此处表示心率低于睡眠检查时间
                if(sleepStart == null){
                    sleepStart = createTime;
                }else{
                    // 如果时间小于等于 1 分钟表明是连续性的
                    if(Duration.between(createTime, sleepStart).toMinutes() <= 1) {
                        sleepStart = createTime;
                        lowSleep.add(healthDataDO);
                    }else{
                        lowSleep.clear();
                        lowSleep.add(healthDataDO);
                    }
                }
            }else{
                // 低心率大于 10 表明睡眠了
                if(lowSleep.size() > 10){
                    double average = average(lowSleep);
                    if(hasPeople == 0 || (heartFreqAverage - average) >= 0.2 * heartFreqAverage){
                        wakeList.add(healthDataDO);
                        if(wakeTime == null){
                            wakeTime = createTime;
                        }else{
                            if(Duration.between(createTime, wakeTime).toMinutes() <= 1) {
                                wakeTime = createTime;
                            }else{
                                lowSleep.addAll(wakeList);
                                wakeList.clear();
                            }
                        }
                        if(wakeList.size() >= 3){
                            break;
                        }
                    }else{
                        lowSleep.addAll(wakeList);
                        lowSleep.add(healthDataDO);
                        wakeList.clear();
                    }
                }else{
                    lowSleep.clear();
                    sleepStart = null;
                }
            }
        }

        double maxHeart = 0.0;
        double minHeart = Double.MAX_VALUE;
        for(HealthDataDO healthData: lowSleep) {
            LocalDateTime createTime = healthData.getCreateTime();
            Double breathFreqAverage = healthData.getBreathFreqAverage();
            Double heartFreqAverage = healthData.getHeartFreqAverage();
            heart.addAndGet(heartFreqAverage);
            breath.addAndGet(breathFreqAverage);
            String time = DateUtil.format(createTime, DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND);
            heartList.add(new BreathAndHeartDataVO().setTime(time).setValue(heartFreqAverage));
            breathList.add(new BreathAndHeartDataVO().setTime(time).setValue(breathFreqAverage));
            maxHeart = Math.max(maxHeart,  heartFreqAverage);
            minHeart = Math.min(minHeart, heartFreqAverage);

        }
        HealthDataDO healthDataDO = lowSleep.get(lowSleep.size() - 1);
        heartVO.setAverage(heart.get() / lowSleep.size());
        heartVO.setCurrent(healthDataDO.getHeartFreqAverage());
        heartVO.setHighest(maxHeart).setLowest(minHeart);
        breathVO.setAverage(breath.get() / lowSleep.size());
        breathVO.setCurrent(healthDataDO.getBreathFreqAverage());
        return success(resVO);
    }

    private double average( List<HealthDataDO> lowSleep){
        return lowSleep.stream().map(HealthDataDO::getHeartFreqAverage)
                .mapToDouble(s -> s).summaryStatistics().getAverage();

    }

}
