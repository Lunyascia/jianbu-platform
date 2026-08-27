package org.dromara.walking.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.walking.domain.WalkingActivity;
import org.dromara.walking.domain.WalkingAward;
import org.dromara.walking.domain.WalkingMember;
import org.dromara.walking.domain.WalkingRegistration;
import org.dromara.walking.domain.WalkingStepRecord;
import org.dromara.walking.domain.vo.admin.AwardExportVo;
import org.dromara.walking.domain.vo.admin.CheckinDetailVo;
import org.dromara.walking.domain.vo.admin.DeptStatVo;
import org.dromara.walking.domain.vo.admin.PointsDayVo;
import org.dromara.walking.domain.vo.admin.PointsDetailVo;
import org.dromara.walking.domain.vo.admin.RankingStatVo;
import org.dromara.walking.domain.vo.admin.StatsOverviewVo;
import org.dromara.walking.domain.vo.admin.UnitEvalVo;
import org.dromara.walking.mapper.WalkingActivityMapper;
import org.dromara.walking.mapper.WalkingAwardMapper;
import org.dromara.walking.mapper.WalkingMemberMapper;
import org.dromara.walking.mapper.WalkingOrgMapper;
import org.dromara.walking.mapper.WalkingPointsMapper;
import org.dromara.walking.mapper.WalkingRegistrationMapper;
import org.dromara.walking.mapper.WalkingStepRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 数据统计（后台）服务
 * 含：总览、单位统计、先进组织单位评选、排行、个人打卡/积分明细、获奖名单
 */
@Service
public class AdminStatsService {

    public static final int DEFAULT_DAILY_TARGET = 7000;

    @Autowired
    private WalkingActivityMapper activityMapper;
    @Autowired
    private WalkingRegistrationMapper registrationMapper;
    @Autowired
    private WalkingMemberMapper memberMapper;
    @Autowired
    private WalkingStepRecordMapper stepRecordMapper;
    @Autowired
    private WalkingPointsMapper pointsMapper;
    @Autowired
    private WalkingOrgMapper orgMapper;
    @Autowired
    private WalkingAwardMapper awardMapper;

    /** 总览统计 */
    public StatsOverviewVo overview(Long activityId) {
        StatsOverviewVo vo = new StatsOverviewVo();
        vo.setTotalActivities(activityMapper.selectCount(new LambdaQueryWrapper<WalkingActivity>()));
        vo.setActiveActivities(activityMapper.selectCount(new LambdaQueryWrapper<WalkingActivity>()
            .eq(WalkingActivity::getStatus, 1)));
        vo.setTotalMembers(memberMapper.selectMemberCount());

        vo.setTotalRegistrations(registrationMapper.selectCount(regWrapper(activityId, null).in(WalkingRegistration::getStatus, 0, 1, 2)));
        vo.setApprovedCount(registrationMapper.selectCount(regWrapper(activityId, 2)));
        vo.setCancelledCount(registrationMapper.selectCount(regWrapper(activityId, 3)));
        vo.setDisabledCount(registrationMapper.selectCount(regWrapper(activityId, 4)));

        // 参与率 = 通过人数 / 组织会员总数(线下统计手动维护)
        long approved = vo.getApprovedCount() == null ? 0 : vo.getApprovedCount();
        Long orgTotal = orgMapper.sumMemberTotal();
        long memberTotal = orgTotal == null ? 0 : orgTotal;
        vo.setOrgMemberTotal(memberTotal);
        if (memberTotal > 0) {
            vo.setParticipationRate(new BigDecimal(approved).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(memberTotal), 1, RoundingMode.HALF_UP).toPlainString() + "%");
        } else {
            vo.setParticipationRate("0%");
        }

