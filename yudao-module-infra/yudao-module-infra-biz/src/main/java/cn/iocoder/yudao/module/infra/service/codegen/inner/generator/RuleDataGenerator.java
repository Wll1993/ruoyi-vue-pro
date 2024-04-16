package cn.iocoder.yudao.module.infra.service.codegen.inner.generator;

import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenColumnDO;
import cn.iocoder.yudao.module.infra.enums.codegen.MockTypeEnum;
import com.mifmif.common.regex.Generex;

import java.util.ArrayList;
import java.util.List;

/**
 * 正则表达式数据生成器
 * 项目：https://github.com/mifmif/Generex
 *
 * @author https://github.com/liyupi
 */
public class RuleDataGenerator implements DataGenerator {

    @Override
    public String getName() {
        return MockTypeEnum.RULE.getLabel();
    }

    @Override
    public List<String> doGenerate(CodegenColumnDO field, int rowNum) {
        String mockParams = field.getMockParams();
        List<String> list = new ArrayList<>(rowNum);
        Generex generex = new Generex(mockParams);
        for (int i = 0; i < rowNum; i++) {
            String randomStr = generex.random();
            list.add(randomStr);
        }
        return list;
    }

    @Override
    public int getOrder() {
        return MockTypeEnum.RULE.getType();
    }
}
