package org.dromara.walking.service;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.walking.domain.WalkingActivity;
import org.dromara.walking.domain.WalkingAuditLog;
import org.dromara.walking.domain.WalkingMember;
import org.dromara.walking.domain.WalkingRegistration;
import org.dromara.walking.domain.bo.RegForm;
import org.dromara.walking.domain.vo.RegistrationVo;
import org.dromara.walking.mapper.WalkingAuditLogMapper;
import org.dromara.walking.mapper.WalkingMemberMapper;
import org.dromara.walking.mapper.WalkingRegistrationMapper;
import org.dromara.walking.wx.WxProperties;
import org.dromara.walking.wx.WxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 报名服务（H5 与小程序共用）
 */
@Service
public class RegistrationService {

    @Autowired
    private WalkingRegistrationMapper registrationMapper;
    @Autowired
    private WalkingMemberMapper memberMapper;
    @Autowired
    private WalkingAuditLogMapper auditLogMapper;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private MemberAuthService authService;
    @Autowired
    private WxService wxService;
    @Autowired
    private WxProperties wxProperties;

    /**
     * 提交报名（自动审核，无敏感性直接报名成功；有敏感性退回草稿提示修改）
     */
    @Transactional(rollbackFor = Exception.class)
    public RegistrationVo submit(Long memberIdFromToken, RegForm form) {
        // 1. 校验表单
        validate(form);

        // 2. 报名时效校验（报名窗口 [registerStart, registerEnd]，先于建会员，窗口外不产生垃圾数据）
        WalkingActivity activity = activityService.getCurrentEntity();
        if (activity == null) {
            throw new ServiceException("当前无进行中的活动");
        }
        if (activity.getRegisterStart() != null && activity.getRegisterStart().after(new Date())) {
            throw new ServiceException("报名尚未开始");
        }
        if (activity.getRegisterEnd() != null && activity.getRegisterEnd().before(new Date())) {
            throw new ServiceException("报名已截止");
        }

        // 3. 找/建会员（支持 token 或 H5 按手机号）
        WalkingMember member = resolveMember(memberIdFromToken, form.getPhone());

        // 4. 内容安全审核
        String auditText = form.getRealName() + " " + form.getDeptName() + " " + form.getPhone();
        boolean pass;
        if (Boolean.TRUE.equals(wxProperties.getContentCheck().getEnabled())) {
            pass = wxService.msgSecCheck(auditText, member.getOpenid());
        } else {
            pass = wxService.localSensitiveCheck(auditText);
        }
        if (!pass) {
            // 敏感内容拦截 → 自动存草稿，提示修改
            saveDraftInternal(member, activity, form, "内容含敏感信息，请修改后重新提交");
            RegistrationVo fail = new RegistrationVo();
            fail.setStatus(0);
            fail.setStatusText("待提交草稿");
            fail.setAuditResult("内容含敏感信息，请修改后重新提交");
            return fail;
        }

        // 5. 提交 → 更新会员信息（审核通过前不绑定）
        member.setRealName(form.getRealName());
        member.setDeptId(form.getDeptId());
        member.setDeptName(form.getDeptName());
        member.setIdCard(form.getIdCard());
        member.setPhone(form.getPhone());
        member.setIsVerified(1);
        memberMapper.updateById(member);

        // 6. 写报名记录（status=1 待审核，管理员审核通过后才能被绑定）
        WalkingRegistration reg = registrationMapper.selectOne(
            new LambdaQueryWrapper<WalkingRegistration>()
                .eq(WalkingRegistration::getMemberId, member.getId())
                .eq(WalkingRegistration::getActivityId, activity.getId()));
        if (reg == null) {
            reg = new WalkingRegistration();
            reg.setMemberId(member.getId());
            reg.setActivityId(activity.getId());
        }
        reg.setStatus(1);
        reg.setAuditResult("待审核，等待管理员审核");
        reg.setSubmitTime(new Date());
        if (reg.getId() == null) {
            registrationMapper.insert(reg);
        } else {
            registrationMapper.updateById(reg);
        }
        // 写提交审核日志
        auditLogMapper.insert(buildAuditLog(reg, "报名提交", "报名已提交，待管理员审核", "系统"));

        RegistrationVo vo = new RegistrationVo();
        vo.setRegistrationId(reg.getId());
        vo.setStatus(1);
        vo.setStatusText("待审核");
        vo.setAuditResult("待审核，等待管理员审核");
        vo.setRealName(member.getRealName());
        vo.setDeptName(member.getDeptName());
        return vo;
    }

    /**
     * 保存报名草稿
     */
    @Transactional(rollbackFor = Exception.class)
    public RegistrationVo saveDraft(Long memberIdFromToken, RegForm form) {
        WalkingMember member = resolveMember(memberIdFromToken, form.getPhone());
        WalkingActivity activity = activityService.getCurrentEntity();
        if (activity == null) {
            throw new ServiceException("当前无进行中的活动");
        }
        saveDraftInternal(member, activity, form, null);

        RegistrationVo vo = new RegistrationVo();
        vo.setStatus(0);
        vo.setStatusText("待提交草稿");
        return vo;
    }

