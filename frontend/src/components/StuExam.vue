<script setup>
import request from "@/utils/request.js";
import { ref, onMounted } from "vue";

const list = ref([])
const loading = ref(false)

const getExams = async () => {
  loading.value = true
  try {
    const result = await request.get('/exam/my')
    if (result.data.code === 0) {
      list.value = result.data.data || []
    }
  } finally {
    loading.value = false
  }
}

const weekday = (date) => {
  if (!date) return '-'
  const d = new Date(date.replace(/-/g, '/'))
  return ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][d.getDay()]
}

onMounted(getExams)
</script>
<template>
  <div class="exam-page">
    <el-card class="glass-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">考试安排</span>
          <span class="card-tip">依据你的选课记录展示本学期考试时间表</span>
        </div>
      </template>
      <el-table :data="list" class="glass-table" v-loading="loading" empty-text="暂无考试安排">
        <el-table-column prop="courseName" label="课程" min-width="150" show-overflow-tooltip />
        <el-table-column label="学期" width="180" align="center">
          <template #default="scope">{{ scope.row.semesterName || '未分学期' }}</template>
        </el-table-column>
        <el-table-column label="考试日期" width="160" align="center">
          <template #default="scope">
            {{ scope.row.examDate }}（{{ weekday(scope.row.examDate) }}）
          </template>
        </el-table-column>
        <el-table-column label="节次" width="90" align="center">
          <template #default="scope">{{ scope.row.startSection }}-{{ scope.row.endSection }}节</template>
        </el-table-column>
        <el-table-column prop="room" label="考场" width="100" align="center" />
        <el-table-column label="类型" width="80" align="center">
          <template #default="scope">
            <el-tag size="small" effect="light" :type="scope.row.type === '期末' ? 'primary' : scope.row.type === '补考' ? 'warning' : 'info'">
              {{ scope.row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="监考教师" width="110" align="center">
          <template #default="scope">{{ scope.row.invigilatorName || '-' }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
<style scoped>
.exam-page {
  display: flex;
  justify-content: center;
  width: 100%;
}
.exam-page .glass-card {
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