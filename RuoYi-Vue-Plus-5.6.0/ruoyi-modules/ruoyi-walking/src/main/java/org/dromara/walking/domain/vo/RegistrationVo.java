package org.dromara.walking.domain.vo;

import lombok.Data;

/**
 * 报名提交/状态返回
 */
@Data
public class RegistrationVo {
    private Long registrationId;
    private Integer status;
    private String statusText;
    /** 审核结果/异常原因 */
    private String auditResult;
    private String realName;
    private String deptName;
}
