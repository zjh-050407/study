<script setup>
import request from "@/utils/request.js";
import { ref, onMounted, computed } from "vue";
import { ElMessage } from 'element-plus'
import { Warning, Download } from "@element-plus/icons-vue";

const failScores = ref([])
const lowGpaStudents = ref([])
const loading = ref(false)

const getData = async () => {
  loading.value = true
  try {
    const result = await request.get('/scoreWarning/list')
    if (result.data.code === 0) {
      failScores.value = result.data.data.failScores || []
      lowGpaStudents.value = result.data.data.lowGpaStudents || []
    }
  } catch (e) {
    ElMessage.error('获取预警数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(getData)

const totalWarnings = computed(() => failScores.value.length + lowGpaStudents.value.length)

const exportCsv = () => {
  const rows = [
    ['类型', '学号', '姓名', '班级', '课程/门数', '总评/GPA'],
    ...failScores.value.map(item => ['不及格', item.sid, item.studentName, item.clazzName, item.courseName, item.total]),
    ...lowGpaStudents.value.map(item => ['低绩点', item.sid, item.studentName, item.clazzName, item.courseCount + '门', item.gpa])
  ]
  const csvContent = rows.map(row => row.join(',')).join('\n')
  const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = '成绩预警名单.csv'
  link.click()
}
</script>
<template>
  <div class="contain">
    <el-card class="glass-card" style="max-width: 1100px; width: 100%">
      <template #header>
        <div class="card-header">
          <div class="card-title">
            <el-icon :size="20" color="#f56c6c"><Warning /></el-icon>
            <span>成绩异常预警中心</span>
          </div>
          <el-button :icon="Download" round @click="exportCsv" class="btn-glass-outline">导出预警名单</el-button>
        </div>
      </template>

      <div class="warning-stats">
        <div class="warning-stat-card fail">
          <div class="warning-num">{{ failScores.length }}</div>
          <div class="warning-label">不及格记录</div>
        </div>
        <div class="warning-stat-card gpa">
          <div class="warning-num">{{ lowGpaStudents.length }}</div>
          <div class="warning-label">低绩点学生</div>
        </div>
        <div class="warning-stat-card total">
          <div class="warning-num">{{ totalWarnings }}</div>
          <div class="warning-label">预警总数</div>
        </div>
      </div>

      <div class="section-title">不及格成绩明细</div>
      <el-table :data="failScores" class="glass-table" v-loading="loading" style="margin-bottom: 24px;">
        <el-table-column prop="sid" label="学号" width="80" />
        <el-table-column prop="studentName" label="姓名" width="120" />
        <el-table-column prop="clazzName" label="班级" width="140" />
        <el-table-column prop="courseName" label="课程" width="180" />
        <el-table-column prop="total" label="总评" width="100">
          <template #default="scope">
            <span class="fail-score">{{ scope.row.total }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="预警类型" width="120">
          <template #default="scope">
            <el-tag type="danger" size="small">{{ scope.row.type }}</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div class="section-title">低绩点学生名单（平均 GPA < 2.0）</div>
      <el-table :data="lowGpaStudents" class="glass-table" v-loading="loading">
        <el-table-column prop="sid" label="学号" width="80" />
        <el-table-column prop="studentName" label="姓名" width="120" />
        <el-table-column prop="clazzName" label="班级" width="140" />
        <el-table-column prop="courseCount" label="已修课程门数" width="130" />
        <el-table-column prop="gpa" label="平均 GPA" width="120">
          <template #default="scope">
            <span class="low-gpa">{{ scope.row.gpa }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="预警类型" width="120">
          <template #default="scope">
            <el-tag type="warning" size="small">{{ scope.row.type }}</el-tag>
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
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}
.warning-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}
.warning-stat-card {
  flex: 1;
  text-align: center;
  padding: 18px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.4);
  border: 1px solid var(--glass-border-light);
}
.warning-stat-card.fail { border-top: 3px solid #f56c6c; }
.warning-stat-card.gpa { border-top: 3px solid #e6a23c; }
.warning-stat-card.total { border-top: 3px solid #4a90e2; }
.warning-num {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
}
.warning-label {
  font-size: 13px;
  color: var(--text-muted);
  margin-top: 4px;
}
.section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
  padding-left: 10px;
  border-left: 4px solid #4a90e2;
}
.fail-score {
  color: #f56c6c;
  font-weight: 700;
}
.low-gpa {
  color: #e6a23c;
  font-weight: 700;
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
:deep(.btn-glass-outline) {
  background: rgba(255, 255, 255, 0.4) !important;
  border: 1px solid var(--glass-border) !important;
  border-radius: 8px !important;
  color: var(--text-secondary);
  transition: all 0.2s;
}
:deep(.btn-glass-outline:hover) {
  background: rgba(255, 255, 255, 0.6) !important;
  border-color: var(--blue-300) !important;
  color: var(--primary);
}
</style>
