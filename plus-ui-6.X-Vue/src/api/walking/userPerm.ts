import type { AxiosPromise } from '@/utils/api-types';
import request from '@/utils/request';

// 行走菜单权限树 + 已勾选（权限配置弹窗）
export function getUserPermTree(userId: string | number): AxiosPromise<{ menus: any[]; checkedKeys: (string | number)[] }> {
  return request({
    url: '/walking/admin/user-perm/' + userId,
    method: 'get'
  });
}

// 保存单个管理员的行走菜单权限
export function saveUserPerm(data: { userId: string | number; menuIds: (string | number)[] }) {
  return request({
    url: '/walking/admin/user-perm/save',
    method: 'post',
    data: data
  });
}

// 批量给多个管理员配置同一组行走菜单权限
export function batchUserPerm(data: { userIds: (string | number)[]; menuIds: (string | number)[] }) {
  return request({
    url: '/walking/admin/user-perm/batch',
    method: 'post',
    data: data
  });
}
