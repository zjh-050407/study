<script setup>
import request from "@/utils/request.js";
import { ref, onMounted } from "vue";
import { ElMessage } from 'element-plus'

const studentOptions = ref([])
const studentLoading = ref(false)
const selectedSid = ref(null)
const requiredTotalCredit = ref(150)
const requiredGpa = ref(2.0)

const auditing = ref(false)
const result = ref(null)
const history = ref([])
const historyLoading = ref(false)

const searchStudents = async (name) => {
  studentLoading.value = true
  try {
    const res = await request.get('/student/stu2', { params: { name: name || '' } })
    if (res.data.code === 0) {
      studentOptions.value = (res.data.data || []).map(s => ({
        sid: s.sid,
        name: s.name,
        clazzName: s.clazzName
      }))
    }
  } finally {
    studentLoading.value = false
  }
}

const getHistory = async () => {
  historyLoading.value = true
  try {
    const params = selectedSid.value ? { sid: selectedSid.value } : {}
    const res = await request.get('/graduate/history', { params })
    if (res.data.code === 0) {
      history.value = res.data.data || []
    }
  } finally {
    historyLoading.value = false
  }
}

const onStudentChange = () => {
  result.value = null
  getHistory()
}

const runAudit = async () => {
  if (!selectedSid.value) {
    ElMessage.warning('请选择学生')
    return
  }
  auditing.value = true
  try {
    const res = await request.post('/graduate/audit', {
      sid: selectedSid.value,
      requiredTotalCredit: requiredTotalCredit.value,
      requiredGpa: requiredGpa.value
    })
    if (res.data.code === 0) {
      result.value = res.data.data || null
      ElMessage.success('审核完成，结论已留档')
      await getHistory()
    } else {
      ElMessage.error(res.data.message || '审核失败')
    }
  } finally {
    auditing.value = false
  }
}

const fmtTime = (t) => t ? String(t).replace('T', ' ').slice(0, 19) : '-'
const modeLabel = (m) => m === 'plan' ? '培养方案模式' : '总学分模式'

