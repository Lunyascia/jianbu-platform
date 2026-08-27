<template>
  <div class="p-2 app-container walking-activity-page">
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
        <el-form ref="queryFormRef" :model="queryParams" :inline="true" class="query-form">
          <el-form-item label="活动名称" prop="activityName">
            <el-input v-model="queryParams.activityName" placeholder="请输入活动名称" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="活动状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 140px">
              <el-option v-for="dict in activity_status" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
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
            <span class="panel-kicker">Activity</span>
            <h3>活动列表</h3>
            <p>共 {{ total }} 条记录，支持活动增删改与参数配置。</p>
          </div>
          <div class="toolbar-actions">
            <el-button v-hasPermi="['walking:activity:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
            <right-toolbar v-model:show-search="showSearch" :search="false" @query-table="getList"></right-toolbar>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="activityList">
        <el-table-column label="活动名称" prop="activityName" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" align="center" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="活动周期" min-width="200">
          <template #default="{ row }">{{ row.startDate }} ~ {{ row.endDate }}</template>
        </el-table-column>
        <el-table-column label="报名窗口" min-width="230">
          <template #default="{ row }">{{ row.registerStart || '-' }} ~ {{ row.registerEnd || '-' }}</template>
        </el-table-column>
        <el-table-column label="每日目标" prop="dailyTargetSteps" align="center" width="100" />
        <el-table-column label="每千步积分" prop="pointsPerThousandSteps" align="center" width="110" />
        <el-table-column label="报名人数" prop="regCount" align="center" width="100" />
        <el-table-column label="操作" width="210" align="center">
          <template #default="{ row }">
            <el-tooltip content="参数配置" placement="top">
              <el-button v-hasPermi="['walking:activity:config']" link type="warning" icon="Setting" @click="handleConfig(row)"></el-button>
            </el-tooltip>
            <el-tooltip content="修改" placement="top">
              <el-button v-hasPermi="['walking:activity:edit']" link type="primary" icon="Edit" @click="handleUpdate(row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['walking:activity:remove']" link type="danger" icon="Delete" @click="handleDelete(row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="total > 0"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        :total="total"
        @pagination="getList"
      />
    </el-card>

    <!-- 新增/修改活动 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="620px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="活动名称" prop="activityName">
          <el-input v-model="form.activityName" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="活动状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="dict in activity_status" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="活动周期" prop="startDate">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            range-separator="~"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="报名窗口" prop="registerStart">
          <el-date-picker
            v-model="registerRange"
            type="datetimerange"
            value-format="YYYY-MM-DD HH:mm:ss"
            range-separator="~"
            start-placeholder="报名开始"
            end-placeholder="报名截止"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="每日目标步数" prop="dailyTargetSteps">
          <el-input-number v-model="form.dailyTargetSteps" :min="0" :step="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="每千步积分" prop="pointsPerThousandSteps">
          <el-input-number v-model="form.pointsPerThousandSteps" :min="0" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="目标总步数" prop="targetSteps">
          <el-input-number v-model="form.targetSteps" :min="0" :step="1000" style="width: 100%" />
        </el-form-item>
        <el-form-item label="活动介绍" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入活动介绍" />
        </el-form-item>
        <el-form-item label="活动规则" prop="ruleContent">
          <el-input v-model="form.ruleContent" type="textarea" :rows="3" placeholder="请输入活动规则" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 活动参数配置（系统管理员） -->
    <el-dialog v-model="configDialog.visible" title="活动参数配置" width="520px" append-to-body>
      <el-form ref="configFormRef" :model="configForm" label-width="110px">
        <el-form-item label="活动周期">
          <el-date-picker
            v-model="configRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            range-separator="~"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="报名窗口">
          <el-date-picker
            v-model="configRegisterRange"
            type="datetimerange"
            value-format="YYYY-MM-DD HH:mm:ss"
            range-separator="~"
            start-placeholder="报名开始"
            end-placeholder="报名截止"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="每日目标步数">
          <el-input-number v-model="configForm.dailyTargetSteps" :min="0" :step="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="每千步积分">
          <el-input-number v-model="configForm.pointsPerThousandSteps" :min="0" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="单日步数上限">
          <el-input-number v-model="configForm.dailyStepLimit" :min="0" :step="1000" style="width: 100%" />
          <div class="el-form-item__tip">超出不计入统计</div>
        </el-form-item>
        <el-form-item label="连续7天奖励分">
          <el-input-number v-model="configForm.streak7Points" :min="0" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="连续14天奖励分">
          <el-input-number v-model="configForm.streak14Points" :min="0" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="全程全勤奖励分">
          <el-input-number v-model="configForm.fullAttendancePoints" :min="0" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="缓冲期时长(天)">
          <el-input-number v-model="configForm.bufferDays" :min="0" :step="1" style="width: 100%" />
          <div class="el-form-item__tip">活动结束后同步数据窗口期</div>
        </el-form-item>
        <el-form-item label="目标总步数">
          <el-input-number v-model="configForm.targetSteps" :min="0" :step="1000" style="width: 100%" />
        </el-form-item>
        <el-form-item label="活动状态">
          <el-radio-group v-model="configForm.status">
            <el-radio v-for="dict in activity_status" :key="dict.value" :value="dict.value">{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitConfig">确 定</el-button>
          <el-button @click="configDialog.visible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="WalkingActivity" lang="ts">
import { listActivity, getActivity, delActivity, addActivity, updateActivity, configActivity } from '@/api/walking/activity';
import { ActivityForm, ActivityQuery, ActivityVO } from '@/api/walking/activity/types';
import { useLoading } from '@/hooks/async/useLoading';
import { useFormDialog } from '@/hooks/dialog/useFormDialog';
import { useSearchReset } from '@/hooks/form/useSearchReset';
import { useSearchToggle } from '@/hooks/form/useSearchToggle';
import modal from '@/plugins/modal';

const activityList = ref<ActivityVO[]>([]);
const { loading, withLoading } = useLoading(true);
const { showSearch } = useSearchToggle();
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();
const formRef = ref<ElFormInstance>();
const configFormRef = ref<ElFormInstance>();

const initFormData: ActivityForm = {
  id: undefined,
  activityName: '',
  coverUrl: '',
  description: '',
  ruleContent: '',
  startDate: '',
  endDate: '',
  targetSteps: 0,
  dailyTargetSteps: 7000,
  pointsPerThousandSteps: 1,
  dailyStepLimit: 15000,
  streak7Points: 2,
  streak14Points: 5,
  fullAttendancePoints: 10,
  bufferDays: 1,
  registerStart: '',
  registerEnd: '',
  status: 0,
  orgId: undefined
};
const data = reactive<PageData<ActivityForm, ActivityQuery>>({
  form: { ...initFormData },
  queryParams: { pageNum: 1, pageSize: 10, activityName: '', status: undefined },
  rules: {
    activityName: [{ required: true, message: '活动名称不能为空', trigger: 'blur' }]
  }
});
const { queryParams, form, rules } = toRefs(data);
const { dialog, resetForm, openDialog, showDialog, closeDialog } = useFormDialog({
  form,
  formRef,
  initialFormData: initFormData
});
const { resetQuery } = useSearchReset({
  queryFormRef,
  queryParams,
  pageNumKey: 'pageNum',
  afterReset: () => handleQuery()
});

const dateRange = ref<[string, string] | null>(null);
const registerRange = ref<[string, string] | null>(null);

const configDialog = reactive({ visible: false });
const configForm = ref<ActivityForm>({ ...initFormData });
const configRange = ref<[string, string] | null>(null);
const configRegisterRange = ref<[string, string] | null>(null);

const activity_status = [
  { value: 0, label: '草稿' },
  { value: 1, label: '进行中' },
  { value: 2, label: '已结束' }
];

function statusText(status: number) {
  return activity_status.find((s) => s.value === status)?.label ?? '';
}
function statusTag(status: number) {
  return status === 1 ? 'success' : status === 2 ? 'warning' : 'info';
}

const getList = async () => {
  await withLoading(async () => {
    const res = await listActivity({ ...queryParams.value });
    activityList.value = res.rows;
    total.value = res.total;
  });
};

const cancel = () => {
  closeDialog();
  resetForm();
};

const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};

