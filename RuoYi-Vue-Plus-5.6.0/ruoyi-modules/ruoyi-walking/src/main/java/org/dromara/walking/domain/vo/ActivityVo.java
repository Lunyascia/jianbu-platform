package org.dromara.walking.domain.vo;

import lombok.Data;

import java.util.Date;

/**
 * 活动信息（小程序展示）
 */
@Data
public class ActivityVo {
    private Long id;
    private String activityName;
    private String coverUrl;
    private String description;
    private String ruleContent;
    private Date startDate;
    private Date endDate;
    private Integer dailyTargetSteps;
    private Integer targetSteps;
    private Date registerStart;
    private Date registerEnd;
    private Integer status;
    /** 报名是否已截止 */
    private Boolean registerClosed;
}
