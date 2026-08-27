package org.dromara.walking.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dromara.walking.domain.vo.OrgVo;
import org.dromara.walking.domain.vo.admin.OrgTreeVo;

import java.util.List;
import java.util.Map;

/**
 * 组织机构查询/维护（直接查 sys_dept，避免跨模块依赖）
 * 工会组织统一以 dept_category='union' 标识，报名下拉/组织机构管理仅显示工会组织
 */
@Mapper
public interface WalkingOrgMapper {

    /** 报名页下拉（公开，仅工会组织；含父级用于 H5 主/子工会两级联动） */
    @Select("SELECT dept_id, dept_name, parent_id FROM sys_dept " +
        "WHERE status = '0' AND del_flag = '0' AND dept_category = 'union' ORDER BY order_num")
    List<OrgVo> selectOrgList();

    /** 组织机构树（后台，仅工会组织；member_total 动态统计直属会员数） */
    @Select("SELECT d.dept_id, d.parent_id, d.dept_name, d.order_num, d.leader, d.phone, " +
        "(SELECT COUNT(*) FROM walking_member m WHERE m.del_flag = '0' AND m.dept_id = d.dept_id) AS member_total " +
        "FROM sys_dept d WHERE d.del_flag = '0' AND d.status = '0' AND d.dept_category = 'union' " +
        "ORDER BY d.parent_id, d.order_num")
    List<OrgTreeVo> selectOrgTree();

    /** 机构详情（后台；member_total 动态统计直属会员数） */
    @Select("SELECT d.dept_id, d.parent_id, d.dept_name, d.order_num, d.leader, d.phone, " +
        "(SELECT COUNT(*) FROM walking_member m WHERE m.del_flag = '0' AND m.dept_id = d.dept_id) AS member_total " +
        "FROM sys_dept d WHERE d.dept_id = #{deptId} AND d.del_flag = '0'")
    OrgTreeVo selectOrgById(@Param("deptId") Long deptId);

    /** 父机构祖级列表 */
    @Select("SELECT ancestors FROM sys_dept WHERE dept_id = #{deptId}")
    String selectAncestors(@Param("deptId") Long deptId);

    /** 是否存在子机构（仅工会组织） */
    @Select("SELECT COUNT(*) FROM sys_dept WHERE parent_id = #{deptId} AND del_flag = '0' AND dept_category = 'union'")
    long countChildren(@Param("deptId") Long deptId);

    /** 机构下是否存在有效会员 */
    @Select("SELECT COUNT(*) FROM walking_member WHERE dept_id = #{deptId} AND del_flag = '0'")
    long countMembers(@Param("deptId") Long deptId);

    /** 新增机构（工会组织；会员总数动态统计，不再手动维护） */
    @Insert("INSERT INTO sys_dept(dept_id, tenant_id, parent_id, ancestors, dept_name, dept_category, order_num, leader, phone, status, del_flag, create_dept, create_by, create_time) " +
        "VALUES(#{deptId}, #{tenantId}, #{parentId}, #{ancestors}, #{deptName}, 'union', #{orderNum}, #{leader}, #{phone}, '0', '0', #{createDept}, #{createBy}, sysdate())")
    int insertOrg(@Param("deptId") Long deptId, @Param("tenantId") String tenantId,
                  @Param("parentId") Long parentId, @Param("ancestors") String ancestors,
                  @Param("deptName") String deptName, @Param("orderNum") Integer orderNum,
                  @Param("leader") String leader, @Param("phone") String phone,
                  @Param("createDept") Long createDept, @Param("createBy") Long createBy);

    /** 修改机构（会员总数动态统计，不再手动维护） */
    @Update("UPDATE sys_dept SET dept_name = #{deptName}, order_num = #{orderNum}, leader = #{leader}, " +
        "phone = #{phone}, update_by = #{updateBy}, update_time = sysdate() " +
        "WHERE dept_id = #{deptId}")
    int updateOrg(@Param("deptId") Long deptId, @Param("deptName") String deptName,
                  @Param("orderNum") Integer orderNum, @Param("leader") String leader,
                  @Param("phone") String phone, @Param("updateBy") Long updateBy);

    /** 删除机构（逻辑删除） */
    @Update("UPDATE sys_dept SET del_flag = '1', update_by = #{updateBy}, update_time = sysdate() WHERE dept_id = #{deptId}")
    int deleteOrg(@Param("deptId") Long deptId, @Param("updateBy") Long updateBy);

    /** 各工会组织直属会员数（统计用，动态统计） */
    @Select("SELECT d.dept_id AS deptId, " +
        "(SELECT COUNT(*) FROM walking_member m WHERE m.del_flag = '0' AND m.dept_id = d.dept_id) AS memberTotal " +
        "FROM sys_dept d WHERE d.dept_category = 'union' AND d.del_flag = '0'")
    List<Map<String, Object>> selectMemberTotalMap();

    /** 全部有效会员数（参与率分母，动态统计） */
    @Select("SELECT COUNT(*) FROM walking_member WHERE del_flag = '0'")
    Long sumMemberTotal();
}
