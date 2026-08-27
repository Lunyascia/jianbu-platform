package org.dromara.walking.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.walking.domain.WalkingCheatLog;
import org.dromara.walking.domain.WalkingMember;
import org.dromara.walking.domain.WalkingRegistration;
import org.dromara.walking.domain.WalkingStepRecord;
import org.dromara.walking.domain.bo.admin.CheatBatchBo;
import org.dromara.walking.domain.vo.admin.CheatLogVo;
import org.dromara.walking.domain.vo.admin.CheatRecordVo;
import org.dromara.walking.mapper.WalkingCheatLogMapper;
import org.dromara.walking.mapper.WalkingMemberMapper;
import org.dromara.walking.mapper.WalkingRegistrationMapper;
import org.dromara.walking.mapper.WalkingStepRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 异常数据处理（后台）服务 —— 作弊检测与处理
 */
@Service
public class AdminCheatService {

    @Autowired
    private WalkingStepRecordMapper stepRecordMapper;
    @Autowired
    private WalkingMemberMapper memberMapper;
    @Autowired
    private WalkingRegistrationMapper registrationMapper;
    @Autowired
    private WalkingCheatLogMapper cheatLogMapper;

    /** 异常步数数据分页列表 */
    public TableDataInfo<CheatRecordVo> pageList(Long activityId, Integer abnormalFlag, String keyword, PageQuery pageQuery) {
        LambdaQueryWrapper<WalkingStepRecord> lqw = new LambdaQueryWrapper<>();
        lqw.eq(activityId != null, WalkingStepRecord::getActivityId, activityId);
        lqw.eq(abnormalFlag != null, WalkingStepRecord::getAbnormalFlag, abnormalFlag);
        if (StrUtil.isNotBlank(keyword)) {
            List<Long> memberIds = memberMapper.selectList(new LambdaQueryWrapper<WalkingMember>()
                    .and(w -> w.like(WalkingMember::getRealName, keyword)
                        .or().like(WalkingMember::getPhone, keyword)))
                .stream().map(WalkingMember::getId).toList();
            if (CollUtil.isEmpty(memberIds)) {
                return new TableDataInfo<>(List.of(), 0);
            }
            lqw.in(WalkingStepRecord::getMemberId, memberIds);
        }
        lqw.orderByDesc(WalkingStepRecord::getRecordDate).orderByAsc(WalkingStepRecord::getMemberId);
        Page<WalkingStepRecord> page = stepRecordMapper.selectPage(pageQuery.build(), lqw);

        Map<Long, WalkingMember> memberMap = loadMemberMap(page.getRecords());
        List<CheatRecordVo> rows = page.getRecords().stream()
            .map(r -> toRecordVo(r, memberMap.get(r.getMemberId())))
            .toList();
        return new TableDataInfo<>(rows, page.getTotal());
    }

    /** 标记异常（指定步数记录 或 指定会员在活动内全部记录） */
    @Transactional(rollbackFor = Exception.class)
    public void mark(CheatBatchBo bo) {
        List<Long> recordIds = resolveRecordIds(bo);
        if (CollUtil.isEmpty(recordIds)) {
            throw new ServiceException("未找到可标记的步数记录");
        }
        List<WalkingStepRecord> records = stepRecordMapper.selectByIds(recordIds);
        for (WalkingStepRecord r : records) {
            r.setAbnormalFlag(1);
            stepRecordMapper.updateById(r);
        }
        writeCheatLogs(bo, records, 1, "标记异常");
    }

    /** 删除异常数据 */
    @Transactional(rollbackFor = Exception.class)
    public void delete(CheatBatchBo bo) {
        List<Long> recordIds = resolveRecordIds(bo);
        if (CollUtil.isEmpty(recordIds)) {
            throw new ServiceException("未找到可删除的步数记录");
        }
        List<WalkingStepRecord> records = stepRecordMapper.selectByIds(recordIds);
        stepRecordMapper.deleteByIds(recordIds);
        writeCheatLogs(bo, records, 2, "删除异常数据");
    }

    /** 恢复异常（取消标记，误标数据放回正常统计/排行） */
    @Transactional(rollbackFor = Exception.class)
    public void unmark(CheatBatchBo bo) {
        List<Long> recordIds = resolveRecordIds(bo);
        if (CollUtil.isEmpty(recordIds)) {
            throw new ServiceException("未找到可恢复的步数记录");
        }
        List<WalkingStepRecord> records = stepRecordMapper.selectByIds(recordIds);
        for (WalkingStepRecord r : records) {
            if (Integer.valueOf(1).equals(r.getAbnormalFlag())) {
                r.setAbnormalFlag(0);
                stepRecordMapper.updateById(r);
            }
        }
        writeCheatLogs(bo, records, 5, "恢复异常数据");
    }

