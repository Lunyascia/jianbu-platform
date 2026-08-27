export interface AwardTierVO {
  id: number | string;
  activityId: number | string;
  awardType: number;
  awardName: string;
  rankStart: number;
  rankEnd: number;
  prizeContent: string;
  imageUrl: string;
  status: number;
  sortOrder: number;
}

export interface AwardConfigForm {
  id?: number | string;
  activityId: number | string;
  awardType: number;
  awardName: string;
  rankStart: number;
  rankEnd: number;
  prizeContent: string;
  imageUrl: string;
  status: number;
  sortOrder: number;
}

export interface AwardConfigQuery extends PageQuery {
  activityId: number | string;
  awardName: string;
}
