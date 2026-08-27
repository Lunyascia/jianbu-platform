<template>
  <div class="p-2 app-container walking-member-page">
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
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 120px">
              <el-option v-for="s in memberStatusOptions" :key="s.value" :label="s.label" :value="s.value" />
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
            <span class="panel-kicker">Member</span>
            <h3>会员列表</h3>
            <p>共 {{ total }} 条记录，支持信息维护、账号停用与打卡信息导出。</p>
          </div>
          <div class="toolbar-actions">
            <el-button v-hasPermi="['walking:member:remove']" type="danger" plain icon="Delete" :disabled="!selectedIds.length" @click="handleDelete()">
              删除
            </el-button>
            <el-button v-hasPermi="['walking:member:export']" type="warning" plain icon="Download" @click="handleExport">
              导出打卡信息
            </el-button>
            <right-toolbar v-model:show-search="showSearch" :search="false" @query-table="getList"></right-toolbar>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="memberList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="姓名" prop="realName" min-width="90" />
        <el-table-column label="手机号" prop="phone" min-width="120" />
        <el-table-column label="单位" prop="deptName" min-width="140" show-overflow-tooltip />
        <el-table-column label="注册时间" prop="registerTime" width="170" />
        <el-table-column label="报名数" prop="regCount" align="center" width="90" />
        <el-table-column label="是否登录小程序" align="center" width="130">
          <template #default="{ row }">
            <el-tag :type="row.loggedIn === 1 ? 'success' : 'info'">{{ row.loggedIn === 1 ? '已登录' : '未登录' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'danger' : 'success'">{{ row.status === 1 ? '已停用' : '正常' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="210" align="center">
          <template #default="{ row }">
            <el-tooltip content="打卡明细" placement="top">
              <el-button link type="success" icon="Calendar" @click="handleCheckin(row)"></el-button>
            </el-tooltip>
            <el-tooltip content="积分明细" placement="top">
              <el-button link type="warning" icon="Coin" @click="handlePoints(row)"></el-button>
            </el-tooltip>
            <el-tooltip content="编辑" placement="top">
              <el-button v-hasPermi="['walking:member:edit']" link type="primary" icon="Edit" @click="handleUpdate(row)"></el-button>
            </el-tooltip>
            <el-tooltip :content="row.status === 1 ? '启用账号' : '停用账号'" placement="top">
              <el-button
                v-hasPermi="['walking:member:disable']"
                link
                :type="row.status === 1 ? 'success' : 'danger'"
                :icon="row.status === 1 ? 'CircleCheck' : 'CircleClose'"
                @click="handleChangeStatus(row)"
              ></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['walking:member:remove']" link type="danger" icon="Delete" @click="handleDelete(row)"></el-button>
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

    <!-- 会员信息维护 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="520px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="所属单位" prop="deptId">
          <el-tree-select
            v-model="form.deptId"
            :data="deptTree"
            :props="{ label: 'deptName', children: 'children' }"
            node-key="deptId"
            check-strictly
            filterable
            style="width: 100%"
            @change="handleDeptChange"
          />
        </el-form-item>
        <el-form-item label="收货人" prop="receiver">
          <el-input v-model="form.receiver" placeholder="请输入收货人" />
        </el-form-item>
        <el-form-item label="收货手机号" prop="addressPhone">
          <el-input v-model="form.addressPhone" placeholder="请输入收货手机号" />
        </el-form-item>
        <el-form-item label="收货地址" prop="address">
          <el-input v-model="form.address" type="textarea" :rows="2" placeholder="请输入收货地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 打卡/积分明细 -->
    <el-dialog v-model="detailDialog.visible" :title="detailDialog.title" width="860px" append-to-body top="5vh">
      <el-form inline style="margin-bottom: 8px">
        <el-form-item label="活动">
          <el-select v-model="detailActivityId" placeholder="请选择活动" clearable filterable style="width: 220px" @change="loadDetail">
            <el-option v-for="a in activityOptions" :key="a.id" :label="a.activityName" :value="a.id" />
          </el-select>
        </el-form-item>
      </el-form>
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

<script setup name="WalkingMember" lang="ts">
import { listMember, getMember, delMember, updateMember, changeMemberStatus } from '@/api/walking/member';
import { MemberForm, MemberQuery, MemberVO } from '@/api/walking/member/types';
import { treeOrg } from '@/api/walking/org';
import { OrgVO } from '@/api/walking/org/types';
import { getCheckinDetail, getPointsDetail } from '@/api/walking/stats';
import { CheckinDetailVO, PointsDetailVO } from '@/api/walking/stats/types';
import { listActivityOptions } from '@/api/walking/activity';
import { ActivityVO } from '@/api/walking/activity/types';
import { useLoading } from '@/hooks/async/useLoading';
import { useFormDialog } from '@/hooks/dialog/useFormDialog';
import { useSearchReset } from '@/hooks/form/useSearchReset';
import { useSearchToggle } from '@/hooks/form/useSearchToggle';
import modal from '@/plugins/modal';
import { download as requestDownload } from '@/utils/request';

const memberList = ref<MemberVO[]>([]);
const deptTree = ref<OrgVO[]>([]);
const activityOptions = ref<ActivityVO[]>([]);
const { loading, withLoading } = useLoading(true);
const { showSearch } = useSearchToggle();
const total = ref(0);

const detailDialog = reactive({ visible: false, title: '' });
const detailTab = ref('checkin');
const detailMemberId = ref<number | string | undefined>(undefined);
const detailActivityId = ref<number | string | undefined>(undefined);
const checkinList = ref<CheckinDetailVO[]>([]);
const pointsDetail = ref<PointsDetailVO | null>(null);

const queryFormRef = ref<ElFormInstance>();
const formRef = ref<ElFormInstance>();

const memberStatusOptions = [
  { value: 0, label: '正常' },
  { value: 1, label: '已停用' }
];

const initFormData: MemberForm = {
  id: undefined,
  deptId: undefined,
  deptName: '',
  realName: '',
  idCard: '',
  receiver: '',
  addressPhone: '',
  address: ''
};
const data = reactive<PageData<MemberForm, MemberQuery>>({
  form: { ...initFormData },
  queryParams: { pageNum: 1, pageSize: 10, deptId: undefined, status: undefined, keyword: '' },
  rules: {
    realName: [{ required: true, message: '姓名不能为空', trigger: 'blur' }]
  }
});
const selectedIds = ref<(number | string)[]>([]);
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

const getList = async () => {
  await withLoading(async () => {
    const res = await listMember({ ...queryParams.value });
    memberList.value = res.rows;
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

const handleUpdate = async (row?: Partial<MemberVO>) => {
  resetForm();
  const res = await getMember(row!.id!);
  Object.assign(form.value, res.data);
  showDialog('编辑会员');
};

const handleSelectionChange = (rows: MemberVO[]) => {
  selectedIds.value = rows.map((r) => r.id);
};

const handleDelete = async (row?: Partial<MemberVO>) => {
  const ids = row ? [row.id!] : selectedIds.value;
  const names = row ? `【${row.realName}】` : '';
  await modal.confirm(`确认删除会员${names}吗？删除后不可恢复。`);
  await delMember(ids);
  modal.msgSuccess('删除成功');
  await getList();
};

const handleDeptChange = (val: number | string) => {
  const found = findDept(deptTree.value, val);
  if (found) {
    form.value.deptName = found.deptName;
  }
};

const submitForm = () => {
  formRef.value?.validate(async (valid: boolean) => {
    if (!valid) {
      return;
    }
    await updateMember(form.value);
    modal.msgSuccess('操作成功');
    closeDialog();
    await getList();
  });
};

const handleChangeStatus = async (row: Partial<MemberVO>) => {
  const toDisable = row.status === 0;
  await modal.confirm(toDisable ? '确认停用【' + row.realName + '】的账号吗？停用将同步撤下其有效报名。' : '确认启用【' + row.realName + '】的账号吗？');
  await changeMemberStatus(row.id!, toDisable ? 1 : 0);
  modal.msgSuccess(toDisable ? '已停用' : '已启用');
  await getList();
};

const handleExport = () => {
  requestDownload(
    'walking/admin/member/export/steps',
    {
      activityId: undefined,
      deptId: queryParams.value.deptId
    },
    `steps_${new Date().getTime()}.xlsx`
  );
};

const handleCheckin = (row: Partial<MemberVO>) => {
  detailMemberId.value = row.id;
  detailDialog.title = '打卡明细：' + row.realName + ' / ' + row.phone;
  detailTab.value = 'checkin';
  detailDialog.visible = true;
  if (detailActivityId.value !== undefined) {
    loadDetail();
  }
};

const handlePoints = (row: Partial<MemberVO>) => {
  detailMemberId.value = row.id;
  detailDialog.title = '积分明细：' + row.realName + ' / ' + row.phone;
  detailTab.value = 'points';
  detailDialog.visible = true;
  if (detailActivityId.value !== undefined) {
    loadDetail();
  }
};

const loadDetail = async () => {
  if (detailMemberId.value === undefined || detailActivityId.value === undefined) {
    return;
  }
  const [c, p] = await Promise.all([getCheckinDetail(detailMemberId.value, detailActivityId.value), getPointsDetail(detailMemberId.value, detailActivityId.value)]);
  checkinList.value = c.data;
  pointsDetail.value = p.data;
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
  const [depts, acts] = await Promise.all([treeOrg(), listActivityOptions()]);
  deptTree.value = depts.data;
  activityOptions.value = acts.data;
  if (activityOptions.value.length) {
    detailActivityId.value = activityOptions.value[0].id;
  }
});
</script>

<style lang="scss" scoped>
@use '@/assets/styles/components/page-shell' as pageShell;

@include pageShell.toolbar-responsive;

.detail-summary {
  margin-bottom: 12px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
</style>
