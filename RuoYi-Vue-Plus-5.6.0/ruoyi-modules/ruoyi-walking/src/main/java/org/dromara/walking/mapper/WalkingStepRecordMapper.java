package org.dromara.walking.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.walking.domain.WalkingStepRecord;
import org.dromara.walking.domain.vo.admin.StepRecordAdminVo;

import java.util.List;
import java.util.Map;

/**
 * 健步走步数打卡 Mapper
 */
@Mapper
public interface WalkingStepRecordMapper extends BaseMapperPlus<WalkingStepRecord, WalkingStepRecord> {

    /** 会员累计步数(仅正常数据) */
    @Select("SELECT member_id AS memberId, SUM(steps) AS total FROM walking_step_record " +
        "WHERE activity_id = #{activityId} AND abnormal_flag = 0 AND del_flag = 0 GROUP BY member_id")
    List<Map<String, Object>> selectStepStatsByMember(@Param("activityId") Long activityId);

    /** 会员达标天数(步数 >= dailyTarget) */
    @Select("SELECT member_id AS memberId, COUNT(*) AS days FROM walking_step_record " +
        "WHERE activity_id = #{activityId} AND abnormal_flag = 0 AND del_flag = 0 AND steps >= #{dailyTarget} " +
        "GROUP BY member_id")
    List<Map<String, Object>> selectQualifyDaysByMember(@Param("activityId") Long activityId, @Param("dailyTarget") int dailyTarget);

    /** 单位累计步数（排除停用会员） */
    @Select("SELECT m.dept_id AS deptId, m.dept_name AS deptName, SUM(s.steps) AS total " +
        "FROM walking_step_record s JOIN walking_member m ON s.member_id = m.id AND m.del_flag = 0 " +
        "WHERE s.activity_id = #{activityId} AND s.abnormal_flag = 0 AND s.del_flag = 0 AND m.status = 0 " +
        "GROUP BY m.dept_id, m.dept_name")
    List<Map<String, Object>> selectStepStatsByDept(@Param("activityId") Long activityId);

    /** 各单位打卡达标会员数（步数>=dailyTarget 的去重会员数，排除停用会员） */
    @Select("SELECT m.dept_id AS deptId, COUNT(DISTINCT s.member_id) AS cnt " +
        "FROM walking_step_record s JOIN walking_member m ON s.member_id = m.id AND m.del_flag = 0 " +
        "WHERE s.activity_id = #{activityId} AND s.abnormal_flag = 0 AND s.del_flag = 0 AND m.status = 0 AND s.steps >= #{dailyTarget} " +
        "GROUP BY m.dept_id")
    List<Map<String, Object>> selectQualifiedMemberCountByDept(@Param("activityId") Long activityId, @Param("dailyTarget") int dailyTarget);

    /** 当日步数（排行用，排除异常数据） */
    @Select("SELECT member_id AS memberId, steps FROM walking_step_record " +
        "WHERE activity_id = #{activityId} AND record_date = #{date} AND abnormal_flag = 0 AND del_flag = 0")
    List<Map<String, Object>> selectTodaySteps(@Param("activityId") Long activityId, @Param("date") java.sql.Date date);

    /** 活动总步数 */
    @Select("SELECT COALESCE(SUM(steps), 0) FROM walking_step_record " +
        "WHERE activity_id = #{activityId} AND abnormal_flag = 0 AND del_flag = 0")
    Long sumSteps(@Param("activityId") Long activityId);

    /** 异常数据条数 */
    @Select("<script>" +
        "SELECT COUNT(*) FROM walking_step_record WHERE abnormal_flag = 1 AND del_flag = 0 " +
        "<if test='activityId != null'>AND activity_id = #{activityId}</if>" +
        "</script>")
    Long countAbnormal(@Param("activityId") Long activityId);


    /** 分单位导出打卡信息（join 会员） */
    @Select("<script>" +
        "SELECT s.member_id, s.activity_id, s.record_date, s.steps, s.source, s.abnormal_flag, " +
        "       m.real_name, m.phone, m.dept_id, m.dept_name " +
        "FROM walking_step_record s " +
        "JOIN walking_member m ON s.member_id = m.id AND m.del_flag = 0 " +
        "WHERE s.del_flag = 0 " +
        "<if test='activityId != null'>AND s.activity_id = #{activityId}</if> " +
        "<if test='deptId != null'>AND m.dept_id = #{deptId}</if> " +
        "ORDER BY m.dept_id, s.record_date" +
        "</script>")
    List<StepRecordAdminVo> selectExportList(Long activityId, Long deptId);
}
