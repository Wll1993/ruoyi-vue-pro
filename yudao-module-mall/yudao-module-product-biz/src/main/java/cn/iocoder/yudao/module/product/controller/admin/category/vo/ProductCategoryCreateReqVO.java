package cn.iocoder.yudao.module.product.controller.admin.category.vo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(title = "管理后台 - 商品分类创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductCategoryCreateReqVO extends ProductCategoryBaseVO {

}
