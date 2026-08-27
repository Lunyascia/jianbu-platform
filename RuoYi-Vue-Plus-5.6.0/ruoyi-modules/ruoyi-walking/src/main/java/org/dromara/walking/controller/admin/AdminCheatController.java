package org.dromara.walking.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.common.core.domain.R;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.walking.domain.bo.admin.CheatBatchBo;
import org.dromara.walking.domain.vo.admin.CheatLogVo;
import org.dromara.walking.domain.vo.admin.CheatRecordVo;
import org.dromara.walking.service.AdminCheatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 异常数据处理（后台）
 */
@Validated
@RestController
@RequestMapping("/walking/admin/cheat")
public class AdminCheatController {

    @Autowired
    private AdminCheatService cheatService;

    /** 异常步数数据分页列表 */
    @SaCheckPermission("walking:cheat:list")
    @GetMapping("/list")
    public TableDataInfo<CheatRecordVo> list(Long activityId, Integer abnormalFlag, String keyword, PageQuery pageQuery) {
        return cheatService.pageList(activityId, abnormalFlag, keyword, pageQuery);
    }

    /** 标记异常 */
    @SaCheckPermission("walking:cheat:mark")
    @Log(title = "健步走异常数据处理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/mark")
    public R<Void> mark(@RequestBody CheatBatchBo bo) {
        cheatService.mark(bo);
        return R.ok();
    }

    /** 删除异常数据 */
    @SaCheckPermission("walking:cheat:delete")
    @Log(title = "健步走异常数据处理", businessType = BusinessType.DELETE)
    @RepeatSubmit()
    @PostMapping("/delete")
    public R<Void> delete(@RequestBody CheatBatchBo bo) {
        cheatService.delete(bo);
        return R.ok();
    }

    /** 恢复异常（取消标记，误标数据放回正常统计/排行） */
    @SaCheckPermission("walking:cheat:mark")
    @Log(title = "健步走异常数据处理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/unmark")
    public R<Void> unmark(@RequestBody CheatBatchBo bo) {
        cheatService.unmark(bo);
        return R.ok();
    }

    /** 批量处理作弊账号（系统管理员） */
    @SaCheckPermission("walking:cheat:batch")
    @Log(title = "健步走批量处理作弊", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/batch")
    public R<Void> batch(@RequestBody CheatBatchBo bo) {
        cheatService.batchHandle(bo);
        return R.ok();
    }

    /** 作弊处理日志 */
    @SaCheckPermission("walking:cheat:list")
    @GetMapping("/logs")
    public TableDataInfo<CheatLogVo> logs(Long activityId, String keyword, PageQuery pageQuery) {
        return cheatService.logPage(activityId, keyword, pageQuery);
    }
}
