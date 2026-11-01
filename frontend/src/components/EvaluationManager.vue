<script setup>
import request from "@/utils/request.js";
import { ref, onMounted, computed } from "vue";
import { ElMessage, ElMessageBox } from 'element-plus'

const activeTab = ref('tasks')
const isAdmin = computed(() => {
  const userStr = localStorage.getItem('user')
  if (!userStr) return false
  try {
    return JSON.parse(userStr).role === 'admin'
  } catch (e) {
    return false
  }
})

/* ============ 任务管理 ============ */
const taskList = ref([])
const taskLoading = ref(false)
const createDialogVisible = ref(false)
const createForm = ref({ startTime: '', endTime: '' })
const creating = ref(false)

const getTasks = async () => {
  taskLoading.value = true
  try {
    const result = await request.get('/evaluation/task/list')
    if (result.data.code === 0) {
      taskList.value = result.data.data || []
    }
  } finally {
    taskLoading.value = false
  }
}

const createTask = async () => {
  if (!createForm.value.startTime || !createForm.value.endTime) {
    ElMessage.warning('请选择起止时间')
    return
  }
  creating.value = true
  try {
    const result = await request.post('/evaluation/task', {
      startTime: createForm.value.startTime,
      endTime: createForm.value.endTime
    })
    if (result.data.code === 0) {
      createDialogVisible.value = false
      ElMessage.success('评教任务已发起')
      await Promise.all([getTasks(), getStats()])
    } else {
      ElMessage.error(result.data.message || '发起失败')
    }
  } finally {
    creating.value = false
  }
}

const closeTask = (row) => {
  ElMessageBox.confirm(`确认关闭「${row.semesterName || ''}」的评教任务？关闭后学生将无法提交评教。`, '关闭评教任务', {
    type: 'warning',
    confirmButtonText: '确认关闭',
    cancelButtonText: '取消'
  }).then(async () => {
    const result = await request.put(`/evaluation/task/${row.id}/close`)
    if (result.data.code === 0) {
      ElMessage.success('已关闭')
      await getTasks()
    } else {
      ElMessage.error(result.data.message || '操作失败')
    }
  }).catch(() => {})
}

/* ============ 评价统计 ============ */
const statsList = ref([])
const statsLoading = ref(false)
const selectedTaskId = ref(null)

const getStats = async () => {
  if (!selectedTaskId.value) {
    statsList.value = []
    return
  }
  statsLoading.value = true
  try {
    const result = await request.get('/evaluation/stats', { params: { taskId: selectedTaskId.value } })
    if (result.data.code === 0) {
      statsList.value = result.data.data || []
    } else {
      ElMessage.error(result.data.message || '加载失败')
    }
  } finally {
    statsLoading.value = false
  }
}

const onTaskChange = () => {
  getStats()
}

const exportCsv = async () => {
  if (!selectedTaskId.value) {
    ElMessage.warning('请先选择评教任务')
    return
  }
  const result = await request.get('/evaluation/export', { params: { taskId: selectedTaskId.value } })
  if (result.data.code === 0) {
    const csv = result.data.data || ''
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `评教统计_任务${selectedTaskId.value}.csv`
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } else {
    ElMessage.error(result.data.message || '导出失败')
  }
}

const formatTime = (t) => t ? String(t).replace('T', ' ').slice(0, 19) : '-'

