package org.dromara.walking.service;

import org.dromara.walking.domain.vo.OrgVo;
import org.dromara.walking.mapper.WalkingOrgMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 组织机构服务（复用 sys_dept）
 */
@Service
public class OrgService {

    @Autowired
    private WalkingOrgMapper orgMapper;

    public List<OrgVo> getOrgList() {
        return orgMapper.selectOrgList();
    }
}
