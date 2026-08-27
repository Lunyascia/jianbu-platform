package org.dromara.walking.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import org.dromara.common.core.domain.R;
import org.dromara.walking.domain.vo.OrgVo;
import org.dromara.walking.service.OrgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 组织机构接口（公开，复用 sys_dept）
 */
@RestController
@SaIgnore
@RequestMapping("/walking/org")
public class OrgController {

    @Autowired
    private OrgService orgService;

    @GetMapping("/list")
    public R<List<OrgVo>> list() {
        return R.ok(orgService.getOrgList());
    }
}
