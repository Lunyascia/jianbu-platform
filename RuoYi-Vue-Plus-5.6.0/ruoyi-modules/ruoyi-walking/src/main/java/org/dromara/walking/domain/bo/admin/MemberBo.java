package org.dromara.walking.domain.bo.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 会员管理（后台）维护表单 —— 新增/调整单位/收货地址等
 */
@Data
public class MemberBo {

    /** 会员id(新增为空,修改必填) */
    private Long id;

    /** 所属单位 */
    private Long deptId;

    /** 单位名称 */
    private String deptName;

    /** 收货人 */
    private String receiver;

    /** 收货手机号 */
    private String addressPhone;

    /** 收货地址 */
    private String address;

    /** 姓名(后台可修正误填/新增必填) */
    private String realName;

    /** 身份证号 */
    private String idCard;

    /** 手机号(新增必填) */
    private String phone;

    /** 状态(0正常 1停用) */
    private Integer status;
}
