export interface AwardWinnerVO {
  id: number | string;
  memberId: number | string;
  activityId: number | string;
  realName: string;
  phone: string;
  deptName: string;
  awardLevel: string;
  rank: number;
  receiver: string;
  addressPhone: string;
  address: string;
  memberStatus: number;
  issueTime: string;
}

export interface AwardForm {
  activityId: number | string;
  memberId: number | string;
  awardLevel: string;
  rank: number;
}

export interface AwardQuery extends PageQuery {
  activityId: number | string;
  keyword: string;
}
