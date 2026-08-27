export interface StatsOverviewVO {
  totalActivities: number;
  activeActivities: number;
  totalMembers: number;
  orgMemberTotal: number;
  totalRegistrations: number;
  approvedCount: number;
  cancelledCount: number;
  disabledCount: number;
  participationRate: string;
  deptCount: number;
  totalSteps: number;
  abnormalCount: number;
}

export interface DeptStatVO {
  deptId: number | string;
  deptName: string;
  memberTotal: number;
  regCount: number;
  approvedCount: number;
  participationRateText: string;
  checkinCount: number;
  checkinRateText: string;
  awardCount: number;
  totalSteps: number;
  totalPoints: number;
  award1Count: number;
  award2Count: number;
  award3Count: number;
  awardExcellentCount: number;
}

export interface UnitEvalVO {
  rank: number;
  deptId: number | string;
  deptName: string;
  memberTotal: number;
  approvedCount: number;
  participationRateText: string;
  checkinCount: number;
  checkinRateText: string;
  award1Count: number;
  award2Count: number;
  award3Count: number;
  awardExcellentCount: number;
  participationScore: number;
  checkinScore: number;
  awardScore: number;
  totalScore: number;
}

export interface RankingStatVO {
  rank: number;
  memberId: number | string;
  deptId: number | string;
  realName: string;
  phone: string;
  deptName: string;
  totalSteps: number;
  totalPoints: number;
  qualifyDays: number;
}

export interface CheckinDetailVO {
  memberId: number | string;
  realName: string;
  phone: string;
  deptName: string;
  recordDate: string;
  steps: number;
  dailyTarget: number;
  reachedText: string;
  source: number;
  sourceText: string;
  abnormalFlag: number;
  locked: number;
}

export interface PointsDayVO {
  recordDate: string;
  steps: number;
  reached: boolean;
  basePoints: number;
  bonusPoints: number;
  dayTotal: number;
  cumulative: number;
}

export interface PointsDetailVO {
  memberId: number | string;
  realName: string;
  phone: string;
  deptName: string;
  activityId: number | string;
  basePoints: number;
  bonusPoints: number;
  totalPoints: number;
  daily: PointsDayVO[];
}

export interface AwardVO {
  rank: number;
  realName: string;
  phone: string;
  deptName: string;
  awardLevel: string;
  totalSteps: number;
  totalPoints: number;
}
