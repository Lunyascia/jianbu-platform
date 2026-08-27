<template>
  <div class="p-2 app-container admin-identity-page">
    <!-- 两种身份 Tab -->
    <el-tabs v-model="activeRoleId" class="identity-tabs" @tab-change="switchIdentity">
      <el-tab-pane label="系统管理员" :name="'100'"></el-tab-pane>
      <el-tab-pane label="管理员" :name="'101'"></el-tab-pane>
    </el-tabs>

    <!-- 当前身份账号列表 -->
    <el-card v-loading="loading" shadow="hover" class="table-panel">
      <template #header>
        <div class="toolbar-shell">
          <div class="table-heading">
            <span class="panel-kicker">Admin Identity</span>
            <h3>{{ identityName }}账号</h3>
            <p>管理后台登录账号。系统管理员拥有全部权限；管理员可停用异常报名与账号。</p>
          </div>
          <div class="toolbar-actions">
            <el-button
              v-if="activeRoleId === '101'"
              v-hasPermi="['walking:userPerm:edit']"
              type="warning"
              plain
              icon="Operation"
              @click="openBatchPerm"
            >批量权限配置</el-button>
            <el-button
              v-else
              v-hasPermi="['system:role:edit']"
              type="warning"
              plain
              icon="Setting"
              @click="openPermission"
            >权限配置</el-button>
            <el-button v-hasPermi="['system:user:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="userList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="账号" prop="userName" min-width="120" />
        <el-table-column label="昵称" prop="nickName" min-width="120">
          <template #default="{ row }">{{ row.nickName || '-' }}</template>
        </el-table-column>
        <el-table-column label="部门" prop="deptName" min-width="130">
          <template #default="{ row }">{{ row.dept?.deptName || row.deptName || '-' }}</template>
        </el-table-column>
        <el-table-column label="手机号" prop="phoneNumber" min-width="120">
          <template #default="{ row }">{{ row.phoneNumber || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" align="center" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === '1' ? 'danger' : 'success'">{{ row.status === '1' ? '停用' : '正常' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="170" />
        <el-table-column label="操作" width="320" align="center">
          <template #default="{ row }">
            <el-tooltip v-if="activeRoleId === '101'" content="权限配置" placement="top">
              <el-button v-hasPermi="['walking:userPerm:edit']" link type="success" icon="Setting" @click="openUserPerm(row)"></el-button>
            </el-tooltip>
            <el-tooltip content="编辑" placement="top">
              <el-button v-hasPermi="['system:user:edit']" link type="primary" icon="Edit" @click="handleUpdate(row)"></el-button>
            </el-tooltip>
            <el-tooltip content="重置密码" placement="top">
              <el-button v-hasPermi="['system:user:resetPwd']" link type="warning" icon="Key" @click="handleResetPwd(row)"></el-button>
            </el-tooltip>
            <el-tooltip :content="row.status === '1' ? '启用' : '停用'" placement="top">
              <el-button
                v-hasPermi="['system:user:edit']"
                link
                :type="row.status === '1' ? 'success' : 'danger'"
                :icon="row.status === '1' ? 'CircleCheck' : 'CircleClose'"
                @click="handleStatus(row)"
              ></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['system:user:remove']" link type="danger" icon="Delete" @click="handleDelete(row)"></el-button>
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

    <!-- 新增/编辑账号 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="520px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="账号" prop="userName">
          <el-input v-model="form.userName" :disabled="!!form.userId" placeholder="登录账号" />
        </el-form-item>
        <el-form-item v-if="!form.userId" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="登录密码" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickName">
          <el-input v-model="form.nickName" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="归属部门" prop="deptId">
          <el-tree-select
            v-model="form.deptId"
            :data="deptTree"
            :props="{ label: 'label', children: 'children' }"
            value-key="id"
            check-strictly
            filterable
            placeholder="请选择部门"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phoneNumber">
          <el-input v-model="form.phoneNumber" maxlength="11" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="身份">
          <el-tag>{{ identityName }}</el-tag>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 权限配置 -->
    <el-dialog v-model="permDialog.visible" :title="permDialog.title" width="520px" append-to-body>
      <el-alert type="info" :closable="false" style="margin-bottom: 12px">
        <p>勾选该管理员可访问的健步走子菜单与按钮权限。未勾选的不出现在该管理员登录后的侧边栏、也不可访问。</p>
      </el-alert>
      <el-tree
        ref="permTreeRef"
        :data="permTreeData"
        :props="{ label: 'label', children: 'children' }"
        show-checkbox
        node-key="id"
        default-expand-all
      />
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="permDialog.userIds.length ? submitUserPerm() : submitPermission()">保存权限</el-button>
          <el-button @click="permDialog.visible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AdminIdentity" lang="ts">
