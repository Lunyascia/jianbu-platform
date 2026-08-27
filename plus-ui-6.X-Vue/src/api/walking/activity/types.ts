export interface ActivityVO {
  id: number | string;
  activityName: string;
  coverUrl: string;
  description: string;
  ruleContent: string;
  startDate: string;
  endDate: string;
  targetSteps: number;
  dailyTargetSteps: number;
  pointsPerThousandSteps: number;
  dailyStepLimit: number;
  streak7Points: number;
  streak14Points: number;
  fullAttendancePoints: number;
  bufferDays: number;
  registerStart: string;
  registerEnd: string;
  status: number;
  statusText: string;
  orgId: number | string;
  regCount: number;
}

export interface ActivityForm {
  id: number | string | undefined;
  activityName: string;
  coverUrl: string;
  description: string;
  ruleContent: string;
  startDate: string;
  endDate: string;
  targetSteps: number;
  dailyTargetSteps: number;
  pointsPerThousandSteps: number;
  dailyStepLimit: number;
  streak7Points: number;
  streak14Points: number;
  fullAttendancePoints: number;
  bufferDays: number;
  registerStart: string;
  registerEnd: string;
  status: number;
  orgId: number | string;
}

export interface ActivityQuery extends PageQuery {
  activityName: string;
  status: number;
}
