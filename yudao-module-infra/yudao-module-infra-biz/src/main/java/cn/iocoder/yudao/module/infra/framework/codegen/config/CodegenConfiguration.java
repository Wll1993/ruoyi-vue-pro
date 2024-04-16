package cn.iocoder.yudao.module.infra.framework.codegen.config;

import cn.iocoder.yudao.module.infra.service.codegen.inner.DataGeneratorFactory;
import cn.iocoder.yudao.module.infra.service.codegen.inner.generator.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(CodegenProperties.class)
public class CodegenConfiguration {
    /**
     * 初始化一个数据生成器工厂
     *
     * @param dataGenerators 其他的数据生成器
     * @return 数据生成器工厂
     */
    @Bean
    public DataGeneratorFactory dataGeneratorFactory(ObjectProvider<DataGenerator> dataGenerators, ObjectProvider<ObjectMapper> objectMapper) {
        final List<DataGenerator> mockTypeDataGeneratorMap = new LinkedList<>();
        mockTypeDataGeneratorMap.add(new DefaultDataGenerator());
        mockTypeDataGeneratorMap.add(new FixedDataGenerator());
        mockTypeDataGeneratorMap.add(new RandomDataGenerator());
        mockTypeDataGeneratorMap.add(new RuleDataGenerator());
        mockTypeDataGeneratorMap.add(new DictDataGenerator(objectMapper.getIfAvailable(ObjectMapper::new)));
        mockTypeDataGeneratorMap.add(new IncreaseDataGenerator());
        //不允许覆盖默认处理器
        dataGenerators.orderedStream().forEach(
                dataGenerator -> {
                    mockTypeDataGeneratorMap.add(dataGenerator.getOrder(), dataGenerator);
                }
        );
        return new DataGeneratorFactory(mockTypeDataGeneratorMap);
    }
}
