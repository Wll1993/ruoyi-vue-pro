package cn.iocoder.yudao.module.steam.controller.app.selling;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.steam.controller.admin.selling.vo.SellingPageReqVO;
import cn.iocoder.yudao.module.steam.controller.admin.selling.vo.SellingRespVO;
import cn.iocoder.yudao.module.steam.controller.app.droplist.vo.InvPageReqVo;
import cn.iocoder.yudao.module.steam.controller.app.selling.vo.SellingChangePriceReqVo;
import cn.iocoder.yudao.module.steam.controller.app.selling.vo.SellingMergeListVO;
import cn.iocoder.yudao.module.steam.controller.app.selling.vo.SellingReqVo;
import cn.iocoder.yudao.module.steam.dal.dataobject.selling.SellingDO;
import cn.iocoder.yudao.module.steam.service.selling.SellingExtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Optional;

@RestController
@Validated
@RequestMapping("/steam-app/sale")
@Slf4j
@Tag(name="饰品出售管理")
public class AppSellingController {

    @Resource
    private SellingExtService sellingExtService;

    @GetMapping("/onsale")
    @Operation(summary = "饰品上架出售")
    public CommonResult<SellingDO> getOnSale(@Valid InvPageReqVo invPageReqVo) {
        // 入参：id  price
        // 查询 steam_inv
        SellingDO invPage = sellingExtService.getToSale(invPageReqVo);
        return CommonResult.success(invPage);

    }
    @GetMapping("/offsale")
    @Operation(summary = "饰品下架")
    public CommonResult<Optional<SellingDO>> getOffSale(@Valid SellingReqVo sellingReqVo) {

        Optional<SellingDO> invPage = sellingExtService.getOffSale(sellingReqVo);
        return CommonResult.success(invPage);

    }
    @GetMapping("/changePrice")
    @Operation(summary = "饰品改价")
    public CommonResult<Integer> changePrice(@RequestBody @Valid SellingChangePriceReqVo reqVo) {

        Integer integer = sellingExtService.changePrice(reqVo);
        return CommonResult.success(integer);
    }
    @GetMapping("/user/sellingUnMerge")
    @Operation(summary = "出售未合并")
    public CommonResult<PageResult<SellingRespVO>> sellingUnMerge(@Valid SellingPageReqVO sellingPageReqVO) {
        SellingPageReqVO pageReqVO = new SellingPageReqVO();

        PageResult<SellingRespVO> sellingDOS = sellingExtService.sellingUnMerge(sellingPageReqVO);
        pageReqVO.setPageSize(20);
        pageReqVO.setPageNo(1);

        return CommonResult.success(sellingDOS);

    }
    @GetMapping("/user/sellingMerge")
    @Operation(summary = "出售合并")
    public CommonResult<PageResult<SellingMergeListVO>> sellingMerge(@Valid SellingPageReqVO sellingPageReqVO) {
        SellingPageReqVO pageReqVO = new SellingPageReqVO();
        pageReqVO.setPageSize(20);
        pageReqVO.setPageNo(1);
        PageResult<SellingMergeListVO> invPage = sellingExtService.sellingMerge(sellingPageReqVO);
        return CommonResult.success(invPage);
    }
}

