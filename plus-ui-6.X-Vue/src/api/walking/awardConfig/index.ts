import type { AxiosPromise, TableDataPromise } from '@/utils/api-types';
import request from '@/utils/request';
import type { AwardConfigForm, AwardConfigQuery, AwardTierVO } from './types';

// 奖励档位分页
export function listAwardConfig(query: AwardConfigQuery): TableDataPromise<AwardTierVO> {
  return request({
    url: '/walking/admin/award-config/list',
    method: 'get',
    params: query
  });
}

// 奖励档位详情
export function getAwardConfig(id: string | number): AxiosPromise<AwardTierVO> {
  return request({
    url: '/walking/admin/award-config/' + id,
    method: 'get'
  });
}

// 新增奖励档位
export function addAwardConfig(data: AwardConfigForm) {
  return request({
    url: '/walking/admin/award-config',
    method: 'post',
    data: data
  });
}

// 修改奖励档位
export function updateAwardConfig(data: AwardConfigForm) {
  return request({
    url: '/walking/admin/award-config',
    method: 'put',
    data: data
  });
}

// 删除奖励档位
export function delAwardConfig(ids: string | number | Array<string | number>) {
  return request({
    url: '/walking/admin/award-config/' + ids,
    method: 'delete'
  });
}