    /** 批量处理作弊账号：1标记 2删数据 3停用 4取消报名 */
    @Transactional(rollbackFor = Exception.class)
    public void batchHandle(CheatBatchBo bo) {
        if (CollUtil.isEmpty(bo.getMemberIds()) && CollUtil.isEmpty(bo.getRecordIds())) {
            throw new ServiceException("请选择要处理的会员或数据");
        }
        int handleType = bo.getHandleType() == null ? 0 : bo.getHandleType();
        switch (handleType) {
            case 1 -> mark(bo);
            case 2 -> delete(bo);
            case 3 -> disableMembers(bo);
            case 4 -> cancelRegistrations(bo);
            case 5 -> unmark(bo);
            default -> throw new ServiceException("处理方式不正确");
        }
    }

    /** 作弊处理日志分页 */
    public TableDataInfo<CheatLogVo> logPage(Long activityId, String keyword, PageQuery pageQuery) {
        LambdaQueryWrapper<WalkingCheatLog> lqw = new LambdaQueryWrapper<>();
        lqw.eq(activityId != null, WalkingCheatLog::getActivityId, activityId);
        if (StrUtil.isNotBlank(keyword)) {
            List<Long> memberIds = memberMapper.selectList(new LambdaQueryWrapper<WalkingMember>()
                    .and(w -> w.like(WalkingMember::getRealName, keyword)
                        .or().like(WalkingMember::getPhone, keyword)))
                .stream().map(WalkingMember::getId).toList();
            if (CollUtil.isEmpty(memberIds)) {
                return new TableDataInfo<>(List.of(), 0);
            }
            lqw.in(WalkingCheatLog::getMemberId, memberIds);
        }
        lqw.orderByDesc(WalkingCheatLog::getCreateTime);
        Page<WalkingCheatLog> page = cheatLogMapper.selectPage(pageQuery.build(), lqw);

        Map<Long, WalkingMember> memberMap = loadMemberMap2(page.getRecords());
        List<CheatLogVo> rows = page.getRecords().stream()
            .map(l -> toLogVo(l, memberMap.get(l.getMemberId())))
            .toList();
        return new TableDataInfo<>(rows, page.getTotal());
    }

    // ---------------- 私有方法 ----------------

    /** 解析待处理的步数记录id：优先 recordIds，否则按 memberIds+activityId 查询 */
    private List<Long> resolveRecordIds(CheatBatchBo bo) {
        if (CollUtil.isNotEmpty(bo.getRecordIds())) {
            return bo.getRecordIds();
        }
        if (CollUtil.isNotEmpty(bo.getMemberIds()) && bo.getActivityId() != null) {
            return stepRecordMapper.selectList(new LambdaQueryWrapper<WalkingStepRecord>()
                    .eq(WalkingStepRecord::getActivityId, bo.getActivityId())
                    .in(WalkingStepRecord::getMemberId, bo.getMemberIds()))
                .stream().map(WalkingStepRecord::getId).toList();
        }
        return List.of();
    }

    /** 停用会员账号并撤下其活动内报名 */
    @Transactional(rollbackFor = Exception.class)
    public void disableMembers(CheatBatchBo bo) {
        for (Long memberId : bo.getMemberIds()) {
            WalkingMember m = memberMapper.selectById(memberId);
            if (m == null) {
                continue;
            }
            m.setStatus(1);
            memberMapper.updateById(m);
            if (bo.getActivityId() != null) {
                registrationMapper.update(null, new LambdaUpdateWrapper<WalkingRegistration>()
                    .eq(WalkingRegistration::getMemberId, memberId)
                    .eq(WalkingRegistration::getActivityId, bo.getActivityId())
                    .in(WalkingRegistration::getStatus, 1, 2)
                    .set(WalkingRegistration::getStatus, 4)
                    .set(WalkingRegistration::getCancelBy, LoginHelper.getUsername())
                    .set(WalkingRegistration::getCancelTime, new Date())
                    .set(WalkingRegistration::getAuditResult, "批量处理作弊停用账号"));
            }
            writeSingleCheatLog(memberId, bo.getActivityId(), 3, "停用账号");
        }
    }

