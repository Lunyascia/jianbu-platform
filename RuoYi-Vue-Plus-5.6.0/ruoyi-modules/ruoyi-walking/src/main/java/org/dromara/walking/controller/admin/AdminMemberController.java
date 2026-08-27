package org.dromara.walking.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
import org.dromara.common.core.domain.R;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.walking.domain.bo.admin.MemberBo;
import org.dromara.walking.domain.vo.admin.MemberAdminVo;
import org.dromara.walking.service.AdminMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * 会员管理（后台）
 */
@Validated
@RestController
@RequestMapping("/walking/admin/member")
public class AdminMemberController {

    @Autowired
    private AdminMemberService memberService;

    /** 会员分页列表 */
    @SaCheckPermission("walking:member:list")
    @GetMapping("/list")
    public TableDataInfo<MemberAdminVo> list(Long deptId, Integer status, String keyword, PageQuery pageQuery) {
        return memberService.pageList(deptId, status, keyword, pageQuery);
    }

    /** 会员详情 */
    @SaCheckPermission("walking:member:query")
    @GetMapping("/{id}")
    public R<MemberAdminVo> getInfo(@PathVariable Long id) {
        return R.ok(memberService.getById(id));
    }

    /** 会员信息维护（调整单位/收货地址） */
    @SaCheckPermission("walking:member:edit")
    @Log(title = "健步走会员管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public R<Void> edit(@Validated @RequestBody MemberBo bo) {
        memberService.update(bo);
        return R.ok();
    }

    /** 删除会员（系统管理员，无报名/打卡数据方可删除） */
    @SaCheckPermission("walking:member:remove")
    @Log(title = "健步走会员管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        List<Long> list = Arrays.asList(ids);
        memberService.remove(list);
        return R.ok();
    }

    /** 停用/启用账号（审核类操作） */
    @SaCheckPermission("walking:member:disable")
    @Log(title = "健步走会员管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/{id}/status")
    public R<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        memberService.changeStatus(id, status);
        return R.ok();
    }

    /** 分单位导出打卡信息（系统管理员） */
    @SaCheckPermission("walking:member:export")
    @Log(title = "健步走打卡导出", businessType = BusinessType.EXPORT)
    @PostMapping("/export/steps")
    public void exportSteps(Long activityId, Long deptId, HttpServletResponse response) {
        memberService.exportSteps(activityId, deptId, response);
    }
}
