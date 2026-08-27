package org.dromara.walking.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import lombok.Data;
import org.dromara.common.core.domain.R;
import org.dromara.walking.domain.vo.AwardMineVo;
import org.dromara.walking.domain.vo.AwardTierVo;
import org.dromara.walking.service.AwardService;
import org.dromara.walking.service.MemberAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 奖励/证书接口（小程序端，需登录；奖励档位列表公开给 H5 报名页）
 */
@RestController
@SaIgnore
@RequestMapping("/walking/award")
public class AwardController {

    @Autowired
    private AwardService awardService;
    @Autowired
    private MemberAuthService authService;

    /** 奖励档位列表（公开：当前活动启用的奖励，H5 报名页 + 小程序奖励页共用） */
    @GetMapping("/list")
    public R<List<AwardTierVo>> list() {
        return R.ok(awardService.listCurrentTiers());
    }

    /** 我的获奖 */
    @GetMapping("/mine")
    public R<AwardMineVo> mine() {
        Long memberId = authService.getMemberIdByToken();
        return R.ok(awardService.getMine(memberId));
    }

    /** 保存收货地址 */
    @PostMapping("/address")
    public R<Void> address(@RequestBody AddressReq req) {
        Long memberId = authService.getMemberIdByToken();
        awardService.saveAddress(memberId, req.getReceiver(), req.getPhone(), req.getAddress());
        return R.ok();
    }

    @Data
    public static class AddressReq {
        private String receiver;
        private String phone;
        private String address;
    }
}
