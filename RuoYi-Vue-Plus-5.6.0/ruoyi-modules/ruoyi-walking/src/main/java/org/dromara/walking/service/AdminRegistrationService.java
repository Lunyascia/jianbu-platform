package org.dromara.walking.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.walking.domain.WalkingAuditLog;
import org.dromara.walking.domain.WalkingMember;
import org.dromara.walking.domain.WalkingRegistration;
import org.dromara.walking.domain.vo.admin.RegistrationAdminVo;
import org.dromara.walking.mapper.WalkingAuditLogMapper;
import org.dromara.walking.mapper.WalkingMemberMapper;
import org.dromara.walking.mapper.WalkingRegistrationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 报名管理（后台）服务
 */
@Service
public class AdminRegistrationService {

    @Autowired
    private WalkingRegistrationMapper registrationMapper;
    @Autowired
    private WalkingMemberMapper memberMapper;
    @Autowired
    private WalkingAuditLogMapper auditLogMapper;

    /** 报名分页列表 */
    public TableDataInfo<RegistrationAdminVo> pageList(Long activityId, Integer status, Long deptId, String keyword, PageQuery pageQuery) {
        LambdaQueryWrapper<WalkingRegistration> lqw = new LambdaQueryWrapper<>();
        lqw.eq(activityId != null, WalkingRegistration::getActivityId, activityId);
        lqw.eq(status != null, WalkingRegistration::getStatus, status);
        lqw.in(status == null, WalkingRegistration::getStatus, 1, 2); // 默认只看已提交(1/2)
        if (StrUtil.isNotBlank(keyword)) {
            List<Long> memberIds = memberMapper.selectList(new LambdaQueryWrapper<WalkingMember>()
                    .and(w -> w.like(WalkingMember::getRealName, keyword)
                        .or().like(WalkingMember::getPhone, keyword)))
                .stream().map(WalkingMember::getId).toList();
            if (CollUtil.isEmpty(memberIds)) {
                return new TableDataInfo<>(List.of(), 0);
            }
            lqw.in(WalkingRegistration::getMemberId, memberIds);
        }
        lqw.orderByDesc(WalkingRegistration::getCreateTime);
        Page<WalkingRegistration> page = registrationMapper.selectPage(pageQuery.build(), lqw);

        Map<Long, WalkingMember> memberMap = loadMemberMap(page.getRecords());
        List<RegistrationAdminVo> rows = page.getRecords().stream()
            .map(reg -> toVo(reg, memberMap.get(reg.getMemberId())))
            .toList();
        return new TableDataInfo<>(rows, page.getTotal());
    }

    /** 报名详情 */
    public RegistrationAdminVo getById(Long id) {
        WalkingRegistration reg = registrationMapper.selectById(id);
        if (reg == null) {
            throw new ServiceException("报名记录不存在");
        }
        return toVo(reg, memberMapper.selectById(reg.getMemberId()));
    }

