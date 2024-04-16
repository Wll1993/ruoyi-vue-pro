package cn.iocoder.yudao.module.infra.enums.codegen;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenColumnDO;
import org.apache.commons.lang3.StringUtils;

/**
 * 字段类型枚举
 *
 * @author https://github.com/liyupi
 */
public enum FieldTypeEnum {

    TINYINT("TINYINT", "Integer", "number"),
    SMALLINT("SMALLINT", "Integer", "number"),
    MEDIUMINT("MEDIUMINT", "Integer", "number"),
    INT("INT", "Integer", "number"),
    BIGINT("BIGINT", "Long", "number"),
    FLOAT("FLOAT", "Double", "number"),
    DOUBLE("DOUBLE", "Double", "number"),
    DECIMAL("DECIMAL", "BigDecimal", "number"),
    DATE("DATE", "Date", "Date"),
    TIME("TIME", "Time", "Date"),
    YEAR("YEAR", "Integer", "number"),
    DATETIME("DATETIME", "Date", "Date"),
    TIMESTAMP("TIMESTAMP", "Long", "number"),
    CHAR("CHAR", "String", "string"),
    VARCHAR("VARCHAR", "String", "string"),
    TINYTEXT("TINYTEXT", "String", "string"),
    TEXT("TEXT", "String", "string"),
    MEDIUMTEXT("MEDIUMTEXT", "String", "string"),
    LONGTEXT("LONGTEXT", "String", "string"),
    TINYBLOB("TINYBLOB", "byte[]", "string"),
    BLOB("BLOB", "byte[]", "string"),
    MEDIUMBLOB("MEDIUMBLOB", "byte[]", "string"),
    LONGBLOB("LONGBLOB", "byte[]", "string"),
    BINARY("BINARY", "byte[]", "string"),
    VARBINARY("VARBINARY", "byte[]", "string");

    private final String value;

    private final String javaType;

    private final String typescriptType;

    FieldTypeEnum(String value, String javaType, String typescriptType) {
        this.value = value;
        this.javaType = javaType;
        this.typescriptType = typescriptType;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(FieldTypeEnum::getValue).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static FieldTypeEnum getEnumByValue(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        for (FieldTypeEnum mockTypeEnum : FieldTypeEnum.values()) {
            if (mockTypeEnum.value.equals(value)) {
                return mockTypeEnum;
            }
        }
        return null;
    }

    /**
     * 根据列的属性获取值字符串
     *
     * @param field 字段
     * @param value 结果值
     * @return 按照数据库指定类型转换后的结果值
     */
    public static String getValueStr(CodegenColumnDO field, Object value) {
        if (field == null || value == null) {
            return "''";
        }
        FieldTypeEnum fieldTypeEnum = Optional.ofNullable(getEnumByValue(field.getDataType()))
                .orElse(TEXT);
        String result = String.valueOf(value);
        switch (fieldTypeEnum) {
            case DATETIME:
            case TIMESTAMP:
                return result.equalsIgnoreCase("CURRENT_TIMESTAMP") ? result : String.format("'%s'", value);
            case DATE:
            case TIME:
            case CHAR:
            case VARCHAR:
            case TINYTEXT:
            case TEXT:
            case MEDIUMTEXT:
            case LONGTEXT:
            case TINYBLOB:
            case BLOB:
            case MEDIUMBLOB:
            case LONGBLOB:
            case BINARY:
            case VARBINARY:
                return String.format("'%s'", value);
            default:
                return result;
        }
    }

    public String getValue() {
        return value;
    }

    public String getJavaType() {
        return javaType;
    }

    public String getTypescriptType() {
        return typescriptType;
    }
}
