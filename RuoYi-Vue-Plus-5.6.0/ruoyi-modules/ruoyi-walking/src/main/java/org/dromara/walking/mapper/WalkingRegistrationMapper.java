package org.dromara.walking.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.walking.domain.WalkingRegistration;
import org.dromara.walking.domain.vo.admin.RegistrationAdminVo;

import java.util.List;
import java.util.Map;

/**
 * 健步走报名 Mapper
 */
@Mapper
public interface WalkingRegistrationMapper extends BaseMapperPlus<WalkingRegistration, WalkingRegistration> {

    /** 各活动已生效报名数(status 1/2) */
    @Select("SELECT activity_id AS activityId, COUNT(*) AS cnt FROM walking_registration " +
        "WHERE status IN (1, 2) AND del_flag = 0 GROUP BY activity_id")
    List<Map<String, Object>> selectApprovedCountGroupByActivity();

    /** 按单位统计报名人数(status 1/2) */
    @Select("SELECT m.dept_id AS deptId, m.dept_name AS deptName, COUNT(*) AS cnt " +
        "FROM walking_registration r JOIN walking_member m ON r.member_id = m.id AND m.del_flag = 0 " +
        "WHERE r.activity_id = #{activityId} AND r.del_flag = 0 AND r.status IN (1, 2) " +
        "GROUP BY m.dept_id, m.dept_name")
    List<Map<String, Object>> selectCountGroupByDept(Long activityId);

    /** 按单位统计参与人数(status 2) */
    @Select("SELECT m.dept_id AS deptId, m.dept_name AS deptName, COUNT(*) AS cnt " +
        "FROM walking_registration r JOIN walking_member m ON r.member_id = m.id AND m.del_flag = 0 " +
        "WHERE r.activity_id = #{activityId} AND r.del_flag = 0 AND r.status = 2 " +
        "GROUP BY m.dept_id, m.dept_name")
    List<Map<String, Object>> selectApprovedCountGroupByDept(Long activityId);

    /** 分单位导出报名信息（join 会员，支持单位过滤） */
    @Select("<script>" +
        "SELECT r.id, r.member_id, r.activity_id, r.status, r.audit_result, r.submit_time, " +
        "       r.audit_time, r.cancel_by, r.cancel_time, " +
        "       m.real_name, m.phone, m.dept_id, m.dept_name, m.id_card, m.status AS member_status " +
        "FROM walking_registration r " +
        "JOIN walking_member m ON r.member_id = m.id AND m.del_flag = 0 " +
        "WHERE r.del_flag = 0 AND r.status IN (1, 2) " +
        "<if test='activityId != null'>AND r.activity_id = #{activityId}</if> " +
        "<if test='deptId != null'>AND m.dept_id = #{deptId}</if> " +
        "ORDER BY m.dept_id, r.create_time" +
        "</script>")
    List<RegistrationAdminVo> selectExportList(Long activityId, Long deptId);

    /** 各会员已生效报名数(status 1/2) */
    @Select("SELECT member_id AS memberId, COUNT(*) AS cnt FROM walking_registration " +
        "WHERE status IN (1, 2) AND del_flag = 0 GROUP BY member_id")
    List<Map<String, Object>> selectRegCountGroupByMember();
}

