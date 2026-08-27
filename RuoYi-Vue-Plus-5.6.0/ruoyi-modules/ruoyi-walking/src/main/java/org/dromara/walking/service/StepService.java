package org.dromara.walking.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.walking.domain.WalkingActivity;
import org.dromara.walking.domain.WalkingMember;
import org.dromara.walking.domain.WalkingRegistration;
import org.dromara.walking.domain.WalkingStepRecord;
import org.dromara.walking.domain.vo.StepRecordVo;
import org.dromara.walking.domain.vo.TodayStepVo;
import org.dromara.walking.mapper.WalkingMemberMapper;
import org.dromara.walking.mapper.WalkingRegistrationMapper;
import org.dromara.walking.mapper.WalkingStepRecordMapper;
import org.dromara.walking.wx.WxProperties;
import org.dromara.walking.wx.WxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * 步数打卡服务
 * 达标线/单日上限/阶段奖励等参数均取活动配置，未配置时用默认值
 */
@Service
public class StepService {

    /** 默认每日达标步数 */
    public static final int DEFAULT_DAILY_TARGET = 7000;
    /** 默认单日步数上限 */
    public static final int DEFAULT_DAILY_LIMIT = 15000;

    @Autowired
    private WalkingStepRecordMapper stepMapper;
    @Autowired
    private WalkingMemberMapper memberMapper;
    @Autowired
    private WalkingRegistrationMapper registrationMapper;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private WxService wxService;
    @Autowired
    private MemberAuthService authService;

    /**
     * 同步今日步数（打卡）
     * 活动期外（缓冲期内允许打开同步历史，但不记录缓冲日步数；缓冲期结束锁榜后拒绝）
     */
    @Transactional(rollbackFor = Exception.class)
    public TodayStepVo sync(Long memberId, Long activityId, String encryptedData, String iv, Integer steps) {
        WalkingMember member = memberMapperById(memberId);
        WalkingActivity activity = activityService.getCurrentEntity();
        if (activity == null) {
            throw new ServiceException("当前无进行中的活动");
        }

        // 必须已报名通过
        WalkingRegistration reg = registrationOf(memberId, activity.getId());
        if (reg == null || reg.getStatus() != 2) {
            throw new ServiceException("请先报名并审核通过后再打卡");
        }

        // 确定今日步数
        int todaySteps;
        if (StrUtil.isNotBlank(encryptedData)) {
            String sessionKey = authService.getSessionKey(memberId);
            if (StrUtil.isBlank(sessionKey)) {
                throw new ServiceException("会话已过期，请重新登录");
            }
            todaySteps = wxService.decryptWeRunData(encryptedData, sessionKey, iv);
        } else {
            todaySteps = steps == null ? 0 : steps;
        }

        // 封顶：活动配置的单日上限
        todaySteps = Math.min(todaySteps, dailyLimit(activity));

        LocalDate today = LocalDate.now();
        LocalDate endDate = toLocalDate(activity.getEndDate());
        int bufferDays = bufferDays(activity);

        // 活动已结束并超过缓冲期 → 锁榜后不再接收
        if (endDate != null && today.isAfter(endDate.plusDays(bufferDays))) {
            throw new ServiceException("活动已结束，排行榜已锁定");
        }
        // 活动期外/缓冲期内：允许打开同步历史，但不记录今日(缓冲日)步数，不参与积分排名
        if (today.isBefore(toLocalDate(activity.getStartDate())) || (endDate != null && today.isAfter(endDate))) {
            return buildTodayVo(memberId, activity.getId());
        }
        // 报名前不追认
        if (reg.getSubmitTime() != null && today.isBefore(toLocalDate(reg.getSubmitTime()))) {
            throw new ServiceException("报名前步数不追认");
        }

        // upsert 今日记录
        WalkingStepRecord record = stepMapper.selectOne(
            new LambdaQueryWrapper<WalkingStepRecord>()
                .eq(WalkingStepRecord::getMemberId, memberId)
                .eq(WalkingStepRecord::getActivityId, activity.getId())
                .eq(WalkingStepRecord::getRecordDate, Date.valueOf(today)));
        if (record == null) {
            record = new WalkingStepRecord();
            record.setMemberId(memberId);
            record.setActivityId(activity.getId());
            record.setRecordDate(Date.valueOf(today));
            record.setSteps(todaySteps);
            record.setSource(1);
            record.setAbnormalFlag(0);
            record.setLocked(0);
            stepMapper.insert(record);
        } else {
            record.setSteps(todaySteps);
            stepMapper.updateById(record);
        }

        return buildTodayVo(memberId, activity.getId());
    }

