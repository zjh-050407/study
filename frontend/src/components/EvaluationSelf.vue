<script setup>
import request from "@/utils/request.js";
import { ref, onMounted } from "vue";
import { ElMessage } from 'element-plus'

const windowInfo = ref({ semesterName: null, task: null, windowOpen: false })
const courseList = ref([])
const loading = ref(false)

const dialogVisible = ref(false)
const currentRow = ref(null)
const dims = ref({ dim1: 0, dim2: 0, dim3: 0, dim4: 0 })
const suggestion = ref('')
const submitting = ref(false)

const dimLabels = [
  { key: 'dim1', label: '教学态度' },
  { key: 'dim2', label: '教学内容' },
  { key: 'dim3', label: '教学方法' },
  { key: 'dim4', label: '教学效果' },
]

const getWindow = async () => {
  const result = await request.get('/evaluation/active')
  if (result.data.code === 0) {
    windowInfo.value = result.data.data || { semesterName: null, task: null, windowOpen: false }
  }
}

const getCourses = async () => {
  loading.value = true
  try {
    const result = await request.get('/evaluation/my/courses')
    if (result.data.code === 0) {
      courseList.value = result.data.data || []
    }
  } finally {
    loading.value = false
  }
}

const formatTime = (t) => t ? String(t).replace('T', ' ').slice(0, 16) : '-'

const openDialog = (row) => {
  currentRow.value = row
  dims.value = { dim1: 0, dim2: 0, dim3: 0, dim4: 0 }
  suggestion.value = ''
  dialogVisible.value = true
}

const submitEval = async () => {
  for (const d of dimLabels) {
    if (!dims.value[d.key]) {
      ElMessage.warning(`请完成「${d.label}」评分`)
      return
    }
  }
  submitting.value = true
  try {
    const result = await request.post('/evaluation/submit', {
      taskId: windowInfo.value.task?.id,
      courseId: currentRow.value.courseId,
      teacherUid: currentRow.value.teacherUid,
      dim1: dims.value.dim1,
      dim2: dims.value.dim2,
      dim3: dims.value.dim3,
      dim4: dims.value.dim4,
      suggestion: suggestion.value
    })
    if (result.data.code === 0) {
      dialogVisible.value = false
      ElMessage.success('评教提交成功，感谢你的反馈')
      await getCourses()
    } else {
      ElMessage.error(result.data.message || '提交失败')
    }
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await getWindow()
  await getCourses()
})
</script>
<template>
  <div class="evaluation-page">
    <el-card class="glass-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">教学评教</span>
          <span v-if="windowInfo.semesterName" class="semester-tag">{{ windowInfo.semesterName }}</span>
        </div>
      </template>

      <el-alert
          v-if="!windowInfo.task"
          class="tip-alert"
          type="info" :closable="false" show-icon
          title="当前暂未发起评教任务，请留意通知" />
      <el-alert
          v-else-if="windowInfo.windowOpen"
          class="tip-alert"
          type="success" :closable="false" show-icon
          :title="`评教进行中：${formatTime(windowInfo.task.startTime)} 至 ${formatTime(windowInfo.task.endTime)}，请为你的任课教师评分`" />
      <el-alert
          v-else
          class="tip-alert"
          type="warning" :closable="false" show-icon
          :title="`评教未开启：窗口时间为 ${formatTime(windowInfo.task.startTime)} 至 ${formatTime(windowInfo.task.endTime)}`" />

      <el-table :data="courseList" class="glass-table" v-loading="loading" empty-text="本学期暂无可评课程">
        <el-table-column prop="courseName" label="课程名称" min-width="160" show-overflow-tooltip />
        <el-table-column label="学分" width="70" align="center">
          <template #default="scope">{{ scope.row.credit ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="teacherName" label="任课教师" min-width="110" align="center">
          <template #default="scope">{{ scope.row.teacherName || '-' }}</template>
        </el-table-column>
        <el-table-column label="评价状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.evaluated ? 'success' : 'info'" size="small" effect="light">
              {{ scope.row.evaluated ? '已评' : '待评' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="scope">
            <el-button
                size="small"
                class="btn-glass-edit"
                :disabled="!windowInfo.windowOpen || scope.row.evaluated"
                @click="openDialog(scope.row)"
            >{{ scope.row.evaluated ? '已评价' : '去评价' }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="privacy-tip">评教为匿名评价：系统不记录你的身份信息，每位学生对每门课程仅可评价一次。</div>
    </el-card>

    <el-dialog v-model="dialogVisible" title="课程评价" width="520" class="glass-dialog">
      <div class="eval-course">
        <span class="eval-course-name">{{ currentRow?.courseName }}</span>
        <span class="eval-teacher">{{ currentRow?.teacherName }}</span>
      </div>
      <el-form label-position="top">
        <el-form-item v-for="d in dimLabels" :key="d.key" :label="d.label">
          <el-rate v-model="dims[d.key]" :max="5" size="large" show-score
                   score-template="{value} 分" />
        </el-form-item>
        <el-form-item label="文字建议（选填，匿名）">
          <el-input v-model="suggestion" type="textarea" :rows="3" maxlength="500" show-word-limit
                    placeholder="写下你想对老师说的话或改进建议" class="glass-input-sm" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false" class="btn-glass-outline">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitEval" class="btn-glass-primary">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<style scoped>
.evaluation-page {
  display: flex;
  justify-content: center;
  width: 100%;
}
.evaluation-page .glass-card {
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
.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
}
.card-title {
  font-family: var(--font-serif);
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}
.semester-tag {
  font-size: 12px;
  color: var(--blue-600);
  background: var(--blue-50);
  border: 1px solid var(--blue-100);
  border-radius: 3px;
  padding: 1px 8px;
}
.tip-alert {
  margin-bottom: 12px;
}
.privacy-tip {
  margin-top: 12px;
  font-size: 12px;
  color: var(--text-muted);
}
.eval-course {
  display: flex;
  align-items: baseline;
  gap: 12px;
  padding: 0 0 8px;
}
.eval-course-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}
.eval-teacher {
  font-size: 13px;
  color: var(--text-secondary);
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