    /** 取消会员在活动内的报名 */
    @Transactional(rollbackFor = Exception.class)
    public void cancelRegistrations(CheatBatchBo bo) {
        if (bo.getActivityId() == null) {
            throw new ServiceException("取消报名需指定活动");
        }
        for (Long memberId : bo.getMemberIds()) {
            registrationMapper.update(null, new LambdaUpdateWrapper<WalkingRegistration>()
                .eq(WalkingRegistration::getMemberId, memberId)
                .eq(WalkingRegistration::getActivityId, bo.getActivityId())
                .eq(WalkingRegistration::getStatus, 2)
                .set(WalkingRegistration::getStatus, 3)
                .set(WalkingRegistration::getCancelBy, LoginHelper.getUsername())
                .set(WalkingRegistration::getCancelTime, new Date())
                .set(WalkingRegistration::getAuditResult, "批量处理作弊取消报名"));
            writeSingleCheatLog(memberId, bo.getActivityId(), 4, "取消报名");
        }
    }

    private void writeCheatLogs(CheatBatchBo bo, List<WalkingStepRecord> records, int handleType, String action) {
        Map<Long, Long> memberActivity = records.stream()
            .collect(Collectors.toMap(WalkingStepRecord::getMemberId, WalkingStepRecord::getActivityId,
                (a, b) -> a, java.util.LinkedHashMap::new));
        memberActivity.forEach((memberId, activityId) ->
            writeSingleCheatLog(memberId, activityId, handleType, action + (StrUtil.isBlank(bo.getRemark()) ? "" : "：" + bo.getRemark())));
    }

    private void writeSingleCheatLog(Long memberId, Long activityId, int handleType, String remark) {
        WalkingCheatLog log = new WalkingCheatLog();
        log.setMemberId(memberId);
        log.setActivityId(activityId);
        log.setRecordDate(new Date());
        log.setAbnormalType("作弊/异常");
        log.setHandleType(handleType);
        log.setOperator(LoginHelper.getUsername());
        log.setRemark(remark);
        cheatLogMapper.insert(log);
    }

    private Map<Long, WalkingMember> loadMemberMap(List<WalkingStepRecord> records) {
        List<Long> memberIds = records.stream().map(WalkingStepRecord::getMemberId).distinct().toList();
        if (CollUtil.isEmpty(memberIds)) {
            return Map.of();
        }
        return memberMapper.selectByIds(memberIds).stream()
            .collect(Collectors.toMap(WalkingMember::getId, Function.identity()));
    }

    private Map<Long, WalkingMember> loadMemberMap2(List<WalkingCheatLog> logs) {
        List<Long> memberIds = logs.stream().map(WalkingCheatLog::getMemberId).distinct().toList();
        if (CollUtil.isEmpty(memberIds)) {
            return Map.of();
        }
        return memberMapper.selectByIds(memberIds).stream()
            .collect(Collectors.toMap(WalkingMember::getId, Function.identity()));
    }

    private CheatRecordVo toRecordVo(WalkingStepRecord r, WalkingMember m) {
        CheatRecordVo vo = new CheatRecordVo();
        vo.setId(r.getId());
        vo.setMemberId(r.getMemberId());
        vo.setActivityId(r.getActivityId());
        vo.setRecordDate(r.getRecordDate());
        vo.setSteps(r.getSteps());
        vo.setSource(r.getSource());
        vo.setAbnormalFlag(r.getAbnormalFlag());
        vo.setLocked(r.getLocked());
        if (m != null) {
            vo.setRealName(m.getRealName());
            vo.setPhone(m.getPhone());
            vo.setDeptName(m.getDeptName());
        }
        return vo;
    }

    private CheatLogVo toLogVo(WalkingCheatLog l, WalkingMember m) {
        CheatLogVo vo = new CheatLogVo();
        vo.setId(l.getId());
        vo.setMemberId(l.getMemberId());
        vo.setActivityId(l.getActivityId());
        vo.setRecordDate(l.getRecordDate());
        vo.setAbnormalType(l.getAbnormalType());
        vo.setHandleType(l.getHandleType());
        vo.setHandleTypeText(handleTypeText(l.getHandleType()));
        vo.setOperator(l.getOperator());
        vo.setRemark(l.getRemark());
        vo.setCreateTime(l.getCreateTime());
        if (m != null) {
            vo.setRealName(m.getRealName());
            vo.setPhone(m.getPhone());
        }
        return vo;
    }

    private String handleTypeText(Integer t) {
        return switch (t == null ? -1 : t) {
            case 1 -> "标记异常";
            case 2 -> "删除数据";
            case 3 -> "停用账号";
            case 4 -> "取消报名";
            case 5 -> "恢复异常";
            default -> "";
        };
    }
}