    /**
     * 今日步数统计（不打卡，仅查询）
     */
    public TodayStepVo getToday(Long memberId, Long activityId) {
        WalkingActivity activity = activityService.getCurrentEntity();
        if (activity == null) {
            return emptyTodayVo();
        }
        return buildTodayVo(memberId, activity.getId());
    }

    // ============ 内部计算 ============

    /** 计算某会员在某活动的总积分（排行/展示用），取活动配置 */
    public Integer computeTotalPoints(Long memberId, Long activityId) {
        WalkingActivity activity = activityMapperById(activityId);
        List<WalkingStepRecord> records = stepMapper.selectList(
            new LambdaQueryWrapper<WalkingStepRecord>()
                .eq(WalkingStepRecord::getMemberId, memberId)
                .eq(WalkingStepRecord::getActivityId, activityId)
                .eq(WalkingStepRecord::getAbnormalFlag, 0)
                .orderByAsc(WalkingStepRecord::getRecordDate));
        return computePoints(records, activity);
    }

    private TodayStepVo buildTodayVo(Long memberId, Long activityId) {
        WalkingActivity activity = activityMapperById(activityId);
        int dailyTarget = dailyTarget(activity);
        List<WalkingStepRecord> records = stepMapper.selectList(
            new LambdaQueryWrapper<WalkingStepRecord>()
                .eq(WalkingStepRecord::getMemberId, memberId)
                .eq(WalkingStepRecord::getActivityId, activityId)
                .eq(WalkingStepRecord::getAbnormalFlag, 0)
                .orderByAsc(WalkingStepRecord::getRecordDate));

        LocalDate today = LocalDate.now();
        TodayStepVo vo = new TodayStepVo();

        // 今日记录
        WalkingStepRecord todayRecord = records.stream()
            .filter(r -> r.getRecordDate().toLocalDate().equals(today))
            .findFirst().orElse(null);
        vo.setSyncedToday(todayRecord != null);
        vo.setTodaySteps(todayRecord == null ? 0 : todayRecord.getSteps());
        vo.setReached(todayRecord != null && todayRecord.getSteps() >= dailyTarget);

        // 达标天数（累计打卡天数）
        long validDays = records.stream()
            .filter(r -> r.getSteps() >= dailyTarget)
            .count();
        vo.setCheckInDays((int) validDays);

        // 当前连续打卡天数（从今天或昨天往前推）
        vo.setStreakDays(computeStreak(records, today, dailyTarget));

        // 总积分 = 基础分 + 阶段奖励
        vo.setTotalPoints(computePoints(records, activity));

        // 活动总步数
        vo.setTotalSteps(records.stream().mapToInt(WalkingStepRecord::getSteps).sum());
        return vo;
    }

    /**
     * 打卡日历记录（当前活动内该会员每天的打卡情况）
     */
    public List<StepRecordVo> getRecords(Long memberId, Long activityId) {
        WalkingActivity activity = activityService.getCurrentEntity();
        if (activity == null) {
            return List.of();
        }
        int dailyTarget = dailyTarget(activity);
        List<WalkingStepRecord> records = stepMapper.selectList(
            new LambdaQueryWrapper<WalkingStepRecord>()
                .eq(WalkingStepRecord::getMemberId, memberId)
                .eq(WalkingStepRecord::getActivityId, activity.getId())
                .orderByAsc(WalkingStepRecord::getRecordDate));
        return records.stream().map(r -> {
            StepRecordVo vo = new StepRecordVo();
            vo.setDate(r.getRecordDate().toLocalDate().toString());
            vo.setSteps(r.getSteps());
            vo.setReached(r.getSteps() >= dailyTarget);
            vo.setHasRecord(true);
            return vo;
        }).toList();
    }

