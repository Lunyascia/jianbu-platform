package org.dromara.walking.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
import org.dromara.common.core.domain.R;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.walking.domain.bo.admin.OrgBo;
import org.dromara.walking.domain.bo.admin.OrgImportBo;
import org.dromara.walking.domain.vo.admin.OrgTreeVo;
import org.dromara.walking.service.AdminOrgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 组织机构管理（后台）—— 复用 sys_dept
 */
@Validated
@RestController
@RequestMapping("/walking/admin/org")
public class AdminOrgController {

    @Autowired
    private AdminOrgService orgService;

    /** 组织机构树 */
    @SaCheckPermission("walking:org:list")
    @GetMapping("/tree")
    public R<List<OrgTreeVo>> tree() {
        return R.ok(orgService.tree());
    }

    /** 机构详情 */
    @SaCheckPermission("walking:org:query")
    @GetMapping("/{deptId}")
    public R<OrgTreeVo> getInfo(@PathVariable Long deptId) {
        return R.ok(orgService.getById(deptId));
    }

    /** 新增机构 */
    @SaCheckPermission("walking:org:add")
    @Log(title = "健步走组织机构管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping
    public R<Void> add(@Validated @RequestBody OrgBo bo) {
        orgService.add(bo);
        return R.ok();
    }

    /** 修改机构 */
    @SaCheckPermission("walking:org:edit")
    @Log(title = "健步走组织机构管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public R<Void> edit(@Validated @RequestBody OrgBo bo) {
        orgService.update(bo);
        return R.ok();
    }

    /** 删除机构 */
    @SaCheckPermission("walking:org:remove")
    @Log(title = "健步走组织机构管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{deptIds}")
    public R<Void> remove(@PathVariable Long[] deptIds) {
        orgService.delete(Arrays.asList(deptIds));
        return R.ok();
    }

    /** 下载导入模板 */
    @SaCheckPermission("walking:org:import")
    @Log(title = "健步走组织机构导入", businessType = BusinessType.EXPORT)
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) {
        ExcelUtil.exportExcel(new ArrayList<>(), "组织机构导入", OrgImportBo.class, response);
    }

    /** 批量导入组织机构 */
    @SaCheckPermission("walking:org:import")
    @Log(title = "健步走组织机构导入", businessType = BusinessType.IMPORT)
    @RepeatSubmit()
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<Void> importData(@RequestPart("file") MultipartFile file) throws Exception {
        List<OrgImportBo> list = ExcelUtil.importExcel(file.getInputStream(), OrgImportBo.class);
        orgService.importOrgs(list);
        return R.ok();
    }
}
