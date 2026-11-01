<script setup>
import request from "@/utils/request.js";
import { ref, onMounted, computed } from "vue";
import { ElMessage } from 'element-plus'

const changeTypes = ['休学', '复学', '转专业', '退学']
const activeTab = ref('all')
const studentOptions = ref([])
const studentLoading = ref(false)
const clazzes = ref([])

const form = ref({ sid: null, type: '', effectiveDate: '', reason: '', targetCid: null })
const submitting = ref(false)
const currentStatus = ref(null)

const allList = ref([])
const allLoading = ref(false)

const querySid = ref(null)
const queryList = ref([])
const queryLoading = ref(false)
const queryStatus = ref(null)

const searchStudents = async (name) => {
  studentLoading.value = true
  try {
    const res = await request.get('/student/stu2', { params: { name: name || '' } })
    if (res.data.code === 0) {
      studentOptions.value = (res.data.data || []).map(s => ({ sid: s.sid, name: s.name, clazzName: s.clazzName }))
    }
  } finally {
    studentLoading.value = false
  }
}

const getClazzes = async () => {
  const res = await request.get('/clazz/list')
  if (res.data.code === 0) {
    clazzes.value = res.data.data || []
  }
}

const showTargetCid = computed(() => form.value.type === '转专业')

const refreshCurrentStatus = async (sid) => {
  if (!sid) {
    currentStatus.value = null
    return
  }
  const res = await request.get('/statusChange/current', { params: { sid } })
  if (res.data.code === 0) {
    currentStatus.value = res.data.data || null
  }
}

const submitChange = async () => {
  if (!form.value.sid) {
    ElMessage.warning('请选择学生')
    return
  }
  if (!form.value.type) {
    ElMessage.warning('请选择异动类型')
    return
  }
  if (!form.value.effectiveDate) {
    ElMessage.warning('请选择生效日期')
    return
  }
  if (form.value.type === '转专业' && !form.value.targetCid) {
    ElMessage.warning('转专业必须选择目标班级')
    return
  }
  submitting.value = true
  try {
    const sid = form.value.sid
    const res = await request.post('/statusChange', form.value)
    if (res.data.code === 0) {
      ElMessage.success('异动登记成功')
      form.value = { sid: null, type: '', effectiveDate: '', reason: '', targetCid: null }
      await Promise.all([getAll(), refreshCurrentStatus(sid)])
    } else {
      ElMessage.error(res.data.message || '登记失败')
    }
  } finally {
    submitting.value = false
  }
}

const getAll = async () => {
  allLoading.value = true
  try {
    const res = await request.get('/statusChange/list')
    if (res.data.code === 0) {
      allList.value = res.data.data || []
    }
  } finally {
    allLoading.value = false
  }
}

const changeQuerySid = async () => {
  queryStatus.value = null
  if (!querySid.value) {
    queryList.value = []
    return
  }
  queryLoading.value = true
  try {
    const [hRes, cRes] = await Promise.all([
      request.get('/statusChange/history', { params: { sid: querySid.value } }),
      request.get('/statusChange/current', { params: { sid: querySid.value } })
    ])
    if (hRes.data.code === 0) queryList.value = hRes.data.data || []
    if (cRes.data.code === 0) queryStatus.value = cRes.data.data || null
  } finally {
    queryLoading.value = false
  }
}

const fmtDate = (d) => d ? String(d).slice(0, 10) : '-'
const fmtTime = (t) => t ? String(t).replace('T', ' ').slice(0, 19) : '-'

const typeTag = (t) => t === '休学' ? 'warning' : t === '退学' ? 'danger' : t === '转专业' ? 'primary' : 'success'
const statusTag = (t) => t === '在校' ? 'success' : t === '休学' ? 'warning' : 'danger'

