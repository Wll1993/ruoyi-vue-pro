package cn.iocoder.yudao.module.radar.api.device;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.radar.api.device.dto.DeviceDTO;
import cn.iocoder.yudao.module.radar.controller.admin.device.vo.DeviceBaseVO;
import cn.iocoder.yudao.module.radar.controller.admin.device.vo.DeviceExportReqVO;
import cn.iocoder.yudao.module.radar.controller.admin.device.vo.DeviceRespVO;
import cn.iocoder.yudao.module.radar.controller.admin.device.vo.DeviceUpdateReqVO;
import cn.iocoder.yudao.module.radar.convert.device.DeviceConvert;
import cn.iocoder.yudao.module.radar.dal.dataobject.device.DeviceDO;
import cn.iocoder.yudao.module.radar.service.device.DeviceService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author whycode
 * @title: DeviceApiImpl
 * @projectName ruoyi-vue-pro
 * @description: TODO
 * @date 2023/8/214:09
 */

@Service
public class DeviceApiImpl implements DeviceApi{

    @Resource
    private DeviceService deviceService;

    @Override
    public DeviceDTO queryBySn(String sn) {
        List<DeviceDO> deviceList = deviceService.getDeviceList(new DeviceExportReqVO().setSn(sn));
        return CollUtil.isEmpty(deviceList)? new DeviceDTO(): DeviceConvert.INSTANCE.convertDTO(deviceList.get(0));
    }

    @Override
    public List<DeviceDTO> getByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)){
            return new ArrayList<>();
        }
        List<DeviceDO> deviceList = deviceService.getDeviceList(ids);
        return DeviceConvert.INSTANCE.convertList03(deviceList);
    }

    @Override
    public void bindOrUnBind(Long id, byte bind) {
        TenantUtils.executeIgnore(()->{
            deviceService.updateBind(bind, id);
        });
    }

    @Override
    public DeviceDTO getById(Long id) {
        return DeviceConvert.INSTANCE.convertDTO(deviceService.getDevice(id));
    }
}
