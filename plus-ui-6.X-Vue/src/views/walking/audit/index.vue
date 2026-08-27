<template>
  <div class="p-2 app-container walking-audit-page">
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
          <el-form-item label="审核动作" prop="auditAction">
            <el-select v-model="queryParams.auditAction" placeholder="全部动作" clearable style="width: 150px">
              <el-option v-for="s in actionOptions" :key="s.value" :label="s.label" :value="s.value" />
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
            <span class="panel-kicker">Audit Log</span>
            <h3>报名审核日志</h3>
            <p>共 {{ total }} 条记录，记录报名提交、自动审核与管理员处理过程。</p>
          </div>
          <right-toolbar v-model:show-search="showSearch" :search="false" @query-table="getList"></right-toolbar>
        </div>
      </template>

      <el-table v-loading="loading" :data="auditList">
        <el-table-column label="姓名" prop="realName" min-width="90" />
        <el-table-column label="手机号" prop="phone" min-width="120" />
        <el-table-column label="审核动作" align="center" width="130">
          <template #default="{ row }">
            <el-tag>{{ row.auditAction }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审核结果/说明" prop="auditResult" min-width="200" show-overflow-tooltip />
        <el-table-column label="审核人" prop="auditor" align="center" width="110" />
        <el-table-column label="审核时间" prop="createTime" width="170" />
      </el-table>
      <pagination
        v-show="total > 0"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        :total="total"
        @pagination="getList"
      />
    </el-card>
  </div>
</template>

<script setup name="WalkingAudit" lang="ts">
import { listAudit } from '@/api/walking/audit';
import { AuditLogQuery, AuditLogVO } from '@/api/walking/audit/types';
import { useLoading } from '@/hooks/async/useLoading';
import { useSearchReset } from '@/hooks/form/useSearchReset';
import { useSearchToggle } from '@/hooks/form/useSearchToggle';
import { useRoute } from 'vue-router';

const route = useRoute();
const auditList = ref<AuditLogVO[]>([]);
const { loading, withLoading } = useLoading(true);
const { showSearch } = useSearchToggle();
const total = ref(0);

const queryFormRef = ref<ElFormInstance>();

const actionOptions = [
  { value: '提交', label: '提交' },
  { value: '自动审核通过', label: '自动审核通过' },
  { value: '自动审核拒绝', label: '自动审核拒绝' },
  { value: '取消', label: '取消' },
  { value: '撤下/停用', label: '撤下/停用' },
  { value: '调整单位', label: '调整单位' }
];

const data = reactive<PageData<any, AuditLogQuery>>({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    registrationId: undefined,
    memberId: undefined,
    auditAction: '',
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

const getList = async () => {
  await withLoading(async () => {
    const res = await listAudit({ ...queryParams.value });
    auditList.value = res.rows;
    total.value = res.total;
  });
};

const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};

onMounted(() => {
  const regId = route.query.registrationId as string | undefined;
  if (regId) {
    queryParams.value.registrationId = regId;
  }
  getList();
});
</script>

<style lang="scss" scoped>
@use '@/assets/styles/components/page-shell' as pageShell;

@include pageShell.toolbar-responsive;
</style>
