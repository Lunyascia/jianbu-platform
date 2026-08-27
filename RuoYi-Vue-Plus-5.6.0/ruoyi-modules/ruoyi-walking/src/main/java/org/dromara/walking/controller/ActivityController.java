package org.dromara.walking.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import org.dromara.common.core.domain.R;
import org.dromara.walking.domain.vo.ActivityVo;
import org.dromara.walking.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 活动接口（公开）
 */
@RestController
@SaIgnore
@RequestMapping("/walking/activity")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    /** 当前进行中的活动 */
    @GetMapping("/current")
    public R<ActivityVo> current() {
        return R.ok(activityService.getCurrent());
    }
}
