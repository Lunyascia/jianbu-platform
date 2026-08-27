package org.dromara.walking.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import lombok.Data;
import org.dromara.common.core.domain.R;
import org.dromara.walking.domain.vo.MemberInfoVo;
import org.dromara.walking.domain.vo.MemberLoginVo;
import org.dromara.walking.service.MemberAuthService;
import org.dromara.walking.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 会员接口（小程序端，自定义 token 鉴权）
 */
@RestController
@SaIgnore
@RequestMapping("/walking/member")
public class MemberController {

    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberAuthService authService;

    /** 微信 openid 登录 */
    @PostMapping("/login")
    public R<MemberLoginVo> login(@RequestBody LoginReq req) {
        return R.ok(memberService.login(req.getXcxCode()));
    }

    /** 绑定手机号并匹配报名 */
    @PostMapping("/phone")
    public R<MemberLoginVo> bindPhone(@RequestBody PhoneReq req) {
        Long memberId = authService.getMemberIdByToken();
        return R.ok(memberService.bindPhone(memberId, req.getPhone()));
    }

    /** 会员信息 */
    @GetMapping("/info")
    public R<MemberInfoVo> info() {
        Long memberId = authService.getMemberIdByToken();
        return R.ok(memberService.getInfo(memberId));
    }

    @Data
    public static class LoginReq {
        /** wx.login 返回的 code */
        private String xcxCode;
    }

    @Data
    public static class PhoneReq {
        private String phone;
    }
}
