package org.dromara.walking.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.walking.domain.WalkingPoints;

import java.util.List;
import java.util.Map;

/**
 * 健步走积分 Mapper
 */
@Mapper
public interface WalkingPointsMapper extends BaseMapperPlus<WalkingPoints, WalkingPoints> {

    /** 会员累计积分 */
    @Select("SELECT member_id AS memberId, SUM(points) AS total FROM walking_points " +
        "WHERE activity_id = #{activityId} AND del_flag = 0 GROUP BY member_id")
    List<Map<String, Object>> selectPointsByMember(@Param("activityId") Long activityId);

    /** 单位累计积分（排除停用会员） */
    @Select("SELECT m.dept_id AS deptId, m.dept_name AS deptName, SUM(p.points) AS total " +
        "FROM walking_points p JOIN walking_member m ON p.member_id = m.id AND m.del_flag = 0 " +
        "WHERE p.activity_id = #{activityId} AND p.del_flag = 0 AND m.status = 0 GROUP BY m.dept_id, m.dept_name")
    List<Map<String, Object>> selectPointsByDept(@Param("activityId") Long activityId);
}
