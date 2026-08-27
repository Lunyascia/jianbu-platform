package org.dromara.walking.domain.vo;

import lombok.Data;

/**
 * 我的获奖
 */
@Data
public class AwardMineVo {
    private Boolean hasAward;
    private String awardLevel;
    private String realName;
    private String deptName;
    private String activityName;
    private String issueDate;
    /** 收货地址 */
    private String receiver;
    private String phone;
    private String address;
}
