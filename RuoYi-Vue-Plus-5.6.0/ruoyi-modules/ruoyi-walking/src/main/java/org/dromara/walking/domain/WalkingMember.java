package org.dromara.walking.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.util.Date;

/**
 * 健步走会员
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("walking_member")
public class WalkingMember extends TenantEntity {

    @TableId(value = "id")
    private Long id;

    /** 微信openid */
    private String openid;

    /** 微信unionid */
    private String unionid;

    /** 手机号 */
    private String phone;

    /** 姓名 */
    private String realName;

    /** 身份证号 */
    private String idCard;

    /** 所属单位(关联sys_dept.dept_id) */
    private Long deptId;

    /** 单位名称 */
    private String deptName;

    /** 头像 */
    private String avatar;

    /** 状态(0正常 1停用) */
    private Integer status;

    /** 是否审核锁定(1后姓名/手机号不可改) */
    private Integer isVerified;

    /** 注册/首次登录时间 */
    private Date registerTime;

    /** 收货人 */
    private String receiver;

    /** 收货手机号 */
    private String addressPhone;

    /** 收货地址 */
    private String address;

    @TableLogic
    private Long delFlag;
}
