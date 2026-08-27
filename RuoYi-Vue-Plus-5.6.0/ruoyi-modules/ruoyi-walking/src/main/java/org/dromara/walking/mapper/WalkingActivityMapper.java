package org.dromara.walking.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.walking.domain.WalkingActivity;

/**
 * 健步走活动 Mapper
 */
@Mapper
public interface WalkingActivityMapper extends BaseMapperPlus<WalkingActivity, WalkingActivity> {
}
