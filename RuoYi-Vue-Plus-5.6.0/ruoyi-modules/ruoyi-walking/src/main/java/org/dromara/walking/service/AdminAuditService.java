package org.dromara.walking.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.walking.domain.WalkingAuditLog;
import org.dromara.walking.domain.WalkingMember;
import org.dromara.walking.domain.vo.admin.AuditLogVo;
import org.dromara.walking.mapper.WalkingAuditLogMapper;
import org.dromara.walking.mapper.WalkingMemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 报名审核日志（后台）服务
 */
@Service
public class AdminAuditService {

    @Autowired
    private WalkingAuditLogMapper auditLogMapper;
    @Autowired
    private WalkingMemberMapper memberMapper;

    /** 审核日志分页 */
    public TableDataInfo<AuditLogVo> pageList(Long registrationId, Long memberId, String auditAction, String keyword, PageQuery pageQuery) {
        LambdaQueryWrapper<WalkingAuditLog> lqw = new LambdaQueryWrapper<>();
        lqw.eq(registrationId != null, WalkingAuditLog::getRegistrationId, registrationId);
        lqw.eq(memberId != null, WalkingAuditLog::getMemberId, memberId);
        lqw.eq(StrUtil.isNotBlank(auditAction), WalkingAuditLog::getAuditAction, auditAction);
        if (StrUtil.isNotBlank(keyword)) {
            List<Long> memberIds = memberMapper.selectList(new LambdaQueryWrapper<WalkingMember>()
                    .and(w -> w.like(WalkingMember::getRealName, keyword)
                        .or().like(WalkingMember::getPhone, keyword)))
                .stream().map(WalkingMember::getId).toList();
            if (CollUtil.isEmpty(memberIds)) {
                return new TableDataInfo<>(List.of(), 0);
            }
            lqw.in(WalkingAuditLog::getMemberId, memberIds);
        }
        lqw.orderByDesc(WalkingAuditLog::getCreateTime);
        Page<WalkingAuditLog> page = auditLogMapper.selectPage(pageQuery.build(), lqw);

        List<Long> memberIds = page.getRecords().stream()
            .map(WalkingAuditLog::getMemberId).distinct().toList();
        Map<Long, WalkingMember> memberMap = CollUtil.isEmpty(memberIds)
            ? Map.of()
            : memberMapper.selectByIds(memberIds).stream()
                .collect(Collectors.toMap(WalkingMember::getId, Function.identity()));
        List<AuditLogVo> rows = page.getRecords().stream()
            .map(l -> toVo(l, memberMap.get(l.getMemberId())))
            .toList();
        return new TableDataInfo<>(rows, page.getTotal());
    }

    private AuditLogVo toVo(WalkingAuditLog l, WalkingMember m) {
        AuditLogVo vo = new AuditLogVo();
        vo.setId(l.getId());
        vo.setRegistrationId(l.getRegistrationId());
        vo.setMemberId(l.getMemberId());
        vo.setAuditAction(l.getAuditAction());
        vo.setAuditResult(l.getAuditResult());
        vo.setAuditor(l.getAuditor());
        vo.setCreateTime(l.getCreateTime());
        if (m != null) {
            vo.setRealName(m.getRealName());
            vo.setPhone(m.getPhone());
        }
        return vo;
    }
}
