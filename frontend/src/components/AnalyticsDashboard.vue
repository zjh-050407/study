<script setup>
import request from "@/utils/request.js";
import { ref, onMounted, onBeforeUnmount } from "vue";
import * as echarts from 'echarts';

const loading = ref(false)
const overview = ref(null)
const examData = ref(null)
const charts = {}

const loadAll = async () => {
  loading.value = true
  try {
    const [ov, dept, score, hot, eva, exam] = await Promise.all([
      request.get('/stats/overview'),
      request.get('/stats/studentByDept'),
      request.get('/stats/scoreDistribution'),
      request.get('/stats/selectionHot'),
      request.get('/stats/evaluationAvg'),
      request.get('/stats/examOverview')
    ])
    if (ov.data.code === 0) overview.value = ov.data.data
    if (dept.data.code === 0) renderBar('deptChart', dept.data.data, '院系学生分布', '人数')
    if (score.data.code === 0) renderScoreDist(score.data.data[0])
    if (hot.data.code === 0) renderBar('hotChart', hot.data.data, '本学期选课热度 Top10', '人次', true)
    if (eva.data.code === 0) renderBar('evaChart', eva.data.data, '教师评教均分 Top10', '均分', true)
    if (exam.data.code === 0) {
      examData.value = exam.data.data
      renderExam(exam.data.data)
    }
  } finally {
    loading.value = false
  }
}

const renderBar = (id, data, title, unit, horizontal = false) => {
  const el = document.getElementById(id)
  if (!el || !data || !data.length) return
  if (charts[id]) charts[id].dispose()
  const chart = echarts.init(el)
  charts[id] = chart
  const names = data.map(d => d.name)
  const values = data.map(d => Number(d.value))
  const option = {
    title: { text: title, left: 'center', textStyle: { fontSize: 14, color: '#334155' } },
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 40, bottom: 30 },
    xAxis: horizontal
      ? { type: 'value' }
      : { type: 'category', data: names, axisLabel: { interval: 0, rotate: names.length > 5 ? 30 : 0, fontSize: 11 } },
    yAxis: horizontal
      ? { type: 'category', data: names, axisLabel: { fontSize: 11 } }
      : { type: 'value' },
    series: [{
      type: 'bar',
      data: values,
      barMaxWidth: 26,
      itemStyle: {
        borderRadius: [4, 4, 0, 0],
        color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [
          { offset: 0, color: '#7ab8ff' }, { offset: 1, color: '#4a90e2' } ] }
      },
      label: { show: true, position: horizontal ? 'right' : 'top', fontSize: 10, color: '#334155' }
    }]
  }
  chart.setOption(option)
}

const renderScoreDist = (row) => {
  if (!row) return
  const data = [
    { name: '60分以下', value: Number(row.below60 || 0) },
    { name: '60-69', value: Number(row.s60to70 || 0) },
    { name: '70-79', value: Number(row.s70to80 || 0) },
    { name: '80-89', value: Number(row.s80to90 || 0) },
    { name: '90分以上', value: Number(row.above90 || 0) }
  ]
  const el = document.getElementById('scoreChart')
  if (!el) return
  if (charts.scoreChart) charts.scoreChart.dispose()
  const chart = echarts.init(el)
  charts.scoreChart = chart
  chart.setOption({
    title: { text: '成绩分段分布', left: 'center', textStyle: { fontSize: 14, color: '#334155' } },
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, textStyle: { fontSize: 11 } },
    series: [{
      type: 'pie',
      radius: ['38%', '62%'],
      center: ['50%', '46%'],
      data,
      label: { fontSize: 11 },
      itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 1 }
    }]
  })
}

