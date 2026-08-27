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
import org.dromara.walking.domain.WalkingCertificate;
import org.dromara.walking.domain.WalkingMember;
import org.dromara.walking.domain.bo.admin.AwardBo;
import org.dromara.walking.domain.vo.admin.AwardExportVo;
import org.dromara.walking.domain.vo.admin.AwardWinnerVo;
import org.dromara.walking.mapper.WalkingCertificateMapper;
import org.dromara.walking.mapper.WalkingMemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 中奖名单管理（后台）服务
 */
@Service
public class AdminAwardService {

    @Autowired
    private WalkingCertificateMapper certificateMapper;
    @Autowired
    private WalkingMemberMapper memberMapper;
    @Autowired
    private AdminStatsService statsService;

    /** 中奖名单分页 */
    public TableDataInfo<AwardWinnerVo> pageList(Long activityId, String keyword, PageQuery pageQuery) {
        LambdaQueryWrapper<WalkingCertificate> lqw = new LambdaQueryWrapper<>();
        lqw.eq(activityId != null, WalkingCertificate::getActivityId, activityId);
        if (StrUtil.isNotBlank(keyword)) {
            List<Long> memberIds = memberMapper.selectList(new LambdaQueryWrapper<WalkingMember>()
                    .and(w -> w.like(WalkingMember::getRealName, keyword)
                        .or().like(WalkingMember::getPhone, keyword)))
                .stream().map(WalkingMember::getId).toList();
            if (CollUtil.isEmpty(memberIds)) {
                return new TableDataInfo<>(List.of(), 0);
            }
            lqw.in(WalkingCertificate::getMemberId, memberIds);
        }
        lqw.orderByAsc(WalkingCertificate::getRank).orderByDesc(WalkingCertificate::getCreateTime);
        Page<WalkingCertificate> page = certificateMapper.selectPage(pageQuery.build(), lqw);

        Map<Long, WalkingMember> memberMap = loadMemberMap(page.getRecords());
        List<AwardWinnerVo> rows = page.getRecords().stream()
            .map(c -> toVo(c, memberMap.get(c.getMemberId())))
            .toList();
        return new TableDataInfo<>(rows, page.getTotal());
    }

    /** 标记/调整中奖名单 */
    @Transactional(rollbackFor = Exception.class)
    public void mark(AwardBo bo) {
        WalkingCertificate cert = certificateMapper.selectOne(new LambdaQueryWrapper<WalkingCertificate>()
            .eq(WalkingCertificate::getMemberId, bo.getMemberId())
            .eq(WalkingCertificate::getActivityId, bo.getActivityId())
            .last("limit 1"));
        if (cert == null) {
            cert = new WalkingCertificate();
            cert.setMemberId(bo.getMemberId());
            cert.setActivityId(bo.getActivityId());
            cert.setAwardLevel(bo.getAwardLevel());
            cert.setRank(bo.getRank());
            cert.setCertTitle(bo.getAwardLevel());
            cert.setIssueTime(new Date());
            certificateMapper.insert(cert);
        } else {
            cert.setAwardLevel(bo.getAwardLevel());
            cert.setCertTitle(bo.getAwardLevel());
            if (bo.getRank() != null) {
                cert.setRank(bo.getRank());
            }
            cert.setIssueTime(new Date());
            certificateMapper.updateById(cert);
        }
    }

    /** 批量从排行自动生成中奖名单（按奖项档位/名次，覆盖原名单） */
    @Transactional(rollbackFor = Exception.class)
    public void autoMark(Long activityId) {
        certificateMapper.delete(new LambdaQueryWrapper<WalkingCertificate>()
            .eq(WalkingCertificate::getActivityId, activityId));
        List<AwardExportVo> list = statsService.awardList(activityId);
        for (AwardExportVo vo : list) {
            WalkingCertificate cert = new WalkingCertificate();
            cert.setMemberId(vo.getMemberId());
            cert.setActivityId(activityId);
            cert.setRank(vo.getRank());
            cert.setAwardLevel(vo.getAwardLevel());
            cert.setCertTitle(vo.getAwardLevel());
            cert.setIssueTime(new Date());
            certificateMapper.insert(cert);
        }
    }

    /** 删除名单 */
    @Transactional(rollbackFor = Exception.class)
    public void remove(List<Long> ids) {
        certificateMapper.deleteByIds(ids);
    }

    /** 导出中奖用户信息 */
    public void export(Long activityId, HttpServletResponse response) {
        List<WalkingCertificate> certs = certificateMapper.selectList(new LambdaQueryWrapper<WalkingCertificate>()
            .eq(activityId != null, WalkingCertificate::getActivityId, activityId)
            .orderByAsc(WalkingCertificate::getRank));
        Map<Long, WalkingMember> memberMap = loadMemberMap(certs);
        List<AwardWinnerVo> list = certs.stream()
            .map(c -> toVo(c, memberMap.get(c.getMemberId())))
            .toList();
        ExcelUtil.exportExcel(list, "中奖名单", AwardWinnerVo.class, response);
    }

    // ---------------- 私有 ----------------

    private Map<Long, WalkingMember> loadMemberMap(List<WalkingCertificate> certs) {
        List<Long> memberIds = certs.stream().map(WalkingCertificate::getMemberId).distinct().toList();
        if (CollUtil.isEmpty(memberIds)) {
            return Map.of();
        }
        return memberMapper.selectByIds(memberIds).stream()
            .collect(Collectors.toMap(WalkingMember::getId, Function.identity()));
    }

    private AwardWinnerVo toVo(WalkingCertificate c, WalkingMember m) {
        AwardWinnerVo vo = new AwardWinnerVo();
        vo.setId(c.getId());
        vo.setMemberId(c.getMemberId());
        vo.setActivityId(c.getActivityId());
        vo.setAwardLevel(c.getAwardLevel());
        vo.setRank(c.getRank());
        vo.setIssueTime(c.getIssueTime());
        if (m != null) {
            vo.setRealName(m.getRealName());
            vo.setPhone(m.getPhone());
            vo.setDeptName(m.getDeptName());
            vo.setReceiver(m.getReceiver());
            vo.setAddressPhone(m.getAddressPhone());
            vo.setAddress(m.getAddress());
            vo.setMemberStatus(m.getStatus());
        }
        return vo;
    }
}
