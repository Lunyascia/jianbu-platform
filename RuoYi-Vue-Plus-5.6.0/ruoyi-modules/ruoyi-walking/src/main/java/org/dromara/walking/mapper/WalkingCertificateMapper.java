package org.dromara.walking.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.walking.domain.WalkingCertificate;

/**
 * 健步走证书 Mapper
 */
@Mapper
public interface WalkingCertificateMapper extends BaseMapperPlus<WalkingCertificate, WalkingCertificate> {
}
