<script setup>
import { computed } from "vue";

const props = defineProps({
  slots: { type: Array, default: () => [] },
  title: { type: String, default: '课表' },
  subtitle: { type: String, default: '' },
  loading: { type: Boolean, default: false },
})

const dayOptions = [
  { label: '周一', value: 1 },
  { label: '周二', value: 2 },
  { label: '周三', value: 3 },
  { label: '周四', value: 4 },
  { label: '周五', value: 5 },
  { label: '周六', value: 6 },
  { label: '周日', value: 7 },
]

const maxSection = computed(() => {
  let max = 8
  props.slots.forEach(s => {
    const n = Number(s.section)
    if (n > max) max = n
  })
  return max
})

const sections = computed(() => Array.from({ length: maxSection.value }, (_, i) => i + 1))

const cells = computed(() => {
  const map = {}
  props.slots.forEach(slot => {
    const key = slot.weekDay + '-' + slot.section
    if (!map[key]) map[key] = []
    map[key].push(slot)
  })
  return map
})

const getCell = (day, sec) => cells.value[day + '-' + sec] || []

const weekLabel = (slot) => {
  if (slot.startWeek != null && slot.endWeek != null) {
    return `${slot.startWeek}-${slot.endWeek}周`
  }
  return ''
}
</script>
<template>
  <div class="schedule-page">
    <el-card class="glass-card schedule-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">{{ title }}</span>
          <span class="card-sub">{{ subtitle }}</span>
        </div>
      </template>
      <el-table :data="sections.map(s => ({ section: s }))" class="schedule-table glass-table" v-loading="loading"
                :header-cell-style="{ textAlign: 'center' }" empty-text="暂无数据">
        <el-table-column label="节次" width="90" align="center">
          <template #default="scope">
            <div class="section-label">第{{ scope.row.section }}节</div>
          </template>
        </el-table-column>
        <el-table-column v-for="day in dayOptions" :key="day.value" :label="day.label" min-width="135" align="center">
          <template #default="scope">
            <div class="cell">
              <div v-for="(slot, idx) in getCell(day.value, scope.row.section)" :key="idx" class="course-block">
                <div class="course-name">{{ slot.courseName }}</div>
                <div class="course-meta">@{{ slot.classroom || '未排教室' }}</div>
                <div class="course-meta">{{ slot.teacherName || '未定教师' }}</div>
                <div v-if="weekLabel(slot)" class="course-weeks">{{ weekLabel(slot) }}</div>
              </div>
              <span v-if="getCell(day.value, scope.row.section).length === 0" class="empty-cell"></span>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
<style scoped>
.schedule-page {
  display: flex;
  justify-content: center;
  width: 100%;
}
.schedule-card {
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
:deep(.glass-card .el-card__header) {
  background: rgba(255, 255, 255, 0.3);
  border-bottom: 1px solid var(--glass-border-light);
  border-radius: 16px 16px 0 0;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}
.card-title {
  font-family: var(--font-serif);
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}
.card-sub {
  font-family: var(--font-mono);
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
.section-label {
  font-weight: 600;
  color: var(--text-primary);
}
.cell {
  min-height: 52px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: stretch;
}
.course-block {
  background: linear-gradient(135deg, rgba(74, 144, 226, 0.08), rgba(217, 119, 87, 0.08));
  border: 1px solid rgba(74, 144, 226, 0.2);
  border-radius: 8px;
  padding: 4px 6px;
}
.course-name {
  font-weight: 600;
  font-size: 13px;
  color: var(--text-primary);
}
.course-meta {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--text-muted);
}
.course-weeks {
  font-family: var(--font-mono);
  font-size: 10px;
  color: var(--blue-600);
  background: var(--blue-50);
  border-radius: 3px;
  display: inline-block;
  padding: 0 4px;
  margin-top: 2px;
}
.empty-cell {
  display: inline-block;
  width: 100%;
  min-height: 20px;
}
</style>
