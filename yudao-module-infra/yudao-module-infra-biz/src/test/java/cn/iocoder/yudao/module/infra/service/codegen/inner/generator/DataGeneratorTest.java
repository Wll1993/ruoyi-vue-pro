package cn.iocoder.yudao.module.infra.service.codegen.inner.generator;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenColumnDO;
import cn.iocoder.yudao.module.infra.enums.codegen.MockParamsRandomTypeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author kyle
 * @version 1.00.00
 */
class DataGeneratorTest {

    @DisplayName("测试固定数据生成器")
    @Test
    public void testGenerateFixedData() {
        int rowNum = 10;
        CodegenColumnDO field = new CodegenColumnDO();
        field.setMockParams("fake data");
        DataGenerator dataGenerator = new FixedDataGenerator();
        List<String> strings = dataGenerator.doGenerate(field, rowNum);
        Assertions.assertEquals(10, strings.size());
        System.out.println(StrUtil.format("产生的数据：{}", strings));
    }

    @DisplayName("测试递增数据生成器")
    @Test
    public void testGenerateIncreaseData() {
        int rowNum = 10;
        CodegenColumnDO field = new CodegenColumnDO();
        field.setMockParams("4");
        DataGenerator dataGenerator = new IncreaseDataGenerator();
        List<String> strings = dataGenerator.doGenerate(field, rowNum);
        Assertions.assertEquals(10, strings.size());
        System.out.println(StrUtil.format("产生的数据：{}", strings));
    }

    @DisplayName("测试随机数据生成器")
    @Test
    public void testGenerateRandomData() {
        int rowNum = 10;
        CodegenColumnDO field = new CodegenColumnDO();
        field.setMockParams(MockParamsRandomTypeEnum.IP.getValue());
        DataGenerator dataGenerator = new RandomDataGenerator();
        List<String> strings = dataGenerator.doGenerate(field, rowNum);
        Assertions.assertEquals(10, strings.size());
        System.out.println(StrUtil.format("产生的数据：{}", strings));
    }

    @DisplayName("测试正则表达式数据生成器")
    @Test
    public void testGenerateRuleData() {
        int rowNum = 10;
        CodegenColumnDO field = new CodegenColumnDO();
        field.setMockParams("[0-3]([a-c]|[e-g]{1,2})");
        DataGenerator dataGenerator = new RuleDataGenerator();
        List<String> strings = dataGenerator.doGenerate(field, rowNum);
        Assertions.assertEquals(10, strings.size());
        System.out.println(StrUtil.format("产生的数据：{}", strings));
    }
}