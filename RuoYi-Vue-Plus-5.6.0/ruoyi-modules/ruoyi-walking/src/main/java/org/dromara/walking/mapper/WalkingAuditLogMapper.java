package org.dromara.walking.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.walking.domain.WalkingAuditLog;
import org.dromara.walking.domain.vo.admin.AuditLogVo;

import java.util.List;

/**
 * 健步走报名审核日志 Mapper
 */
@Mapper
public interface WalkingAuditLogMapper extends BaseMapperPlus<WalkingAuditLog, WalkingAuditLog> {

    /** 审核日志列表（join 会员姓名/手机号） */
    @Select("<script>" +
        "SELECT l.id, l.registration_id, l.member_id, l.audit_action, l.audit_result, l.auditor, l.create_time, " +
        "       m.real_name, m.phone " +
        "FROM walking_audit_log l " +
        "LEFT JOIN walking_member m ON l.member_id = m.id AND m.del_flag = 0 " +
        "WHERE l.del_flag = 0 " +
        "<if test='registrationId != null'>AND l.registration_id = #{registrationId}</if> " +
        "<if test='memberId != null'>AND l.member_id = #{memberId}</if> " +
        "<if test='auditAction != null and auditAction != \"\"'>AND l.audit_action = #{auditAction}</if> " +
        "ORDER BY l.create_time DESC" +
        "</script>")
    List<AuditLogVo> selectAuditLogList(Long registrationId, Long memberId, String auditAction);
}
