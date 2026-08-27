package org.dromara.walking.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.walking.domain.WalkingMember;

/**
 * 健步走会员 Mapper
 */
@Mapper
public interface WalkingMemberMapper extends BaseMapperPlus<WalkingMember, WalkingMember> {

    /** 会员总数（仅统计启用会员，停用/已删除不计入） */
    @Select("SELECT COUNT(*) FROM walking_member WHERE del_flag = 0 AND status = 0")
    Long selectMemberCount();

    /** 有会员的单位数 */
    @Select("SELECT COUNT(DISTINCT dept_id) FROM walking_member WHERE dept_id IS NOT NULL AND del_flag = 0")
    Long selectDeptCount();
}
