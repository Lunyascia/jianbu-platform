import type { TableDataPromise } from '@/utils/api-types';
import request from '@/utils/request';
import type { CheatForm, CheatLogQuery, CheatLogVO, CheatQuery, CheatRecordVO } from './types';

// 异常步数数据分页列表
export function listCheat(query: CheatQuery): TableDataPromise<CheatRecordVO> {
  return request({
    url: '/walking/admin/cheat/list',
    method: 'get',
    params: query
  });
}

// 标记异常
export function markCheat(data: CheatForm) {
  return request({
    url: '/walking/admin/cheat/mark',
    method: 'put',
    data: data
  });
}

// 删除异常数据
export function deleteCheat(data: CheatForm) {
  return request({
    url: '/walking/admin/cheat/delete',
    method: 'post',
    data: data
  });
}

// 恢复异常（取消标记，误标数据放回正常统计/排行）
export function unmarkCheat(data: CheatForm) {
  return request({
    url: '/walking/admin/cheat/unmark',
    method: 'post',
    data: data
  });
}

// 批量处理作弊账号（系统管理员）
export function batchCheat(data: CheatForm) {
  return request({
    url: '/walking/admin/cheat/batch',
    method: 'post',
    data: data
  });
}

// 作弊处理日志
export function listCheatLogs(query: CheatLogQuery): TableDataPromise<CheatLogVO> {
  return request({
    url: '/walking/admin/cheat/logs',
    method: 'get',
    params: query
  });
}
