package org.dromara.walking.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
import org.dromara.common.core.domain.R;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.walking.domain.bo.admin.AwardBo;
import org.dromara.walking.domain.vo.admin.AwardWinnerVo;
import org.dromara.walking.service.AdminAwardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * 中奖名单管理（后台）
 */
@Validated
@RestController
@RequestMapping("/walking/admin/award")
public class AdminAwardController {

    @Autowired
    private AdminAwardService awardService;

    /** 中奖名单分页 */
    @SaCheckPermission("walking:award:list")
    @GetMapping("/list")
    public TableDataInfo<AwardWinnerVo> list(Long activityId, String keyword, PageQuery pageQuery) {
        return awardService.pageList(activityId, keyword, pageQuery);
    }

    /** 标记/调整中奖名单 */
    @SaCheckPermission("walking:award:mark")
    @Log(title = "健步走中奖名单", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/mark")
    public R<Void> mark(@Validated @RequestBody AwardBo bo) {
        awardService.mark(bo);
        return R.ok();
    }

    /** 按排行自动生成中奖名单 */
    @SaCheckPermission("walking:award:mark")
    @Log(title = "健步走中奖名单", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/autoMark")
    public R<Void> autoMark(Long activityId) {
        awardService.autoMark(activityId);
        return R.ok();
    }

    /** 删除名单 */
    @SaCheckPermission("walking:award:remove")
    @Log(title = "健步走中奖名单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        awardService.remove(Arrays.asList(ids));
        return R.ok();
    }

    /** 导出中奖用户信息（姓名/手机号/收货地址） */
    @SaCheckPermission("walking:award:export")
    @Log(title = "健步走中奖名单导出", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(Long activityId, HttpServletResponse response) {
        awardService.export(activityId, response);
    }
}
