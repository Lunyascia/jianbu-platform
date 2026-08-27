package org.dromara.walking.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.walking.domain.WalkingActivity;
import org.dromara.walking.domain.WalkingAward;
import org.dromara.walking.domain.bo.admin.AwardConfigBo;
import org.dromara.walking.domain.vo.AwardTierVo;
import org.dromara.walking.mapper.WalkingActivityMapper;
import org.dromara.walking.mapper.WalkingAwardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 奖励档位管理（后台）服务
 */
@Service
public class AdminAwardConfigService {

    @Autowired
    private WalkingAwardMapper awardMapper;
    @Autowired
    private WalkingActivityMapper activityMapper;

    /** 奖励档位分页 */
    public TableDataInfo<AwardTierVo> pageList(Long activityId, String awardName, PageQuery pageQuery) {
        LambdaQueryWrapper<WalkingAward> lqw = new LambdaQueryWrapper<>();
        lqw.eq(activityId != null, WalkingAward::getActivityId, activityId);
        lqw.like(StrUtil.isNotBlank(awardName), WalkingAward::getAwardName, awardName);
        lqw.orderByAsc(WalkingAward::getActivityId)
            .orderByAsc(WalkingAward::getSortOrder)
            .orderByAsc(WalkingAward::getRankStart);
        Page<WalkingAward> page = awardMapper.selectPage(pageQuery.build(), lqw);
        List<AwardTierVo> rows = page.getRecords().stream().map(this::toVo).toList();
        return new TableDataInfo<>(rows, page.getTotal());
    }

    /** 奖励档位详情 */
    public AwardTierVo getById(Long id) {
        WalkingAward a = awardMapper.selectById(id);
        if (a == null) {
            throw new ServiceException("奖励档位不存在");
        }
        return toVo(a);
    }

    /** 新增奖励档位 */
    @Transactional(rollbackFor = Exception.class)
    public void insert(AwardConfigBo bo) {
        WalkingActivity act = activityMapper.selectById(bo.getActivityId());
        if (act == null) {
            throw new ServiceException("所属活动不存在");
        }
        WalkingAward a = new WalkingAward();
        applyBo(a, bo);
        if (a.getStatus() == null) {
            a.setStatus(1);
        }
        if (a.getAwardType() == null) {
            a.setAwardType(1);
        }
        if (a.getSortOrder() == null) {
            a.setSortOrder(0);
        }
        awardMapper.insert(a);
    }

    /** 修改奖励档位 */
    @Transactional(rollbackFor = Exception.class)
    public void update(AwardConfigBo bo) {
        WalkingAward a = awardMapper.selectById(bo.getId());
        if (a == null) {
            throw new ServiceException("奖励档位不存在");
        }
        applyBo(a, bo);
        awardMapper.updateById(a);
    }

    /** 删除奖励档位 */
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {
        awardMapper.deleteByIds(ids);
    }

    private void applyBo(WalkingAward a, AwardConfigBo bo) {
        a.setActivityId(bo.getActivityId());
        a.setAwardType(bo.getAwardType());
        a.setAwardName(bo.getAwardName());
        a.setRankStart(bo.getRankStart());
        a.setRankEnd(bo.getRankEnd());
        a.setPrizeContent(bo.getPrizeContent());
        a.setImageUrl(bo.getImageUrl());
        a.setStatus(bo.getStatus());
        a.setSortOrder(bo.getSortOrder());
    }

    private AwardTierVo toVo(WalkingAward a) {
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
}
