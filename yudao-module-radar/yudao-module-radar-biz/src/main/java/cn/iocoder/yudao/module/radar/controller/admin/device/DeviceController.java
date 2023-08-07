package cn.iocoder.yudao.module.radar.controller.admin.device;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import javax.validation.constraints.*;
import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.radar.controller.admin.device.vo.*;
import cn.iocoder.yudao.module.radar.dal.dataobject.device.DeviceDO;
import cn.iocoder.yudao.module.radar.convert.device.DeviceConvert;
import cn.iocoder.yudao.module.radar.service.device.DeviceService;

@Tag(name = "管理后台 - 设备信息")
@RestController
@RequestMapping("/radar/device")
@Validated
public class DeviceController {

    @Resource
    private DeviceService deviceService;

    @PostMapping("/create")
    @Operation(summary = "创建设备信息")
    @PreAuthorize("@ss.hasPermission('radar:device:create')")
    public CommonResult<Long> createDevice(@Valid @RequestBody DeviceCreateReqVO createReqVO) {
        return success(deviceService.createDevice(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新设备信息")
    @PreAuthorize("@ss.hasPermission('radar:device:update')")
    public CommonResult<Boolean> updateDevice(@Valid @RequestBody DeviceUpdateReqVO updateReqVO) {
        deviceService.updateDevice(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除设备信息")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('radar:device:delete')")
    public CommonResult<Boolean> deleteDevice(@RequestParam("id") Long id) {
        deviceService.deleteDevice(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得设备信息")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('radar:device:query')")
    public CommonResult<DeviceRespVO> getDevice(@RequestParam("id") Long id) {
        DeviceDO device = deviceService.getDevice(id);
        return success(DeviceConvert.INSTANCE.convert(device));
    }

    @GetMapping("/list")
    @Operation(summary = "获得设备信息列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('radar:device:query')")
    public CommonResult<List<DeviceRespVO>> getDeviceList(@RequestParam("ids") Collection<Long> ids) {
        List<DeviceDO> list = deviceService.getDeviceList(ids);
        return success(DeviceConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得设备信息分页")
    @PreAuthorize("@ss.hasPermission('radar:device:query')")
    public CommonResult<PageResult<DeviceRespVO>> getDevicePage(@Valid DevicePageReqVO pageVO) {
        PageResult<DeviceDO> pageResult = deviceService.getDevicePage(pageVO);
        return success(DeviceConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出设备信息 Excel")
    @PreAuthorize("@ss.hasPermission('radar:device:export')")
    @OperateLog(type = EXPORT)
    public void exportDeviceExcel(@Valid DeviceExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<DeviceDO> list = deviceService.getDeviceList(exportReqVO);
        // 导出 Excel
        List<DeviceExcelVO> datas = DeviceConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "设备信息.xls", "数据", DeviceExcelVO.class, datas);
    }

}