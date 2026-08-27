<template>
  <div class="p-2 app-container walking-registration-page">
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
          <el-form-item label="活动" prop="activityId">
            <el-select v-model="queryParams.activityId" placeholder="全部活动" clearable filterable style="width: 180px">
              <el-option v-for="a in activityOptions" :key="a.id" :label="a.activityName" :value="a.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 130px">
              <el-option v-for="s in regStatusOptions" :key="s.value" :label="s.label" :value="s.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="单位" prop="deptId">
            <el-tree-select
              v-model="queryParams.deptId"
              :data="deptTree"
              :props="{ label: 'deptName', children: 'children' }"
              node-key="deptId"
              check-strictly
              clearable
              filterable
              placeholder="全部单位"
              style="width: 180px"
            />
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
            <span class="panel-kicker">Registration</span>
            <h3>报名列表</h3>
            <p>共 {{ total }} 条记录，支持取消报名、调整单位、撤下停用与分单位导出。</p>
          </div>
          <div class="toolbar-actions">
            <el-button v-hasPermi="['walking:registration:export']" type="warning" plain icon="Download" @click="handleExport">
              导出报名信息
            </el-button>
            <right-toolbar v-model:show-search="showSearch" :search="false" @query-table="getList"></right-toolbar>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="registrationList">
        <el-table-column label="姓名" prop="realName" min-width="90" />
        <el-table-column label="手机号" prop="phone" min-width="120" />
        <el-table-column label="单位" prop="deptName" min-width="140" show-overflow-tooltip />
        <el-table-column label="状态" align="center" width="100">
          <template #default="{ row }">
            <el-tag :type="regStatusTag(row.status)">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" prop="submitTime" width="170" />
        <el-table-column label="审核结果" prop="auditResult" min-width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="250" align="center">
          <template #default="{ row }">
            <el-tooltip content="审核通过（待审核→报名成功，通过后才可绑定）" placement="top">
              <el-button
                v-if="row.status === 1"
                v-hasPermi="['walking:registration:approve']"
                link type="success"
                icon="CircleCheck"
                @click="handleApprove(row)"
              >通过</el-button>
            </el-tooltip>
            <el-tooltip content="取消报名" placement="top">
              <el-button
                v-if="row.status === 2"
                v-hasPermi="['walking:registration:cancel']"
                link type="warning"
                icon="CircleClose"
                @click="handleCancel(row)"
              ></el-button>
            </el-tooltip>
            <el-tooltip content="调整单位" placement="top">
              <el-button v-hasPermi="['walking:registration:adjust']" link type="primary" icon="OfficeBuilding" @click="handleAdjust(row)"></el-button>
            </el-tooltip>
            <el-tooltip content="撤下/停用" placement="top">
              <el-button
                v-if="row.status !== 4"
                v-hasPermi="['walking:registration:disable']"
                link type="danger"
                icon="Lock"
                @click="handleDisable(row)"
              ></el-button>
            </el-tooltip>
            <el-tooltip content="审核日志" placement="top">
              <el-button v-hasPermi="['walking:audit:list']" link type="info" icon="Document" @click="handleLog(row)"></el-button>
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

    <!-- 调整单位 -->
    <el-dialog v-model="adjustDialog.visible" title="调整单位" width="420px" append-to-body>
      <el-form ref="adjustFormRef" :model="adjustForm" :rules="adjustRules" label-width="80px">
        <el-form-item label="会员姓名" prop="realName">
          <el-input v-model="adjustForm.realName" disabled />
        </el-form-item>
        <el-form-item label="调整单位" prop="deptId">
          <el-tree-select
            v-model="adjustForm.deptId"
            :data="deptTree"
            :props="{ label: 'deptName', children: 'children' }"
            node-key="deptId"
            check-strictly
            filterable
            style="width: 100%"
            @change="handleDeptChange"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitAdjust">确 定</el-button>
          <el-button @click="adjustDialog.visible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="WalkingRegistration" lang="ts">
