package org.dromara.walking.service;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.walking.domain.WalkingActivity;
import org.dromara.walking.domain.WalkingRegistration;
import org.dromara.walking.domain.bo.admin.ActivityBo;
import org.dromara.walking.domain.vo.admin.ActivityAdminVo;
import org.dromara.walking.mapper.WalkingActivityMapper;
import org.dromara.walking.mapper.WalkingRegistrationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 活动管理（后台）服务
 */
@Service
public class AdminActivityService {

    @Autowired
    private WalkingActivityMapper activityMapper;
    @Autowired
    private WalkingRegistrationMapper registrationMapper;

    /** 活动分页列表（含报名人数） */
    public TableDataInfo<ActivityAdminVo> pageList(String activityName, Integer status, PageQuery pageQuery) {
        LambdaQueryWrapper<WalkingActivity> lqw = new LambdaQueryWrapper<>();
        lqw.like(StrUtil.isNotBlank(activityName), WalkingActivity::getActivityName, activityName);
        lqw.eq(status != null, WalkingActivity::getStatus, status);
        lqw.orderByDesc(WalkingActivity::getCreateTime);
        Page<WalkingActivity> page = activityMapper.selectPage(pageQuery.build(), lqw);
        Map<Long, Long> regCountMap = selectRegCountMap();
        List<ActivityAdminVo> rows = page.getRecords().stream()
            .map(a -> toVo(a, regCountMap.getOrDefault(a.getId(), 0L)))
            .toList();
        return new TableDataInfo<>(rows, page.getTotal());
    }

    /** 活动详情 */
    public ActivityAdminVo getById(Long id) {
        WalkingActivity a = activityMapper.selectById(id);
        if (a == null) {
            throw new ServiceException("活动不存在");
        }
        return toVo(a, 0L);
    }

    /** 新增活动 */
    @Transactional(rollbackFor = Exception.class)
    public void insert(ActivityBo bo) {
        WalkingActivity a = new WalkingActivity();
        applyBo(a, bo);
        if (a.getStatus() == null) {
            a.setStatus(0);
        }
        ensureSingleInProgress(a.getStatus(), null);
        activityMapper.insert(a);
    }

    /** 修改活动 */
    @Transactional(rollbackFor = Exception.class)
    public void update(ActivityBo bo) {
        WalkingActivity a = activityMapper.selectById(bo.getId());
        if (a == null) {
            throw new ServiceException("活动不存在");
        }
        applyBo(a, bo);
        ensureSingleInProgress(a.getStatus(), bo.getId());
        activityMapper.updateById(a);
    }

    /** 活动参数配置（系统管理员）—— 仅更新时间窗口/积分规则等参数 */
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(ActivityBo bo) {
        WalkingActivity a = activityMapper.selectById(bo.getId());
        if (a == null) {
            throw new ServiceException("活动不存在");
        }
        if (bo.getStartDate() != null) {
            a.setStartDate(bo.getStartDate());
        }
        if (bo.getEndDate() != null) {
            a.setEndDate(bo.getEndDate());
        }
        if (bo.getRegisterStart() != null) {
            a.setRegisterStart(bo.getRegisterStart());
        }
        if (bo.getRegisterEnd() != null) {
            a.setRegisterEnd(bo.getRegisterEnd());
        }
        if (bo.getTargetSteps() != null) {
            a.setTargetSteps(bo.getTargetSteps());
        }
        if (bo.getDailyTargetSteps() != null) {
            a.setDailyTargetSteps(bo.getDailyTargetSteps());
        }
        if (bo.getPointsPerThousandSteps() != null) {
            a.setPointsPerThousandSteps(bo.getPointsPerThousandSteps());
        }
        if (bo.getDailyStepLimit() != null) {
            a.setDailyStepLimit(bo.getDailyStepLimit());
        }
        if (bo.getStreak7Points() != null) {
            a.setStreak7Points(bo.getStreak7Points());
        }
        if (bo.getStreak14Points() != null) {
            a.setStreak14Points(bo.getStreak14Points());
        }
        if (bo.getFullAttendancePoints() != null) {
            a.setFullAttendancePoints(bo.getFullAttendancePoints());
        }
        if (bo.getBufferDays() != null) {
            a.setBufferDays(bo.getBufferDays());
        }
        if (bo.getStatus() != null) {
            a.setStatus(bo.getStatus());
        }
        ensureSingleInProgress(a.getStatus(), bo.getId());
        activityMapper.updateById(a);
    }