onMounted(() => {
  searchStudents('')
  getHistory()
})
</script>
<template>
  <div class="audit-page">
    <el-card class="glass-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">毕业审核</span>
          <span class="card-tip">有培养方案的专业按方案校验；无方案回退总学分模式。结论自动留档。</span>
        </div>
      </template>

      <div class="audit-form">
        <div class="form-item">
          <span class="form-label">学生</span>
          <el-select
              v-model="selectedSid"
              filterable
              remote
              clearable
              :remote-method="searchStudents"
              :loading="studentLoading"
              placeholder="输入姓名搜索学生（清空查看全部历史）"
              class="student-select"
              @change="onStudentChange"
          >
            <el-option
                v-for="s in studentOptions"
                :key="s.sid"
                :label="`${s.name}（${s.clazzName || '未分班'}）· ${s.sid}`"
                :value="s.sid"
            />
          </el-select>
        </div>
        <div class="form-item">
          <span class="form-label">总学分要求</span>
          <el-input-number v-model="requiredTotalCredit" :min="0" :step="5" :precision="1" />
        </div>
        <div class="form-item">
          <span class="form-label">绩点要求</span>
          <el-input-number v-model="requiredGpa" :min="0" :max="5" :step="0.1" :precision="2" />
        </div>
        <el-button type="primary" :loading="auditing" class="btn-glass-primary" @click="runAudit">开始审核</el-button>
      </div>

      <div v-if="result && result.detail" class="result-card">
        <div class="result-head">
          <el-tag size="large" effect="dark" :type="result.conclusion === '通过' ? 'success' : 'danger'">
            {{ result.conclusion }}
          </el-tag>
          <span class="result-student">{{ result.studentName }} · {{ result.semesterName || '未分学期' }}</span>
          <el-tag size="small" effect="plain" type="info">{{ modeLabel(result.detail.mode) }}</el-tag>
        </div>
        <div class="metric-grid">
          <div class="metric">
            <div class="metric-label">已获学分 / 要求</div>
            <div class="metric-value" :class="{ bad: !result.detail.creditOk }">
              {{ result.detail.earnedCredit }} / {{ result.detail.requiredCredit }}
            </div>
            <div class="metric-state">{{ result.detail.creditOk ? '达标' : '未达标' }}</div>
          </div>
          <div class="metric">
            <div class="metric-label">绩点 / 要求</div>
            <div class="metric-value" :class="{ bad: !result.detail.gpaOk }">
              {{ result.detail.gpa }} / {{ result.detail.requiredGpa }}
            </div>
            <div class="metric-state">{{ result.detail.gpaOk ? '达标' : '未达标' }}</div>
          </div>
          <div class="metric">
            <div class="metric-label">必修课程</div>
            <div class="metric-value" :class="{ bad: result.detail.requiredOk === false }">
              {{ result.detail.mode === 'plan' ? (result.detail.requiredOk ? '全部通过' : '存在缺口') : (result.detail.passedOk ? '全部通过' : '存在不及格') }}
            </div>
            <div class="metric-state">
              {{ result.detail.mode === 'plan' ? (result.detail.requiredOk ? '达标' : '未达标') : (result.detail.passedOk ? '达标' : '未达标') }}
            </div>
          </div>
          <div v-if="result.detail.mode === 'plan'" class="metric">
            <div class="metric-label">选修学分 已获/要求</div>
            <div class="metric-value" :class="{ bad: !result.detail.electiveOk }">
              {{ result.detail.electiveCreditEarned }} / {{ result.detail.electiveCreditRequired }}
            </div>
            <div class="metric-state">{{ result.detail.electiveOk ? '达标' : '未达标' }}</div>
          </div>
        </div>
        <div v-if="result.detail.missingRequired?.length" class="detail-list">
          <div class="detail-title">缺修必修课程</div>
          <el-tag v-for="(m, i) in result.detail.missingRequired" :key="i" type="danger" effect="plain" class="detail-tag">{{ m }}</el-tag>
        </div>
        <div v-if="result.detail.failedRequired?.length" class="detail-list">
          <div class="detail-title">必修不及格</div>
          <el-tag v-for="(f, i) in result.detail.failedRequired" :key="i" type="danger" effect="plain" class="detail-tag">{{ f }}</el-tag>
        </div>
        <div v-if="result.detail.failedCourses?.length" class="detail-list">
          <div class="detail-title">不及格课程</div>
          <el-tag v-for="(f, i) in result.detail.failedCourses" :key="i" type="danger" effect="plain" class="detail-tag">
            {{ f.courseName }}（{{ f.total }}分）
          </el-tag>
        </div>
      </div>

      <div class="history-block">
        <div class="history-title">审核历史</div>
        <el-table :data="history" class="glass-table" v-loading="historyLoading" empty-text="该学生暂无审核记录">
          <el-table-column type="expand">
            <template #default="scope">
              <div class="history-detail">
                <pre class="detail-json">{{ JSON.stringify(scope.row.detail, null, 2) }}</pre>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="id" label="记录ID" width="80" align="center" />
          <el-table-column prop="studentName" label="学生" min-width="110" />
          <el-table-column label="学期" min-width="150">
            <template #default="scope">{{ scope.row.semesterName || '未分学期' }}</template>
          </el-table-column>
          <el-table-column label="结论" width="90" align="center">
            <template #default="scope">
              <el-tag size="small" effect="light" :type="scope.row.conclusion === '通过' ? 'success' : 'danger'">{{ scope.row.conclusion }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="审核人" width="100" align="center">
            <template #default="scope">{{ scope.row.auditorName || '-' }}</template>
          </el-table-column>
          <el-table-column label="审核时间" width="170" align="center">
            <template #default="scope">{{ fmtTime(scope.row.auditTime) }}</template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>
<style scoped>
.audit-page {
  display: flex;
  justify-content: center;
  width: 100%;
}
.audit-page .glass-card {
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
.card-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}
.card-tip {
  font-size: 12px;
  color: var(--text-muted);
}
.audit-form {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 18px;
  margin-bottom: 18px;
}
.form-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
.form-label {
  font-size: 13px;
  color: var(--text-secondary);
  white-space: nowrap;
}
.student-select {
  width: 240px;
}
.result-card {
  border: 1px solid var(--glass-border);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 18px;
  background: rgba(255, 255, 255, 0.35);
}
.result-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}
.result-student {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}
.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.metric {
  text-align: center;
  padding: 10px;
  border-radius: 10px;
  background: rgba(140, 190, 255, 0.08);
}
.metric-label {
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 6px;
}
.metric-value {
  font-family: var(--font-mono);
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}
.metric-value.bad {
  color: #f56c6c;
}
.metric-state {
  font-size: 12px;
  margin-top: 4px;
  color: var(--text-secondary);
}
.detail-list {
  margin-top: 14px;
}
.detail-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}
.detail-tag {
  margin: 0 8px 8px 0;
}
.history-block {
  margin-top: 6px;
}
.history-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 10px;
}
.history-detail {
  padding: 8px 16px;
}
.detail-json {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--text-secondary);
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0;
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
</style>