package org.dromara.walking.job;

import org.dromara.walking.service.WalkingLockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 锁榜定时任务：每日凌晨自动执行
 *  - 对已超过缓冲期的活动执行最终锁榜
 *  - 总步数排名与积分统计为实时计算（基于截至上次同步的数据），本任务同时刷新固化
 */
@Component
public class WalkingLockJob {

    @Autowired
    private WalkingLockService lockService;

    /** 每日 00:10 执行 */
    @Scheduled(cron = "0 10 0 * * ?")
    public void lockExpiredActivities() {
        lockService.lockExpiredActivities();
    }
}
