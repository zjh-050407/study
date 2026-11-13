<script setup>
import request from "@/utils/request.js";
import { ref } from "vue";
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from "@element-plus/icons-vue";

const tableData = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const getData = async () => {
  loading.value = true
  try {
    const result = await request.get('/classroom/list', { params: { page: currentPage.value, size: pageSize.value } })
    if (result.data.code === 0) {
      tableData.value = result.data.data.list
      total.value = result.data.data.total
    }
  } catch (err) {
    console.error('获取数据失败:', err)
  } finally {
    loading.value = false
  }
}
getData()

const handlePageChange = (page) => {
  currentPage.value = page
  getData()
}

const dialogVisible = ref(false)
const isEdit = ref(false)
const typeOptions = ['普通教室', '实验室', '机房', '报告厅']

const form = ref({
  id: '',
  name: '',
  capacity: 60,
  type: '普通教室',
  building: '',
})

const openAdd = () => {
  isEdit.value = false
  form.value = { id: '', name: '', capacity: 60, type: '普通教室', building: '' }
  dialogVisible.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

const save = async () => {
  if (!form.value.name) {
    ElMessage.warning('请填写教室名称')
    return
  }
  const result = isEdit.value
    ? await request.put('/classroom', form.value)
    : await request.post('/classroom', form.value)
  if (result.data.code === 0) {
    dialogVisible.value = false
    ElMessage.success(isEdit.value ? '教室已更新' : '教室已新增')
    await getData()
  } else {
    ElMessage.error(result.data.message || '保存失败')
  }
}

const removeRow = (row) => {
  ElMessageBox.confirm(`确认删除教室「${row.name}」？`, '删除教室', {
    type: 'warning',
    confirmButtonText: '确认删除',
    cancelButtonText: '取消'
  }).then(async () => {
    const result = await request.delete(`/classroom/${row.id}`)
    if (result.data.code === 0) {
      ElMessage.success('已删除')
      await getData()
    } else {
      ElMessage.error(result.data.message || '删除失败')
    }
  }).catch(() => {})
}
</script>
<template>
  <div class="contain">
    <el-card class="glass-card" style="max-width: 900px">
      <template #header>
        <div class="card-header">
          <span class="card-title">教室管理</span>
          <el-button :icon="Plus" round @click="openAdd" class="btn-neu-add">新增教室</el-button>
        </div>
      </template>
      <el-table :data="tableData" style="width: 100%" class="glass-table" v-loading="loading" empty-text="暂无教室">
        <el-table-column prop="id" label="编号" width="70" />
        <el-table-column prop="name" label="教室名称" width="140" />
        <el-table-column prop="building" label="楼栋" width="120">
          <template #default="scope">{{ scope.row.building || '-' }}</template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="110" align="center" />
        <el-table-column prop="capacity" label="容量（座）" width="110" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small">
              {{ scope.row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="scope">
            <el-button size="small" class="btn-glass-edit" @click="openEdit(scope.row)">编辑</el-button>
            <el-button size="small" class="btn-glass-danger" @click="removeRow(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="display: flex; justify-content: center; margin-top: 12px;">
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50]"
            :total="total"
            layout="total, sizes, prev, pager, next"
            class="glass-pagination"
            @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑教室' : '新增教室'" width="500" class="glass-dialog">
      <el-form label-width="90px">
        <el-form-item label="教室名称" required>
          <el-input v-model="form.name" placeholder="如：A101" class="glass-input-sm" />
        </el-form-item>
        <el-form-item label="楼栋">
          <el-input v-model="form.building" placeholder="如：第一教学楼（选填）" class="glass-input-sm" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type" class="glass-select" style="width: 100%">
            <el-option v-for="t in typeOptions" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="容量（座）">
          <el-input-number v-model="form.capacity" :min="1" :max="500" :step="10" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false" class="btn-glass-outline">取消</el-button>
        <el-button type="primary" @click="save" class="btn-glass-primary">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<style scoped>
.contain {
  display: flex;
  justify-content: center;
  padding: 20px;
  width: 100%;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}
.glass-card {
  background: var(--glass-bg-strong);
  backdrop-filter: var(--blur-md);
  -webkit-backdrop-filter: var(--blur-md);
  border: 1px solid var(--glass-border);
  border-radius: 16px;
  box-shadow: var(--glass-shadow);
}
:deep(.glass-card .el-card__header) {
  background: rgba(255, 255, 255, 0.3);
  border-bottom: 1px solid var(--glass-border-light);
  border-radius: 16px 16px 0 0;
}
:deep(.glass-card .el-card__body) {
  background: transparent;
}
:deep(.glass-table) {
  --el-table-header-bg-color: rgba(220, 240, 255, 0.5);
  --el-table-row-hover-bg-color: rgba(140, 190, 255, 0.1);
  --el-table-border-color: var(--glass-border-light);
  border-radius: 8px;
  overflow: hidden;
}
:deep(.glass-table th.el-table__cell) {
  color: var(--text-primary);
  font-weight: 600;
}
:deep(.glass-table td.el-table__cell) {
  color: var(--text-secondary);
}
:deep(.btn-glass-primary) {
  background: linear-gradient(135deg, var(--blue-400), var(--blue-500)) !important;
  border: none !important;
  border-radius: 10px !important;
  box-shadow: 0 4px 14px rgba(74, 144, 226, 0.25);
}
:deep(.btn-glass-outline) {
  background: rgba(255, 255, 255, 0.4) !important;
  border: 1px solid var(--glass-border) !important;
  border-radius: 8px !important;
  color: var(--text-secondary);
}
:deep(.btn-glass-edit) {
  background: rgba(74, 144, 226, 0.1) !important;
  border: 1px solid rgba(74, 144, 226, 0.25) !important;
  color: var(--primary) !important;
  border-radius: 6px !important;
}
:deep(.btn-glass-danger) {
  background: rgba(245, 108, 108, 0.08) !important;
  border: 1px solid rgba(245, 108, 108, 0.2) !important;
  color: #f56c6c !important;
  border-radius: 6px !important;
}
:deep(.glass-input-sm .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.5) !important;
  border: 1px solid var(--glass-border-light) !important;
  border-radius: 8px !important;
  box-shadow: none !important;
}
:deep(.glass-input-sm .el-input__wrapper.is-focus) {
  border-color: var(--primary) !important;
  box-shadow: 0 0 0 3px rgba(74, 144, 226, 0.1) !important;
}
:deep(.glass-dialog .el-dialog) {
  background: var(--glass-bg-strong);
  backdrop-filter: var(--blur-lg);
  -webkit-backdrop-filter: var(--blur-lg);
  border: 1px solid var(--glass-border);
  border-radius: 16px;
  box-shadow: var(--glass-shadow);
}
:deep(.glass-dialog .el-dialog__header) {
  border-bottom: 1px solid var(--glass-border-light);
}
:deep(.glass-dialog .el-dialog__title) {
  color: var(--text-primary);
  font-weight: 600;
}
:deep(.glass-dialog .el-dialog__body) {
  color: var(--text-secondary);
}
:deep(.glass-dialog .el-dialog__footer) {
  border-top: 1px solid var(--glass-border-light);
}
</style>
