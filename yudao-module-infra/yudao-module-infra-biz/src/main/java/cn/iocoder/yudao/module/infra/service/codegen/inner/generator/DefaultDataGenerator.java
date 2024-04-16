package cn.iocoder.yudao.module.infra.service.codegen.inner.generator;

import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenColumnDO;
import cn.iocoder.yudao.module.infra.enums.codegen.MockTypeEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 固定值数据生成器
 *
 * @author https://github.com/liyupi
 */
public class DefaultDataGenerator implements DataGenerator {

    @Override
    public String getName() {
        return MockTypeEnum.NONE.getLabel();
    }

    @Override
    public List<String> doGenerate(CodegenColumnDO field, int rowNum) {
        List<String> list = new ArrayList<>(rowNum);
        // 主键采用递增策略
        if (field.getPrimaryKey()) {
            String mockParams = field.getMockParams();
            if (StringUtils.isBlank(mockParams)) {
                mockParams = "1";
            }
            int initValue = Integer.parseInt(mockParams);
            for (int i = 0; i < rowNum; i++) {
                list.add(String.valueOf(initValue + i));
            }
            return list;
        }
        // 使用默认值
        String defaultValue = field.getDefaultValue();
        // 特殊逻辑，日期要伪造数据
        if ("CURRENT_TIMESTAMP".equals(defaultValue)) {
            defaultValue = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        }
        if (StringUtils.isNotBlank(defaultValue)) {
            for (int i = 0; i < rowNum; i++) {
                list.add(defaultValue);
            }
        }
        return list;
    }

    @Override
    public int getOrder() {
        return MockTypeEnum.NONE.getType();
    }
}
