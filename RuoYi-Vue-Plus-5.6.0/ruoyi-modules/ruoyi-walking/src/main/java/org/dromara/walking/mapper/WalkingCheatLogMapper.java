package org.dromara.walking.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.walking.domain.WalkingCheatLog;

/**
 * 健步走作弊审计 Mapper
 */
@Mapper
public interface WalkingCheatLogMapper extends BaseMapperPlus<WalkingCheatLog, WalkingCheatLog> {
}
