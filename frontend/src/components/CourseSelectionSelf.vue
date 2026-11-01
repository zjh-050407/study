<script setup>
import request from "@/utils/request.js";
import { ref, onMounted } from "vue";
import { ElMessage } from 'element-plus'

const activeTab = ref('courses')
const courseList = ref([])
const mySelections = ref([])
const myApplies = ref([])
const loading = ref(false)

const applyDialogVisible = ref(false)
const applyCourse = ref(null)
const applyReason = ref('')

const getCourses = async () => {
  loading.value = true
  try {
    const result = await request.get('/selection/self/courses')
    if (result.data.code === 0) {
      courseList.value = result.data.data || []
    }
  } catch (err) {
    console.error('获取课程失败:', err)
  } finally {
    loading.value = false
  }
}

const getMySelections = async () => {
  const result = await request.get('/selection/self/list')
  if (result.data.code === 0) {
    mySelections.value = result.data.data || []
  }
}

const getMyApplies = async () => {
  const result = await request.get('/selectionApply/my')
  if (result.data.code === 0) {
    myApplies.value = result.data.data || []
  }
}

const refreshAll = async () => {
  await Promise.all([getCourses(), getMySelections(), getMyApplies()])
}

const selectCourse = async (course) => {
  const result = await request.post('/selection/self', { courseId: course.courseId })
  if (result.data.code === 0) {
    ElMessage.success(`选课成功：${course.name}`)
    await refreshAll()
  } else {
    ElMessage.error(result.data.message || '选课失败')
  }
}

const openApply = (row) => {
  applyCourse.value = row
  applyReason.value = ''
  applyDialogVisible.value = true
}

const submitApply = async () => {
  const result = await request.post('/selectionApply', {
    courseId: applyCourse.value.courseId,
    reason: applyReason.value
  })
  if (result.data.code === 0) {
    applyDialogVisible.value = false
    ElMessage.success('退选申请已提交，等待教师审批')
    await getMyApplies()
  } else {
    ElMessage.error(result.data.message || '提交失败')
  }
}

const statusMap = {
  0: { text: '待审批', type: 'warning' },
  1: { text: '已通过', type: 'success' },
  2: { text: '已拒绝', type: 'danger' },
}

const isApplied = (courseId) => myApplies.value.some(a => a.courseId === courseId && a.status === 0)

onMounted(refreshAll)
</script>
<template>
  <div class="selection-page">
    <el-card class="glass-card" shadow="never">
      <el-tabs v-model="activeTab" class="glass-tabs">
        <el-tab-pane label="全部课程" name="courses">
          <el-table :data="courseList" class="glass-table" v-loading="loading" empty-text="暂无课程">
            <el-table-column prop="name" label="课程名称" min-width="160" show-overflow-tooltip />
            <el-table-column label="学分" width="70" align="center">
              <template #default="scope">{{ scope.row.credit }}</template>
            </el-table-column>
            <el-table-column label="学时" width="70" align="center">
              <template #default="scope">{{ scope.row.hours }}</template>
            </el-table-column>
            <el-table-column label="任课教师" min-width="120" align="center">
              <template #default="scope">{{ scope.row.teacherNames || '-' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right" align="center">
              <template #default="scope">
                <el-button
                    size="small"
                    class="btn-glass-edit"
                    :disabled="Number(scope.row.selected) > 0"
                    @click="selectCourse(scope.row)"
                >{{ Number(scope.row.selected) > 0 ? '已选' : '选课' }}</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="我的课程" name="mine">
          <el-alert class="tip-alert" type="info" :closable="false" show-icon
                    title="退选需提交申请，经教师审批通过后生效" />
          <el-table :data="mySelections" class="glass-table" empty-text="你还没有选课">
            <el-table-column prop="courseName" label="课程名称" min-width="160" show-overflow-tooltip />
            <el-table-column label="学分" width="70" align="center">
              <template #default="scope">{{ scope.row.credit ?? '-' }}</template>
            </el-table-column>
            <el-table-column label="学期" width="160" align="center">
              <template #default="scope">{{ scope.row.semesterName || '未分学期' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right" align="center">
              <template #default="scope">
                <el-button
                    size="small"
                    class="btn-glass-danger"
                    :disabled="isApplied(scope.row.courseId)"
                    @click="openApply(scope.row)"
                >{{ isApplied(scope.row.courseId) ? '审批中' : '退选申请' }}</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="退选申请记录" name="applies">
          <el-table :data="myApplies" class="glass-table" empty-text="暂无退选申请">
            <el-table-column prop="id" label="单号" width="70" align="center" />
            <el-table-column prop="courseName" label="课程" min-width="140" show-overflow-tooltip />
            <el-table-column prop="reason" label="退选原因" min-width="180" show-overflow-tooltip>
              <template #default="scope">{{ scope.row.reason || '-' }}</template>
            </el-table-column>
            <el-table-column label="申请时间" width="170" align="center">
              <template #default="scope">{{ scope.row.applyTime ? String(scope.row.applyTime).replace('T', ' ').slice(0, 19) : '-' }}</template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="scope">
                <el-tag :type="statusMap[scope.row.status]?.type" size="small" effect="light">
                  {{ statusMap[scope.row.status]?.text }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="applyDialogVisible" title="提交退选申请" width="460" class="glass-dialog">
      <el-form label-width="90px">
        <el-form-item label="课程">
          <el-input :model-value="applyCourse?.courseName" disabled class="glass-input-sm" />
        </el-form-item>
        <el-form-item label="退选原因">
          <el-input v-model="applyReason" type="textarea" :rows="3" maxlength="200" show-word-limit
                    placeholder="请填写退选原因（选填）" class="glass-input-sm" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyDialogVisible = false" class="btn-glass-outline">取消</el-button>
        <el-button type="primary" @click="submitApply" class="btn-glass-primary">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<style scoped>
.selection-page {
  display: flex;
  justify-content: center;
  width: 100%;
}
.selection-page .glass-card {
  width: 100%;
  max-width: 900px;
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
.tip-alert {
  margin-bottom: 12px;
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
:deep(.btn-glass-edit) {
  background: rgba(74, 144, 226, 0.1) !important;
  border: 1px solid rgba(74, 144, 226, 0.25) !important;
  color: var(--primary) !important;
  border-radius: 6px !important;
}
:deep(.btn-glass-edit.is-disabled) {
  background: rgba(0, 0, 0, 0.03) !important;
  color: var(--text-muted) !important;
  border-color: var(--glass-border-light) !important;
}
:deep(.btn-glass-danger) {
  background: rgba(245, 108, 108, 0.08) !important;
  border: 1px solid rgba(245, 108, 108, 0.2) !important;
  color: #f56c6c !important;
  border-radius: 6px !important;
}
:deep(.btn-glass-danger.is-disabled) {
  background: rgba(0, 0, 0, 0.03) !important;
  color: var(--text-muted) !important;
  border-color: var(--glass-border-light) !important;
}
:deep(.glass-input-sm .el-input__wrapper),
:deep(.glass-input-sm .el-textarea__inner) {
  background: rgba(255, 255, 255, 0.5) !important;
  border: 1px solid var(--glass-border-light) !important;
  border-radius: 8px !important;
  box-shadow: none !important;
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
:deep(.glass-dialog .el-dialog) {
  background: var(--glass-bg-strong);
  backdrop-filter: var(--blur-lg);
  -webkit-backdrop-filter: var(--blur-lg);
  border: 1px solid var(--glass-border);
  border-radius: 16px;
  box-shadow: var(--glass-shadow);
}
</style>