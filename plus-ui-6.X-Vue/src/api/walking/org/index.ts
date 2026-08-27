import type { AxiosPromise } from '@/utils/api-types';
import request from '@/utils/request';
import type { OrgForm, OrgVO } from './types';

// 组织机构树
export function treeOrg(): AxiosPromise<OrgVO[]> {
  return request({
    url: '/walking/admin/org/tree',
    method: 'get'
  });
}

// 机构详情
export function getOrg(deptId: string | number): AxiosPromise<OrgVO> {
  return request({
    url: '/walking/admin/org/' + deptId,
    method: 'get'
  });
}

// 新增机构
export function addOrg(data: OrgForm) {
  return request({
    url: '/walking/admin/org',
    method: 'post',
    data: data
  });
}

// 修改机构
export function updateOrg(data: OrgForm) {
  return request({
    url: '/walking/admin/org',
    method: 'put',
    data: data
  });
}

// 删除机构
export function delOrg(deptIds: string | number | Array<string | number>) {
  return request({
    url: '/walking/admin/org/' + deptIds,
    method: 'delete'
  });
}

// 批量导入机构
export function importOrg(data: FormData) {
  return request({
    url: '/walking/admin/org/import',
    method: 'post',
    data: data
  });
}
