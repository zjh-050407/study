<script setup>
import request from "@/utils/request.js";
import { ref, onMounted } from "vue";
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from "@element-plus/icons-vue";

const list = ref([])
const loading = ref(false)
const teachers = ref([])

const getData = async () => {
  loading.value = true
  try {
    const [taskRes, teacherRes] = await Promise.all([
      request.get('/teachTask/list'),
      request.get('/teacher/list')
    ])
    if (taskRes.data.code === 0) list.value = taskRes.data.data || []
    if (teacherRes.data.code === 0) teachers.value = teacherRes.data.data || []
  } finally {
    loading.value = false
  }
}
onMounted(getData)

const generate = async () => {
  const result = await request.post('/teachTask/generate')
  if (result.data.code === 0) {
    const created = result.data.data?.created ?? 0
    ElMessage.success(created > 0 ? `已生成 ${created} 条教学任务` : '培养方案课程均已生成任务，无需重复生成')
    await getData()
  } else {
    ElMessage.error(result.data.message || '生成失败')
  }
}

const assignTeacher = async (row) => {
  const { value } = await ElMessageBox.prompt(`为「${row.courseName}·${row.clazzName}」分配授课教师，请输入教师编号：`, '分配教师', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '如：1',
    inputValue: row.tid ? String(row.tid) : ''
  }).catch(() => ({ value: null }))
  if (value === null || value === '') return
  const tid = Number(value)
  if (!tid) {
    ElMessage.warning('教师编号无效')
    return
  }
  const result = await request.put(`/teachTask/${row.id}/teacher`, { tid })
  if (result.data.code === 0) {
    ElMessage.success('已分配教师')
    await getData()
  } else {
    ElMessage.error(result.data.message || '分配失败')
  }
}

const markDone = async (row) => {
  const result = await request.put(`/teachTask/${row.id}/status`, { status: 1 })
  if (result.data.code === 0) {
    ElMessage.success('已标记为已排课')
    await getData()
  } else {
    ElMessage.error(result.data.message || '操作失败')
  }
}

const removeRow = (row) => {
  ElMessageBox.confirm(`确认删除教学任务「${row.courseName}·${row.clazzName}」？`, '删除任务', {
    type: 'warning',
    confirmButtonText: '确认删除',
    cancelButtonText: '取消'
  }).then(async () => {
    const result = await request.delete(`/teachTask/${row.id}`)
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
    <el-card class="glass-card" style="max-width: 1000px">
      <template #header>
        <div class="card-header">
          <span class="card-title">教学任务下达</span>
          <el-button :icon="Plus" round @click="generate" class="btn-neu-add">按培养方案生成任务</el-button>
        </div>
      </template>
      <el-table :data="list" style="width: 100%" class="glass-table" v-loading="loading" empty-text="暂无教学任务，请先按培养方案生成">
        <el-table-column prop="id" label="编号" width="70" />
        <el-table-column prop="courseName" label="课程" min-width="140" show-overflow-tooltip />
        <el-table-column prop="credit" label="学分" width="70" align="center" />
        <el-table-column prop="clazzName" label="班级" width="120" />
        <el-table-column prop="semesterName" label="学期" width="160">
          <template #default="scope">{{ scope.row.semesterName || '未分学期' }}</template>
        </el-table-column>
        <el-table-column prop="teacherName" label="授课教师" width="120">
          <template #default="scope">{{ scope.row.teacherName || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'" size="small">
              {{ scope.row.status === 1 ? '已排课' : '未排课' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="scope">
            <el-button size="small" class="btn-glass-edit" @click="assignTeacher(scope.row)">分配教师</el-button>
            <el-button v-if="scope.row.status !== 1" size="small" class="btn-glass-outline" @click="markDone(scope.row)">标记已排课</el-button>
            <el-button size="small" class="btn-glass-danger" @click="removeRow(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
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
:deep(.btn-glass-edit) {
  background: rgba(74, 144, 226, 0.1) !important;
  border: 1px solid rgba(74, 144, 226, 0.25) !important;
  color: var(--primary) !important;
  border-radius: 6px !important;
}
:deep(.btn-glass-outline) {
  background: rgba(255, 255, 255, 0.4) !important;
  border: 1px solid var(--glass-border) !important;
  border-radius: 6px !important;
  color: var(--text-secondary);
}
:deep(.btn-glass-danger) {
  background: rgba(245, 108, 108, 0.08) !important;
  border: 1px solid rgba(245, 108, 108, 0.2) !important;
  color: #f56c6c !important;
  border-radius: 6px !important;
}
</style>