onMounted(async () => {
  await Promise.all([searchStudents(''), getClazzes(), getAll()])
})
</script>
<template>
  <div class="status-page">
    <el-card class="glass-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">学籍异动</span>
          <span class="card-tip">休学/退学学生将被拦截选课与毕业审核；转专业自动联动班级</span>
        </div>
      </template>

      <div class="register-form glass-inner">
        <div class="form-row-title">异动登记</div>
        <div class="form-row">
          <el-select
              v-model="form.sid"
              filterable
              remote
              :remote-method="searchStudents"
              :loading="studentLoading"
              placeholder="输入姓名搜索学生"
              class="student-select"
              @change="refreshCurrentStatus"
          >
            <el-option
                v-for="s in studentOptions"
                :key="s.sid"
                :label="`${s.name}（${s.clazzName || '未分班'}）· ${s.sid}`"
                :value="s.sid"
            />
          </el-select>
          <el-select v-model="form.type" placeholder="异动类型" class="type-select">
            <el-option v-for="t in changeTypes" :key="t" :label="t" :value="t" />
          </el-select>
          <el-date-picker v-model="form.effectiveDate" type="date" placeholder="生效日期"
                          value-format="YYYY-MM-DD" class="date-picker" />
          <el-input v-model="form.reason" placeholder="原因说明（选填）" class="reason-input" />
        </div>
        <div v-if="showTargetCid" class="form-row">
          <span class="field-label">目标班级</span>
          <el-select v-model="form.targetCid" placeholder="转专业目标班级" class="clazz-select">
            <el-option v-for="c in clazzes" :key="c.cid" :label="c.name" :value="c.cid" />
          </el-select>
        </div>
        <div class="form-row">
          <el-button type="primary" :loading="submitting" class="btn-glass-primary" @click="submitChange">确认登记</el-button>
          <span v-if="currentStatus" class="status-hint">
            当前状态：
            <el-tag size="small" effect="light" :type="statusTag(currentStatus.type)">{{ currentStatus.type }}</el-tag>
            <span v-if="currentStatus.effectiveDate" class="muted-text">（{{ currentStatus.effectiveDate }} 起）</span>
          </span>
        </div>
      </div>

      <el-tabs v-model="activeTab" class="glass-tabs">
        <el-tab-pane label="全部异动记录" name="all">
          <el-table :data="allList" class="glass-table" v-loading="allLoading" empty-text="暂无异动记录">
            <el-table-column prop="id" label="记录ID" width="80" align="center" />
            <el-table-column label="学生" min-width="140">
              <template #default="scope">{{ scope.row.studentName }}（{{ scope.row.sid }}）</template>
            </el-table-column>
            <el-table-column label="类型" width="90" align="center">
              <template #default="scope">
                <el-tag size="small" effect="light" :type="typeTag(scope.row.type)">{{ scope.row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="生效日期" width="110" align="center">
              <template #default="scope">{{ fmtDate(scope.row.effectiveDate) }}</template>
            </el-table-column>
            <el-table-column label="原因" min-width="160" show-overflow-tooltip>
              <template #default="scope">{{ scope.row.reason || '-' }}</template>
            </el-table-column>
            <el-table-column label="目标班级" width="110" align="center">
              <template #default="scope">{{ scope.row.targetClazzName || '-' }}</template>
            </el-table-column>
            <el-table-column label="经办人" width="100" align="center">
              <template #default="scope">{{ scope.row.operatorName || '-' }}</template>
            </el-table-column>
            <el-table-column label="登记时间" width="170" align="center">
              <template #default="scope">{{ fmtTime(scope.row.createTime) }}</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="按学生查询" name="query">
          <div class="toolbar">
            <el-select
                v-model="querySid"
                filterable
                remote
                :remote-method="searchStudents"
                :loading="studentLoading"
                placeholder="输入姓名搜索学生"
                class="student-select"
                @change="changeQuerySid"
            >
              <el-option
                  v-for="s in studentOptions"
                  :key="s.sid"
                  :label="`${s.name}（${s.clazzName || '未分班'}）· ${s.sid}`"
                  :value="s.sid"
              />
            </el-select>
            <span v-if="queryStatus" class="status-hint">
              当前状态：
              <el-tag size="small" effect="light" :type="statusTag(queryStatus.type)">{{ queryStatus.type }}</el-tag>
            </span>
          </div>
          <el-table :data="queryList" class="glass-table" v-loading="queryLoading" empty-text="该学生暂无异动记录">
            <el-table-column prop="id" label="记录ID" width="80" align="center" />
            <el-table-column label="类型" width="90" align="center">
              <template #default="scope">
                <el-tag size="small" effect="light" :type="typeTag(scope.row.type)">{{ scope.row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="生效日期" width="110" align="center">
              <template #default="scope">{{ fmtDate(scope.row.effectiveDate) }}</template>
            </el-table-column>
            <el-table-column label="原因" min-width="160" show-overflow-tooltip>
              <template #default="scope">{{ scope.row.reason || '-' }}</template>
            </el-table-column>
            <el-table-column label="目标班级" width="110" align="center">
              <template #default="scope">{{ scope.row.targetClazzName || '-' }}</template>
            </el-table-column>
            <el-table-column label="经办人" width="100" align="center">
              <template #default="scope">{{ scope.row.operatorName || '-' }}</template>
            </el-table-column>
            <el-table-column label="登记时间" width="170" align="center">
              <template #default="scope">{{ fmtTime(scope.row.createTime) }}</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>
<style scoped>
.status-page {
  display: flex;
  justify-content: center;
  width: 100%;
}
.status-page .glass-card {
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
.register-form {
  border-radius: 12px;
  padding: 14px 16px;
  margin-bottom: 16px;
}
.glass-inner {
  background: rgba(255, 255, 255, 0.35);
  border: 1px solid var(--glass-border-light);
}
.form-row-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 10px;
}
.form-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 10px;
}
.form-row:last-child {
  margin-bottom: 0;
}
.student-select {
  width: 240px;
}
.type-select {
  width: 120px;
}
.date-picker {
  width: 160px;
}
.reason-input {
  width: 280px;
}
.clazz-select {
  width: 200px;
}
.field-label {
  font-size: 13px;
  color: var(--text-secondary);
}
.status-hint {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-secondary);
}
.muted-text {
  color: var(--text-muted);
  font-size: 12px;
}
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}
:deep(.glass-tabs .el-tabs__item.is-active) {
  color: var(--primary);
}
:deep(.glass-tabs .el-tabs__active-bar) {
  background-color: var(--primary);
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