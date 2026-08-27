package org.dromara.walking.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.walking.domain.vo.admin.AuditLogVo;
import org.dromara.walking.service.AdminAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 报名审核日志（后台）
 */
@Validated
@RestController
@RequestMapping("/walking/admin/audit")
public class AdminAuditController {

    @Autowired
    private AdminAuditService auditService;

    /** 审核日志分页列表 */
    @SaCheckPermission("walking:audit:list")
    @GetMapping("/list")
    public TableDataInfo<AuditLogVo> list(Long registrationId, Long memberId, String auditAction, String keyword, PageQuery pageQuery) {
        return auditService.pageList(registrationId, memberId, auditAction, keyword, pageQuery);
    }
}
