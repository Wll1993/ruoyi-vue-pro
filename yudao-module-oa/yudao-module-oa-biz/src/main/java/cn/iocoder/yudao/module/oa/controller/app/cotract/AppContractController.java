package cn.iocoder.yudao.module.oa.controller.app.cotract;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.framework.security.core.annotations.PreAuthenticated;
import cn.iocoder.yudao.module.oa.controller.admin.contract.vo.*;
import cn.iocoder.yudao.module.oa.convert.contract.ContractConvert;
import cn.iocoder.yudao.module.oa.dal.dataobject.contract.ContractDO;
import cn.iocoder.yudao.module.oa.service.contract.ContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Tag(name = "用户App - 合同")
@RestController
@RequestMapping("/oa/contract")
@Validated
public class AppContractController {

    @Resource
    private ContractService contractService;

    @PostMapping("/create")
    @Operation(summary = "创建合同")
    @PreAuthenticated
    public CommonResult<Long> createContract(@Valid @RequestBody ContractCreateReqVO createReqVO) {
        return success(contractService.createContract(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新合同")
    @PreAuthenticated
    public CommonResult<Boolean> updateContract(@Valid @RequestBody ContractUpdateReqVO updateReqVO) {
        contractService.updateContract(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除合同")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthenticated
    public CommonResult<Boolean> deleteContract(@RequestParam("id") Long id) {
        contractService.deleteContract(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得合同")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthenticated
    public CommonResult<ContractRespVO> getContract(@RequestParam("id") Long id) {
        ContractDO contract = contractService.getContract(id);
        return success(ContractConvert.INSTANCE.convert(contract));
    }

    @GetMapping("/list")
    @Operation(summary = "获得合同列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthenticated
    public CommonResult<List<ContractRespVO>> getContractList(@RequestParam("ids") Collection<Long> ids) {
        List<ContractDO> list = contractService.getContractList(ids);
        return success(ContractConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得合同分页")
    @PreAuthenticated
    public CommonResult<PageResult<ContractRespVO>> getContractPage(@Valid ContractPageReqVO pageVO) {
        PageResult<ContractDO> pageResult = contractService.getContractPage(pageVO);
        return success(ContractConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出合同 Excel")
    @OperateLog(type = EXPORT)
    @PreAuthenticated
    public void exportContractExcel(@Valid ContractExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<ContractDO> list = contractService.getContractList(exportReqVO);
        // 导出 Excel
        List<ContractExcelVO> datas = ContractConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "合同.xls", "数据", ContractExcelVO.class, datas);
    }

}
