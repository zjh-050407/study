<script setup>
import request from "@/utils/request.js";
import { ref, onMounted } from "vue";

const loading = ref(false)
const scoreList = ref([])
const gpa = ref(0)
const totalCredit = ref(0)
const semesterOptions = ref([])
const semesterId = ref('ALL')

const getSemesters = async () => {
  try {
    const result = await request.get('/semester/list')
    if (result.data.code === 0) {
      semesterOptions.value = result.data.data || []
    }
  } catch (err) {
    console.error('获取学期失败:', err)
  }
}

const getScores = async () => {
  loading.value = true
  try {
    const params = {}
    if (semesterId.value === 'LEGACY') {
      params.semesterId = 0
    } else if (semesterId.value !== 'ALL') {
      params.semesterId = semesterId.value
    }
    const result = await request.get('/score/my', { params })
    if (result.data.code === 0) {
      scoreList.value = result.data.data.list || []
      gpa.value = result.data.data.gpa || 0
      totalCredit.value = result.data.data.totalCredit || 0
    }
  } catch (err) {
    console.error('获取成绩失败:', err)
  } finally {
    loading.value = false
  }
}

const handleSemesterChange = () => getScores()

const gpTag = (gp) => {
  if (gp == null) return 'info'
  if (gp >= 3.0) return 'success'
  if (gp >= 2.0) return 'warning'
  return 'danger'
}

onMounted(async () => {
  await getSemesters()
  getScores()
})
</script>
<template>
  <div class="score-page">
    <el-card class="glass-card summary-card" shadow="never">
      <div class="summary-row">
        <div class="summary-item">
          <div class="summary-value">{{ gpa }}</div>
          <div class="summary-label">平均绩点 GPA（加权）</div>
        </div>
        <div class="summary-divider"></div>
        <div class="summary-item">
          <div class="summary-value">{{ totalCredit }}</div>
          <div class="summary-label">已修总学分</div>
        </div>
        <div class="filter-box">
          <span class="filter-label">学期</span>
          <el-select v-model="semesterId" class="glass-select" style="width: 200px" @change="handleSemesterChange">
            <el-option label="全部学期" value="ALL" />
            <el-option v-for="sem in semesterOptions" :key="sem.id" :label="sem.name" :value="sem.id" />
            <el-option label="未分学期（旧数据）" value="LEGACY" />
          </el-select>
        </div>
      </div>
    </el-card>

    <el-card class="glass-card list-card" shadow="never" v-loading="loading">
      <el-table :data="scoreList" class="glass-table" empty-text="暂无成绩记录">
        <el-table-column prop="courseName" label="课程" min-width="150" show-overflow-tooltip />
        <el-table-column label="学分" width="70" align="center">
          <template #default="scope">{{ scope.row.credit ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="usual" label="平时" width="70" align="center" />
        <el-table-column prop="mid" label="期中" width="70" align="center" />
        <el-table-column prop="finalScore" label="期末" width="70" align="center" />
        <el-table-column prop="total" label="总评" width="80" align="center" />
        <el-table-column label="绩点" width="90" align="center">
          <template #default="scope">
            <el-tag :type="gpTag(scope.row.gp)" size="small" effect="light">{{ scope.row.gp }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="学期" width="160" align="center">
          <template #default="scope">{{ scope.row.semesterName || '未分学期' }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
<style scoped>
.score-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: center;
  width: 100%;
}
.summary-card, .list-card {
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
.summary-row {
  display: flex;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
}
.summary-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  min-width: 140px;
}
.summary-value {
  font-family: var(--font-mono);
  font-size: 34px;
  font-weight: 700;
  color: var(--primary);
}
.summary-label {
  font-size: 12px;
  color: var(--text-muted);
}
.summary-divider {
  width: 1px;
  height: 48px;
  background: var(--glass-border);
}
.filter-box {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 8px;
}
.filter-label {
  font-size: 13px;
  color: var(--text-secondary);
}
:deep(.glass-select .el-input__wrapper) {
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
</style>