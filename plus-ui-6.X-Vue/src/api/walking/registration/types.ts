export interface RegistrationVO {
  id: number | string;
  memberId: number | string;
  activityId: number | string;
  activityName: string;
  realName: string;
  phone: string;
  deptId: number | string;
  deptName: string;
  idCard: string;
  memberStatus: number;
  status: number;
  statusText: string;
  auditResult: string;
  submitTime: string;
  auditTime: string;
  cancelBy: string;
  cancelTime: string;
}

export interface RegistrationQuery extends PageQuery {
  activityId: number | string;
  status: number;
  deptId: number | string;
  keyword: string;
}
