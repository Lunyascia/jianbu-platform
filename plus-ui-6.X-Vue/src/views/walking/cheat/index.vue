<template>
  <div class="p-2 app-container walking-cheat-page">
    <div class="search-wrap">
      <el-card shadow="hover" class="search-panel" :class="{ 'is-collapsed': !showSearch }">
        <template #header>
          <div class="panel-heading search-panel-toggle" @click.stop="showSearch = !showSearch">
            <div>
              <span class="panel-kicker">Search Filters</span>
              <h3>筛选条件</h3>
            </div>
          </div>
        </template>
        <el-form :model="queryParams" :inline="true" class="query-form">
          <el-form-item label="活动" prop="activityId">
            <el-select v-model="queryParams.activityId" placeholder="全部活动" clearable filterable style="width: 180px" @change="handleQuery">
              <el-option v-for="a in activityOptions" :key="a.id" :label="a.activityName" :value="a.id" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="activeTab === 'records'" label="异常标志" prop="abnormalFlag">
            <el-select v-model="queryParams.abnormalFlag" placeholder="全部" clearable style="width: 120px">
              <el-option label="正常" :value="0" />
              <el-option label="异常" :value="1" />
            </el-select>
          </el-form-item>
          <el-form-item label="关键词" prop="keyword">
            <el-input v-model="queryParams.keyword" placeholder="姓名/手机号" clearable style="width: 160px" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <el-card v-loading="loading" shadow="hover" class="table-panel">
      <template #header>
        <div class="toolbar-shell">
          <div class="table-heading">
            <span class="panel-kicker">Cheat Monitor</span>
            <h3>异常数据处理</h3>
            <p>对疑似作弊的步数数据进行标记、删除，或批量停用/取消。</p>
          </div>
          <div class="toolbar-actions">
            <el-button
              v-if="activeTab === 'records'"
              v-hasPermi="['walking:cheat:mark']"
              type="warning"
              plain
              icon="Warning"
              :disabled="!selectedRows.length"
              @click="handleMark"
            >
              标记异常
            </el-button>
            <el-button
              v-if="activeTab === 'records'"
              v-hasPermi="['walking:cheat:delete']"
              type="danger"
              plain
              icon="Delete"
              :disabled="!selectedRows.length"
              @click="handleDelete"
            >
              删除数据
            </el-button>
            <el-button
              v-if="activeTab === 'records'"
              v-hasPermi="['walking:cheat:mark']"
              type="success"
              plain
              icon="RefreshLeft"
              :disabled="!selectedRows.length"
              @click="handleUnmark"
            >
              恢复异常
            </el-button>
            <el-button
              v-if="activeTab === 'records'"
              v-hasPermi="['walking:cheat:batch']"
              type="primary"
              plain
              icon="Operation"
              :disabled="!selectedRows.length"
              @click="openBatch"
            >
              批量处理
            </el-button>
          </div>
        </div>
        <el-tabs v-model="activeTab" class="cheat-tabs" @tab-change="handleTabChange">
          <el-tab-pane label="异常数据" name="records" />
          <el-tab-pane label="处理日志" name="logs" />
        </el-tabs>
      </template>

      <el-table
        v-if="activeTab === 'records'"
        v-loading="loading"
        :data="recordList"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="姓名" prop="realName" min-width="90" />
        <el-table-column label="手机号" prop="phone" min-width="120" />
        <el-table-column label="单位" prop="deptName" min-width="130" show-overflow-tooltip />
        <el-table-column label="打卡日期" prop="recordDate" width="120" />
        <el-table-column label="步数" prop="steps" align="center" width="100" />
        <el-table-column label="异常标志" align="center" width="100">
          <template #default="{ row }">
            <el-tag :type="row.abnormalFlag === 1 ? 'danger' : 'success'">{{ row.abnormalFlag === 1 ? '异常' : '正常' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <el-table v-else v-loading="loading" :data="logList">
        <el-table-column label="姓名" prop="realName" min-width="90" />
        <el-table-column label="手机号" prop="phone" min-width="120" />
        <el-table-column label="异常类型" prop="abnormalType" min-width="120" />
        <el-table-column label="处理方式" align="center" width="110">
          <template #default="{ row }">
            <el-tag>{{ row.handleTypeText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作人" prop="operator" align="center" width="100" />
        <el-table-column label="备注" prop="remark" min-width="160" show-overflow-tooltip />
        <el-table-column label="处理时间" prop="createTime" width="170" />
      </el-table>

      <pagination
        v-show="total > 0"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        :total="total"
        @pagination="getList"
      />
    </el-card>

    <!-- 批量处理 -->
    <el-dialog v-model="batchDialog.visible" title="批量处理作弊账号" width="480px" append-to-body>
      <el-form :model="batchForm" label-width="90px">
        <el-form-item label="处理方式">
          <el-radio-group v-model="batchForm.handleType">
            <el-radio :value="1">标记异常</el-radio>
            <el-radio :value="2">删除数据</el-radio>
            <el-radio :value="3">停用账号</el-radio>
            <el-radio :value="4">取消报名</el-radio>
            <el-radio :value="5">恢复异常</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="影响会员">
          <span>{{ selectedMemberCount }} 人（含所选记录对应会员）</span>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="batchForm.remark" type="textarea" :rows="3" placeholder="请输入处理备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitBatch">确 定</el-button>
          <el-button @click="batchDialog.visible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="WalkingCheat" lang="ts">
import { listCheat, markCheat, deleteCheat, batchCheat, listCheatLogs, unmarkCheat } from '@/api/walking/cheat';
import { CheatForm, CheatLogQuery, CheatLogVO, CheatQuery, CheatRecordVO } from '@/api/walking/cheat/types';
import { listActivityOptions } from '@/api/walking/activity';
import { ActivityVO } from '@/api/walking/activity/types';
import { useLoading } from '@/hooks/async/useLoading';
import { useSearchReset } from '@/hooks/form/useSearchReset';
import { useSearchToggle } from '@/hooks/form/useSearchToggle';
import modal from '@/plugins/modal';

const activeTab = ref('records');
const recordList = ref<CheatRecordVO[]>([]);
const logList = ref<CheatLogVO[]>([]);
const activityOptions = ref<ActivityVO[]>([]);
const { loading, withLoading } = useLoading(true);
const { showSearch } = useSearchToggle();
const total = ref(0);
const queryFormRef = ref<ElFormInstance>();
const selectedRows = ref<CheatRecordVO[]>([]);
const selectedMemberCount = computed(() => {
  const set = new Set<number | string>();
  selectedRows.value.forEach((r) => set.add(r.memberId));
  return set.size;
});

const data = reactive<PageData<any, CheatQuery & CheatLogQuery>>({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    activityId: undefined,
    abnormalFlag: 1,
    keyword: ''
  },
  rules: {}
});
const { queryParams } = toRefs(data);
const { resetQuery } = useSearchReset({
  queryFormRef,
  queryParams,
  pageNumKey: 'pageNum',
  afterReset: () => handleQuery()
});

const batchDialog = reactive({ visible: false });
const batchForm = ref<CheatForm>({ memberIds: [], recordIds: [], activityId: undefined, handleType: 1, remark: '' });

const getList = async () => {
  await withLoading(async () => {
    if (activeTab.value === 'records') {
      const res = await listCheat({ ...queryParams.value });
      recordList.value = res.rows;
      total.value = res.total;
    } else {
      const res = await listCheatLogs({ ...queryParams.value });
      logList.value = res.rows;
      total.value = res.total;
    }
  });
};

const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};

const handleTabChange = () => {
  queryParams.value.pageNum = 1;
  queryParams.value.abnormalFlag = 1;
  getList();
};

const handleSelectionChange = (rows: CheatRecordVO[]) => {
  selectedRows.value = rows;
};

const buildForm = (handleType: number): CheatForm => {
  return {
    memberIds: [...new Set(selectedRows.value.map((r) => r.memberId))],
    recordIds: selectedRows.value.map((r) => r.id),
    activityId: queryParams.value.activityId,
    handleType,
    remark: ''
  };
};

const handleMark = async () => {
  await modal.confirm('确认将所选数据标记为异常吗？');
  await markCheat(buildForm(1));
  modal.msgSuccess('已标记');
  await getList();
};

const handleDelete = async () => {
  await modal.confirm('确认删除所选步数数据吗？删除后不可恢复。');
  await deleteCheat(buildForm(2));
  modal.msgSuccess('已删除');
  await getList();
};

const handleUnmark = async () => {
  await modal.confirm('确认将所选数据恢复正常吗？恢复后将重新计入统计与排行。');
  await unmarkCheat(buildForm(5));
  modal.msgSuccess('已恢复');
  await getList();
};

const openBatch = () => {
  batchForm.value = buildForm(1);
  batchDialog.visible = true;
};

const submitBatch = async () => {
  await batchCheat(batchForm.value);
  modal.msgSuccess('批量处理完成');
  batchDialog.visible = false;
  await getList();
};

onMounted(async () => {
  getList();
  const res = await listActivityOptions();
  activityOptions.value = res.data;
});
</script>

<style lang="scss" scoped>
@use '@/assets/styles/components/page-shell' as pageShell;

@include pageShell.toolbar-responsive;

.cheat-tabs {
  margin-top: 8px;
}
</style>
