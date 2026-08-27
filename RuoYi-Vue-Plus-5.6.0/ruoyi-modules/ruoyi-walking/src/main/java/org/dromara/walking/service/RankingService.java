package org.dromara.walking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.dromara.walking.domain.WalkingActivity;
import org.dromara.walking.domain.WalkingMember;
import org.dromara.walking.domain.WalkingStepRecord;
import org.dromara.walking.domain.vo.RankItemVo;
import org.dromara.walking.mapper.WalkingMemberMapper;
import org.dromara.walking.mapper.WalkingStepRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 排行服务
 */
@Service
public class RankingService {

    @Autowired
    private WalkingStepRecordMapper stepMapper;
    @Autowired
    private WalkingMemberMapper memberMapper;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private StepService stepService;

    /**
     * @param board today=当日排名 total=总排名 points=积分排名
     */
    public List<RankItemVo> getRanking(String board) {
        WalkingActivity activity = activityService.getCurrentEntity();
        if (activity == null) {
            return List.of();
        }
        List<WalkingStepRecord> records = stepMapper.selectList(
            new LambdaQueryWrapper<WalkingStepRecord>()
                .eq(WalkingStepRecord::getActivityId, activity.getId())
                .eq(WalkingStepRecord::getAbnormalFlag, 0));

        // 按会员聚合
        Map<Long, Integer> valueMap = new HashMap<>();
        LocalDate today = LocalDate.now();
        for (WalkingStepRecord r : records) {
            if ("today".equals(board)) {
                if (r.getRecordDate().toLocalDate().equals(today)) {
                    valueMap.merge(r.getMemberId(), r.getSteps(), Integer::sum);
                }
            } else if ("points".equals(board)) {
                // 积分 = 基础分 + 阶段奖励（按会员单独算）
                valueMap.merge(r.getMemberId(), 0, Integer::sum); // 占位，下面统一算
            } else {
                valueMap.merge(r.getMemberId(), r.getSteps(), Integer::sum);
            }
        }

        List<RankItemVo> result = new ArrayList<>();
        for (Map.Entry<Long, Integer> e : valueMap.entrySet()) {
            WalkingMember m = memberMapper.selectById(e.getKey());
            if (m == null) {
                continue;
            }
            RankItemVo vo = new RankItemVo();
            vo.setMemberId(e.getKey());
            vo.setName(m.getRealName());
            vo.setDept(m.getDeptName());
            vo.setValue(e.getValue());
            result.add(vo);
        }
        if ("points".equals(board)) {
            // 积分按活动内所有有效打卡日计算（基础+连续奖励）
            for (RankItemVo vo : result) {
                vo.setValue(stepService.computeTotalPoints(vo.getMemberId(), activity.getId()));
            }
        }
        result.sort((a, b) -> b.getValue() - a.getValue());
        return result;
    }
}
