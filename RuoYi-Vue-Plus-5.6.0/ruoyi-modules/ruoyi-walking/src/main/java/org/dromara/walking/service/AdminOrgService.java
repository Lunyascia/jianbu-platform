package org.dromara.walking.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.walking.domain.bo.admin.OrgBo;
import org.dromara.walking.domain.bo.admin.OrgImportBo;
import org.dromara.walking.domain.vo.admin.OrgTreeVo;
import org.dromara.walking.mapper.WalkingOrgMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 组织机构管理（后台）服务 —— 复用 sys_dept
 */
@Service
public class AdminOrgService {

    @Autowired
    private WalkingOrgMapper orgMapper;

    /** 组织机构树（会员总数=本机构+所有子机构合计，动态统计） */
    public List<OrgTreeVo> tree() {
        List<OrgTreeVo> all = orgMapper.selectOrgTree();
        if (CollUtil.isEmpty(all)) {
            return new ArrayList<>();
        }
        Map<Long, OrgTreeVo> map = new HashMap<>();
        for (OrgTreeVo node : all) {
            map.put(node.getDeptId(), node);
        }
        List<OrgTreeVo> roots = new ArrayList<>();
        for (OrgTreeVo node : all) {
            OrgTreeVo parent = map.get(node.getParentId());
            if (parent != null) {
                parent.getChildren().add(node);
            } else {
                roots.add(node);
            }
        }
        // 后序遍历：把子机构会员数累加到父机构（含子机构汇总）
        for (OrgTreeVo root : roots) {
            fillMemberTotal(root);
        }
        return roots;
    }

    /** 子树会员总数后序遍历累加，返回本节点(含子节点)会员总数 */
    private long fillMemberTotal(OrgTreeVo node) {
        long sum = node.getMemberTotal() == null ? 0 : node.getMemberTotal();
        for (OrgTreeVo child : node.getChildren()) {
            sum += fillMemberTotal(child);
        }
        node.setMemberTotal((int) sum);
        return sum;
    }

    /** 机构详情 */
    public OrgTreeVo getById(Long deptId) {
        OrgTreeVo vo = orgMapper.selectOrgById(deptId);
        if (vo == null) {
            throw new ServiceException("机构不存在");
        }
        return vo;
    }

    /** 新增机构 */
    @Transactional(rollbackFor = Exception.class)
    public void add(OrgBo bo) {
        if (StrUtil.isBlank(bo.getDeptName())) {
            throw new ServiceException("机构名称不能为空");
        }
        Long parentId = bo.getParentId() == null ? 0L : bo.getParentId();
        String ancestors = resolveAncestors(parentId);
        Long deptId = IdWorker.getId();
        orgMapper.insertOrg(deptId, LoginHelper.getTenantId(), parentId, ancestors,
            bo.getDeptName().trim(), bo.getOrderNum() == null ? 0 : bo.getOrderNum(),
            bo.getLeader(), bo.getPhone(),
            LoginHelper.getDeptId(), LoginHelper.getUserId());
    }

    /** 修改机构 */
    @Transactional(rollbackFor = Exception.class)
    public void update(OrgBo bo) {
        if (bo.getDeptId() == null) {
            throw new ServiceException("机构id不能为空");
        }
        if (orgMapper.selectOrgById(bo.getDeptId()) == null) {
            throw new ServiceException("机构不存在");
        }
        orgMapper.updateOrg(bo.getDeptId(), bo.getDeptName().trim(),
            bo.getOrderNum() == null ? 0 : bo.getOrderNum(),
            bo.getLeader(), bo.getPhone(),
            LoginHelper.getUserId());
    }

    /** 删除机构 */
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> deptIds) {
        for (Long deptId : deptIds) {
            if (orgMapper.countChildren(deptId) > 0) {
                throw new ServiceException("存在子机构，不能删除");
            }
            if (orgMapper.countMembers(deptId) > 0) {
                throw new ServiceException("机构下存在会员，不能删除");
            }
            orgMapper.deleteOrg(deptId, LoginHelper.getUserId());
        }
    }

    /** 批量导入组织机构 */
    @Transactional(rollbackFor = Exception.class)
    public void importOrgs(List<OrgImportBo> list) {
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException("导入数据为空");
        }
        // 机构名(去空格) -> deptId，先加载现有机构，再并入本次新增
        Map<String, Long> nameMap = new HashMap<>();
        for (OrgTreeVo node : orgMapper.selectOrgTree()) {
            nameMap.put(normalize(node.getDeptName()), node.getDeptId());
        }
        int success = 0;
        for (int i = 0; i < list.size(); i++) {
            OrgImportBo row = list.get(i);
            if (StrUtil.isBlank(row.getDeptName())) {
                throw new ServiceException("第" + (i + 1) + "行机构名称不能为空");
            }
            String name = normalize(row.getDeptName());
            if (nameMap.containsKey(name)) {
                continue; // 已存在则跳过
            }
            Long parentId = 0L;
            if (StrUtil.isNotBlank(row.getParentName())) {
                Long p = nameMap.get(normalize(row.getParentName()));
                if (p == null) {
                    throw new ServiceException("第" + (i + 1) + "行上级机构【" + row.getParentName() + "】不存在，请先导入上级或核对名称");
                }
                parentId = p;
            }
            Long deptId = IdWorker.getId();
            orgMapper.insertOrg(deptId, LoginHelper.getTenantId(), parentId, resolveAncestors(parentId),
                row.getDeptName().trim(), row.getOrderNum() == null ? 0 : row.getOrderNum(),
                row.getLeader(), row.getPhone(),
                LoginHelper.getDeptId(), LoginHelper.getUserId());
            nameMap.put(name, deptId);
            success++;
        }
        if (success == 0) {
            throw new ServiceException("全部机构已存在，无需导入");
        }
    }

    private String resolveAncestors(Long parentId) {
        if (parentId == null || parentId == 0L) {
            return "0";
        }
        String parentAncestors = orgMapper.selectAncestors(parentId);
        if (StrUtil.isBlank(parentAncestors)) {
            throw new ServiceException("上级机构不存在");
        }
        return parentAncestors + "," + parentId;
    }

    private String normalize(String s) {
        return s == null ? "" : s.replaceAll("\\s+", "");
    }
}
