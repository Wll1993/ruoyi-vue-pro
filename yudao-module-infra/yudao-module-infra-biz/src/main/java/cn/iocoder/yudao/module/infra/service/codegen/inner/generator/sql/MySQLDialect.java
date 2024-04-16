package cn.iocoder.yudao.module.infra.service.codegen.inner.generator.sql;

/**
 * MySQL 方言
 *
 * @author https://github.com/liyupi
 */
public class MySQLDialect implements SQLDialect {

    @Override
    public String wrapFieldName(String name) {
        return String.format("`%s`", name);
    }

    @Override
    public String parseFieldName(String fieldName) {
        if (fieldName.startsWith("`") && fieldName.endsWith("`")) {
            return fieldName.substring(1, fieldName.length() - 1);
        }
        return fieldName;
    }

    @Override
    public String wrapTableName(String name) {
        return String.format("`%s`", name);
    }

    @Override
    public String parseTableName(String tableName) {
        if (tableName.startsWith("`") && tableName.endsWith("`")) {
            return tableName.substring(1, tableName.length() - 1);
        }
        return tableName;
    }
}