    /** 计算当前连续达标天数 */
    private int computeStreak(List<WalkingStepRecord> records, LocalDate today, int dailyTarget) {
        int streak = 0;
        LocalDate day = today;
        // 今天未达标则从昨天开始算连续
        WalkingStepRecord todayRec = records.stream().filter(r -> r.getRecordDate().toLocalDate().equals(today)).findFirst().orElse(null);
        if (todayRec == null || todayRec.getSteps() < dailyTarget) {
            day = today.minusDays(1);
        }
        while (true) {
            LocalDate d = day;
            WalkingStepRecord rec = records.stream().filter(r -> r.getRecordDate().toLocalDate().equals(d)).findFirst().orElse(null);
            if (rec != null && rec.getSteps() >= dailyTarget) {
                streak++;
                day = day.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
    }

    /**
     * 计算总积分 = 每日基础分(达标+1) + 阶段奖励(连续7天/14天/全勤)，取活动配置
     */
    private int computePoints(List<WalkingStepRecord> records, WalkingActivity activity) {
        int dailyTarget = dailyTarget(activity);
        int s7 = activity.getStreak7Points() == null ? 2 : activity.getStreak7Points();
        int s14 = activity.getStreak14Points() == null ? 5 : activity.getStreak14Points();
        int full = activity.getFullAttendancePoints() == null ? 10 : activity.getFullAttendancePoints();

        int base = 0;
        int streakReward = 0;
        int sevenCount = 0;
        int fourteenCount = 0;
        int streak = 0;
        boolean fullAttendance = true;
        int totalDays = records.size();

        for (WalkingStepRecord r : records) {
            boolean valid = r.getSteps() >= dailyTarget;
            if (valid) {
                base++;
                streak++;
                if (streak == 7 && sevenCount < 3) {
                    streakReward += s7;
                    sevenCount++;
                }
                if (streak == 14 && fourteenCount < 2) {
                    streakReward += s14;
                    fourteenCount++;
                }
            } else {
                streak = 0;
                fullAttendance = false;
            }
        }
        // 21 天全勤（记录内无中断且满 21 天）
        if (fullAttendance && totalDays >= 21) {
            streakReward += full;
        }
        return base + streakReward;
    }

    private TodayStepVo emptyTodayVo() {
        TodayStepVo vo = new TodayStepVo();
        vo.setSyncedToday(false);
        vo.setTodaySteps(0);
        vo.setReached(false);
        vo.setCheckInDays(0);
        vo.setStreakDays(0);
        vo.setTotalPoints(0);
        return vo;
    }

    // ============ 活动参数解析 ============

    /** 每日达标线（活动配置，默认7000） */
    public static int dailyTarget(WalkingActivity a) {
        return a != null && a.getDailyTargetSteps() != null && a.getDailyTargetSteps() > 0
            ? a.getDailyTargetSteps() : DEFAULT_DAILY_TARGET;
    }

    /** 单日步数上限（活动配置，默认15000） */
    public static int dailyLimit(WalkingActivity a) {
        return a != null && a.getDailyStepLimit() != null && a.getDailyStepLimit() > 0
            ? a.getDailyStepLimit() : DEFAULT_DAILY_LIMIT;
    }

    /** 缓冲期天数（活动配置，默认1） */
    public static int bufferDays(WalkingActivity a) {
        return a != null && a.getBufferDays() != null && a.getBufferDays() > 0
            ? a.getBufferDays() : 1;
    }

    private WalkingActivity activityMapperById(Long activityId) {
        WalkingActivity a = activityService.getEntityById(activityId);
        if (a == null) {
            throw new ServiceException("活动不存在");
        }
        return a;
    }

    private WalkingMember memberMapperById(Long id) {
        WalkingMember m = memberMapper.selectById(id);
        if (m == null) {
            throw new ServiceException("会员不存在");
        }
        return m;
    }

    private WalkingRegistration registrationOf(Long memberId, Long activityId) {
        return registrationMapper.selectOne(
            new LambdaQueryWrapper<WalkingRegistration>()
                .eq(WalkingRegistration::getMemberId, memberId)
                .eq(WalkingRegistration::getActivityId, activityId));
    }

    private LocalDate toLocalDate(java.util.Date date) {
        return date == null ? null : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
