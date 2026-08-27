export interface OrgVO {
  deptId: number | string;
  parentId: number | string;
  deptName: string;
  orderNum: number;
  leader: string;
  phone: string;
  memberTotal: number;
  children: OrgVO[];
}

export interface OrgForm {
  deptId: number | string | undefined;
  parentId: number | string;
  deptName: string;
  orderNum: number;
  leader: string;
  phone: string;
}
