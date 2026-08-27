export interface CheatRecordVO {
  id: number | string;
  memberId: number | string;
  activityId: number | string;
  realName: string;
  phone: string;
  deptName: string;
  recordDate: string;
  steps: number;
  source: number;
  abnormalFlag: number;
  locked: number;
}

export interface CheatLogVO {
  id: number | string;
  memberId: number | string;
  realName: string;
  phone: string;
  activityId: number | string;
  recordDate: string;
  abnormalType: string;
  handleType: number;
  handleTypeText: string;
  operator: string;
  remark: string;
  createTime: string;
}

export interface CheatForm {
  memberIds: Array<number | string>;
  recordIds?: Array<number | string>;
  activityId: number | string;
  handleType: number;
  remark: string;
}

export interface CheatQuery extends PageQuery {
  activityId: number | string;
  abnormalFlag: number;
  keyword: string;
}

export interface CheatLogQuery extends PageQuery {
  activityId: number | string;
  keyword: string;
}