import { addUser, updateUser, delUser, resetUserPwd, changeUserStatus, getUser, deptTreeSelect } from '@/api/system/user';
import { allocatedUserList, getRole, updateRole } from '@/api/system/role';
import { roleMenuTreeselect } from '@/api/system/menu';
import { getUserPermTree, saveUserPerm, batchUserPerm } from '@/api/walking/userPerm';
import type { UserForm, UserVO } from '@/api/system/user/types';
import { useLoading } from '@/hooks/async/useLoading';
import { useFormDialog } from '@/hooks/dialog/useFormDialog';
import modal from '@/plugins/modal';

// 两种固定身份
type IdentityRoleId = '100' | '101';

const IDENTITIES: Record<IdentityRoleId, { name: string; roleId: number }> = {
  '100': { name: '系统管理员', roleId: 100 },
  '101': { name: '管理员', roleId: 101 }
};

const activeRoleId = ref<IdentityRoleId>('100');
const identityName = computed(() => IDENTITIES[activeRoleId.value].name);

const userList = ref<UserVO[]>([]);
const deptTree = ref<any[]>([]);
const total = ref(0);
const { loading, withLoading } = useLoading(true);

const formRef = ref<ElFormInstance>();
const permTreeRef = ref<ElTreeInstance>();
const permDialog = reactive({ visible: false, title: '', userIds: [] as (string | number)[] });
const permTreeData = ref<any[]>([]);
const selectedRows = ref<UserVO[]>([]);

const queryParams = reactive({ pageNum: 1, pageSize: 10, userName: '', status: '' });

const initFormData: UserForm = {
  userId: undefined,
  userName: '',
  password: '',
  nickName: '',
  deptId: undefined,
  phoneNumber: '',
  email: '',
  status: '0',
  roleIds: [],
  postIds: []
};
const data = reactive<PageData<UserForm, any>>({
  form: { ...initFormData },
  queryParams: { pageNum: 1, pageSize: 10 },
  rules: {
    userName: [
      { required: true, message: '账号不能为空', trigger: 'blur' },
      { min: 2, max: 20, message: '账号长度必须介于 2 和 20 之间', trigger: 'blur' }
    ],
    password: [{ required: true, message: '密码不能为空', trigger: 'blur' }],
    nickName: [{ required: true, message: '昵称不能为空', trigger: 'blur' }],
    phoneNumber: [{ pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }]
  }
});
const { form, rules } = toRefs(data);
const { dialog, resetForm, openDialog, showDialog, closeDialog } = useFormDialog({
  form,
  formRef,
  initialFormData: initFormData
});

const getList = async () => {
  await withLoading(async () => {
    const roleId = IDENTITIES[activeRoleId.value].roleId;
    const res = await allocatedUserList({ ...queryParams, roleId });
    userList.value = res.rows;
    total.value = res.total;
  });
};

const switchIdentity = () => {
  queryParams.pageNum = 1;
  getList();
};

const loadDeptTree = async () => {
  const res = await deptTreeSelect();
  deptTree.value = res.data || [];
};

const cancel = () => {
  closeDialog();
  resetForm();
};

const handleAdd = () => {
  resetForm();
  // 固定当前身份角色
  form.value.roleIds = [activeRoleId.value];
  openDialog('新增' + identityName.value);
};

const handleUpdate = async (row: Partial<UserVO>) => {
  resetForm();
  const res = await getUser(row.userId);
  Object.assign(form.value, res.data.user);
  form.value.roleIds = res.data.roleIds?.length ? res.data.roleIds : [activeRoleId.value];
  showDialog('编辑' + identityName.value);
};

const submitForm = () => {
  formRef.value?.validate(async (valid: boolean) => {
    if (!valid) {
      return;
    }
    if (form.value.userId) {
      await updateUser(form.value);
    } else {
      await addUser(form.value);
    }
    modal.msgSuccess('操作成功');
    closeDialog();
    await getList();
  });
};