    /** 取消报名（审核通过 → 已取消） */
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        WalkingRegistration reg = getReg(id);
        if (!Integer.valueOf(2).equals(reg.getStatus())) {
            throw new ServiceException("仅审核通过的报名可取消");
        }
        reg.setStatus(3);
        reg.setCancelBy(LoginHelper.getUsername());
        reg.setCancelTime(new Date());
        reg.setAuditResult("管理员取消报名");
        registrationMapper.updateById(reg);
        saveAuditLog(reg, "取消", "管理员取消报名", LoginHelper.getUsername());
    }

    /** 撤下报名 + 停用会员账号（审核类操作） */
    @Transactional(rollbackFor = Exception.class)
    public void disable(Long id) {
        WalkingRegistration reg = getReg(id);
        if (Integer.valueOf(4).equals(reg.getStatus())) {
            throw new ServiceException("该报名已停用");
        }
        reg.setStatus(4);
        reg.setCancelBy(LoginHelper.getUsername());
        reg.setCancelTime(new Date());
        reg.setAuditResult("管理员撤下报名并停用账号");
        registrationMapper.updateById(reg);
        // 停用会员账号
        WalkingMember member = memberMapper.selectById(reg.getMemberId());
        if (member != null) {
            member.setStatus(1);
            memberMapper.updateById(member);
        }
        saveAuditLog(reg, "撤下/停用", "撤下报名信息并停用会员账号", LoginHelper.getUsername());
    }

    /** 审核通过（待审核 → 报名成功，之后才可绑定手机号） */
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id) {
        WalkingRegistration reg = getReg(id);
        if (!Integer.valueOf(1).equals(reg.getStatus())) {
            throw new ServiceException("仅待审核的报名可通过");
        }
        reg.setStatus(2);
        reg.setAuditResult("审核通过");
        reg.setAuditTime(new Date());
        registrationMapper.updateById(reg);
        saveAuditLog(reg, "审核通过", "管理员审核通过，可绑定手机号", LoginHelper.getUsername());
    }

    /** 调整单位 */
    @Transactional(rollbackFor = Exception.class)
    public void adjustUnit(Long id, Long deptId, String deptName) {
        if (deptId == null || StrUtil.isBlank(deptName)) {
            throw new ServiceException("请选择单位");
        }
        WalkingRegistration reg = getReg(id);
        WalkingMember member = memberMapper.selectById(reg.getMemberId());
        if (member == null) {
            throw new ServiceException("会员不存在");
        }
        member.setDeptId(deptId);
        member.setDeptName(deptName);
        memberMapper.updateById(member);
        saveAuditLog(reg, "调整单位", "单位调整为【" + deptName + "】", LoginHelper.getUsername());
    }

    /** 分单位导出报名信息 */
    public void export(Long activityId, Long deptId, HttpServletResponse response) {
        List<RegistrationAdminVo> list = registrationMapper.selectExportList(activityId, deptId);
        ExcelUtil.exportExcel(list, "报名人员信息", RegistrationAdminVo.class, response);
    }

    // ---------------- 私有方法 ----------------

    private WalkingRegistration getReg(Long id) {
        WalkingRegistration reg = registrationMapper.selectById(id);
        if (reg == null) {
            throw new ServiceException("报名记录不存在");
        }
        return reg;
    }

    private Map<Long, WalkingMember> loadMemberMap(List<WalkingRegistration> regs) {
        List<Long> memberIds = regs.stream().map(WalkingRegistration::getMemberId).distinct().toList();
        if (CollUtil.isEmpty(memberIds)) {
            return Map.of();
        }
        return memberMapper.selectByIds(memberIds).stream()
            .collect(Collectors.toMap(WalkingMember::getId, Function.identity()));
    }

    private RegistrationAdminVo toVo(WalkingRegistration reg, WalkingMember member) {
        RegistrationAdminVo vo = new RegistrationAdminVo();
        vo.setId(reg.getId());
        vo.setMemberId(reg.getMemberId());
        vo.setActivityId(reg.getActivityId());
        vo.setStatus(reg.getStatus());
        vo.setStatusText(statusText(reg.getStatus()));
        vo.setAuditResult(reg.getAuditResult());
        vo.setSubmitTime(reg.getSubmitTime());
        vo.setAuditTime(reg.getAuditTime());
        vo.setCancelBy(reg.getCancelBy());
        vo.setCancelTime(reg.getCancelTime());
        if (member != null) {
            vo.setRealName(member.getRealName());
            vo.setPhone(member.getPhone());
            vo.setDeptId(member.getDeptId());
            vo.setDeptName(member.getDeptName());
            vo.setIdCard(member.getIdCard());
            vo.setMemberStatus(member.getStatus());
        }
        return vo;
    }

    private void saveAuditLog(WalkingRegistration reg, String action, String result, String auditor) {
        WalkingAuditLog log = new WalkingAuditLog();
        log.setRegistrationId(reg.getId());
        log.setMemberId(reg.getMemberId());
        log.setAuditAction(action);
        log.setAuditResult(result);
        log.setAuditor(auditor);
        auditLogMapper.insert(log);
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
