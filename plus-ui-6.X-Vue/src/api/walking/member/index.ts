import type { AxiosPromise, TableDataPromise } from '@/utils/api-types';
import request from '@/utils/request';
import type { MemberForm, MemberQuery, MemberVO } from './types';

// 会员分页列表
export function listMember(query: MemberQuery): TableDataPromise<MemberVO> {
  return request({
    url: '/walking/admin/member/list',
    method: 'get',
    params: query
  });
}

// 会员详情
export function getMember(id: string | number): AxiosPromise<MemberVO> {
  return request({
    url: '/walking/admin/member/' + id,
    method: 'get'
  });
}

// 删除会员（系统管理员，无报名/打卡数据方可删除）
export function delMember(ids: (string | number)[]) {
  return request({
    url: '/walking/admin/member/' + ids.join(','),
    method: 'delete'
  });
}

// 会员信息维护（调整单位/收货地址）
export function updateMember(data: MemberForm) {
  return request({
    url: '/walking/admin/member',
    method: 'put',
    data: data
  });
}

// 停用/启用账号（审核类操作）
export function changeMemberStatus(id: string | number, status: number) {
  return request({
    url: '/walking/admin/member/' + id + '/status',
    method: 'put',
    params: { status: status }
  });
}

// 分单位导出打卡信息（系统管理员）
export function exportMemberSteps(params: { activityId?: number | string; deptId?: number | string }) {
  return request({
    url: '/walking/admin/member/export/steps',
    method: 'post',
    data: params
  });
}
