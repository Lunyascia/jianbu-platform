package org.dromara.walking.service;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.walking.domain.WalkingMember;
import org.dromara.walking.domain.WalkingRegistration;
import org.dromara.walking.domain.vo.MemberInfoVo;
import org.dromara.walking.domain.vo.MemberLoginVo;
import org.dromara.walking.mapper.WalkingMemberMapper;
import org.dromara.walking.mapper.WalkingRegistrationMapper;
import org.dromara.walking.wx.WxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 会员服务
 */
@Service
public class MemberService {

    @Autowired
    private WalkingMemberMapper memberMapper;
    @Autowired
    private WalkingRegistrationMapper registrationMapper;
    @Autowired
    private WxService wxService;
    @Autowired
    private MemberAuthService authService;

    /**
     * 微信 openid 登录
     */
    public MemberLoginVo login(String xcxCode) {
        WxService.Code2SessionResult r = wxService.code2Session(xcxCode);
        WalkingMember member = memberMapper.selectOne(
            new LambdaQueryWrapper<WalkingMember>().eq(WalkingMember::getOpenid, r.getOpenid()));
        if (member == null) {
            member = new WalkingMember();
            member.setOpenid(r.getOpenid());
            member.setUnionid(r.getUnionid());
            member.setStatus(0);
            member.setRegisterTime(new Date());
            memberMapper.insert(member);
        }
        authService.cacheSessionKey(member.getId(), r.getSessionKey());

        MemberLoginVo vo = new MemberLoginVo();
        vo.setMemberId(member.getId());
        vo.setAccessToken(authService.createToken(member.getId()));
        vo.setNeedPhone(StrUtil.isBlank(member.getPhone()));
        vo.setBound(false);
        return vo;
    }

    /**
     * 绑定手机号并匹配报名记录
     */
    @Transactional(rollbackFor = Exception.class)
    public MemberLoginVo bindPhone(Long memberId, String phone) {
        if (phone == null || !ReUtil.isMatch("^1\\d{10}$", phone)) {
            throw new ServiceException("手机号格式不正确");
        }
        WalkingMember current = memberMapper.selectById(memberId);
        if (current == null) {
            throw new ServiceException("会员不存在");
        }

        // 手机号唯一：查是否已有会员占用
        WalkingMember byPhone = memberMapper.selectOne(
            new LambdaQueryWrapper<WalkingMember>().eq(WalkingMember::getPhone, phone));
        if (byPhone != null && !byPhone.getId().equals(memberId)) {
            // 该手机号属于另一会员（多为 H5 报名创建的无 openid 会员）→ 把当前微信身份绑定到该报名会员
            if (StrUtil.isBlank(byPhone.getOpenid())) {
                String openid = current.getOpenid();
                String unionid = current.getUnionid();
                // 1. 强制清空临时会员的 openid（updateById 默认跳过 null，必须用 UpdateWrapper）
                memberMapper.update(null, new LambdaUpdateWrapper<WalkingMember>()
                    .eq(WalkingMember::getId, memberId)
                    .set(WalkingMember::getOpenid, null)
                    .set(WalkingMember::getUnionid, null));
                // 2. 把微信身份赋给报名会员
                byPhone.setOpenid(openid);
                byPhone.setUnionid(unionid);
                memberMapper.updateById(byPhone);
                // 3. 删除临时会员
                memberMapper.deleteById(memberId);
                memberId = byPhone.getId();
            } else {
                throw new ServiceException("该手机号已被其他微信账号绑定");
            }
        } else {
            current.setPhone(phone);
            memberMapper.updateById(current);
        }

        // 匹配该手机号会员的"审核通过"报名记录
        WalkingRegistration reg = registrationMapper.selectOne(
            new LambdaQueryWrapper<WalkingRegistration>()
                .eq(WalkingRegistration::getMemberId, memberId)
                .eq(WalkingRegistration::getStatus, 2));
        boolean bound = reg != null;

        MemberLoginVo vo = new MemberLoginVo();
        vo.setMemberId(memberId);
        vo.setAccessToken(authService.createToken(memberId));
        vo.setNeedPhone(false);
        vo.setBound(bound);
        return vo;
    }

    /**
     * 会员信息
     */
    public MemberInfoVo getInfo(Long memberId) {
        WalkingMember m = memberMapper.selectById(memberId);
        if (m == null) {
            throw new ServiceException("会员不存在");
        }
        MemberInfoVo vo = new MemberInfoVo();
        vo.setMemberId(m.getId());
        vo.setPhone(m.getPhone());
        vo.setRealName(m.getRealName());
        vo.setDeptName(m.getDeptName());
        vo.setDeptId(m.getDeptId());
        vo.setReceiver(m.getReceiver());
        vo.setAddressPhone(m.getAddressPhone());
        vo.setAddress(m.getAddress());

        WalkingRegistration reg = registrationMapper.selectOne(
            new LambdaQueryWrapper<WalkingRegistration>()
                .eq(WalkingRegistration::getMemberId, memberId)
                .last("limit 1"));
        if (reg != null) {
            vo.setRegStatus(reg.getStatus());
            vo.setRegStatusText(statusText(reg.getStatus()));
            vo.setRegistered(reg.getStatus() == 2);
        } else {
            vo.setRegistered(false);
        }
        return vo;
    }

    private String statusText(Integer status) {
        return switch (status == null ? -1 : status) {
            case 0 -> "待提交草稿";
            case 1 -> "待审核";
            case 2 -> "报名成功";
            case 3 -> "已取消";
            case 4 -> "已停用";
            default -> "";
        };
    }
}
