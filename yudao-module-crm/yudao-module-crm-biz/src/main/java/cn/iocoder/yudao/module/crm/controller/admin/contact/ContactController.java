package cn.iocoder.yudao.module.crm.controller.admin.contact;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerExportReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.service.customer.CrmCustomerService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.google.common.collect.Lists;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import javax.validation.constraints.*;
import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;
import java.util.stream.Collectors;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.ContactDO;
import cn.iocoder.yudao.module.crm.convert.contact.ContactConvert;
import cn.iocoder.yudao.module.crm.service.contact.ContactService;

@Tag(name = "管理后台 - crm联系人")
@RestController
@RequestMapping("/crm/contact")
@Validated
public class ContactController {

    @Resource
    private ContactService contactService;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private CrmCustomerService crmCustomerService;

    @PostMapping("/create")
    @Operation(summary = "创建crm联系人")
    @PreAuthorize("@ss.hasPermission('crm:contact:create')")
    public CommonResult<Long> createContact(@Valid @RequestBody ContactCreateReqVO createReqVO) {
        return success(contactService.createContact(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新crm联系人")
    @PreAuthorize("@ss.hasPermission('crm:contact:update')")
    public CommonResult<Boolean> updateContact(@Valid @RequestBody ContactUpdateReqVO updateReqVO) {
        contactService.updateContact(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除crm联系人")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('crm:contact:delete')")
    public CommonResult<Boolean> deleteContact(@RequestParam("id") Long id) {
        contactService.deleteContact(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得crm联系人")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('crm:contact:query')")
    public CommonResult<ContactRespVO> getContact(@RequestParam("id") Long id) {
        ContactDO contact = contactService.getContact(id);
        ContactRespVO contactRespVO  = ContactConvert.INSTANCE.convert(contact);
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(CollUtil.removeNull(Lists.newArrayList(
                NumberUtil.parseLong(contact.getCreator()))));
        contactRespVO.setCreatorName(Optional.ofNullable(userMap.get(NumberUtil.parseLong(contact.getCreator()))).map(AdminUserRespDTO::getNickname).orElse(null));
        contactRespVO.setCustomerName(Optional.ofNullable(crmCustomerService.getCustomer(contact.getCustomerId())).map(CrmCustomerDO::getName).orElse(null));
        return success(contactRespVO);
    }

    @GetMapping("/list")
    @Operation(summary = "获得crm联系人列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('crm:contact:query')")
    public CommonResult<List<ContactRespVO>> getContactList(@RequestParam("ids") Collection<Long> ids) {
        List<ContactDO> list = contactService.getContactList(ids);
        return success(ContactConvert.INSTANCE.convertList(list));
    }
    @GetMapping("/simpleAlllist")
    @Operation(summary = "获得crm联系人列表")
    @PreAuthorize("@ss.hasPermission('crm:contact:query')")
    public CommonResult<List<ContactSimpleRespVO>> simpleAlllist() {
        List<ContactDO> list = contactService.allContactList();
        return success(ContactConvert.INSTANCE.convertAllList(list));
    }
    @GetMapping("/page")
    @Operation(summary = "获得crm联系人分页")
    @PreAuthorize("@ss.hasPermission('crm:contact:query')")
    public CommonResult<PageResult<ContactRespVO>> getContactPage(@Valid ContactPageReqVO pageVO) {
        PageResult<ContactDO> pageData = contactService.getContactPage(pageVO);
        PageResult<ContactRespVO> pageResult =ContactConvert.INSTANCE.convertPage(pageData);
        //待接口实现后修改
        List<CrmCustomerDO> crmCustomerDOList = crmCustomerService.getCustomerList(new CrmCustomerExportReqVO());
        Map<Long,CrmCustomerDO> crmCustomerDOMap = crmCustomerDOList.stream().collect(Collectors.toMap(CrmCustomerDO::getId,v->v));
        pageResult.getList().forEach(item -> {
            item.setCustomerName(Optional.ofNullable(crmCustomerDOMap.get(item.getCustomerId())).map(CrmCustomerDO::getName).orElse(null));
        });
        return success(pageResult);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出crm联系人 Excel")
    @PreAuthorize("@ss.hasPermission('crm:contact:export')")
    @OperateLog(type = EXPORT)
    public void exportContactExcel(@Valid ContactExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<ContactDO> list = contactService.getContactList(exportReqVO);
        // 导出 Excel
        List<ContactExcelVO> datas = ContactConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "crm联系人.xls", "数据", ContactExcelVO.class, datas);
    }

}
