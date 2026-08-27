package org.dromara.walking.domain.vo;

import lombok.Data;

/**
 * 会员信息
 */
@Data
public class MemberInfoVo {
    private Long memberId;
    private String phone;
    private String realName;
    private String deptName;
    private Long deptId;
    /** 是否已报名通过 */
    private Boolean registered;
    /** 报名状态(0草稿 1待审核 2通过 3取消 4停用) */
    private Integer regStatus;
    private String regStatusText;
    /** 收货地址 */
    private String receiver;
    private String addressPhone;
    private String address;
}
