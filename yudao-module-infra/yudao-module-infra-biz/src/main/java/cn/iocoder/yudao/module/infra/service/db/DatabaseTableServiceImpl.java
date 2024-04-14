package cn.iocoder.yudao.module.infra.service.db;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.infra.dal.dataobject.db.DataSourceConfigDO;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.builder.ConfigBuilder;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.query.SQLQuery;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据库表 Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class DatabaseTableServiceImpl implements DatabaseTableService {

    @Resource
    private DataSourceConfigService dataSourceConfigService;

    @Override
    public List<TableInfo> getTableList(Long dataSourceConfigId, String nameLike, String commentLike) {
        List<TableInfo> tables = getTableList0(dataSourceConfigId, null);
        return tables.stream().filter(tableInfo -> (StrUtil.isEmpty(nameLike) || tableInfo.getName().contains(nameLike))
                        && (StrUtil.isEmpty(commentLike) || tableInfo.getComment().contains(commentLike)))
                .collect(Collectors.toList());
    }

    @Override
    public TableInfo getTable(Long dataSourceConfigId, String name) {
        return CollUtil.getFirst(getTableList0(dataSourceConfigId, name));
    }

    private List<TableInfo> getTableList0(Long dataSourceConfigId, String name) {
        // 获得数据源配置
        DataSourceConfigDO config = dataSourceConfigService.getDataSourceConfig(dataSourceConfigId);
        Assert.notNull(config, "数据源({}) 不存在！", dataSourceConfigId);
        // 使用 MyBatis Plus Generator 解析表结构
        DataSourceConfig dataSourceConfig = null;
        DbType dbType = getDbType(config.getUrl());
        //mybatisplus-generator中DefaultQuery实现查询数据库表对MySQL正常，但SQL server存在无法获取注释的问题
        //解决方案，databaseQueryClass(SQLQuery.class)，使用旧版SQLQuery查询
        if (dbType.equals(DbType.SQL_SERVER)) {
            dataSourceConfig = new DataSourceConfig.Builder(config.getUrl(), config.getUsername(),
                    config.getPassword()).databaseQueryClass(SQLQuery.class).build();
        } else {
            dataSourceConfig = new DataSourceConfig.Builder(config.getUrl(), config.getUsername(),
                    config.getPassword()).build();
        }
        StrategyConfig.Builder strategyConfig = new StrategyConfig.Builder();
//        SqlServer数据库中mybatisPlusGenerator会把table和view都查询出来，导致空指针异常，
        strategyConfig.enableSkipView();
        if (StrUtil.isNotEmpty(name)) {
            strategyConfig.addInclude(name);
        } else {
            // 移除工作流和定时任务前缀的表名 // TODO 未来做成可配置
            strategyConfig.addExclude("ACT_[\\S\\s]+|QRTZ_[\\S\\s]+|FLW_[\\S\\s]+");
        }

        GlobalConfig globalConfig = new GlobalConfig.Builder().dateType(DateType.TIME_PACK).build(); // 只使用 LocalDateTime 类型，不使用 LocalDate
        ConfigBuilder builder = new ConfigBuilder(null, dataSourceConfig, strategyConfig.build(),
                null, globalConfig, null);
        // 按照名字排序
        List<TableInfo> tables = builder.getTableInfoList();
        tables.sort(Comparator.comparing(TableInfo::getName));
        return tables;
    }

    private DbType getDbType(String str) {
        if (str.contains(":mysql:") || str.contains(":cobar:")) {
            return DbType.MYSQL;
        } else if (str.contains(":oracle:")) {
            return DbType.ORACLE;
        } else if (str.contains(":postgresql:")) {
            return DbType.POSTGRE_SQL;
        } else if (str.contains(":sqlserver:")) {
            return DbType.SQL_SERVER;
        } else if (str.contains(":db2:")) {
            return DbType.DB2;
        } else if (str.contains(":mariadb:")) {
            return DbType.MARIADB;
        } else if (str.contains(":sqlite:")) {
            return DbType.SQLITE;
        } else if (str.contains(":h2:")) {
            return DbType.H2;
        } else if (str.contains(":lealone:")) {
            return DbType.LEALONE;
        } else if (str.contains(":kingbase:") || str.contains(":kingbase8:")) {
            return DbType.KINGBASE_ES;
        } else if (str.contains(":dm:")) {
            return DbType.DM;
        } else if (str.contains(":zenith:")) {
            return DbType.GAUSS;
        } else if (str.contains(":oscar:")) {
            return DbType.OSCAR;
        } else if (str.contains(":firebird:")) {
            return DbType.FIREBIRD;
        } else if (str.contains(":xugu:")) {
            return DbType.XU_GU;
        } else if (str.contains(":clickhouse:")) {
            return DbType.CLICK_HOUSE;
        } else if (str.contains(":sybase:")) {
            return DbType.SYBASE;
        } else {
            return DbType.OTHER;
        }
    }
}
