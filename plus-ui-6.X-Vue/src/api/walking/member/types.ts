export interface MemberVO {
  id: number | string;
  openid: string;
  loggedIn: number;
  phone: string;
  realName: string;
  idCard: string;
  deptId: number | string;
  deptName: string;
  status: number;
  isVerified: number;
  registerTime: string;
  receiver: string;
  addressPhone: string;
  address: string;
  regCount: number;
}

export interface MemberForm {
  id: number | string | undefined;
  deptId: number | string;
  deptName: string;
  realName: string;
  idCard: string;
  receiver: string;
  addressPhone: string;
  address: string;
  phone?: string;
  status?: number;
}

export interface MemberQuery extends PageQuery {
  deptId: number | string;
  status: number;
  keyword: string;
}