    /** 删除活动 */
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        // 存在已生效报名的活动不允许直接删除
        for (Long id : ids) {
            long cnt = registrationMapper.selectCount(new LambdaQueryWrapper<WalkingRegistration>()
                .eq(WalkingRegistration::getActivityId, id)
                .in(WalkingRegistration::getStatus, 1, 2));
            if (cnt > 0) {
                WalkingActivity a = activityMapper.selectById(id);
                throw new ServiceException("活动【" + (a == null ? id : a.getActivityName()) + "】已有报名数据，不能删除，可改为停用");
            }
        }
        activityMapper.deleteByIds(ids);
    }

    /**
     * 校验同时只能有一个进行中的活动：设置为进行中(status=1)时，若已有其他活动进行中则拒绝
     */
    private void ensureSingleInProgress(Integer newStatus, Long excludeActivityId) {
        if (newStatus != null && newStatus == 1) {
            Long cnt = activityMapper.selectCount(new LambdaQueryWrapper<WalkingActivity>()
                .eq(WalkingActivity::getStatus, 1)
                .ne(excludeActivityId != null, WalkingActivity::getId, excludeActivityId));
            if (cnt != null && cnt > 0) {
                throw new ServiceException("不可同时进行两个活动，请先结束当前进行中的活动");
            }
        }
    }

    private Map<Long, Long> selectRegCountMap() {
        Map<Long, Long> map = new HashMap<>();
        for (Map<String, Object> row : registrationMapper.selectApprovedCountGroupByActivity()) {
            map.put(Convert.toLong(row.get("activityId")), Convert.toLong(row.get("cnt")));
        }
        return map;
    }

    private void applyBo(WalkingActivity a, ActivityBo bo) {
        a.setActivityName(bo.getActivityName());
        a.setCoverUrl(bo.getCoverUrl());
        a.setDescription(bo.getDescription());
        a.setRuleContent(bo.getRuleContent());
        a.setStartDate(bo.getStartDate());
        a.setEndDate(bo.getEndDate());
        a.setTargetSteps(bo.getTargetSteps());
        a.setDailyTargetSteps(bo.getDailyTargetSteps());
        a.setPointsPerThousandSteps(bo.getPointsPerThousandSteps());
        a.setDailyStepLimit(bo.getDailyStepLimit());
        a.setStreak7Points(bo.getStreak7Points());
        a.setStreak14Points(bo.getStreak14Points());
        a.setFullAttendancePoints(bo.getFullAttendancePoints());
        a.setBufferDays(bo.getBufferDays());
        a.setRegisterStart(bo.getRegisterStart());
        a.setRegisterEnd(bo.getRegisterEnd());
        a.setStatus(bo.getStatus());
        a.setOrgId(bo.getOrgId());
    }

    private ActivityAdminVo toVo(WalkingActivity a, Long regCount) {
        ActivityAdminVo vo = new ActivityAdminVo();
        vo.setId(a.getId());
        vo.setActivityName(a.getActivityName());
        vo.setCoverUrl(a.getCoverUrl());
        vo.setDescription(a.getDescription());
        vo.setRuleContent(a.getRuleContent());
        vo.setStartDate(a.getStartDate());
        vo.setEndDate(a.getEndDate());
        vo.setTargetSteps(a.getTargetSteps());
        vo.setDailyTargetSteps(a.getDailyTargetSteps());
        vo.setPointsPerThousandSteps(a.getPointsPerThousandSteps());
        vo.setDailyStepLimit(a.getDailyStepLimit());
        vo.setStreak7Points(a.getStreak7Points());
        vo.setStreak14Points(a.getStreak14Points());
        vo.setFullAttendancePoints(a.getFullAttendancePoints());
        vo.setBufferDays(a.getBufferDays());
        vo.setRegisterStart(a.getRegisterStart());
        vo.setRegisterEnd(a.getRegisterEnd());
        vo.setStatus(a.getStatus());
        vo.setOrgId(a.getOrgId());
        vo.setStatusText(statusText(a.getStatus()));
        vo.setRegCount(regCount);
        return vo;
    }

    private String statusText(Integer status) {
        return switch (status == null ? -1 : status) {
            case 0 -> "草稿";
            case 1 -> "进行中";
            case 2 -> "已结束";
            default -> "";
        };
    }
}