import { listRegistration, cancelRegistration, disableRegistration, adjustRegistration, approveRegistration } from '@/api/walking/registration';
import { RegistrationQuery, RegistrationVO } from '@/api/walking/registration/types';
import { listActivityOptions } from '@/api/walking/activity';
import { ActivityVO } from '@/api/walking/activity/types';
import { treeOrg } from '@/api/walking/org';
import { OrgVO } from '@/api/walking/org/types';
import { useLoading } from '@/hooks/async/useLoading';
import { useSearchReset } from '@/hooks/form/useSearchReset';
import { useSearchToggle } from '@/hooks/form/useSearchToggle';
import modal from '@/plugins/modal';
import { download as requestDownload } from '@/utils/request';
import { useRouter } from 'vue-router';

const router = useRouter();

const registrationList = ref<RegistrationVO[]>([]);
const activityOptions = ref<ActivityVO[]>([]);
const deptTree = ref<OrgVO[]>([]);
const { loading, withLoading } = useLoading(true);
const { showSearch } = useSearchToggle();
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();
const adjustFormRef = ref<ElFormInstance>();

const regStatusOptions = [
  { value: 1, label: '待审核' },
  { value: 2, label: '报名成功' },
  { value: 3, label: '已取消' },
  { value: 4, label: '已停用' }
];

const data = reactive<PageData<any, RegistrationQuery>>({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    activityId: undefined,
    status: undefined,
    deptId: undefined,
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

const adjustDialog = reactive({ visible: false });
const adjustForm = ref<Partial<RegistrationVO>>({});
const adjustRules = {
  deptId: [{ required: true, message: '请选择调整后的单位', trigger: 'change' }]
};

function regStatusTag(status: number) {
  return status === 2 ? 'success' : status === 1 ? 'warning' : status === 4 ? 'danger' : 'info';
}

const getList = async () => {
  await withLoading(async () => {
    const res = await listRegistration({ ...queryParams.value });
    registrationList.value = res.rows;
    total.value = res.total;
  });
};

const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};

const handleApprove = async (row: Partial<RegistrationVO>) => {
  await modal.confirm('确认通过【' + row.realName + '】的报名吗？通过后即可在小程序绑定该手机号。');
  await approveRegistration(row.id!);
  modal.msgSuccess('已审核通过');
  await getList();
};

const handleCancel = async (row: Partial<RegistrationVO>) => {
  await modal.confirm('确认取消会员【' + row.realName + '】的报名吗？');
  await cancelRegistration(row.id!);
  modal.msgSuccess('已取消报名');
  await getList();
};

const handleDisable = async (row: Partial<RegistrationVO>) => {
  await modal.confirm('确认撤下【' + row.realName + '】的报名信息并停用其账号吗？此操作会同步停用账号。');
  await disableRegistration(row.id!);
  modal.msgSuccess('已撤下并停用');
  await getList();
};

const handleAdjust = (row: Partial<RegistrationVO>) => {
  adjustForm.value = { ...row };
  adjustDialog.visible = true;
};

const handleDeptChange = (val: number | string) => {
  const found = findDept(deptTree.value, val);
  if (found) {
    adjustForm.value.deptName = found.deptName;
  }
};

const submitAdjust = () => {
  adjustFormRef.value?.validate(async (valid: boolean) => {
    if (!valid) {
      return;
    }
    await adjustRegistration(adjustForm.value.id!, adjustForm.value.deptId!, adjustForm.value.deptName || '');
    modal.msgSuccess('调整成功');
    adjustDialog.visible = false;
    await getList();
  });
};

const handleExport = () => {
  requestDownload(
    'walking/admin/registration/export',
    {
      activityId: queryParams.value.activityId,
      deptId: queryParams.value.deptId
    },
    `registration_${new Date().getTime()}.xlsx`
  );
};

const handleLog = (row: Partial<RegistrationVO>) => {
  // 跳转审核日志页并携带报名id
  router.push({ path: '/walking/audit', query: { registrationId: String(row.id) } });
};

function findDept(nodes: OrgVO[], deptId: number | string): OrgVO | undefined {
  for (const node of nodes) {
    if (String(node.deptId) === String(deptId)) {
      return node;
    }
    if (node.children && node.children.length) {
      const found = findDept(node.children, deptId);
      if (found) {
        return found;
      }
    }
  }
  return undefined;
}

onMounted(async () => {
  getList();
  const [acts, depts] = await Promise.all([listActivityOptions(), treeOrg()]);
  activityOptions.value = acts.data;
  deptTree.value = depts.data;
});
</script>

<style lang="scss" scoped>
@use '@/assets/styles/components/page-shell' as pageShell;

@include pageShell.toolbar-responsive;
</style>
