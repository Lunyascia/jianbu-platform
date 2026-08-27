<template>
  <div class="p-2 app-container walking-stats-page">
    <div class="search-wrap">
      <el-card shadow="hover" class="search-panel">
        <el-form :model="query" inline>
          <el-form-item label="选择活动" prop="activityId">
            <el-select v-model="query.activityId" placeholder="请选择活动" clearable filterable style="width: 220px" @change="loadAll">
              <el-option v-for="a in activityOptions" :key="a.id" :label="a.activityName" :value="a.id" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Refresh" @click="loadAll">刷新</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <el-card v-loading="loading" shadow="hover" class="overview-panel">
      <template #header>
        <div class="panel-heading">
          <div>
            <span class="panel-kicker">Overview</span>
            <h3>数据总览</h3>
          </div>
        </div>
      </template>
      <div class="stat-grid">
        <div class="stat-item" v-for="item in statCards" :key="item.label">
          <div class="stat-value" :style="{ color: item.color }">{{ item.value }}</div>
          <div class="stat-label">{{ item.label }}</div>
        </div>
      </div>
    </el-card>

    <div class="stats-grid">
      <el-card v-loading="loading" shadow="hover" class="table-panel">
        <template #header>
          <div class="toolbar-shell">
            <div class="table-heading">
              <span class="panel-kicker">By Dept</span>
              <h3>单位统计</h3>
              <p>报名人数 / 参与人数 / 参与率 / 打卡率 / 获奖人数</p>
            </div>
            <div class="toolbar-actions">
              <el-button v-hasPermi="['walking:stats:export']" type="warning" plain icon="Download" @click="handleExportDept">
                导出
              </el-button>
            </div>
          </div>
        </template>
        <el-table :data="deptStats" :height="420">
          <el-table-column prop="deptName" label="单位" min-width="150" show-overflow-tooltip />
          <el-table-column prop="memberTotal" label="会员总数" align="center" width="90" />
          <el-table-column prop="regCount" label="报名人数" align="center" width="90" />
          <el-table-column prop="approvedCount" label="参与人数" align="center" width="90" />
          <el-table-column prop="participationRateText" label="参与率" align="center" width="80" />
          <el-table-column prop="checkinCount" label="打卡人数" align="center" width="90" />
          <el-table-column prop="checkinRateText" label="打卡率" align="center" width="80" />
          <el-table-column prop="awardCount" label="获奖人数" align="center" width="90" />
        </el-table>
      </el-card>

      <el-card v-loading="loading" shadow="hover" class="table-panel">
        <template #header>
          <div class="toolbar-shell">
            <div class="table-heading">
              <span class="panel-kicker">Advanced Org Eval</span>
              <h3>先进组织单位评选参考</h3>
              <p>计分：参与率×40 + 打卡率×30 + 获奖分布×30，最终由管理员综合判断</p>
            </div>
            <div class="toolbar-actions">
              <el-button v-hasPermi="['walking:stats:export']" type="warning" plain icon="Download" @click="handleExportEval">
                导出
              </el-button>
            </div>
          </div>
        </template>
        <el-table :data="unitEvalList" :height="420">
          <el-table-column label="名次" width="70" align="center">
            <template #default="{ row }">
              <el-tag :type="row.rank <= 3 ? 'danger' : 'warning'">{{ row.rank }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="deptName" label="单位" min-width="130" show-overflow-tooltip />
          <el-table-column prop="participationRateText" label="参与率" align="center" width="80" />
          <el-table-column prop="checkinRateText" label="打卡率" align="center" width="80" />
          <el-table-column label="获奖分布" min-width="130">
            <template #default="{ row }">
              一{{ row.award1Count }} 二{{ row.award2Count }} 三{{ row.award3Count }} 优{{ row.awardExcellentCount }}
            </template>
          </el-table-column>
          <el-table-column label="参与率分" prop="participationScore" align="center" width="85" />
          <el-table-column label="打卡率分" prop="checkinScore" align="center" width="85" />
          <el-table-column label="获奖分" prop="awardScore" align="center" width="75" />
          <el-table-column label="总分" width="80" align="center">
            <template #default="{ row }">
              <b style="color: #e6a23c">{{ row.totalScore }}</b>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <el-card v-loading="loading" shadow="hover" class="table-panel">
      <template #header>
        <div class="toolbar-shell">
          <div class="table-heading">
            <span class="panel-kicker">Ranking</span>
            <h3>排行榜（前100）</h3>
          </div>
          <div class="toolbar-actions">
            <el-radio-group v-model="rankingType" style="margin-right: 8px" @change="handleRankingTypeChange">
              <el-radio-button value="today">当日步数</el-radio-button>
              <el-radio-button value="steps">总步数</el-radio-button>
              <el-radio-button value="points">积分</el-radio-button>
            </el-radio-group>
            <el-button type="info" plain icon="Refresh" @click="handleRefresh">手动刷新</el-button>
            <el-select
              v-model="rankingDeptId"
              clearable
              filterable
              placeholder="选择单位(按单位导出)"
              style="width: 200px; margin-right: 8px"
            >
              <el-option v-for="d in leafDepts" :key="d.deptId" :label="d.deptName" :value="d.deptId" />
            </el-select>
            <el-button v-hasPermi="['walking:stats:export']" type="primary" plain icon="Download" @click="handleExportRanking">
              全局导出
            </el-button>
            <el-button
              v-hasPermi="['walking:stats:export']"
              type="success"
              plain
              icon="Download"
              :disabled="!rankingDeptId"
              @click="handleExportRankingDept"
            >
              按单位导出
            </el-button>
            <el-button v-hasPermi="['walking:stats:export']" type="warning" plain icon="Trophy" @click="handleExportAwardList">
              获奖名单
            </el-button>
          </div>
        </div>
      </template>
      <el-table :data="rankingList" :height="460">
        <el-table-column type="index" label="名次" width="70" align="center" />
        <el-table-column prop="realName" label="姓名" min-width="90" />
        <el-table-column prop="phone" label="手机号" min-width="120" />
        <el-table-column prop="deptName" label="单位" min-width="130" show-overflow-tooltip />
        <el-table-column prop="totalSteps" label="累计步数" align="center" width="110" sortable />
        <el-table-column prop="totalPoints" label="累计积分" align="center" width="100" sortable />
        <el-table-column prop="qualifyDays" label="达标天数" align="center" width="100" />
        <el-table-column label="操作" width="160" align="center">
          <template #default="{ row }">
            <el-button link type="primary" icon="View" @click="openMemberDetail(row as RankingStatVO)">明细</el-button>
            <el-button v-hasPermi="['walking:cheat:mark']" link type="danger" icon="Warning" @click="handleMarkAbnormal(row as RankingStatVO)">标记异常</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 个人打卡/积分明细 -->
    <el-dialog v-model="detailDialog.visible" :title="detailDialog.title" width="860px" append-to-body top="5vh">
      <div v-if="pointsDetail" class="detail-summary">
        <el-tag type="primary">{{ pointsDetail.realName }} / {{ pointsDetail.phone }}</el-tag>
        <el-tag type="success">基础分 {{ pointsDetail.basePoints }}</el-tag>
        <el-tag type="warning">阶段奖励分 {{ pointsDetail.bonusPoints }}</el-tag>
        <el-tag type="danger">总积分 {{ pointsDetail.totalPoints }}</el-tag>
      </div>
      <el-tabs v-model="detailTab">
        <el-tab-pane label="打卡明细" name="checkin">
          <el-table :data="checkinList" max-height="420">
            <el-table-column prop="recordDate" label="日期" width="120" />
            <el-table-column prop="steps" label="步数" align="center" width="100" />
            <el-table-column prop="dailyTarget" label="当日目标" align="center" width="100" />
            <el-table-column prop="reachedText" label="达标" align="center" width="80">
              <template #default="{ row }">
                <el-tag :type="row.reachedText === '达标' ? 'success' : 'info'">{{ row.reachedText }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sourceText" label="同步方式" align="center" width="100" />
            <el-table-column label="异常" align="center" width="80">
              <template #default="{ row }">
                <el-tag v-if="row.abnormalFlag === 1" type="danger">异常</el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="积分明细" name="points">
          <el-table :data="pointsDetail?.daily || []" max-height="420">
            <el-table-column prop="recordDate" label="日期" width="120" />
            <el-table-column prop="steps" label="步数" align="center" width="100" />
            <el-table-column label="达标" align="center" width="80">
              <template #default="{ row }">
                <el-tag :type="row.reached ? 'success' : 'info'">{{ row.reached ? '是' : '否' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="basePoints" label="基础分" align="center" width="80" />
            <el-table-column prop="bonusPoints" label="阶段奖励" align="center" width="90" />
            <el-table-column prop="dayTotal" label="当日小计" align="center" width="90" />
            <el-table-column prop="cumulative" label="累计" align="center" width="90" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup name="WalkingStats" lang="ts">
import {
  getOverview,
  listDeptStats,
  getUnitEval,
  listRanking,
  getCheckinDetail,
  getPointsDetail,
  refreshStats
} from '@/api/walking/stats';
import {
  CheckinDetailVO,
  DeptStatVO,
  PointsDetailVO,
  RankingStatVO,
  StatsOverviewVO,
  UnitEvalVO
} from '@/api/walking/stats/types';
import { listActivityOptions } from '@/api/walking/activity';
import { ActivityVO } from '@/api/walking/activity/types';
import { batchCheat } from '@/api/walking/cheat';
import { treeOrg } from '@/api/walking/org';
import { OrgVO } from '@/api/walking/org/types';
import { useLoading } from '@/hooks/async/useLoading';
import modal from '@/plugins/modal';
import { download as requestDownload } from '@/utils/request';

const activityOptions = ref<ActivityVO[]>([]);
const deptTree = ref<OrgVO[]>([]);
const leafDepts = computed(() => collectLeaves(deptTree.value));
const query = reactive({ activityId: undefined as number | string | undefined });
const { loading, withLoading } = useLoading(true);

const overview = ref<Partial<StatsOverviewVO>>({});
const deptStats = ref<DeptStatVO[]>([]);
const unitEvalList = ref<UnitEvalVO[]>([]);
const rankingList = ref<RankingStatVO[]>([]);
const rankingDeptId = ref<number | string | undefined>(undefined);
const rankingType = ref('steps');

const detailDialog = reactive({ visible: false, title: '' });
const detailTab = ref('checkin');
const checkinList = ref<CheckinDetailVO[]>([]);
const pointsDetail = ref<PointsDetailVO | null>(null);

const statCards = computed(() => [
  { label: '会员总数(注册)', value: overview.value.totalMembers ?? 0, color: '#409EFF' },
  { label: '组织会员总数', value: overview.value.orgMemberTotal ?? 0, color: '#67C23A' },
  { label: '报名总数', value: overview.value.totalRegistrations ?? 0, color: '#67C23A' },
  { label: '报名通过', value: overview.value.approvedCount ?? 0, color: '#67C23A' },
  { label: '参与率', value: overview.value.participationRate ?? '0%', color: '#E6A23C' },
  { label: '已取消', value: overview.value.cancelledCount ?? 0, color: '#909399' },
  { label: '已停用', value: overview.value.disabledCount ?? 0, color: '#F56C6C' },
  { label: '单位数', value: overview.value.deptCount ?? 0, color: '#409EFF' },
  { label: '活动总数', value: overview.value.totalActivities ?? 0, color: '#909399' },
  { label: '总步数', value: (overview.value.totalSteps ?? 0).toLocaleString(), color: '#67C23A' },
  { label: '异常数据', value: overview.value.abnormalCount ?? 0, color: '#F56C6C' }
]);

const loadAll = async () => {
  await withLoading(async () => {
    const [o, d, e, r] = await Promise.all([
      getOverview(query.activityId),
      listDeptStats(query.activityId),
      getUnitEval(query.activityId),
      listRanking(query.activityId, undefined, rankingType.value, 100)
    ]);
    overview.value = o.data;
    deptStats.value = d.data;
    unitEvalList.value = e.data;
    rankingList.value = r.data;
  });
};

const handleRankingTypeChange = () => {
  getRanking();
};

const getRanking = async () => {
  const r = await listRanking(query.activityId, undefined, rankingType.value, 100);
  rankingList.value = r.data;
};

const handleRefresh = async () => {
  await modal.confirm('确认手动刷新排名统计吗？将触发锁榜评估。');
  const res = await refreshStats();
  modal.msgSuccess(res.data?.message || '刷新完成');
  await loadAll();
};

const handleMarkAbnormal = async (row: RankingStatVO) => {
  if (!query.activityId) {
    modal.msgError('请先选择活动');
    return;
  }
  await modal.confirm('确认将【' + row.realName + ' / ' + row.phone + '】在本次活动内的全部步数数据标记为异常吗？标记后不计积分、不进排行，并进入异常数据处理。');
  await batchCheat({ memberIds: [row.memberId], activityId: query.activityId, handleType: 1, remark: '数据统计手动标记' });
  modal.msgSuccess('已标记异常');
  await loadAll();
};

const openMemberDetail = async (row: RankingStatVO) => {
  detailDialog.title = '会员明细：' + row.realName + ' / ' + row.phone;
  detailDialog.visible = true;
  detailTab.value = 'checkin';
  const [c, p] = await Promise.all([getCheckinDetail(row.memberId, query.activityId), getPointsDetail(row.memberId, query.activityId)]);
  checkinList.value = c.data;
  pointsDetail.value = p.data;
};

const handleExportDept = () => {
  requestDownload('walking/admin/stats/export/dept', { activityId: query.activityId }, `dept_stats_${new Date().getTime()}.xlsx`);
};

const handleExportEval = () => {
  requestDownload('walking/admin/stats/export/eval', { activityId: query.activityId }, `org_eval_${new Date().getTime()}.xlsx`);
};

const handleExportRanking = () => {
  requestDownload('walking/admin/stats/export/ranking', { activityId: query.activityId }, `ranking_${new Date().getTime()}.xlsx`);
};

const handleExportRankingDept = () => {
  if (!rankingDeptId.value) {
    return;
  }
  requestDownload(
    'walking/admin/stats/export/ranking',
    { activityId: query.activityId, deptId: rankingDeptId.value },
    `ranking_dept_${new Date().getTime()}.xlsx`
  );
};

const handleExportAwardList = () => {
  requestDownload('walking/admin/stats/export/awardList', { activityId: query.activityId }, `award_list_${new Date().getTime()}.xlsx`);
};

function collectLeaves(nodes: OrgVO[]): OrgVO[] {
  const result: OrgVO[] = [];
  const walk = (list: OrgVO[]) => {
    list.forEach((n) => {
      if (n.children && n.children.length) {
        walk(n.children);
      } else {
        result.push(n);
      }
    });
  };
  walk(nodes);
  return result;
}

onMounted(async () => {
  const [acts, depts] = await Promise.all([listActivityOptions(), treeOrg()]);
  activityOptions.value = acts.data;
  deptTree.value = depts.data;
  if (activityOptions.value.length) {
    // 默认选"进行中"的活动(status=1)，没有进行中的再选第一个，避免默认落到空活动导致排行看不到人
    const active = activityOptions.value.find((a) => a.status === 1);
    query.activityId = (active || activityOptions.value[0]).id;
  }
  loadAll();
});
</script>

<style lang="scss" scoped>
@use '@/assets/styles/components/page-shell' as pageShell;

@include pageShell.toolbar-responsive;

.stat-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 14px;
}

.stat-item {
  background: var(--el-fill-color-light);
  border-radius: 8px;
  padding: 16px 12px;
  text-align: center;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  line-height: 1.3;
}

.stat-label {
  margin-top: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-top: 16px;
}

.detail-summary {
  margin-bottom: 12px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  .stat-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
