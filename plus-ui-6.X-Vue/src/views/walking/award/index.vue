<template>
  <div class="p-2 app-container walking-award-page">
    <div class="search-wrap">
      <el-card shadow="hover" class="search-panel">
        <el-form :model="queryParams" inline>
          <el-form-item label="选择活动" prop="activityId">
            <el-select v-model="queryParams.activityId" placeholder="请选择活动" clearable filterable style="width: 220px" @change="handleQuery">
              <el-option v-for="a in activityOptions" :key="a.id" :label="a.activityName" :value="a.id" />
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
            <span class="panel-kicker">Award List</span>
            <h3>中奖名单</h3>
            <p>标记中奖人员（一等奖/二等奖/三等奖/优秀奖），中奖用户可在小程序奖项页查看，可导出收货信息。</p>
          </div>
          <div class="toolbar-actions">
            <el-button v-hasPermi="['walking:award:mark']" type="success" plain icon="Trophy" @click="handleAutoMark">按排行自动生成</el-button>
            <el-button v-hasPermi="['walking:award:mark']" type="primary" plain icon="Plus" @click="handleMark">标记中奖</el-button>
            <el-button v-hasPermi="['walking:award:export']" type="warning" plain icon="Download" @click="handleExport">导出名单</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="awardList">
        <el-table-column prop="rank" label="名次" align="center" width="70" />
        <el-table-column prop="realName" label="姓名" min-width="90" />
        <el-table-column prop="phone" label="手机号" min-width="120" />
        <el-table-column prop="deptName" label="单位" min-width="130" show-overflow-tooltip />
        <el-table-column label="奖项" align="center" width="100">
          <template #default="{ row }">
            <el-tag :type="awardTag(row.awardLevel)">{{ row.awardLevel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="收货地址" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ row.receiver ? row.receiver + ' ' + row.addressPhone + ' ' + row.address : '未填写' }}</template>
        </el-table-column>
        <el-table-column prop="issueTime" label="颁发时间" width="170" />
        <el-table-column label="操作" width="80" align="center">
          <template #default="{ row }">
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['walking:award:remove']" link type="danger" icon="Delete" @click="handleDelete(row)"></el-button>
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

    <!-- 标记中奖 -->
    <el-dialog v-model="markDialog.visible" title="标记中奖" width="480px" append-to-body>
      <el-form :model="markForm" :rules="markRules" ref="markFormRef" label-width="90px">
        <el-form-item label="选择会员" prop="memberId">
          <el-select
            v-model="markForm.memberId"
            filterable
            remote
            :remote-method="searchMember"
            :loading="memberLoading"
            placeholder="输入姓名/手机号搜索"
            style="width: 100%"
          >
            <el-option v-for="m in memberOptions" :key="m.id" :label="m.realName + ' / ' + m.phone + ' / ' + m.deptName" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="奖项级别" prop="awardLevel">
          <el-select v-model="markForm.awardLevel" style="width: 100%">
            <el-option v-for="l in awardLevels" :key="l.value" :label="l.label" :value="l.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="名次" prop="rank">
          <el-input-number v-model="markForm.rank" :min="1" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitMark">确 定</el-button>
          <el-button @click="markDialog.visible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="WalkingAward" lang="ts">
import { listAward, markAward, autoMarkAward, delAward } from '@/api/walking/award';
import { AwardForm, AwardQuery, AwardWinnerVO } from '@/api/walking/award/types';
import { listActivityOptions } from '@/api/walking/activity';
import { ActivityVO } from '@/api/walking/activity/types';
import { listMember } from '@/api/walking/member';
import { MemberVO } from '@/api/walking/member/types';
import { useLoading } from '@/hooks/async/useLoading';
import { useSearchReset } from '@/hooks/form/useSearchReset';
import modal from '@/plugins/modal';
import { download as requestDownload } from '@/utils/request';

const awardList = ref<AwardWinnerVO[]>([]);
const activityOptions = ref<ActivityVO[]>([]);
const memberOptions = ref<MemberVO[]>([]);
const memberLoading = ref(false);
const { loading, withLoading } = useLoading(true);
const total = ref(0);
const queryFormRef = ref<ElFormInstance>();

const data = reactive<PageData<any, AwardQuery>>({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, activityId: undefined, keyword: '' },
  rules: {}
});
const { queryParams } = toRefs(data);
const { resetQuery } = useSearchReset({
  queryFormRef,
  queryParams,
  pageNumKey: 'pageNum',
  afterReset: () => handleQuery()
});

const markDialog = reactive({ visible: false });
const markFormRef = ref<ElFormInstance>();
const markForm = ref<AwardForm>({ activityId: undefined, memberId: undefined, awardLevel: '三等奖', rank: 1 });
const markRules = {
  memberId: [{ required: true, message: '请选择会员', trigger: 'change' }],
  awardLevel: [{ required: true, message: '请选择奖项级别', trigger: 'change' }]
};
const awardLevels = [
  { value: '一等奖', label: '一等奖' },
  { value: '二等奖', label: '二等奖' },
  { value: '三等奖', label: '三等奖' },
  { value: '优秀奖', label: '优秀奖' }
];

function awardTag(level: string) {
  return level === '一等奖' ? 'danger' : level === '二等奖' ? 'warning' : level === '三等奖' ? 'primary' : 'info';
}

const getList = async () => {
  await withLoading(async () => {
    const res = await listAward({ ...queryParams.value });
    awardList.value = res.rows;
    total.value = res.total;
  });
};

const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};

const searchMember = async (keyword: string) => {
  if (!keyword) {
    memberOptions.value = [];
    return;
  }
  memberLoading.value = true;
  try {
    const res = await listMember({ pageNum: 1, pageSize: 20, deptId: undefined, status: undefined, keyword });
    memberOptions.value = res.rows;
  } finally {
    memberLoading.value = false;
  }
};

const handleMark = () => {
  if (!queryParams.value.activityId) {
    modal.msgWarning('请先选择活动');
    return;
  }
  markForm.value = { activityId: queryParams.value.activityId, memberId: undefined, awardLevel: '三等奖', rank: 1 };
  markDialog.visible = true;
};

const submitMark = () => {
  markFormRef.value?.validate(async (valid: boolean) => {
    if (!valid) {
      return;
    }
    await markAward(markForm.value);
    modal.msgSuccess('标记成功');
    markDialog.visible = false;
    await getList();
  });
};

const handleAutoMark = async () => {
  if (!queryParams.value.activityId) {
    modal.msgWarning('请先选择活动');
    return;
  }
  await modal.confirm('将按当前排行与奖项档位自动生成中奖名单（覆盖原名单），确认执行吗？');
  await autoMarkAward(queryParams.value.activityId);
  modal.msgSuccess('已自动生成');
  await getList();
};

const handleDelete = async (row: Partial<AwardWinnerVO>) => {
  await modal.confirm('确认从名单中移除【' + row.realName + '】吗？');
  await delAward(row.id!);
  await getList();
  modal.msgSuccess('已移除');
};

const handleExport = () => {
  requestDownload('walking/admin/award/export', { activityId: queryParams.value.activityId }, `award_${new Date().getTime()}.xlsx`);
};

onMounted(async () => {
  const res = await listActivityOptions();
  activityOptions.value = res.data;
  if (activityOptions.value.length) {
    queryParams.value.activityId = activityOptions.value[0].id;
  }
  getList();
});
</script>

<style lang="scss" scoped>
@use '@/assets/styles/components/page-shell' as pageShell;

@include pageShell.toolbar-responsive;
</style>
