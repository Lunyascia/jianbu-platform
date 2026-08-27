package org.dromara.walking.service;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.walking.domain.WalkingMember;
import org.dromara.walking.domain.WalkingRegistration;
import org.dromara.walking.domain.WalkingStepRecord;
import org.dromara.walking.domain.bo.admin.MemberBo;
import org.dromara.walking.domain.vo.admin.MemberAdminVo;
import org.dromara.walking.domain.vo.admin.StepRecordAdminVo;
import org.dromara.walking.mapper.WalkingMemberMapper;
import org.dromara.walking.mapper.WalkingRegistrationMapper;
import org.dromara.walking.mapper.WalkingStepRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 会员管理（后台）服务
 */
@Service
public class AdminMemberService {

    @Autowired
    private WalkingMemberMapper memberMapper;
    @Autowired
    private WalkingRegistrationMapper registrationMapper;
    @Autowired
    private WalkingStepRecordMapper stepRecordMapper;

    /** 会员分页列表（展示所有已报名会员） */
    public TableDataInfo<MemberAdminVo> pageList(Long deptId, Integer status, String keyword, PageQuery pageQuery) {
        LambdaQueryWrapper<WalkingMember> lqw = new LambdaQueryWrapper<>();
        // 仅展示已报名会员（有效报名：待审核/审核通过）
        lqw.inSql(WalkingMember::getId,
            "select member_id from walking_registration where del_flag = 0 and status in (1,2)");
        lqw.eq(deptId != null, WalkingMember::getDeptId, deptId);
        lqw.eq(status != null, WalkingMember::getStatus, status);
        if (StrUtil.isNotBlank(keyword)) {
            lqw.and(w -> w.like(WalkingMember::getRealName, keyword)
                .or().like(WalkingMember::getPhone, keyword));
        }
        lqw.orderByDesc(WalkingMember::getRegisterTime);
        Page<WalkingMember> page = memberMapper.selectPage(pageQuery.build(), lqw);

        Map<Long, Long> regCountMap = selectRegCountMap();
        List<MemberAdminVo> rows = page.getRecords().stream()
            .map(m -> toVo(m, regCountMap.getOrDefault(m.getId(), 0L)))
            .toList();
        return new TableDataInfo<>(rows, page.getTotal());
    }

    /** 会员详情 */
    public MemberAdminVo getById(Long id) {
        WalkingMember m = memberMapper.selectById(id);
        if (m == null) {
            throw new ServiceException("会员不存在");
        }
        Long regCount = registrationMapper.selectCount(new LambdaQueryWrapper<WalkingRegistration>()
            .eq(WalkingRegistration::getMemberId, id)
            .in(WalkingRegistration::getStatus, 1, 2));
        return toVo(m, regCount);
    }

    /** 会员信息维护（调整单位/收货地址等） */
    @Transactional(rollbackFor = Exception.class)
    public void update(MemberBo bo) {
        WalkingMember m = memberMapper.selectById(bo.getId());
        if (m == null) {
            throw new ServiceException("会员不存在");
        }
        if (bo.getDeptId() != null) {
            m.setDeptId(bo.getDeptId());
        }
        if (StrUtil.isNotBlank(bo.getDeptName())) {
            m.setDeptName(bo.getDeptName());
        }
        if (StrUtil.isNotBlank(bo.getRealName())) {
            m.setRealName(bo.getRealName());
        }
        if (StrUtil.isNotBlank(bo.getIdCard())) {
            m.setIdCard(bo.getIdCard());
        }
        if (bo.getReceiver() != null) {
            m.setReceiver(bo.getReceiver());
        }
        if (bo.getAddressPhone() != null) {
            m.setAddressPhone(bo.getAddressPhone());
        }
        if (bo.getAddress() != null) {
            m.setAddress(bo.getAddress());
        }
        memberMapper.updateById(m);
    }

    /** 停用/启用账号（审核类操作） */
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new ServiceException("状态参数不正确");
        }
        WalkingMember m = memberMapper.selectById(id);
        if (m == null) {
            throw new ServiceException("会员不存在");
        }
        m.setStatus(status);
        memberMapper.updateById(m);
        // 停用账号时同步停用其未取消/未停用的报名
        if (status == 1) {
            registrationMapper.update(null, new LambdaUpdateWrapper<WalkingRegistration>()
                .eq(WalkingRegistration::getMemberId, id)
                .in(WalkingRegistration::getStatus, 1, 2)
                .set(WalkingRegistration::getStatus, 4)
                .set(WalkingRegistration::getCancelBy, LoginHelper.getUsername())
                .set(WalkingRegistration::getCancelTime, new java.util.Date())
                .set(WalkingRegistration::getAuditResult, "停用账号同步撤下报名"));
        }
    }

    /** 删除会员（后台，无业务数据方可删除） */
    @Transactional(rollbackFor = Exception.class)
    public void remove(List<Long> ids) {
        for (Long id : ids) {
            WalkingMember m = memberMapper.selectById(id);
            if (m == null) {
                continue;
            }
            // 有有效报名（非已取消）→ 拒绝
            Long regCnt = registrationMapper.selectCount(new LambdaQueryWrapper<WalkingRegistration>()
                .eq(WalkingRegistration::getMemberId, id)
                .ne(WalkingRegistration::getStatus, 3));
            if (regCnt > 0) {
                throw new ServiceException("会员【" + m.getRealName() + "】存在报名记录，请使用停用账号功能");
            }
            // 有打卡记录 → 拒绝
            Long stepCnt = stepRecordMapper.selectCount(new LambdaQueryWrapper<WalkingStepRecord>()
                .eq(WalkingStepRecord::getMemberId, id));
            if (stepCnt > 0) {
                throw new ServiceException("会员【" + m.getRealName() + "】存在打卡记录，请使用停用账号功能");
            }
            memberMapper.deleteById(id);
        }
    }

    /** 分单位导出打卡信息（系统管理员） */
    public void exportSteps(Long activityId, Long deptId, HttpServletResponse response) {
        List<StepRecordAdminVo> list = stepRecordMapper.selectExportList(activityId, deptId);
        ExcelUtil.exportExcel(list, "打卡信息", StepRecordAdminVo.class, response);
    }

    private Map<Long, Long> selectRegCountMap() {
        Map<Long, Long> map = new HashMap<>();
        for (Map<String, Object> row : registrationMapper.selectRegCountGroupByMember()) {
            map.put(Convert.toLong(row.get("memberId")), Convert.toLong(row.get("cnt")));
        }
        return map;
    }

    private MemberAdminVo toVo(WalkingMember m, Long regCount) {
        MemberAdminVo vo = new MemberAdminVo();
        vo.setId(m.getId());
        vo.setOpenid(m.getOpenid());
        vo.setLoggedIn(StrUtil.isNotBlank(m.getOpenid()) ? 1 : 0);
        vo.setPhone(m.getPhone());
        vo.setRealName(m.getRealName());
        vo.setIdCard(m.getIdCard());
        vo.setDeptId(m.getDeptId());
        vo.setDeptName(m.getDeptName());
        vo.setStatus(m.getStatus());
        vo.setIsVerified(m.getIsVerified());
        vo.setRegisterTime(m.getRegisterTime());
        vo.setReceiver(m.getReceiver());
        vo.setAddressPhone(m.getAddressPhone());
        vo.setAddress(m.getAddress());
        vo.setRegCount(regCount);
        return vo;
    }
}
