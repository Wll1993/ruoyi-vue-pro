package cn.iocoder.yudao.module.infra.service.codegen.inner.generator;

import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenColumnDO;
import cn.iocoder.yudao.module.infra.enums.codegen.MockTypeEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 固定值数据生成器
 *
 * @author https://github.com/liyupi
 */
public class FixedDataGenerator implements DataGenerator {

    @Override
    public List<String> doGenerate(CodegenColumnDO field, int rowNum) {
        String mockParams = field.getMockParams();
        if (StringUtils.isBlank(mockParams)) {
            mockParams = "6";
        }
        List<String> list = new ArrayList<>(rowNum);
        for (int i = 0; i < rowNum; i++) {
            list.add(mockParams);
        }
        return list;
    }

    @Override
    public int getOrder() {
        return MockTypeEnum.FIXED.getType();
    }

    @Override
    public String getName() {
        return MockTypeEnum.FIXED.getLabel();
    }
}