    private void saveDraftInternal(WalkingMember member, WalkingActivity activity, RegForm form, String auditResult) {
        member.setRealName(form.getRealName());
        member.setDeptId(form.getDeptId());
        member.setDeptName(form.getDeptName());
        member.setPhone(form.getPhone());
        memberMapper.updateById(member);

        WalkingRegistration reg = registrationMapper.selectOne(
            new LambdaQueryWrapper<WalkingRegistration>()
                .eq(WalkingRegistration::getMemberId, member.getId())
                .eq(WalkingRegistration::getActivityId, activity.getId()));
        if (reg == null) {
            reg = new WalkingRegistration();
            reg.setMemberId(member.getId());
            reg.setActivityId(activity.getId());
        }
        reg.setStatus(0);
        reg.setAuditResult(auditResult);
        if (reg.getId() == null) {
            registrationMapper.insert(reg);
        } else {
            registrationMapper.updateById(reg);
        }
    }

    /**
     * 我的报名状态
     */
    public RegistrationVo getMine(Long memberId) {
        WalkingRegistration reg = registrationMapper.selectOne(
            new LambdaQueryWrapper<WalkingRegistration>()
                .eq(WalkingRegistration::getMemberId, memberId)
                .last("limit 1"));
        RegistrationVo vo = new RegistrationVo();
        if (reg == null) {
            vo.setStatus(null);
            vo.setStatusText("未报名");
            return vo;
        }
        vo.setRegistrationId(reg.getId());
        vo.setStatus(reg.getStatus());
        vo.setStatusText(statusText(reg.getStatus()));
        vo.setAuditResult(reg.getAuditResult());
        WalkingMember m = memberMapper.selectById(memberId);
        if (m != null) {
            vo.setRealName(m.getRealName());
            vo.setDeptName(m.getDeptName());
        }
        return vo;
    }

    /** 解析会员：优先 token，否则按手机号找/建（支持 H5 无 token） */
    private WalkingMember resolveMember(Long memberIdFromToken, String phone) {
        if (memberIdFromToken != null) {
            WalkingMember current = memberMapper.selectById(memberIdFromToken);
            if (current == null) {
                throw new ServiceException("会员不存在");
            }
            // 若手机号被其他会员占用 → 合并
            WalkingMember byPhone = memberMapper.selectOne(
                new LambdaQueryWrapper<WalkingMember>().eq(WalkingMember::getPhone, phone));
            if (byPhone != null && !byPhone.getId().equals(current.getId())) {
                if (StrUtil.isBlank(byPhone.getOpenid())) {
                    String openid = current.getOpenid();
                    String unionid = current.getUnionid();
                    // 强制清空临时会员 openid（updateById 默认跳过 null）
                    memberMapper.update(null, new LambdaUpdateWrapper<WalkingMember>()
                        .eq(WalkingMember::getId, current.getId())
                        .set(WalkingMember::getOpenid, null)
                        .set(WalkingMember::getUnionid, null));
                    byPhone.setOpenid(openid);
                    byPhone.setUnionid(unionid);
                    memberMapper.updateById(byPhone);
                    memberMapper.deleteById(current.getId());
                    return byPhone;
                }
                throw new ServiceException("该手机号已被其他账号报名");
            }
            return current;
        }
        // H5：按手机号找/建会员
        WalkingMember byPhone = memberMapper.selectOne(
            new LambdaQueryWrapper<WalkingMember>().eq(WalkingMember::getPhone, phone));
        if (byPhone == null) {
            byPhone = new WalkingMember();
            byPhone.setPhone(phone);
            byPhone.setStatus(0);
            byPhone.setRegisterTime(new Date());
            memberMapper.insert(byPhone);
        }
        return byPhone;
    }

    private void validate(RegForm form) {
        if (StrUtil.isBlank(form.getDeptName())) {
            throw new ServiceException("请选择所在单位");
        }
        if (StrUtil.isBlank(form.getRealName())) {
            throw new ServiceException("请输入姓名");
        }
        if (form.getPhone() == null || !ReUtil.isMatch("^1\\d{10}$", form.getPhone())) {
            throw new ServiceException("手机号格式不正确");
        }
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

    private WalkingAuditLog buildAuditLog(WalkingRegistration reg, String action, String result, String auditor) {
        WalkingAuditLog log = new WalkingAuditLog();
        log.setRegistrationId(reg.getId());
        log.setMemberId(reg.getMemberId());
        log.setAuditAction(action);
        log.setAuditResult(result);
        log.setAuditor(auditor);
        return log;
    }
}
