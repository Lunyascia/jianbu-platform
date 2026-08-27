import type { TableDataPromise } from '@/utils/api-types';
import request from '@/utils/request';
import type { AwardForm, AwardQuery, AwardWinnerVO } from './types';

// 中奖名单分页
export function listAward(query: AwardQuery): TableDataPromise<AwardWinnerVO> {
  return request({
    url: '/walking/admin/award/list',
    method: 'get',
    params: query
  });
}

// 标记/调整中奖名单
export function markAward(data: AwardForm) {
  return request({
    url: '/walking/admin/award/mark',
    method: 'post',
    data: data
  });
}

// 按排行自动生成中奖名单
export function autoMarkAward(activityId: number | string) {
  return request({
    url: '/walking/admin/award/autoMark',
    method: 'post',
    params: { activityId: activityId }
  });
}

// 删除名单
export function delAward(ids: string | number | Array<string | number>) {
  return request({
    url: '/walking/admin/award/' + ids,
    method: 'delete'
  });
}

// 导出中奖用户信息
export function exportAward(params: { activityId?: number | string }) {
  return request({
    url: '/walking/admin/award/export',
    method: 'post',
    data: params
  });
}
