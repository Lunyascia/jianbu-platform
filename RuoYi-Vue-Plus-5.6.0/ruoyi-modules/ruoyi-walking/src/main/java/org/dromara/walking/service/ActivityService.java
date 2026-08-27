package org.dromara.walking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.dromara.walking.domain.WalkingActivity;
import org.dromara.walking.domain.vo.ActivityVo;
import org.dromara.walking.mapper.WalkingActivityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 活动服务
 */
@Service
public class ActivityService {

    @Autowired
    private WalkingActivityMapper activityMapper;

    /** 当前进行中的活动：优先取"当前时间落在活动周期内"的活动，取不到再回退最近一条 */
    public ActivityVo getCurrent() {
        WalkingActivity a = selectCurrent();
        return a == null ? null : toVo(a);
    }

    public WalkingActivity getCurrentEntity() {
        return selectCurrent();
    }

    /**
     * 当前活动选择规则：
     * 1. 优先 status=1 且 活动周期 [start_date, end_date] 覆盖今天（start<=今天结束 && end>=今天开始），取开始时间最近一条；
     * 2. 兜底 status=1 中开始时间最近一条（活动未到开始日/刚结束也保证有数据展示）。
     */
    private WalkingActivity selectCurrent() {
        Date todayStart = cn.hutool.core.date.DateUtil.beginOfDay(new Date());
        Date todayEnd = cn.hutool.core.date.DateUtil.endOfDay(new Date());
        WalkingActivity inWindow = activityMapper.selectOne(
            new LambdaQueryWrapper<WalkingActivity>()
                .eq(WalkingActivity::getStatus, 1)
                .le(WalkingActivity::getStartDate, todayEnd)
                .ge(WalkingActivity::getEndDate, todayStart)
                .orderByDesc(WalkingActivity::getStartDate)
                .last("limit 1"));
        if (inWindow != null) {
            return inWindow;
        }
        return activityMapper.selectOne(
            new LambdaQueryWrapper<WalkingActivity>()
                .eq(WalkingActivity::getStatus, 1)
                .orderByDesc(WalkingActivity::getStartDate)
                .last("limit 1"));
    }

    /** 按id取活动实体 */
    public WalkingActivity getEntityById(Long id) {
        return activityMapper.selectById(id);
    }

    private ActivityVo toVo(WalkingActivity a) {
        ActivityVo vo = new ActivityVo();
        vo.setId(a.getId());
        vo.setActivityName(a.getActivityName());
        vo.setCoverUrl(a.getCoverUrl());
        vo.setDescription(a.getDescription());
        vo.setRuleContent(a.getRuleContent());
        vo.setStartDate(a.getStartDate());
        vo.setEndDate(a.getEndDate());
        vo.setDailyTargetSteps(a.getDailyTargetSteps());
        vo.setTargetSteps(a.getTargetSteps());
        vo.setRegisterStart(a.getRegisterStart());
        vo.setRegisterEnd(a.getRegisterEnd());
        vo.setStatus(a.getStatus());
        vo.setRegisterClosed(a.getRegisterEnd() != null && a.getRegisterEnd().before(new Date()));
        return vo;
    }
}
