<script setup>
import request from "@/utils/request.js";
import { ref, onMounted } from "vue";
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from "@element-plus/icons-vue";

const tableData = ref([])
const loading = ref(false)
const role = ref('')

const getData = async () => {
  loading.value = true
  try {
    const result = await request.get('/semester/list')
    if (result.data.code === 0) {
      tableData.value = result.data.data
    }
  } catch (err) {
    console.error('获取数据失败:', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    try {
      role.value = JSON.parse(userStr).role || ''
    } catch (e) {}
  }
  getData()
})

const canManage = () => role.value === 'professor' || role.value === 'admin'

// ---------- 新增 / 编辑 ----------
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({
  id: '',
  name: '',
  startDate: '',
  endDate: '',
})

const openAdd = () => {
  isEdit.value = false
  form.value = { id: '', name: '', startDate: '', endDate: '' }
  dialogVisible.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  form.value = {
    id: row.id,
    name: row.name,
    startDate: row.startDate,
    endDate: row.endDate,
  }
  dialogVisible.value = true
}

const submitConfirm = async () => {
  if (!form.value.name || !form.value.name.trim()) {
    ElMessage.warning('请输入学期名称')
    return
  }
  const url = '/semester'
  const result = isEdit.value ? await request.put(url, form.value) : await request.post(url, form.value)
  if (result.data.code === 0) {
    dialogVisible.value = false
    await getData()
    ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
  } else {
    ElMessage.error(result.data.message || '操作失败')
  }
}

// ---------- 设为当前学期 ----------
const handleSetCurrent = async (row) => {
  ElMessageBox.confirm(`确认将「${row.name}」设为当前学期？`, {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    const result = await request.put('/semester/current/' + row.id)
    if (result.data.code === 0) {
      await getData()
      ElMessage.success('已设为当前学期')
    } else {
      ElMessage.error(result.data.message || '设置失败')
    }
  }).catch(() => {})
}

// ---------- 删除 ----------
const handleDelete = async (row) => {
  ElMessageBox.confirm(`是否确认删除学期「${row.name}」？`, {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    const result = await request.delete('/semester/' + row.id)
    if (result.data.code === 0) {
      await getData()
      ElMessage.success('删除成功')
    } else {
      ElMessage.error(result.data.message || '删除失败')
    }
  }).catch(() => {})
}

const formatDate = (date) => date ? date : '-'
</script>
<template>
  <div class="contain">
    <el-card class="glass-card" style="max-width: 860px">
      <template #header>
        <div class="card-header">
          <span class="card-title">学期管理</span>
          <el-button v-if="canManage()" :icon="Plus" round @click="openAdd" class="btn-neu-add">新增学期</el-button>
        </div>
      </template>
      <el-table :data="tableData" style="width: 100%" class="glass-table" v-loading="loading" empty-text="暂无学期">
        <el-table-column prop="name" label="学期名称" min-width="220" show-overflow-tooltip />
        <el-table-column label="开始日期" width="130">
          <template #default="scope">{{ formatDate(scope.row.startDate) }}</template>
        </el-table-column>
        <el-table-column label="结束日期" width="130">
          <template #default="scope">{{ formatDate(scope.row.endDate) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <span v-if="scope.row.isCurrent === 1" class="current-tag">当前学期</span>
            <span v-else class="normal-tag">历史学期</span>
          </template>
        </el-table-column>
        <el-table-column v-if="canManage()" label="操作" width="240" fixed="right">
          <template #default="scope">
            <el-button v-if="scope.row.isCurrent !== 1" size="small" class="btn-glass-current" @click="handleSetCurrent(scope.row)">设为当前</el-button>
            <el-button size="small" class="btn-glass-edit" @click="openEdit(scope.row)">编辑</el-button>
            <el-button size="small" class="btn-glass-danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑学期' : '新增学期'" width="480" class="glass-dialog">
      <el-form :model="form" label-width="90px">
        <el-form-item label="学期名称">
          <el-input v-model="form.name" maxlength="50" placeholder="如：2026-2027学年第一学期" class="glass-input-sm"/>
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="选择开始日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" placeholder="选择结束日期" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false" class="btn-glass-outline">取消</el-button>
          <el-button type="primary" @click="submitConfirm" class="btn-glass-primary">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
<style scoped>
.contain {
  display: flex;
  justify-content: center;
  flex-direction: column;
  align-items: center;
  width: 100%;
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-title {
  font-family: var(--font-serif);
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

:deep(.btn-neu-add) {
  background: var(--primary) !important;
  border: none !important;
  border-radius: 8px !important;
  color: #fff !important;
  box-shadow: 0 4px 14px rgba(217, 119, 87, 0.25);
  transition: all 0.25s ease;
}
:deep(.btn-neu-add:hover) {
  transform: translateY(-1px);
  box-shadow: 0 6px 18px rgba(217, 119, 87, 0.35);
}
:deep(.btn-glass-primary) {
  background: linear-gradient(135deg, var(--blue-400), var(--blue-500)) !important;
  border: none !important;
  border-radius: 10px !important;
  box-shadow: 0 4px 14px rgba(74, 144, 226, 0.25);
  transition: all 0.25s ease;
}
:deep(.btn-glass-outline) {
  background: rgba(255, 255, 255, 0.4) !important;
  border: 1px solid var(--glass-border) !important;
  border-radius: 8px !important;
  color: var(--text-secondary);
  transition: all 0.2s;
}
:deep(.btn-glass-edit) {
  background: rgba(74, 144, 226, 0.1) !important;
  border: 1px solid rgba(74, 144, 226, 0.25) !important;
  color: var(--primary) !important;
  border-radius: 6px !important;
  transition: all 0.2s;
}
:deep(.btn-glass-edit:hover) {
  background: rgba(74, 144, 226, 0.2) !important;
}
:deep(.btn-glass-danger) {
  background: rgba(245, 108, 108, 0.08) !important;
  border: 1px solid rgba(245, 108, 108, 0.2) !important;
  color: #f56c6c !important;
  border-radius: 6px !important;
  transition: all 0.2s;
}
:deep(.btn-glass-danger:hover) {
  background: rgba(245, 108, 108, 0.15) !important;
}
:deep(.btn-glass-current) {
  background: rgba(103, 194, 58, 0.08) !important;
  border: 1px solid rgba(103, 194, 58, 0.25) !important;
  color: #67c23a !important;
  border-radius: 6px !important;
  transition: all 0.2s;
}
:deep(.btn-glass-current:hover) {
  background: rgba(103, 194, 58, 0.15) !important;
}
:deep(.glass-input-sm .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.5) !important;
  border: 1px solid var(--glass-border-light) !important;
  border-radius: 8px !important;
  box-shadow: none !important;
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
:deep(.glass-dialog .el-dialog__footer) {
  border-top: 1px solid var(--glass-border-light);
}

.current-tag {
  font-size: 12px;
  color: #67c23a;
  background: rgba(103, 194, 58, 0.1);
  border: 1px solid rgba(103, 194, 58, 0.3);
  border-radius: 3px;
  padding: 1px 8px;
}
.normal-tag {
  font-size: 12px;
  color: var(--text-muted);
  background: rgba(110, 106, 91, 0.06);
  border: 1px solid var(--glass-border-light);
  border-radius: 3px;
  padding: 1px 8px;
}
</style>