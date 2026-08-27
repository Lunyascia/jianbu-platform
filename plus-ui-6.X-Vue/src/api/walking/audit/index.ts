import type { TableDataPromise } from '@/utils/api-types';
import request from '@/utils/request';
import type { AuditLogQuery, AuditLogVO } from './types';

// 审核日志分页列表
export function listAudit(query: AuditLogQuery): TableDataPromise<AuditLogVO> {
  return request({
    url: '/walking/admin/audit/list',
    method: 'get',
    params: query
  });
}
