package cn.iocoder.yudao.module.infra.enums.codegen;

import cn.hutool.core.util.ObjectUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 模拟类型枚举
 *
 * @author https://github.com/liyupi
 */
public enum MockTypeEnum {

    /**
     * 不模拟
     */
    NONE(1, "不模拟"),

    /**
     * 递增
     */
    INCREASE(2, "递增"),
    /**
     * 固定
     */
    FIXED(3, "固定"),
    /**
     * 随机
     */
    RANDOM(4, "随机"),
    /**
     * 规则
     */
    RULE(5, "规则"),
    /**
     * 词库
     */
    DICT(6, "词库");

    private final Integer type;
    private final String label;

    MockTypeEnum(Integer type, String label) {
        this.type = type;
        this.label = label;
    }

    /**
     * 获取标签列表
     *
     * @return 标签列表
     */
    public static List<String> getLabels() {
        return Arrays.stream(values()).map(MockTypeEnum::getLabel).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 类型值
     * @return 模拟类型
     */
    public static MockTypeEnum getEnumByValue(Integer value) {
        if (ObjectUtil.isNull(value)) {
            return null;
        }
        for (MockTypeEnum mockTypeEnum : MockTypeEnum.values()) {
            if (mockTypeEnum.type.equals(value)) {
                return mockTypeEnum;
            }
        }
        return null;
    }

    public Integer getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }
}
