package org.dromara.walking.controller.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.walking.domain.vo.admin.AwardExportVo;
import org.dromara.walking.domain.vo.admin.CheckinDetailVo;
import org.dromara.walking.domain.vo.admin.DeptStatVo;
import org.dromara.walking.domain.vo.admin.PointsDetailVo;
import org.dromara.walking.domain.vo.admin.RankingStatVo;
import org.dromara.walking.domain.vo.admin.StatsOverviewVo;
import org.dromara.walking.domain.vo.admin.UnitEvalVo;
import org.dromara.walking.service.AdminStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 数据统计（后台）
 */
@Validated
@RestController
@RequestMapping("/walking/admin/stats")
public class AdminStatsController {

    @Autowired
    private AdminStatsService statsService;
    @Autowired
    private org.dromara.walking.service.WalkingLockService lockService;

    /** 手动刷新排名统计（触发锁榜评估） */
    @SaCheckPermission("walking:stats:list")
    @Log(title = "健步走统计刷新", businessType = BusinessType.UPDATE)
    @PostMapping("/refresh")
    public R<Object> refresh() {
        return R.ok(lockService.lockExpiredActivities());
    }

    /** 总览统计 */
    @SaCheckPermission("walking:stats:list")
    @GetMapping("/overview")
    public R<StatsOverviewVo> overview(Long activityId) {
        return R.ok(statsService.overview(activityId));
    }

    /** 单位统计（报名/参与/打卡/获奖） */
    @SaCheckPermission("walking:stats:list")
    @GetMapping("/dept")
    public R<List<DeptStatVo>> deptStats(Long activityId) {
        return R.ok(statsService.deptStats(activityId));
    }

    /** 先进组织单位评选参考（量化计分排名） */
    @SaCheckPermission("walking:stats:list")
    @GetMapping("/unit/eval")
    public R<List<UnitEvalVo>> unitEval(Long activityId) {
        return R.ok(statsService.unitEval(activityId));
    }

    /** 排行统计（type: today当日/points积分/steps总步数，支持按单位过滤） */
    @SaCheckPermission("walking:stats:list")
    @GetMapping("/ranking")
    public R<List<RankingStatVo>> ranking(Long activityId, Long deptId,
                                          @RequestParam(defaultValue = "steps") String type,
                                          @RequestParam(defaultValue = "100") int limit) {
        return R.ok(statsService.ranking(activityId, deptId, type, limit));
    }

    /** 个人打卡明细 */
    @SaCheckPermission("walking:stats:list")
    @GetMapping("/checkin/detail")
    public R<List<CheckinDetailVo>> checkinDetail(Long memberId, Long activityId) {
        return R.ok(statsService.checkinDetail(memberId, activityId));
    }

    /** 个人积分明细 */
    @SaCheckPermission("walking:stats:list")
    @GetMapping("/points/detail")
    public R<PointsDetailVo> pointsDetail(Long memberId, Long activityId) {
        return R.ok(statsService.pointsDetail(memberId, activityId));
    }

    /** 获奖名单 */
    @SaCheckPermission("walking:stats:list")
    @GetMapping("/award/list")
    public R<List<AwardExportVo>> awardList(Long activityId) {
        return R.ok(statsService.awardList(activityId));
    }

    /** 分单位导出统计（系统管理员） */
    @SaCheckPermission("walking:stats:export")
    @Log(title = "健步走统计导出", businessType = BusinessType.EXPORT)
    @PostMapping("/export/dept")
    public void exportDept(Long activityId, HttpServletResponse response) {
        statsService.exportDeptStats(activityId, response);
    }

    /** 导出排行榜（全局或按单位，系统管理员） */
    @SaCheckPermission("walking:stats:export")
    @Log(title = "健步走排行导出", businessType = BusinessType.EXPORT)
    @PostMapping("/export/ranking")
    public void exportRanking(Long activityId, Long deptId, HttpServletResponse response) {
        statsService.exportRanking(activityId, deptId, response);
    }

    /** 一键导出获奖名单（系统管理员） */
    @SaCheckPermission("walking:stats:export")
    @Log(title = "健步走获奖名单导出", businessType = BusinessType.EXPORT)
    @PostMapping("/export/awardList")
    public void exportAwardList(Long activityId, HttpServletResponse response) {
        statsService.exportAwardList(activityId, response);
    }

    /** 导出先进组织评选（系统管理员） */
    @SaCheckPermission("walking:stats:export")
    @Log(title = "健步走先进组织导出", businessType = BusinessType.EXPORT)
    @PostMapping("/export/eval")
    public void exportEval(Long activityId, HttpServletResponse response) {
        statsService.exportUnitEval(activityId, response);
    }
}
