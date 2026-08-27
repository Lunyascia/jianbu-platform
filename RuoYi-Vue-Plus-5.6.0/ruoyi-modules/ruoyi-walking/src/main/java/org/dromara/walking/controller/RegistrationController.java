package org.dromara.walking.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.walking.domain.bo.RegForm;
import org.dromara.walking.domain.vo.RegistrationVo;
import org.dromara.walking.service.MemberAuthService;
import org.dromara.walking.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 报名接口（H5 与小程序共用，token 可选）
 */
@RestController
@SaIgnore
@RequestMapping("/walking/registration")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;
    @Autowired
    private MemberAuthService authService;

    /** 提交报名（自动审核，内容安全拦截自动存草稿） */
    @PostMapping("/submit")
    public R<RegistrationVo> submit(@RequestBody RegForm form) {
        return R.ok(registrationService.submit(memberIdOrNull(), form));
    }

    /** 保存报名草稿 */
    @PostMapping("/draft")
    public R<RegistrationVo> draft(@RequestBody RegForm form) {
        return R.ok(registrationService.saveDraft(memberIdOrNull(), form));
    }

    /** 我的报名状态（需登录） */
    @GetMapping("/mine")
    public R<RegistrationVo> mine() {
        Long memberId = authService.getMemberIdByToken();
        return R.ok(registrationService.getMine(memberId));
    }

    /** 取当前 token 对应会员id，无 token 时返回 null（H5 场景） */
    private Long memberIdOrNull() {
        try {
            return authService.getMemberIdByToken();
        } catch (ServiceException e) {
            return null;
        }
    }
}