const handleAdd = () => {
  resetForm();
  dateRange.value = null;
  registerRange.value = null;
  openDialog('新增活动');
};

const handleUpdate = async (row?: Partial<ActivityVO>) => {
  resetForm();
  const res = await getActivity(row!.id!);
  Object.assign(form.value, res.data);
  dateRange.value = res.data.startDate && res.data.endDate ? [res.data.startDate, res.data.endDate] : null;
  registerRange.value = res.data.registerStart && res.data.registerEnd ? [res.data.registerStart, res.data.registerEnd] : null;
  showDialog('修改活动');
};

const submitForm = () => {
  formRef.value?.validate(async (valid: boolean) => {
    if (!valid) {
      return;
    }
    if (dateRange.value && dateRange.value.length === 2) {
      form.value.startDate = dateRange.value[0];
      form.value.endDate = dateRange.value[1];
    }
    if (registerRange.value && registerRange.value.length === 2) {
      form.value.registerStart = registerRange.value[0];
      form.value.registerEnd = registerRange.value[1];
    }
    form.value.id ? await updateActivity(form.value) : await addActivity(form.value);
    modal.msgSuccess('操作成功');
    closeDialog();
    await getList();
  });
};

const handleConfig = (row: Partial<ActivityVO>) => {
  Object.assign(configForm.value, row);
  configRange.value = row.startDate && row.endDate ? [row.startDate, row.endDate] : null;
  configRegisterRange.value = row.registerStart && row.registerEnd ? [row.registerStart, row.registerEnd] : null;
  configDialog.visible = true;
};

const submitConfig = async () => {
  const cfg = configForm.value;
  if (configRange.value && configRange.value.length === 2) {
    cfg.startDate = configRange.value[0];
    cfg.endDate = configRange.value[1];
  }
  if (configRegisterRange.value && configRegisterRange.value.length === 2) {
    cfg.registerStart = configRegisterRange.value[0];
    cfg.registerEnd = configRegisterRange.value[1];
  }
  await configActivity(cfg);
  modal.msgSuccess('参数配置成功');
  configDialog.visible = false;
  await getList();
};

const handleDelete = async (row?: Partial<ActivityVO>) => {
  await modal.confirm('是否确认删除活动【' + row?.activityName + '】？');
  await delActivity(row!.id!);
  await getList();
  modal.msgSuccess('删除成功');
};

onMounted(() => {
  getList();
});
</script>

<style lang="scss" scoped>
@use '@/assets/styles/components/page-shell' as pageShell;

@include pageShell.toolbar-responsive;
</style>