const handleStatus = async (row: Partial<UserVO>) => {
  const toDisable = row.status === '0';
  await modal.confirm(toDisable ? '确认停用账号【' + row.userName + '】吗？' : '确认启用账号【' + row.userName + '】吗？');
  await changeUserStatus(row.userId!, toDisable ? '1' : '0');
  modal.msgSuccess('操作成功');
  await getList();
};

const handleResetPwd = async (row: Partial<UserVO>) => {
  const res = await ElMessageBox.prompt('请输入【' + row.userName + '】的新密码', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /^.{6,20}$/,
    inputErrorMessage: '密码长度为6-20位'
  });
  await resetUserPwd(row.userId!, res.value);
  modal.msgSuccess('密码已重置');
};

const handleDelete = async (row: Partial<UserVO>) => {
  await modal.confirm('确认删除账号【' + row.userName + '】吗？');
  await delUser(row.userId!);
  modal.msgSuccess('删除成功');
  await getList();
};

// 权限配置（保存角色菜单权限，走 PUT /system/role）
const openPermission = async () => {
  const roleId = IDENTITIES[activeRoleId.value].roleId;
  permDialog.userIds = []; // 空 = 角色模式
  permDialog.title = '权限配置：' + identityName.value;
  permDialog.visible = true;
  const [menuRes, roleRes] = await Promise.all([
    roleMenuTreeselect(roleId),
    getRole(roleId)
  ]);
  permTreeData.value = menuRes.data?.menus || [];
  await nextTick(() => {
    const checked = roleRes.data?.menuIds?.length ? roleRes.data.menuIds : menuRes.data?.checkedKeys || [];
    permTreeRef.value?.setCheckedKeys(checked);
  });
};

const submitPermission = async () => {
  const roleId = IDENTITIES[activeRoleId.value].roleId;
  const menuIds = [...permTreeRef.value!.getCheckedKeys(), ...permTreeRef.value!.getHalfCheckedKeys()];
  const { data: role } = await getRole(roleId);
  if (!role) {
    throw new Error('角色信息加载失败');
  }
  await updateRole({ ...role, menuIds });
  modal.msgSuccess('权限保存成功');
  permDialog.visible = false;
};

const handleSelectionChange = (rows: UserVO[]) => {
  selectedRows.value = rows;
};

// 后端 WalkingMenuNode → el-tree {id,label,children}
const mapPermNode = (n: any): any => ({
  id: n.menuId,
  label: n.menuName,
  menuType: n.menuType,
  perms: n.perms,
  children: (n.children || []).map(mapPermNode)
});

// 单个管理员的行走菜单权限配置
const openUserPerm = async (row: Partial<UserVO>) => {
  permDialog.userIds = [row.userId!];
  permDialog.title = '权限配置：' + row.userName + '（' + identityName.value + '）';
  permDialog.visible = true;
  const res = await getUserPermTree(row.userId!);
  permTreeData.value = (res.data?.menus || []).map(mapPermNode);
  await nextTick(() => permTreeRef.value?.setCheckedKeys(res.data?.checkedKeys || []));
};

// 批量给多个管理员配置同一组行走菜单权限
const openBatchPerm = async () => {
  if (!selectedRows.value.length) {
    modal.msgWarning('请先勾选要配置权限的管理员');
    return;
  }
  permDialog.userIds = selectedRows.value.map((u) => u.userId!);
  permDialog.title = '批量权限配置：' + permDialog.userIds.length + ' 个管理员';
  permDialog.visible = true;
  // 取第一个管理员的菜单树结构，勾选留空由操作者新选
  const res = await getUserPermTree(permDialog.userIds[0]);
  permTreeData.value = (res.data?.menus || []).map(mapPermNode);
  await nextTick(() => permTreeRef.value?.setCheckedKeys([]));
};

const submitUserPerm = async () => {
  const menuIds = [...(permTreeRef.value?.getCheckedKeys() || []), ...(permTreeRef.value?.getHalfCheckedKeys() || [])] as (string | number)[];
  if (permDialog.userIds.length === 1) {
    await saveUserPerm({ userId: permDialog.userIds[0], menuIds });
  } else {
    await batchUserPerm({ userIds: permDialog.userIds, menuIds });
  }
  modal.msgSuccess('权限保存成功');
  permDialog.visible = false;
};

onMounted(() => {
  getList();
  loadDeptTree();
});
</script>

<style lang="scss" scoped>
@use '@/assets/styles/components/page-shell' as pageShell;

@include pageShell.toolbar-responsive;

.identity-tabs {
  margin-bottom: 16px;
}
</style>
