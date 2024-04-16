package cn.iocoder.yudao.module.infra.service.codegen.inner.generator;

import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenColumnDO;
import cn.iocoder.yudao.module.infra.enums.codegen.MockTypeEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 递增值数据生成器
 *
 * @author https://github.com/liyupi
 */
public class IncreaseDataGenerator implements DataGenerator {

    @Override
    public String getName() {
        return MockTypeEnum.INCREASE.getLabel();
    }

    @Override
    public List<String> doGenerate(CodegenColumnDO field, int rowNum) {
        String mockParams = field.getMockParams();
        List<String> list = new ArrayList<>(rowNum);
        if (StringUtils.isBlank(mockParams)) {
            mockParams = "1";
        }
        int initValue = Integer.parseInt(mockParams);
        for (int i = 0; i < rowNum; i++) {
            list.add(String.valueOf(initValue + i));
        }
        return list;
    }

    @Override
    public int getOrder() {
        return MockTypeEnum.INCREASE.getType();
    }
}
