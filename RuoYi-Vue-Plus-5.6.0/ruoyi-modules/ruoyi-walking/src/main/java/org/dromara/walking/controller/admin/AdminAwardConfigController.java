package org.dromara.walking.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.common.core.domain.R;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.walking.domain.bo.admin.AwardConfigBo;
import org.dromara.walking.domain.vo.AwardTierVo;
import org.dromara.walking.service.AdminAwardConfigService;
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
 * 奖励档位管理（后台）—— 修改后在 H5 报名页 / 小程序奖励页即时生效
 */
@Validated
@RestController
@RequestMapping("/walking/admin/award-config")
public class AdminAwardConfigController {

    @Autowired
    private AdminAwardConfigService awardConfigService;

    /** 奖励档位分页 */
    @SaCheckPermission("walking:awardConfig:list")
    @GetMapping("/list")
    public TableDataInfo<AwardTierVo> list(Long activityId, String awardName, PageQuery pageQuery) {
        return awardConfigService.pageList(activityId, awardName, pageQuery);
    }

    /** 奖励档位详情 */
    @SaCheckPermission("walking:awardConfig:query")
    @GetMapping("/{id}")
    public R<AwardTierVo> getInfo(@PathVariable Long id) {
        return R.ok(awardConfigService.getById(id));
    }

    /** 新增奖励档位 */
    @SaCheckPermission("walking:awardConfig:add")
    @Log(title = "健步走奖励管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping
    public R<Void> add(@Validated @RequestBody AwardConfigBo bo) {
        awardConfigService.insert(bo);
        return R.ok();
    }

    /** 修改奖励档位 */
    @SaCheckPermission("walking:awardConfig:edit")
    @Log(title = "健步走奖励管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public R<Void> edit(@Validated @RequestBody AwardConfigBo bo) {
        awardConfigService.update(bo);
        return R.ok();
    }

    /** 删除奖励档位 */
    @SaCheckPermission("walking:awardConfig:remove")
    @Log(title = "健步走奖励管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        awardConfigService.delete(Arrays.asList(ids));
        return R.ok();
    }
}
