package cn.iocoder.yudao.module.infra.service.codegen.inner;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.infra.enums.codegen.MockTypeEnum;
import cn.iocoder.yudao.module.infra.service.codegen.inner.generator.DataGenerator;
import cn.iocoder.yudao.module.infra.service.codegen.inner.generator.DefaultDataGenerator;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * 数据生成器工厂
 * 工厂 + 单例模式，降低开销
 *
 * @author https://github.com/liyupi
 */
public class DataGeneratorFactory {

    /**
     * 模拟类型 => 生成器映射
     */
    private final List<DataGenerator> MOCK_TYPE_DATA_GENERATOR_COLL;

    /**
     * 默认兜底
     */
    private final DataGenerator DEFAULT;

    public DataGeneratorFactory(List<DataGenerator> mockTypeDataGeneratorMap) {
        MOCK_TYPE_DATA_GENERATOR_COLL = mockTypeDataGeneratorMap;
        DEFAULT = new DefaultDataGenerator();
    }

    /**
     * 生成一个新的List防止有人动数据生成器
     *
     * @return 返回现在所有的数据生成器
     */
    public List<DataGenerator> getDataGeneratorList() {
        return new LinkedList<>(MOCK_TYPE_DATA_GENERATOR_COLL);
    }

    /**
     * 获取实例，一定保证有默认处理器
     *
     * @param type 处理类型
     * @return 相应数据生成器
     */
    public DataGenerator getGenerator(Integer type) {
        Integer mockType = Optional.ofNullable(type).orElse(MockTypeEnum.NONE.getType());
        return MOCK_TYPE_DATA_GENERATOR_COLL.stream()
                .filter(dataGenerator -> ObjectUtil.equals(dataGenerator.getOrder(), mockType))
                .findFirst()
                .orElse(DEFAULT);
    }

}
