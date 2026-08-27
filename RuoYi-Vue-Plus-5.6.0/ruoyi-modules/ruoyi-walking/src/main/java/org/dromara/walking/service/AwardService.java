package org.dromara.walking.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.walking.domain.WalkingActivity;
import org.dromara.walking.domain.WalkingAward;
import org.dromara.walking.domain.WalkingCertificate;
import org.dromara.walking.domain.WalkingMember;
import org.dromara.walking.domain.vo.AwardMineVo;
import org.dromara.walking.domain.vo.AwardTierVo;
import org.dromara.walking.mapper.WalkingAwardMapper;
import org.dromara.walking.mapper.WalkingCertificateMapper;
import org.dromara.walking.mapper.WalkingMemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 奖励/证书服务
 */
@Service
public class AwardService {

    @Autowired
    private WalkingCertificateMapper certificateMapper;
    @Autowired
    private WalkingMemberMapper memberMapper;
    @Autowired
    private WalkingAwardMapper awardMapper;
    @Autowired
    private ActivityService activityService;

    /** 当前活动启用的奖励档位（H5 报名页 / 小程序奖励页展示） */
    public List<AwardTierVo> listCurrentTiers() {
        WalkingActivity activity = activityService.getCurrentEntity();
        if (activity == null) {
            return List.of();
        }
        return awardMapper.selectList(
                new LambdaQueryWrapper<WalkingAward>()
                    .eq(WalkingAward::getActivityId, activity.getId())
                    .eq(WalkingAward::getStatus, 1)
                    .orderByAsc(WalkingAward::getSortOrder)
                    .orderByAsc(WalkingAward::getRankStart))
            .stream().map(this::toTierVo).toList();
    }

    private AwardTierVo toTierVo(WalkingAward a) {
        AwardTierVo vo = new AwardTierVo();
        vo.setId(a.getId());
        vo.setActivityId(a.getActivityId());
        vo.setAwardType(a.getAwardType());
        vo.setAwardName(a.getAwardName());
        vo.setRankStart(a.getRankStart());
        vo.setRankEnd(a.getRankEnd());
        vo.setPrizeContent(a.getPrizeContent());
        vo.setImageUrl(a.getImageUrl());
        vo.setStatus(a.getStatus());
        vo.setSortOrder(a.getSortOrder());
        return vo;
    }

    /** 我的获奖 */
    public AwardMineVo getMine(Long memberId) {
        WalkingActivity activity = activityService.getCurrentEntity();
        AwardMineVo vo = new AwardMineVo();
        vo.setHasAward(false);
        if (activity == null) {
            return vo;
        }
        WalkingCertificate cert = certificateMapper.selectOne(
            new LambdaQueryWrapper<WalkingCertificate>()
                .eq(WalkingCertificate::getMemberId, memberId)
                .eq(WalkingCertificate::getActivityId, activity.getId())
                .last("limit 1"));
        if (cert == null) {
            return vo;
        }
        WalkingMember m = memberMapper.selectById(memberId);
        vo.setHasAward(true);
        // 优先使用后台标记的奖项级别，否则按名次推导
        vo.setAwardLevel(StrUtil.isNotBlank(cert.getAwardLevel()) ? cert.getAwardLevel() : rankToLevel(cert.getRank()));
        vo.setRealName(m == null ? "" : m.getRealName());
        vo.setDeptName(m == null ? "" : m.getDeptName());
        vo.setActivityName(activity.getActivityName());
        vo.setIssueDate(cert.getIssueTime() == null ? "" : cn.hutool.core.date.DateUtil.formatDate(cert.getIssueTime()));
        if (m != null) {
            vo.setReceiver(m.getReceiver());
            vo.setPhone(m.getAddressPhone());
            vo.setAddress(m.getAddress());
        }
        return vo;
    }

    /** 保存收货地址 */
    @Transactional(rollbackFor = Exception.class)
    public void saveAddress(Long memberId, String receiver, String phone, String address) {
        if (StrUtil.isBlank(receiver) || StrUtil.isBlank(address)) {
            throw new ServiceException("收货人和详细地址不能为空");
        }
        WalkingMember m = memberMapper.selectById(memberId);
        if (m == null) {
            throw new ServiceException("会员不存在");
        }
        m.setReceiver(receiver);
        m.setAddressPhone(phone);
        m.setAddress(address);
        memberMapper.updateById(m);
    }

    private String rankToLevel(Integer rank) {
        if (rank == null) {
            return "优秀奖";
        }
        if (rank <= 5) {
            return "一等奖";
        } else if (rank <= 15) {
            return "二等奖";
        } else if (rank <= 35) {
            return "三等奖";
        } else {
            return "优秀奖";
        }
    }
}
