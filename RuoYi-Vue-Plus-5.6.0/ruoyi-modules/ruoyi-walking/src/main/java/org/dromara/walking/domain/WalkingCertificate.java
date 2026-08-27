package org.dromara.walking.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * 健步走获奖证书
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("walking_certificate")
public class WalkingCertificate extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 会员id */
    private Long memberId;

    /** 活动id */
    private Long activityId;

    /** 名次(rank 是 MySQL 保留字，用反引号转义) */
    @TableField("`rank`")
    private Integer rank;

    /** 奖项级别(一等奖/二等奖/三等奖/优秀奖) */
    private String awardLevel;

    /** 证书编号 */
    private String certNo;

    /** 证书标题 */
    private String certTitle;

    /** 证书模板/图片 */
    private String templateUrl;

    /** 颁发时间 */
    private Date issueTime;

    @TableLogic
    private Long delFlag;
}
