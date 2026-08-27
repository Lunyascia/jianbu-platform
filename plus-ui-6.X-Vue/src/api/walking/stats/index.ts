import type { AxiosPromise } from '@/utils/api-types';
import request from '@/utils/request';
import type {
  AwardVO,
  CheckinDetailVO,
  DeptStatVO,
  PointsDetailVO,
  RankingStatVO,
  StatsOverviewVO,
  UnitEvalVO
} from './types';

// 总览统计
export function getOverview(activityId?: number | string): AxiosPromise<StatsOverviewVO> {
  return request({
    url: '/walking/admin/stats/overview',
    method: 'get',
    params: { activityId: activityId }
  });
}

// 单位统计
export function listDeptStats(activityId?: number | string): AxiosPromise<DeptStatVO[]> {
  return request({
    url: '/walking/admin/stats/dept',
    method: 'get',
    params: { activityId: activityId }
  });
}

// 先进组织单位评选参考
export function getUnitEval(activityId?: number | string): AxiosPromise<UnitEvalVO[]> {
  return request({
    url: '/walking/admin/stats/unit/eval',
    method: 'get',
    params: { activityId: activityId }
  });
}

// 排行统计（type: today当日/points积分/steps总步数，支持按单位过滤）
export function listRanking(activityId?: number | string, deptId?: number | string, type = 'steps', limit = 100): AxiosPromise<RankingStatVO[]> {
  return request({
    url: '/walking/admin/stats/ranking',
    method: 'get',
    params: { activityId: activityId, deptId: deptId, type: type, limit: limit }
  });
}

// 手动刷新排名统计（触发锁榜评估）
export function refreshStats(): AxiosPromise<{ message: string; lockedActivities: number; lockedRecords: number }> {
  return request({
    url: '/walking/admin/stats/refresh',
    method: 'post'
  });
}

// 个人打卡明细
export function getCheckinDetail(memberId: number | string, activityId?: number | string): AxiosPromise<CheckinDetailVO[]> {
  return request({
    url: '/walking/admin/stats/checkin/detail',
    method: 'get',
    params: { memberId: memberId, activityId: activityId }
  });
}

// 个人积分明细
export function getPointsDetail(memberId: number | string, activityId?: number | string): AxiosPromise<PointsDetailVO> {
  return request({
    url: '/walking/admin/stats/points/detail',
    method: 'get',
    params: { memberId: memberId, activityId: activityId }
  });
}

// 获奖名单
export function getAwardList(activityId?: number | string): AxiosPromise<AwardVO[]> {
  return request({
    url: '/walking/admin/stats/award/list',
    method: 'get',
    params: { activityId: activityId }
  });
}

// 分单位导出统计（系统管理员）
export function exportDeptStats(params: { activityId?: number | string }) {
  return request({
    url: '/walking/admin/stats/export/dept',
    method: 'post',
    data: params
  });
}

// 导出排行榜（全局或按单位，系统管理员）
export function exportRanking(params: { activityId?: number | string; deptId?: number | string }) {
  return request({
    url: '/walking/admin/stats/export/ranking',
    method: 'post',
    data: params
  });
}

// 一键导出获奖名单（系统管理员）
export function exportAwardList(params: { activityId?: number | string }) {
  return request({
    url: '/walking/admin/stats/export/awardList',
    method: 'post',
    data: params
  });
}

// 导出先进组织评选（系统管理员）
export function exportEval(params: { activityId?: number | string }) {
  return request({
    url: '/walking/admin/stats/export/eval',
    method: 'post',
    data: params
  });
}