        vo.setDeptCount(memberMapper.selectDeptCount());
        vo.setTotalSteps(stepRecordMapper.sumSteps(activityId));
        vo.setAbnormalCount(stepRecordMapper.countAbnormal(activityId));
        return vo;
    }

    /** 单位统计 */
    public List<DeptStatVo> deptStats(Long activityId) {
        Map<Long, DeptStatVo> map = new LinkedHashMap<>();
        WalkingActivity act = activityMapper.selectById(activityId);
        int dailyTarget = StepService.dailyTarget(act);

        // 报名人数(status 1/2) 与 参与人数(status 2)
        for (Map<String, Object> row : registrationMapper.selectCountGroupByDept(activityId)) {
            Long deptId = Convert.toLong(row.get("deptId"));
            DeptStatVo vo = map.computeIfAbsent(deptId, k -> newVo(row.get("deptName")));
            vo.setDeptId(deptId);
            vo.setRegCount(Convert.toLong(row.get("cnt")));
        }
        for (Map<String, Object> row : registrationMapper.selectApprovedCountGroupByDept(activityId)) {
            Long deptId = Convert.toLong(row.get("deptId"));
            DeptStatVo vo = map.computeIfAbsent(deptId, k -> newVo(row.get("deptName")));
            vo.setDeptId(deptId);
            vo.setApprovedCount(Convert.toLong(row.get("cnt")));
        }
        // 会员总数
        for (Map<String, Object> row : orgMapper.selectMemberTotalMap()) {
            Long deptId = Convert.toLong(row.get("deptId"));
            DeptStatVo vo = map.computeIfAbsent(deptId, k -> newVo(null));
            vo.setDeptId(deptId);
            vo.setMemberTotal(Convert.toLong(row.get("memberTotal")));
        }
        // 打卡达标人数
        for (Map<String, Object> row : stepRecordMapper.selectQualifiedMemberCountByDept(activityId, dailyTarget)) {
            Long deptId = Convert.toLong(row.get("deptId"));
            DeptStatVo vo = map.computeIfAbsent(deptId, k -> newVo(null));
            vo.setDeptId(deptId);
            vo.setCheckinCount(Convert.toLong(row.get("cnt")));
        }
        // 步数/积分
        for (Map<String, Object> row : stepRecordMapper.selectStepStatsByDept(activityId)) {
            Long deptId = Convert.toLong(row.get("deptId"));
            DeptStatVo vo = map.computeIfAbsent(deptId, k -> newVo(row.get("deptName")));
            vo.setDeptId(deptId);
            vo.setTotalSteps(Convert.toLong(row.get("total")));
        }
        for (Map<String, Object> row : pointsMapper.selectPointsByDept(activityId)) {
            Long deptId = Convert.toLong(row.get("deptId"));
            DeptStatVo vo = map.computeIfAbsent(deptId, k -> newVo(row.get("deptName")));
            vo.setDeptId(deptId);
            vo.setTotalPoints(Convert.toLong(row.get("total")));
        }
        // 获奖分布（基于完整排行）
        fillAwardDistribution(activityId, map);
        // 参与率/打卡率
        for (DeptStatVo vo : map.values()) {
            computeRates(vo);
        }
        return new ArrayList<>(map.values());
    }

    /** 先进组织单位评选参考（量化计分排名） */
    public List<UnitEvalVo> unitEval(Long activityId) {
        List<DeptStatVo> stats = deptStats(activityId);
        List<UnitEvalVo> list = new ArrayList<>();
        for (DeptStatVo s : stats) {
            UnitEvalVo vo = new UnitEvalVo();
            vo.setDeptId(s.getDeptId());
            vo.setDeptName(s.getDeptName());
            vo.setMemberTotal(s.getMemberTotal());
            vo.setApprovedCount(s.getApprovedCount());
            vo.setParticipationRateText(s.getParticipationRateText());
            vo.setCheckinCount(s.getCheckinCount());
            vo.setCheckinRateText(s.getCheckinRateText());
            vo.setAward1Count(s.getAward1Count());
            vo.setAward2Count(s.getAward2Count());
            vo.setAward3Count(s.getAward3Count());
            vo.setAwardExcellentCount(s.getAwardExcellentCount());

            double participationRate = s.getParticipationRate() == null ? 0 : s.getParticipationRate();
            double checkinRate = s.getCheckinRate() == null ? 0 : s.getCheckinRate();
            long approved = s.getApprovedCount() == null ? 0 : s.getApprovedCount();
            double participationScore = participationRate * 40;
            double checkinScore = checkinRate * 30;
            double awardWeighted = (s.getAward1Count() == null ? 0 : s.getAward1Count()) * 3
                + (s.getAward2Count() == null ? 0 : s.getAward2Count()) * 2
                + (s.getAward3Count() == null ? 0 : s.getAward3Count()) * 1
                + (s.getAwardExcellentCount() == null ? 0 : s.getAwardExcellentCount()) * 0.5;
            double awardScore = approved > 0 ? Math.min(30, awardWeighted / approved * 30) : 0;
            vo.setParticipationScore(round1(participationScore));
            vo.setCheckinScore(round1(checkinScore));
            vo.setAwardScore(round1(awardScore));
            vo.setTotalScore(round1(participationScore + checkinScore + awardScore));
            list.add(vo);
        }
        list.sort(Comparator.comparing(UnitEvalVo::getTotalScore).reversed());
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setRank(i + 1);
        }
        return list;
    }

    /**
     * 排行统计（支持按单位过滤）
     * type: steps=总步数排名(默认) / points=积分排名 / today=当日步数排名
     */
    public List<RankingStatVo> ranking(Long activityId, Long deptId, String type, int limit) {
        List<RankingStatVo> all;
        if ("points".equals(type)) {
            all = pointsRanking(activityId);
        } else if ("today".equals(type)) {
            all = todayRanking(activityId);
        } else {
            all = fullRanking(activityId);
        }
        if (deptId != null) {
            all = all.stream().filter(r -> deptId.equals(r.getDeptId())).toList();
        }
        return all.size() > limit ? all.subList(0, limit) : all;
    }

    /** 个人打卡明细 */
    public List<CheckinDetailVo> checkinDetail(Long memberId, Long activityId) {
        WalkingActivity act = activityMapper.selectById(activityId);
        int dailyTarget = StepService.dailyTarget(act);
        WalkingMember m = memberMapper.selectById(memberId);
        List<WalkingStepRecord> records = stepRecordMapper.selectList(new LambdaQueryWrapper<WalkingStepRecord>()
            .eq(WalkingStepRecord::getMemberId, memberId)
            .eq(WalkingStepRecord::getActivityId, activityId)
            .orderByAsc(WalkingStepRecord::getRecordDate));
        List<CheckinDetailVo> list = new ArrayList<>();
        for (WalkingStepRecord r : records) {
            CheckinDetailVo vo = new CheckinDetailVo();
            vo.setMemberId(memberId);
            vo.setRealName(m == null ? "" : m.getRealName());
            vo.setPhone(m == null ? "" : m.getPhone());
            vo.setDeptName(m == null ? "" : m.getDeptName());
            vo.setRecordDate(r.getRecordDate().toLocalDate().toString());
            vo.setSteps(r.getSteps());
            vo.setDailyTarget(dailyTarget);
            vo.setReachedText(r.getSteps() >= dailyTarget ? "达标" : "未达标");
            vo.setSource(r.getSource());
            vo.setSourceText(r.getSource() != null && r.getSource() == 2 ? "人工" : "微信运动");
            vo.setAbnormalFlag(r.getAbnormalFlag());
            vo.setLocked(r.getLocked());
            list.add(vo);
        }
        return list;
    }

    /** 个人积分明细（基础分+阶段奖励，逐日拆解，规则与小程序一致，取活动配置） */
    public PointsDetailVo pointsDetail(Long memberId, Long activityId) {
        WalkingActivity act = activityMapper.selectById(activityId);
        int dailyTarget = StepService.dailyTarget(act);
        int s7 = act == null || act.getStreak7Points() == null ? 2 : act.getStreak7Points();
        int s14 = act == null || act.getStreak14Points() == null ? 5 : act.getStreak14Points();
        int full = act == null || act.getFullAttendancePoints() == null ? 10 : act.getFullAttendancePoints();
        WalkingMember m = memberMapper.selectById(memberId);
        List<WalkingStepRecord> records = stepRecordMapper.selectList(new LambdaQueryWrapper<WalkingStepRecord>()
            .eq(WalkingStepRecord::getMemberId, memberId)
            .eq(WalkingStepRecord::getActivityId, activityId)
            .eq(WalkingStepRecord::getAbnormalFlag, 0)
            .orderByAsc(WalkingStepRecord::getRecordDate));

        PointsDetailVo vo = new PointsDetailVo();
        vo.setMemberId(memberId);
        vo.setRealName(m == null ? "" : m.getRealName());
        vo.setPhone(m == null ? "" : m.getPhone());
        vo.setDeptName(m == null ? "" : m.getDeptName());
        vo.setActivityId(activityId);

        int base = 0, bonus = 0, streak = 0, sevenCount = 0, fourteenCount = 0, cumulative = 0;
        boolean fullAttendance = true;
        int totalDays = records.size();
        for (int i = 0; i < records.size(); i++) {
            WalkingStepRecord r = records.get(i);
            boolean valid = r.getSteps() >= dailyTarget;
            int dayBase = 0, dayBonus = 0;
            if (valid) {
                dayBase = 1;
                base++;
                streak++;
                if (streak == 7 && sevenCount < 3) {
                    dayBonus += s7;
                    sevenCount++;
                }
                if (streak == 14 && fourteenCount < 2) {
                    dayBonus += s14;
                    fourteenCount++;
                }
            } else {
                streak = 0;
                fullAttendance = false;
            }
            if (fullAttendance && totalDays >= 21 && i == totalDays - 1) {
                dayBonus += full;
            }
            bonus += dayBonus;
            cumulative += dayBase + dayBonus;

            PointsDayVo day = new PointsDayVo();
            day.setRecordDate(r.getRecordDate().toLocalDate().toString());
            day.setSteps(r.getSteps());
            day.setReached(valid);
            day.setBasePoints(dayBase);
            day.setBonusPoints(dayBonus);
            day.setDayTotal(dayBase + dayBonus);
            day.setCumulative(cumulative);
            vo.getDaily().add(day);
        }
        vo.setBasePoints(base);
        vo.setBonusPoints(bonus);
        vo.setTotalPoints(base + bonus);
        return vo;
    }

    /** 获奖名单 */
    public List<AwardExportVo> awardList(Long activityId) {
        List<RankingStatVo> ranked = fullRanking(activityId);
        List<WalkingAward> awards = awardMapper.selectList(new LambdaQueryWrapper<WalkingAward>()
            .eq(WalkingAward::getActivityId, activityId)
            .eq(WalkingAward::getStatus, 1));
        List<AwardExportVo> list = new ArrayList<>();
        for (RankingStatVo r : ranked) {
            AwardExportVo vo = new AwardExportVo();
            vo.setRank(r.getRank());
            vo.setMemberId(r.getMemberId());
            vo.setRealName(r.getRealName());
            vo.setPhone(r.getPhone());
            vo.setDeptName(r.getDeptName());
            vo.setAwardLevel(awardLevel(r.getRank(), awards));
            vo.setTotalSteps(r.getTotalSteps());
            vo.setTotalPoints(r.getTotalPoints());
            list.add(vo);
        }
        return list;
    }

    /** 分单位导出统计 */
    public void exportDeptStats(Long activityId, HttpServletResponse response) {
        ExcelUtil.exportExcel(deptStats(activityId), "单位统计", DeptStatVo.class, response);
    }

    /** 导出排行榜 */
    public void exportRanking(Long activityId, Long deptId, HttpServletResponse response) {
        ExcelUtil.exportExcel(ranking(activityId, deptId, "steps", 5000), "排行榜", RankingStatVo.class, response);
    }

    /** 导出获奖名单 */
    public void exportAwardList(Long activityId, HttpServletResponse response) {
        ExcelUtil.exportExcel(awardList(activityId), "获奖名单", AwardExportVo.class, response);
    }

    /** 导出先进组织单位评选 */
    public void exportUnitEval(Long activityId, HttpServletResponse response) {
        ExcelUtil.exportExcel(unitEval(activityId), "先进组织评选", UnitEvalVo.class, response);
    }

    // ---------------- 私有方法 ----------------

    /** 完整排行（所有有步数的会员，含单位/积分/达标天数） */
    private List<RankingStatVo> fullRanking(Long activityId) {
        Map<String, Object> stepMap = mapOf(stepRecordMapper.selectStepStatsByMember(activityId), "memberId", "total");
        Map<String, Object> pointsMap = mapOf(pointsMapper.selectPointsByMember(activityId), "memberId", "total");
        WalkingActivity act = activityMapper.selectById(activityId);
        int dailyTarget = act == null || act.getDailyTargetSteps() == null ? DEFAULT_DAILY_TARGET : act.getDailyTargetSteps();
        Map<String, Object> qualifyMap = mapOf(stepRecordMapper.selectQualifyDaysByMember(activityId, dailyTarget), "memberId", "days");

        List<Long> memberIds = stepMap.keySet().stream().map(Convert::toLong).toList();
        Map<Long, WalkingMember> memberInfoMap = CollUtil.isEmpty(memberIds)
            ? Map.of()
            : memberMapper.selectByIds(memberIds).stream().collect(Collectors.toMap(WalkingMember::getId, Function.identity()));

        List<RankingStatVo> list = new ArrayList<>();
        for (Map.Entry<String, Object> e : stepMap.entrySet()) {
            Long memberId = Convert.toLong(e.getKey());
            RankingStatVo vo = new RankingStatVo();
            vo.setMemberId(memberId);
            vo.setTotalSteps(Convert.toLong(e.getValue()));
            vo.setTotalPoints(Convert.toLong(pointsMap.getOrDefault(e.getKey(), 0L)));
            vo.setQualifyDays(Convert.toLong(qualifyMap.getOrDefault(e.getKey(), 0L)));
            WalkingMember m = memberInfoMap.get(memberId);
            // 已停用/作弊账号不参与排行
            if (m == null || (m.getStatus() != null && m.getStatus() == 1)) {
                continue;
            }
            vo.setRealName(m.getRealName());
            vo.setPhone(m.getPhone());
            vo.setDeptId(m.getDeptId());
            vo.setDeptName(m.getDeptName());
            list.add(vo);
        }
        list.sort(Comparator.comparing(RankingStatVo::getTotalSteps).reversed());
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setRank(i + 1);
        }
        return list;
    }

    /**
     * 积分排名：活动累计总积分降序；并列时依次比较连续打卡天数 → 总步数
     */
    private List<RankingStatVo> pointsRanking(Long activityId) {
        List<RankingStatVo> all = fullRanking(activityId);
        all.sort(Comparator.comparing(RankingStatVo::getTotalPoints).reversed()
            .thenComparing(RankingStatVo::getQualifyDays, Comparator.reverseOrder())
            .thenComparing(RankingStatVo::getTotalSteps, Comparator.reverseOrder()));
        for (int i = 0; i < all.size(); i++) {
            all.get(i).setRank(i + 1);
        }
        return all;
    }

    /**
     * 当日步数排名：按当日步数降序，无并列处理（并列按会员id升序）
     */
    private List<RankingStatVo> todayRanking(Long activityId) {
        java.sql.Date today = java.sql.Date.valueOf(LocalDate.now());
        List<Map<String, Object>> rows = stepRecordMapper.selectTodaySteps(activityId, today);
        List<Long> memberIds = rows.stream().map(r -> Convert.toLong(r.get("memberId"))).toList();
        Map<Long, WalkingMember> memberMap = CollUtil.isEmpty(memberIds)
            ? Map.of()
            : memberMapper.selectByIds(memberIds).stream().collect(Collectors.toMap(WalkingMember::getId, Function.identity()));
        List<RankingStatVo> list = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Long memberId = Convert.toLong(row.get("memberId"));
            WalkingMember m = memberMap.get(memberId);
            if (m == null || (m.getStatus() != null && m.getStatus() == 1)) {
                continue;
            }
            RankingStatVo vo = new RankingStatVo();
            vo.setMemberId(memberId);
            vo.setTotalSteps(Convert.toLong(row.get("steps")));
            vo.setRealName(m.getRealName());
            vo.setPhone(m.getPhone());
            vo.setDeptId(m.getDeptId());
            vo.setDeptName(m.getDeptName());
            list.add(vo);
        }
        list.sort(Comparator.comparing(RankingStatVo::getTotalSteps).reversed()
            .thenComparing(RankingStatVo::getMemberId));
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setRank(i + 1);
        }
        return list;
    }

    /** 填充各单位获奖分布 */
    private void fillAwardDistribution(Long activityId, Map<Long, DeptStatVo> map) {
        List<RankingStatVo> ranked = fullRanking(activityId);
        List<WalkingAward> awards = awardMapper.selectList(new LambdaQueryWrapper<WalkingAward>()
            .eq(WalkingAward::getActivityId, activityId)
            .eq(WalkingAward::getStatus, 1));
        Map<Long, int[]> cnt = new HashMap<>();
        for (RankingStatVo r : ranked) {
            Long deptId = r.getDeptId();
            if (deptId == null) {
                continue;
            }
            String level = awardLevel(r.getRank(), awards);
            int[] c = cnt.computeIfAbsent(deptId, k -> new int[4]);
            switch (level) {
                case "一等奖" -> c[0]++;
                case "二等奖" -> c[1]++;
                case "三等奖" -> c[2]++;
                default -> c[3]++;
            }
        }
        for (Map.Entry<Long, int[]> e : cnt.entrySet()) {
            DeptStatVo vo = map.get(e.getKey());
            if (vo == null) {
                continue;
            }
            int[] c = e.getValue();
            vo.setAward1Count((long) c[0]);
            vo.setAward2Count((long) c[1]);
            vo.setAward3Count((long) c[2]);
            vo.setAwardExcellentCount((long) c[3]);
            vo.setAwardCount((long) (c[0] + c[1] + c[2] + c[3]));
        }
    }

    /** 计算参与率/打卡率文案 */
    private void computeRates(DeptStatVo vo) {
        long approved = vo.getApprovedCount() == null ? 0 : vo.getApprovedCount();
        long memberTotal = vo.getMemberTotal() == null ? 0 : vo.getMemberTotal();
        long checkin = vo.getCheckinCount() == null ? 0 : vo.getCheckinCount();
        if (memberTotal > 0) {
            double rate = BigDecimal.valueOf(approved).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(memberTotal), 1, RoundingMode.HALF_UP).doubleValue();
            vo.setParticipationRate(rate / 100);
            vo.setParticipationRateText(rate + "%");
        } else {
            vo.setParticipationRate(0d);
            vo.setParticipationRateText("0%");
        }
        if (approved > 0) {
            double rate = BigDecimal.valueOf(checkin).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(approved), 1, RoundingMode.HALF_UP).doubleValue();
            vo.setCheckinRate(rate / 100);
            vo.setCheckinRateText(rate + "%");
        } else {
            vo.setCheckinRate(0d);
            vo.setCheckinRateText("0%");
        }
    }

    /** 奖项映射：优先活动配置的档位，否则内置规则 */
    private String awardLevel(Integer rank, List<WalkingAward> awards) {
        if (rank == null) {
            return "优秀奖";
        }
        if (CollUtil.isNotEmpty(awards)) {
            for (WalkingAward a : awards) {
                if (rank >= a.getRankStart() && rank <= a.getRankEnd()) {
                    return a.getAwardName();
                }
            }
        }
        if (rank <= 5) {
            return "一等奖";
        } else if (rank <= 15) {
            return "二等奖";
        } else if (rank <= 35) {
            return "三等奖";
        }
        return "优秀奖";
    }

    private LambdaQueryWrapper<WalkingRegistration> regWrapper(Long activityId, Integer status) {
        LambdaQueryWrapper<WalkingRegistration> lqw = new LambdaQueryWrapper<WalkingRegistration>()
            .eq(activityId != null, WalkingRegistration::getActivityId, activityId);
        lqw.eq(status != null, WalkingRegistration::getStatus, status);
        return lqw;
    }

    private DeptStatVo newVo(Object deptName) {
        DeptStatVo vo = new DeptStatVo();
        vo.setDeptName(deptName == null ? "" : deptName.toString());
        vo.setRegCount(0L);
        vo.setApprovedCount(0L);
        vo.setMemberTotal(0L);
        vo.setCheckinCount(0L);
        vo.setAwardCount(0L);
        vo.setTotalSteps(0L);
        vo.setTotalPoints(0L);
        return vo;
    }

    private Map<String, Object> mapOf(List<Map<String, Object>> rows, String keyCol, String valCol) {
        Map<String, Object> map = new HashMap<>();
        if (CollUtil.isEmpty(rows)) {
            return map;
        }
        for (Map<String, Object> row : rows) {
            Object k = row.get(keyCol);
            if (k != null) {
                map.put(String.valueOf(k), row.get(valCol));
            }
        }
        return map;
    }

    private Double round1(double v) {
        return BigDecimal.valueOf(v).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
