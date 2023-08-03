package cn.iocoder.yudao.module.radar.api.device;

import cn.iocoder.yudao.module.radar.api.device.dto.DeviceDTO;

/**
 * @author whycode
 * @title: DeviceApi
 * @projectName ruoyi-vue-pro
 * @description: TODO
 * @date 2023/8/214:05
 */
public interface DeviceApi {

    /**
     * 根据设备编号返回设备
     * @param sn 设备编号
     * @return
     */
    DeviceDTO queryBySn(String sn);

}
