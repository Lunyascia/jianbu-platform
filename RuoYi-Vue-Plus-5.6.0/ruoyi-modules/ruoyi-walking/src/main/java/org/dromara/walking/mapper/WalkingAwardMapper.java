package org.dromara.walking.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.walking.domain.WalkingAward;

/**
 * 健步走奖励 Mapper
 */
@Mapper
public interface WalkingAwardMapper extends BaseMapperPlus<WalkingAward, WalkingAward> {
}