const renderExam = (row) => {
  if (!row) return
  const el = document.getElementById('examChart')
  if (!el) return
  if (charts.examChart) charts.examChart.dispose()
  const chart = echarts.init(el)
  charts.examChart = chart
  chart.setOption({
    title: { text: '考务概览', left: 'center', textStyle: { fontSize: 14, color: '#334155' } },
    tooltip: {},
    series: [{
      type: 'gauge',
      radius: '80%',
      data: [{ value: Number(row.weekExamCount || 0), name: '本周考试场次' }],
      detail: { fontSize: 18, formatter: '{value} 场' },
      axisLine: { lineStyle: { color: [[0.3, '#67c23a'], [0.7, '#e6a23c'], [1, '#f56c6c']], width: 8 } }
    }]
  })
}

const resize = () => {
  Object.values(charts).forEach(c => c && c.resize())
}

onMounted(() => {
  loadAll()
  window.addEventListener('resize', resize)
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  Object.values(charts).forEach(c => c && c.dispose())
})
</script>
<template>
  <div class="dashboard" v-loading="loading">
    <div class="metric-grid">
      <div class="glass-card metric-card">
        <div class="metric-label">在校学生</div>
        <div class="metric-value">{{ overview?.studentCount ?? '-' }}</div>
      </div>
      <div class="glass-card metric-card">
        <div class="metric-label">教师人数</div>
        <div class="metric-value">{{ overview?.teacherCount ?? '-' }}</div>
      </div>
      <div class="glass-card metric-card">
        <div class="metric-label">课程总数</div>
        <div class="metric-value">{{ overview?.courseCount ?? '-' }}</div>
      </div>
      <div class="glass-card metric-card">
        <div class="metric-label">班级数量</div>
        <div class="metric-value">{{ overview?.clazzCount ?? '-' }}</div>
      </div>
      <div class="glass-card metric-card">
        <div class="metric-label">本学期选课人次</div>
        <div class="metric-value">{{ overview?.selectionCount ?? '-' }}</div>
      </div>
    </div>

    <div class="chart-grid">
      <div class="glass-card chart-card"><div id="deptChart" class="chart-box"></div></div>
      <div class="glass-card chart-card"><div id="scoreChart" class="chart-box"></div></div>
      <div class="glass-card chart-card"><div id="hotChart" class="chart-box"></div></div>
      <div class="glass-card chart-card"><div id="evaChart" class="chart-box"></div></div>
      <div class="glass-card chart-card"><div id="examChart" class="chart-box"></div></div>
      <div class="glass-card chart-card metric-board">
        <div class="board-title">考务数据</div>
        <div class="board-row"><span>本周考试场次</span><b>{{ examData?.weekExamCount ?? '-' }}</b></div>
        <div class="board-row"><span>累计考试安排</span><b>{{ examData?.totalExamCount ?? '-' }}</b></div>
        <div class="board-row"><span>待排课教学任务</span><b>{{ examData?.pendingTaskCount ?? '-' }}</b></div>
        <div class="board-tip">本周口径：未来7天内</div>
      </div>
    </div>
  </div>
</template>
<style scoped>
.dashboard {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.metric-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 14px;
}
.metric-card {
  padding: 18px 20px;
  text-align: center;
}
.metric-label {
  font-size: 13px;
  color: var(--text-secondary);
}
.metric-value {
  margin-top: 6px;
  font-size: 26px;
  font-weight: 700;
  color: var(--primary);
}
.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}
.chart-card {
  padding: 10px;
}
.chart-box {
  height: 260px;
}
.metric-board {
  padding: 18px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 14px;
}
.board-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  text-align: center;
}
.board-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: var(--text-secondary);
}
.board-row b {
  color: var(--primary);
  font-size: 16px;
}
.board-tip {
  font-size: 12px;
  color: var(--text-muted);
  text-align: center;
}
.glass-card {
  background: var(--glass-bg-strong);
  backdrop-filter: var(--blur-md);
  -webkit-backdrop-filter: var(--blur-md);
  border: 1px solid var(--glass-border);
  border-radius: 16px;
  box-shadow: var(--glass-shadow);
}
@media (max-width: 1100px) {
  .metric-grid { grid-template-columns: repeat(2, 1fr); }
  .chart-grid { grid-template-columns: 1fr; }
}
</style>
