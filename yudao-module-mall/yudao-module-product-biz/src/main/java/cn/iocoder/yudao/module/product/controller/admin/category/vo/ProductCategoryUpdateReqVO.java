package cn.iocoder.yudao.module.product.controller.admin.category.vo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 商品分类更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductCategoryUpdateReqVO extends ProductCategoryBaseVO {

    @Schema(description = "分类编号", required = true, example = "2")
    @NotNull(message = "分类编号不能为空")
    private Long id;

}
