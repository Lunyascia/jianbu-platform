import type { AxiosPromise, TableDataPromise } from '@/utils/api-types';
import request from '@/utils/request';
import type { ActivityForm, ActivityQuery, ActivityVO } from './types';

// 活动分页列表
export function listActivity(query: ActivityQuery): TableDataPromise<ActivityVO> {
  return request({
    url: '/walking/admin/activity/list',
    method: 'get',
    params: query
  });
}

// 活动详情
export function getActivity(activityId: string | number): AxiosPromise<ActivityVO> {
  return request({
    url: '/walking/admin/activity/' + activityId,
    method: 'get'
  });
}

// 新增活动
export function addActivity(data: ActivityForm) {
  return request({
    url: '/walking/admin/activity',
    method: 'post',
    data: data
  });
}

// 修改活动
export function updateActivity(data: ActivityForm) {
  return request({
    url: '/walking/admin/activity',
    method: 'put',
    data: data
  });
}

// 活动参数配置（系统管理员）
export function configActivity(data: ActivityForm) {
  return request({
    url: '/walking/admin/activity/config',
    method: 'put',
    data: data
  });
}

// 删除活动
export function delActivity(activityId: string | number | Array<string | number>) {
  return request({
    url: '/walking/admin/activity/' + activityId,
    method: 'delete'
  });
}

// 活动下拉选项
export function listActivityOptions(): AxiosPromise<ActivityVO[]> {
  return request({
    url: '/walking/admin/activity/options',
    method: 'get'
  });
}
