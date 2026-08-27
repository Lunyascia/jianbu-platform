import type { AxiosPromise, TableDataPromise } from '@/utils/api-types';
import request from '@/utils/request';
import type { RegistrationQuery, RegistrationVO } from './types';

// 报名分页列表
export function listRegistration(query: RegistrationQuery): TableDataPromise<RegistrationVO> {
  return request({
    url: '/walking/admin/registration/list',
    method: 'get',
    params: query
  });
}

// 报名详情
export function getRegistration(id: string | number): AxiosPromise<RegistrationVO> {
  return request({
    url: '/walking/admin/registration/' + id,
    method: 'get'
  });
}

// 取消报名
export function cancelRegistration(id: string | number) {
  return request({
    url: '/walking/admin/registration/' + id + '/cancel',
    method: 'put'
  });
}

// 撤下报名信息 + 停用会员账号（审核类操作）
export function disableRegistration(id: string | number) {
  return request({
    url: '/walking/admin/registration/' + id + '/disable',
    method: 'put'
  });
}

// 审核通过（待审核 → 报名成功，通过后才可绑定手机号）
export function approveRegistration(id: string | number) {
  return request({
    url: '/walking/admin/registration/' + id + '/approve',
    method: 'put'
  });
}

// 调整单位
export function adjustRegistration(id: string | number, deptId: number | string, deptName: string) {
  return request({
    url: '/walking/admin/registration/' + id + '/adjust',
    method: 'put',
    params: { deptId: deptId, deptName: deptName }
  });
}

// 分单位导出报名信息（系统管理员）
export function exportRegistration(params: { activityId?: number | string; deptId?: number | string }) {
  return request({
    url: '/walking/admin/registration/export',
    method: 'post',
    data: params
  });
}
