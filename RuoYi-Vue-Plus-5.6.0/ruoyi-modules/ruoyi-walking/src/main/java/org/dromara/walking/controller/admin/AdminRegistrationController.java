package org.dromara.walking.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
import org.dromara.common.core.domain.R;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.walking.domain.vo.admin.RegistrationAdminVo;
import org.dromara.walking.service.AdminRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 报名管理（后台）
 */
@Validated
@RestController
@RequestMapping("/walking/admin/registration")
public class AdminRegistrationController {

    @Autowired
    private AdminRegistrationService registrationService;

    /** 报名分页列表 */
    @SaCheckPermission("walking:registration:list")
    @GetMapping("/list")
    public TableDataInfo<RegistrationAdminVo> list(Long activityId, Integer status, Long deptId, String keyword, PageQuery pageQuery) {
        return registrationService.pageList(activityId, status, deptId, keyword, pageQuery);
    }

    /** 报名详情 */
    @SaCheckPermission("walking:registration:query")
    @GetMapping("/{id}")
    public R<RegistrationAdminVo> getInfo(@PathVariable Long id) {
        return R.ok(registrationService.getById(id));
    }

    /** 取消报名 */
    @SaCheckPermission("walking:registration:cancel")
    @Log(title = "健步走报名管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/{id}/cancel")
    public R<Void> cancel(@PathVariable Long id) {
        registrationService.cancel(id);
        return R.ok();
    }

    /** 撤下报名信息 + 停用会员账号（审核类操作） */
    @SaCheckPermission("walking:registration:disable")
    @Log(title = "健步走报名管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/{id}/disable")
    public R<Void> disable(@PathVariable Long id) {
        registrationService.disable(id);
        return R.ok();
    }

    /** 审核通过（待审核 → 报名成功，通过后才可绑定手机号） */
    @SaCheckPermission("walking:registration:approve")
    @Log(title = "健步走报名管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/{id}/approve")
    public R<Void> approve(@PathVariable Long id) {
        registrationService.approve(id);
        return R.ok();
    }

    /** 调整单位 */
    @SaCheckPermission("walking:registration:adjust")
    @Log(title = "健步走报名管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/{id}/adjust")
    public R<Void> adjust(@PathVariable Long id, @RequestParam Long deptId, @RequestParam String deptName) {
        registrationService.adjustUnit(id, deptId, deptName);
        return R.ok();
    }

    /** 分单位导出报名人员信息（系统管理员） */
    @SaCheckPermission("walking:registration:export")
    @Log(title = "健步走报名导出", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(Long activityId, Long deptId, HttpServletResponse response) {
        registrationService.export(activityId, deptId, response);
    }
}
