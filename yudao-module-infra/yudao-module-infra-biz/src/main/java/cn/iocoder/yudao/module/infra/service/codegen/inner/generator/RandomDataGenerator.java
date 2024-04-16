package cn.iocoder.yudao.module.infra.service.codegen.inner.generator;

import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenColumnDO;
import cn.iocoder.yudao.module.infra.enums.codegen.MockParamsRandomTypeEnum;
import cn.iocoder.yudao.module.infra.enums.codegen.MockTypeEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 随机值数据生成器
 *
 * @author https://github.com/liyupi
 */
public class RandomDataGenerator implements DataGenerator {

    @Override
    public String getName() {
        return MockTypeEnum.RANDOM.getLabel();
    }

    @Override
    public List<String> doGenerate(CodegenColumnDO field, int rowNum) {
        String mockParams = field.getMockParams();
        List<String> list = new ArrayList<>(rowNum);
        for (int i = 0; i < rowNum; i++) {
            MockParamsRandomTypeEnum randomTypeEnum = Optional.ofNullable(
                            MockParamsRandomTypeEnum.getEnumByValue(mockParams))
                    .orElse(MockParamsRandomTypeEnum.STRING);
            String randomString = FakerUtils.getRandomValue(randomTypeEnum);
            list.add(randomString);
        }
        return list;
    }

    @Override
    public int getOrder() {
        return MockTypeEnum.RANDOM.getType();
    }
}
