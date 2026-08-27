package org.dromara.walking.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.common.core.domain.R;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.walking.domain.bo.admin.ActivityBo;
import org.dromara.walking.domain.vo.admin.ActivityAdminVo;
import org.dromara.walking.service.AdminActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * 活动管理（后台）
 */
@Validated
@RestController
@RequestMapping("/walking/admin/activity")
public class AdminActivityController {

    @Autowired
    private AdminActivityService activityService;

    /** 活动分页列表 */
    @SaCheckPermission("walking:activity:list")
    @GetMapping("/list")
    public TableDataInfo<ActivityAdminVo> list(String activityName, Integer status, PageQuery pageQuery) {
        return activityService.pageList(activityName, status, pageQuery);
    }

    /** 活动详情 */
    @SaCheckPermission("walking:activity:query")
    @GetMapping("/{id}")
    public R<ActivityAdminVo> getInfo(@PathVariable Long id) {
        return R.ok(activityService.getById(id));
    }

    /** 新增活动 */
    @SaCheckPermission("walking:activity:add")
    @Log(title = "健步走活动管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping
    public R<Void> add(@Validated @RequestBody ActivityBo bo) {
        activityService.insert(bo);
        return R.ok();
    }

    /** 修改活动 */
    @SaCheckPermission("walking:activity:edit")
    @Log(title = "健步走活动管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public R<Void> edit(@Validated @RequestBody ActivityBo bo) {
        activityService.update(bo);
        return R.ok();
    }

    /** 活动参数配置（系统管理员） */
    @SaCheckPermission("walking:activity:config")
    @Log(title = "健步走活动参数配置", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/config")
    public R<Void> config(@Validated @RequestBody ActivityBo bo) {
        activityService.updateConfig(bo);
        return R.ok();
    }

    /** 删除活动 */
    @SaCheckPermission("walking:activity:remove")
    @Log(title = "健步走活动管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        activityService.delete(Arrays.asList(ids));
        return R.ok();
    }

    /** 活动下拉选项（报名/统计页复用） */
    @SaCheckPermission("walking:activity:query")
    @GetMapping("/options")
    public R<List<ActivityAdminVo>> options() {
        return R.ok(activityService.pageList(null, null, new PageQuery(1000, 1)).getRows());
    }
}
