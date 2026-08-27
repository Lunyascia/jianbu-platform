package org.dromara.walking.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import lombok.Data;
import org.dromara.common.core.domain.R;
import org.dromara.walking.domain.vo.StepRecordVo;
import org.dromara.walking.domain.vo.TodayStepVo;

import java.util.List;
import org.dromara.walking.service.MemberAuthService;
import org.dromara.walking.service.StepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 步数接口（小程序端，需登录）
 */
@RestController
@SaIgnore
@RequestMapping("/walking/step")
public class StepController {

    @Autowired
    private StepService stepService;
    @Autowired
    private MemberAuthService authService;

    /** 同步今日步数（打卡） */
    @PostMapping("/sync")
    public R<TodayStepVo> sync(@RequestBody SyncReq req) {
        Long memberId = authService.getMemberIdByToken();
        return R.ok(stepService.sync(memberId, req.getActivityId(), req.getEncryptedData(), req.getIv(), req.getSteps()));
    }

    /** 今日步数统计 */
    @GetMapping("/today")
    public R<TodayStepVo> today(Long activityId) {
        Long memberId = authService.getMemberIdByToken();
        return R.ok(stepService.getToday(memberId, activityId));
    }

    /** 打卡日历记录（当前活动内每天打卡情况） */
    @GetMapping("/records")
    public R<List<StepRecordVo>> records() {
        Long memberId = authService.getMemberIdByToken();
        return R.ok(stepService.getRecords(memberId, null));
    }

    @Data
    public static class SyncReq {
        private Long activityId;
        /** 微信运动加密数据（真实场景） */
        private String encryptedData;
        private String iv;
        /** 模拟步数（encryptedData 为空时使用，便于测试） */
        private Integer steps;
    }
}
