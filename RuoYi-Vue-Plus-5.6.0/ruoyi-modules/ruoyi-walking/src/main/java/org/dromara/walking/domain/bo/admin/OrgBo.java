package org.dromara.walking.domain.bo.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 组织机构（后台）表单 —— 复用 sys_dept
 */
@Data
public class OrgBo {

    /** 机构id(新增为空) */
    private Long deptId;

    /** 父机构id(0为顶级) */
    private Long parentId;

    /** 机构名称 */
    @NotBlank(message = "机构名称不能为空")
    private String deptName;

    /** 显示顺序 */
    private Integer orderNum;

    /** 负责人 */
    private String leader;

    /** 联系电话 */
    private String phone;
}
