package org.dromara.walking.domain.vo.admin;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 组织机构树节点（复用 sys_dept）
 */
@Data
public class OrgTreeVo {

    private Long deptId;

    private Long parentId;

    /** 机构名称 */
    private String deptName;

    /** 显示顺序 */
    private Integer orderNum;

    /** 负责人 */
    private String leader;

    /** 联系电话 */
    private String phone;

    /** 会员总数（线下统计，管理员手动维护，用于参与率） */
    private Integer memberTotal;

    private List<OrgTreeVo> children = new ArrayList<>();
}
