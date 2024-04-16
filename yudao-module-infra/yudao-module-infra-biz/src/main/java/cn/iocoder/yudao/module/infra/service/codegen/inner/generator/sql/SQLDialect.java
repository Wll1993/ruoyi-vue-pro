package cn.iocoder.yudao.module.infra.service.codegen.inner.generator.sql;

/**
 * SQL 方言
 *
 * @author https://github.com/liyupi
 */
public interface SQLDialect {

    /**
     * 封装字段名
     *
     * @param name 字段名称
     * @return 包装后的名称
     */
    String wrapFieldName(String name);

    /**
     * 解析字段名
     *
     * @param fieldName 包装后的名称
     * @return 解析出来的字段名称
     */
    String parseFieldName(String fieldName);

    /**
     * 封装表名
     *
     * @param name 表名称
     * @return 包装后的名称
     */
    String wrapTableName(String name);

    /**
     * 解析表名
     *
     * @param tableName 包装后的表名称
     * @return 解析出来的表名称
     */
    String parseTableName(String tableName);
}
