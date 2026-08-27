package org.dromara.walking.domain.vo;

import lombok.Data;

/**
 * 会员登录返回
 */
@Data
public class MemberLoginVo {
    /** 访问令牌 */
    private String accessToken;
    /** 会员id */
    private Long memberId;
    /** 是否需要绑定手机号 */
    private Boolean needPhone;
    /** 是否已匹配到报名记录 */
    private Boolean bound;
}