onMounted(async () => {
  await getTasks()
  if (taskList.value.length > 0) {
    const active = taskList.value.find(t => t.status === 1) || taskList.value[0]
    selectedTaskId.value = active.id
    await getStats()
  }
})
</script>
<template>
  <div class="evaluation-manager-page">
    <el-card class="glass-card" shadow="never">
      <el-tabs v-model="activeTab" class="glass-tabs">
        <!-- 任务管理 -->
        <el-tab-pane label="评教任务" name="tasks">
          <div class="toolbar">
            <el-button type="primary" class="btn-glass-primary" @click="createDialogVisible = true">发起评教</el-button>
            <span class="toolbar-tip">按当前学期发起，同一学期同时仅一个进行中的任务</span>
          </div>
          <el-table :data="taskList" class="glass-table" v-loading="taskLoading" empty-text="暂无评教任务">
            <el-table-column prop="id" label="任务ID" width="80" align="center" />
            <el-table-column prop="semesterName" label="学期" width="180" align="center">
              <template #default="scope">{{ scope.row.semesterName || '-' }}</template>
            </el-table-column>
            <el-table-column label="开始时间" width="170" align="center">
              <template #default="scope">{{ formatTime(scope.row.startTime) }}</template>
            </el-table-column>
            <el-table-column label="结束时间" width="170" align="center">
              <template #default="scope">{{ formatTime(scope.row.endTime) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small" effect="light">
                  {{ scope.row.status === 1 ? '进行中' : '已关闭' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="110" fixed="right" align="center">
              <template #default="scope">
                <el-button
                    v-if="scope.row.status === 1"
                    size="small"
                    class="btn-glass-danger"
                    @click="closeTask(scope.row)"
                >关闭</el-button>
                <span v-else class="muted-text">—</span>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 评价统计 -->
        <el-tab-pane label="评价统计" name="stats">
          <div class="toolbar">
            <el-select v-model="selectedTaskId" placeholder="选择评教任务" class="task-select" @change="onTaskChange">
              <el-option
                  v-for="t in taskList"
                  :key="t.id"
                  :label="`任务${t.id} · ${t.semesterName || '未分学期'}${t.status === 1 ? '（进行中）' : ''}`"
                  :value="t.id"
              />
            </el-select>
            <el-button v-if="isAdmin" type="primary" class="btn-glass-primary" @click="exportCsv">导出CSV</el-button>
          </div>
          <el-table :data="statsList" class="glass-table" v-loading="statsLoading" empty-text="该任务暂无评教数据">
            <el-table-column type="expand">
              <template #default="scope">
                <div class="suggestion-box">
                  <div class="suggestion-title">匿名建议（共 {{ scope.row.suggestions?.length || 0 }} 条）</div>
                  <el-tag
                      v-for="(s, i) in scope.row.suggestions"
                      :key="i"
                      class="suggestion-tag"
                      type="info" effect="plain"
                  >{{ s }}</el-tag>
                  <span v-if="!scope.row.suggestions?.length" class="muted-text">暂无建议</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="courseName" label="课程" min-width="150" show-overflow-tooltip />
            <el-table-column prop="teacherName" label="任课教师" min-width="110" align="center">
              <template #default="scope">{{ scope.row.teacherName || '-' }}</template>
            </el-table-column>
            <el-table-column prop="recordCount" label="参评人数" width="90" align="center" />
            <el-table-column prop="avgDim1" label="教学态度" width="90" align="center" />
            <el-table-column prop="avgDim2" label="教学内容" width="90" align="center" />
            <el-table-column prop="avgDim3" label="教学方法" width="90" align="center" />
            <el-table-column prop="avgDim4" label="教学效果" width="90" align="center" />
            <el-table-column label="综合均分" width="100" align="center">
              <template #default="scope">
                <span class="total-score">{{ scope.row.avgTotal }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="createDialogVisible" title="发起评教任务" width="480" class="glass-dialog">
      <el-form label-width="90px">
        <el-form-item label="开始时间">
          <el-date-picker v-model="createForm.startTime" type="datetime" placeholder="选择开始时间"
                          value-format="YYYY-MM-DD HH:mm:ss" class="picker-full" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="createForm.endTime" type="datetime" placeholder="选择结束时间"
                          value-format="YYYY-MM-DD HH:mm:ss" class="picker-full" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false" class="btn-glass-outline">取消</el-button>
        <el-button type="primary" :loading="creating" @click="createTask" class="btn-glass-primary">发起</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<style scoped>
.evaluation-manager-page {
  display: flex;
  justify-content: center;
  width: 100%;
}
.evaluation-manager-page .glass-card {
  width: 100%;
  max-width: 1100px;
}
.glass-card {
  background: var(--glass-bg-strong);
  backdrop-filter: var(--blur-md);
  -webkit-backdrop-filter: var(--blur-md);
  border: 1px solid var(--glass-border);
  border-radius: 16px;
  box-shadow: var(--glass-shadow);
}
:deep(.glass-tabs .el-tabs__item.is-active) {
  color: var(--primary);
}
:deep(.glass-tabs .el-tabs__active-bar) {
  background-color: var(--primary);
}
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}
.toolbar-tip {
  font-size: 12px;
  color: var(--text-muted);
}
.task-select {
  width: 300px;
}
.muted-text {
  color: var(--text-muted);
  font-size: 13px;
}
.suggestion-box {
  padding: 8px 16px;
}
.suggestion-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}
.suggestion-tag {
  margin: 0 8px 8px 0;
  max-width: 100%;
  white-space: normal;
  height: auto;
  line-height: 1.6;
}
.total-score {
  font-family: var(--font-mono);
  font-weight: 600;
  color: var(--primary);
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
:deep(.btn-glass-danger) {
  background: rgba(245, 108, 108, 0.08) !important;
  border: 1px solid rgba(245, 108, 108, 0.2) !important;
  color: #f56c6c !important;
  border-radius: 6px !important;
}
.picker-full {
  width: 100%;
}
:deep(.glass-dialog .el-dialog) {
  background: var(--glass-bg-strong);
  backdrop-filter: var(--blur-lg);
  -webkit-backdrop-filter: var(--blur-lg);
  border: 1px solid var(--glass-border);
  border-radius: 16px;
  box-shadow: var(--glass-shadow);
}
</style>