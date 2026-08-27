<template>
  <div class="p-2 app-container walking-org-page">
    <el-card v-loading="loading" shadow="hover" class="table-panel">
      <template #header>
        <div class="toolbar-shell">
          <div class="table-heading">
            <span class="panel-kicker">Organization</span>
            <h3>组织机构</h3>
            <p>工会组织机构树，复用系统部门数据，支持增删改与批量导入。</p>
          </div>
          <div class="toolbar-actions">
            <el-button v-hasPermi="['walking:org:import']" type="primary" plain icon="Upload" @click="handleImport">批量导入</el-button>
            <el-button v-hasPermi="['walking:org:import']" type="warning" plain icon="Download" @click="handleTemplate">下载模板</el-button>
            <el-button v-hasPermi="['walking:org:add']" type="success" plain icon="Plus" @click="handleAdd">新增</el-button>
          </div>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="orgList"
        row-key="deptId"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :default-expand-all="true"
      >
        <el-table-column prop="deptName" label="机构名称" min-width="220" />
        <el-table-column prop="orderNum" label="排序" align="center" width="80" />
        <el-table-column prop="memberTotal" label="会员总数" align="center" width="100" />
        <el-table-column prop="leader" label="负责人" align="center" width="100">
          <template #default="{ row }">{{ row.leader || '-' }}</template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" align="center" width="130">
          <template #default="{ row }">{{ row.phone || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="130" align="center">
          <template #default="{ row }">
            <el-tooltip content="修改" placement="top">
              <el-button v-hasPermi="['walking:org:edit']" link type="primary" icon="Edit" @click="handleUpdate(row)"></el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button v-hasPermi="['walking:org:remove']" link type="danger" icon="Delete" @click="handleDelete(row)"></el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/修改机构 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="500px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="上级机构" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="orgSelectTree"
            :props="{ label: 'deptName', children: 'children' }"
            node-key="deptId"
            check-strictly
            clearable
            filterable
            placeholder="不选则为顶级机构"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="机构名称" prop="deptName">
          <el-input v-model="form.deptName" placeholder="请输入机构名称" />
        </el-form-item>
        <el-form-item label="显示顺序" prop="orderNum">
          <el-input-number v-model="form.orderNum" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="负责人" prop="leader">
          <el-input v-model="form.leader" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 批量导入 -->
    <el-dialog v-model="importDialog.visible" title="批量导入组织机构" width="460px" append-to-body>
      <el-alert type="info" :closable="false" style="margin-bottom: 16px">
        <p>1. 请先下载模板填写机构数据；</p>
        <p>2. "上级机构名称"留空表示顶级机构；</p>
        <p>3. 已存在的机构将自动跳过。</p>
      </el-alert>
      <el-upload
        ref="uploadRef"
        drag
        accept=".xlsx,.xls"
        :auto-upload="false"
        :limit="1"
        :on-change="handleFileChange"
        :on-exceed="handleExceed"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击选择</em></div>
      </el-upload>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="importing" @click="submitImport">开始导入</el-button>
          <el-button @click="importDialog.visible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="WalkingOrg" lang="ts">
import { treeOrg, getOrg, addOrg, updateOrg, delOrg, importOrg } from '@/api/walking/org';
import { OrgForm, OrgVO } from '@/api/walking/org/types';
import { useLoading } from '@/hooks/async/useLoading';
import { useFormDialog } from '@/hooks/dialog/useFormDialog';
import modal from '@/plugins/modal';
import { download as requestDownload } from '@/utils/request';
import type { UploadFile, UploadInstance } from 'element-plus';

const orgList = ref<OrgVO[]>([]);
const orgSelectTree = ref<OrgVO[]>([]);
const { loading, withLoading } = useLoading(true);

const formRef = ref<ElFormInstance>();
const uploadRef = ref<UploadInstance>();
const importDialog = reactive({ visible: false });
const importing = ref(false);
let importFile: File | null = null;

const initFormData: OrgForm = {
  deptId: undefined,
  parentId: undefined,
  deptName: '',
  orderNum: 0,
  leader: '',
  phone: ''
};
const data = reactive<PageData<OrgForm, any>>({
  form: { ...initFormData },
  queryParams: {},
  rules: {
    deptName: [{ required: true, message: '机构名称不能为空', trigger: 'blur' }]
  }
});
const { form, rules } = toRefs(data);
const { dialog, resetForm, openDialog, showDialog, closeDialog } = useFormDialog({
  form,
  formRef,
  initialFormData: initFormData
});

const getTree = async () => {
  await withLoading(async () => {
    const res = await treeOrg();
    orgList.value = res.data;
    orgSelectTree.value = res.data;
  });
};

const cancel = () => {
  closeDialog();
  resetForm();
};

const handleAdd = () => {
  resetForm();
  openDialog('新增机构');
};

const handleUpdate = async (row?: Partial<OrgVO>) => {
  resetForm();
  const res = await getOrg(row!.deptId!);
  Object.assign(form.value, res.data);
  showDialog('修改机构');
};

const submitForm = () => {
  formRef.value?.validate(async (valid: boolean) => {
    if (!valid) {
      return;
    }
    form.value.deptId ? await updateOrg(form.value) : await addOrg(form.value);
    modal.msgSuccess('操作成功');
    closeDialog();
    await getTree();
  });
};

const handleDelete = async (row?: Partial<OrgVO>) => {
  await modal.confirm('确认删除机构【' + row?.deptName + '】吗？');
  await delOrg(row!.deptId!);
  await getTree();
  modal.msgSuccess('删除成功');
};

const handleTemplate = () => {
  requestDownload('walking/admin/org/importTemplate', {}, `org_template_${new Date().getTime()}.xlsx`);
};

const handleImport = () => {
  importFile = null;
  uploadRef.value?.clearFiles();
  importDialog.visible = true;
};

const handleFileChange = (file: UploadFile) => {
  importFile = file.raw ?? null;
};

const handleExceed = (files: File[]) => {
  uploadRef.value?.clearFiles();
  uploadRef.value?.handleStart(files[0] as any);
  importFile = files[0];
};

const submitImport = async () => {
  if (!importFile) {
    modal.msgWarning('请先选择要导入的 Excel 文件');
    return;
  }
  importing.value = true;
  try {
    const fd = new FormData();
    fd.append('file', importFile);
    await importOrg(fd);
    modal.msgSuccess('导入成功');
    importDialog.visible = false;
    await getTree();
  } finally {
    importing.value = false;
  }
};

onMounted(() => {
  getTree();
});
</script>

<style lang="scss" scoped>
@use '@/assets/styles/components/page-shell' as pageShell;

@include pageShell.toolbar-responsive;
</style>
