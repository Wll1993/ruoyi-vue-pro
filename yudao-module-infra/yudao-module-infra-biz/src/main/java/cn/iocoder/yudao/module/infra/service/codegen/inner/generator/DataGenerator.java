package cn.iocoder.yudao.module.infra.service.codegen.inner.generator;

import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenColumnDO;

import java.util.Collections;
import java.util.List;

/**
 * 数据生成器
 *
 * @author https://github.com/liyupi
 */
public interface DataGenerator {

    /**
     * 生成
     *
     * @param field  字段信息
     * @param rowNum 行数
     * @return 生成的数据列表
     */
   default List<String> doGenerate(CodegenColumnDO field, int rowNum) {
       return Collections.emptyList();
   }

}
