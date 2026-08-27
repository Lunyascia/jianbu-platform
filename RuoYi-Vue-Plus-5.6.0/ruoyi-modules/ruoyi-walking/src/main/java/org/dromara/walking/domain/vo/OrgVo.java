package org.dromara.walking.domain.vo;

import lombok.Data;

/**
 * 组织机构（sys_dept）
 */
@Data
public class OrgVo {
    private Long deptId;
    private String deptName;
    /** 父机构id（0或非工会机构视为顶级/主工会） */
    private Long parentId;
}
