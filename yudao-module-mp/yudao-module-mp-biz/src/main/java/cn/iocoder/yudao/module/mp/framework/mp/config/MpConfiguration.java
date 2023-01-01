package cn.iocoder.yudao.module.mp.framework.mp.config;

import cn.iocoder.yudao.module.mp.framework.mp.core.DefaultMpServiceFactory;
import cn.iocoder.yudao.module.mp.framework.mp.core.MpServiceFactory;
import cn.iocoder.yudao.module.mp.service.fansmsgres.NullHandler;
import cn.iocoder.yudao.module.mp.service.handler.*;
import com.binarywang.spring.starter.wxjava.mp.properties.WxMpProperties;
import me.chanjar.weixin.common.redis.RedisTemplateWxRedisOps;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 微信公众号的配置类
 *
 * @author 芋道源码
 */
@Configuration
public class MpConfiguration {

    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public RedisTemplateWxRedisOps redisTemplateWxRedisOps(StringRedisTemplate stringRedisTemplate) {
        return new RedisTemplateWxRedisOps(stringRedisTemplate);
    }

    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public MpServiceFactory mpServiceFactory(RedisTemplateWxRedisOps redisTemplateWxRedisOps,
                                             WxMpProperties wxMpProperties,
                                             LogHandler logHandler,
                                             KfSessionHandler kfSessionHandler,
                                             StoreCheckNotifyHandler storeCheckNotifyHandler,
                                             MenuHandler menuHandler,
                                             NullHandler nullHandler,
                                             SubscribeHandler subscribeHandler,
                                             UnsubscribeHandler unsubscribeHandler,
                                             LocationHandler locationHandler,
                                             ScanHandler scanHandler,
                                             DefaultMessageHandler msgHandler) {
        return new DefaultMpServiceFactory(redisTemplateWxRedisOps, wxMpProperties,
                logHandler, kfSessionHandler, storeCheckNotifyHandler, menuHandler,
                nullHandler, subscribeHandler, unsubscribeHandler, locationHandler, scanHandler, msgHandler);
    }

}
