export interface AuditLogVO {
  id: number | string;
  registrationId: number | string;
  memberId: number | string;
  realName: string;
  phone: string;
  auditAction: string;
  auditResult: string;
  auditor: string;
  createTime: string;
}

export interface AuditLogQuery extends PageQuery {
  registrationId: number | string;
  memberId: number | string;
  auditAction: string;
  keyword: string;
}
