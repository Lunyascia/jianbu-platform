<template>
  <div class="p-2 app-container walking-awardconfig-page">
    <div class="search-wrap">
      <el-card shadow="hover" class="search-panel">
        <el-form ref="queryFormRef" :model="queryParams" :inline="true" class="query-form">
          <el-form-item label="所属活动" prop="activityId">
            <el-select v-model="queryParams.activityId" placeholder="全部" clearable filterable style="width: 240px">
              <el-option v-for="opt in activityOptions" :key="opt.id" :label="opt.activityName" :value="opt.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="奖励名称" prop="awardName">
            <el-input v-model="queryParams.awardName" placeholder="请输入奖励名称" clearable @keyup.enter="handleQuery" />
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
            <span class="panel-kicker">Award Config</span>
            <h3>奖励列表</h3>
            <p>共 {{ total }} 条记录。修改后 H5 报名页与小程序奖励页即时展示最新奖励。</p>
          </div>
          <div class="toolbar-actions">
            <el-button v-hasPermi="['walking:awardConfig:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
            <right-toolbar v-model:show-search="showSearch" :search="false" @query-table="getList"></right-toolbar>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="awardList">
        <el-table-column label="所属活动" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ activityName(row.activityId) }}</template>
        </el-table-column>
        <el-table-column label="奖励名称" prop="awardName" min-width="140" show-overflow-tooltip />
        <el-table-column label="类型" align="center" width="80">
          <template #default="{ row }">
            <el-tag :type="row.awardType === 2 ? 'warning' : 'success'">{{ row.awardType === 2 ? '集体' : '个人' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="名额" align="center" width="90">
          <template #default="{ row }">{{ quotaText(row as AwardTierVO) }}</template>
        </el-table-column>
        <el-table-column label="奖励内容" prop="prizeContent" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" align="center" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="排序" prop="sortOrder" align="center" width="80" />
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-tooltip content="修改" placement="top">
              <el-button v-hasPermi="['walking:awardConfig:edit']" link type="primary" icon="Edit" @click="handleUpdate(row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['walking:awardConfig:remove']" link type="danger" icon="Delete" @click="handleDelete(row)"></el-button>
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

    <!-- 新增/修改奖励 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="600px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="所属活动" prop="activityId">
          <el-select v-model="form.activityId" placeholder="请选择活动" filterable style="width: 100%">
            <el-option v-for="opt in activityOptions" :key="opt.id" :label="opt.activityName" :value="opt.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="奖励类型" prop="awardType">
          <el-radio-group v-model="form.awardType">
            <el-radio :value="1">个人奖</el-radio>
            <el-radio :value="2">集体奖</el-radio>
          </el-radio-group>
          <div class="el-form-item__tip">个人奖按名次区间；集体奖（如"先进组织单位"）按名额区间</div>
        </el-form-item>
        <el-form-item label="奖励名称" prop="awardName">
          <el-input v-model="form.awardName" placeholder="如：一等奖 / 先进组织单位" />
        </el-form-item>
        <el-form-item label="名额区间" prop="rankStart">
          <el-input-number v-model="form.rankStart" :min="1" style="width: 160px" />
          <span style="margin: 0 8px">~</span>
          <el-input-number v-model="form.rankEnd" :min="0" style="width: 160px" />
          <div class="el-form-item__tip">第 {{ form.rankStart }} 名 至 第 {{ form.rankEnd }} 名；<b>结束填 0 表示"若干名/若干家"</b></div>
        </el-form-item>
        <el-form-item label="奖励内容" prop="prizeContent">
          <el-input v-model="form.prizeContent" type="textarea" :rows="2" placeholder="如：荣誉证书 + 价值 100 元健身礼包" />
        </el-form-item>
        <el-form-item label="奖品图片">
          <el-input v-model="form.imageUrl" placeholder="奖品图片地址（可留空）" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :step="1" style="width: 160px" />
          <div class="el-form-item__tip">越小越靠前展示</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="WalkingAwardConfig" lang="ts">
import { listAwardConfig, getAwardConfig, delAwardConfig, addAwardConfig, updateAwardConfig } from '@/api/walking/awardConfig';
import { AwardConfigForm, AwardConfigQuery, AwardTierVO } from '@/api/walking/awardConfig/types';
import { listActivityOptions } from '@/api/walking/activity';
import { ActivityVO } from '@/api/walking/activity/types';
import { useLoading } from '@/hooks/async/useLoading';
import { useFormDialog } from '@/hooks/dialog/useFormDialog';
import { useSearchReset } from '@/hooks/form/useSearchReset';
import { useSearchToggle } from '@/hooks/form/useSearchToggle';
import modal from '@/plugins/modal';

const awardList = ref<AwardTierVO[]>([]);
const activityOptions = ref<ActivityVO[]>([]);
const { loading, withLoading } = useLoading(true);
const { showSearch } = useSearchToggle();
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();
const formRef = ref<ElFormInstance>();

const initFormData: AwardConfigForm = {
  id: undefined,
  activityId: undefined,
  awardType: 1,
  awardName: '',
  rankStart: 1,
  rankEnd: 5,
  prizeContent: '',
  imageUrl: '',
  status: 1,
  sortOrder: 0
};
const data = reactive<PageData<AwardConfigForm, AwardConfigQuery>>({
  form: { ...initFormData },
  queryParams: { pageNum: 1, pageSize: 10, activityId: undefined, awardName: '' },
  rules: {
    activityId: [{ required: true, message: '请选择所属活动', trigger: 'change' }],
    awardName: [{ required: true, message: '奖励名称不能为空', trigger: 'blur' }]
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

const activityNameMap = computed(() => {
  const map = new Map<number | string, string>();
  activityOptions.value.forEach((o) => map.set(o.id, o.activityName));
  return map;
});

function activityName(id: number | string) {
  return activityNameMap.value.get(id) ?? '-';
}

function quotaText(row: AwardTierVO) {
  const unit = row.awardType === 2 ? '家' : '名';
  if (!row.rankEnd || row.rankEnd === 0) {
    return '若干' + unit;
  }
  return row.rankEnd - row.rankStart + 1 + unit;
}

const getList = async () => {
  await withLoading(async () => {
    const res = await listAwardConfig({ ...queryParams.value });
    awardList.value = res.rows;
    total.value = res.total;
  });
};

const loadActivities = async () => {
  try {
    const res = await listActivityOptions();
    activityOptions.value = res.data || [];
  } catch (e) { /* 后端未就绪 */ }
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
  if (activityOptions.value.length === 1) {
    form.value.activityId = activityOptions.value[0].id;
  }
  openDialog('新增奖励');
};

const handleUpdate = async (row?: Partial<AwardTierVO>) => {
  resetForm();
  const res = await getAwardConfig(row!.id!);
  Object.assign(form.value, res.data);
  showDialog('修改奖励');
};

const submitForm = () => {
  formRef.value?.validate(async (valid: boolean) => {
    if (!valid) {
      return;
    }
    form.value.id ? await updateAwardConfig(form.value) : await addAwardConfig(form.value);
    modal.msgSuccess('操作成功');
    closeDialog();
    await getList();
  });
};

const handleDelete = async (row?: Partial<AwardTierVO>) => {
  await modal.confirm('是否确认删除奖励【' + row?.awardName + '】？');
  await delAwardConfig(row!.id!);
  await getList();
  modal.msgSuccess('删除成功');
};

onMounted(() => {
  loadActivities();
  getList();
});
</script>

<style lang="scss" scoped>
@use '@/assets/styles/components/page-shell' as pageShell;

@include pageShell.toolbar-responsive;
</style>
