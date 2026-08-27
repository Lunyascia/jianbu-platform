package org.dromara.walking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.dromara.walking.domain.WalkingActivity;
import org.dromara.walking.domain.WalkingStepRecord;
import org.dromara.walking.mapper.WalkingActivityMapper;
import org.dromara.walking.mapper.WalkingStepRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 锁榜服务：活动结束 + 缓冲期后，最终锁榜（历史数据固化）
 */
@Service
public class WalkingLockService {

    @Autowired
    private WalkingActivityMapper activityMapper;
    @Autowired
    private WalkingStepRecordMapper stepMapper;

    /**
     * 对已超过缓冲期的活动执行最终锁榜：
     *  - 步数记录 locked 0→1（固化，排行不再变动）
     *  - 活动状态置为 已结束(2)
     * @return 锁榜结果摘要
     */
    public Map<String, Object> lockExpiredActivities() {
        int lockedActivities = 0;
        int lockedRecords = 0;
        List<WalkingActivity> acts = activityMapper.selectList(new LambdaQueryWrapper<WalkingActivity>()
            .ne(WalkingActivity::getDelFlag, 1));
        LocalDate today = LocalDate.now();
        for (WalkingActivity a : acts) {
            if (a.getEndDate() == null) {
                continue;
            }
            LocalDate end = a.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            int buffer = StepService.bufferDays(a);
            // 缓冲结束次日凌晨锁榜：today > end_date + buffer_days
            if (today.isAfter(end.plusDays(buffer))) {
                int updated = stepMapper.update(null, new LambdaUpdateWrapper<WalkingStepRecord>()
                    .eq(WalkingStepRecord::getActivityId, a.getId())
                    .eq(WalkingStepRecord::getLocked, 0)
                    .set(WalkingStepRecord::getLocked, 1));
                lockedRecords += updated;
                if (a.getStatus() == null || a.getStatus() != 2) {
                    a.setStatus(2);
                    activityMapper.updateById(a);
                }
                lockedActivities++;
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("lockedActivities", lockedActivities);
        result.put("lockedRecords", lockedRecords);
        result.put("message", "本次锁榜活动 " + lockedActivities + " 个，锁定步数记录 " + lockedRecords + " 条");
        return result;
    }
}
