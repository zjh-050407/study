<script setup>
import request from "@/utils/request.js";
import { ref, onMounted } from "vue";
import VChart from "vue-echarts";
import { use } from "echarts/core";
import { CanvasRenderer } from "echarts/renderers";
import { PieChart, BarChart, HeatmapChart } from "echarts/charts";
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent, VisualMapComponent } from "echarts/components";
import { ElMessage } from 'element-plus'
import { User, UserFilled, Notebook, School } from '@element-plus/icons-vue'

use([CanvasRenderer, PieChart, BarChart, HeatmapChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent, VisualMapComponent]);

const loading = ref(true)
const kpi = ref({ studentCount: 0, teacherCount: 0, courseCount: 0, clazzCount: 0 })
const recentLogs = ref([])
const notices = ref([])

const sexOption = ref({
  title: { text: '学生性别分布', left: 'center', textStyle: { color: '#3d3929', fontSize: 15, fontWeight: 600, fontFamily: 'Source Serif 4, serif' } },
  tooltip: { trigger: 'item', backgroundColor: '#ffffff', borderColor: '#e8e5da', textStyle: { color: '#6e6a5b' } },
  legend: { orient: 'vertical', left: 'left', textStyle: { color: '#606266' } },
  series: [{
    type: 'pie',
    radius: ['40%', '65%'],
    itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
    data: [],
    emphasis: { itemStyle: { shadowBlur: 15, shadowOffsetX: 0, shadowColor: 'rgba(217,119,87,0.25)' } },
    color: ['#d97757', '#3d3929', '#b8a98c']
  }]
});

const deptOption = ref({
  title: { text: '各院系学生人数', left: 'center', textStyle: { color: '#3d3929', fontSize: 15, fontWeight: 600, fontFamily: 'Source Serif 4, serif' } },
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, backgroundColor: '#ffffff', borderColor: '#e8e5da', textStyle: { color: '#6e6a5b' } },
  xAxis: { type: 'category', data: [], axisLabel: { color: '#6e6a5b' }, axisLine: { lineStyle: { color: '#e8e5da' } } },
  yAxis: { type: 'value', axisLabel: { color: '#6e6a5b' }, splitLine: { lineStyle: { color: '#efede4' } } },
  series: [{ type: 'bar', data: [], itemStyle: { color: '#d97757', borderRadius: [4, 4, 0, 0] }, barWidth: '50%' }]
});

const ageOption = ref({
  title: { text: '学生年龄分布', left: 'center', textStyle: { color: '#3d3929', fontSize: 15, fontWeight: 600, fontFamily: 'Source Serif 4, serif' } },
  tooltip: { trigger: 'axis', backgroundColor: '#ffffff', borderColor: '#e8e5da', textStyle: { color: '#6e6a5b' } },
  xAxis: { type: 'category', data: [], axisLabel: { color: '#6e6a5b' }, axisLine: { lineStyle: { color: '#e8e5da' } } },
  yAxis: { type: 'value', axisLabel: { color: '#6e6a5b' }, splitLine: { lineStyle: { color: '#efede4' } } },
  series: [{ type: 'bar', data: [], itemStyle: { color: '#c05f3e', borderRadius: [4, 4, 0, 0] }, barWidth: '50%' }]
});

const courseAvgOption = ref({
  title: { text: '课程平均分 TOP5', left: 'center', textStyle: { color: '#3d3929', fontSize: 15, fontWeight: 600, fontFamily: 'Source Serif 4, serif' } },
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, backgroundColor: '#ffffff', borderColor: '#e8e5da', textStyle: { color: '#6e6a5b' } },
  xAxis: { type: 'category', data: [], axisLabel: { color: '#6e6a5b', rotate: 20 }, axisLine: { lineStyle: { color: '#e8e5da' } } },
  yAxis: { type: 'value', min: 0, max: 100, axisLabel: { color: '#6e6a5b' }, splitLine: { lineStyle: { color: '#efede4' } } },
  series: [{ type: 'bar', data: [], itemStyle: { color: '#dfa98d', borderRadius: [4, 4, 0, 0] }, barWidth: '50%' }]
});

const gradeOption = ref({
  title: { text: '成绩等级分布', left: 'center', textStyle: { color: '#3d3929', fontSize: 15, fontWeight: 600, fontFamily: 'Source Serif 4, serif' } },
  tooltip: { trigger: 'item', backgroundColor: '#ffffff', borderColor: '#e8e5da', textStyle: { color: '#6e6a5b' } },
  legend: { orient: 'vertical', left: 'left', textStyle: { color: '#606266' } },
  series: [{
    type: 'pie',
    radius: ['40%', '65%'],
    itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
    data: [],
    emphasis: { itemStyle: { shadowBlur: 15, shadowOffsetX: 0, shadowColor: 'rgba(217,119,87,0.25)' } },
    color: ['#d97757', '#c05f3e', '#dfa98d', '#3d3929']
  }]
});

const scheduleOption = ref({
  title: { text: '排课时间热力图', left: 'center', textStyle: { color: '#3d3929', fontSize: 15, fontWeight: 600, fontFamily: 'Source Serif 4, serif' } },
  tooltip: { position: 'top', backgroundColor: '#ffffff', borderColor: '#e8e5da', textStyle: { color: '#6e6a5b' } },
  grid: { height: '60%', top: '20%' },
  xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五'], splitArea: { show: true } },
  yAxis: { type: 'category', data: ['第1节', '第2节', '第3节', '第4节'], splitArea: { show: true } },
  visualMap: { min: 0, max: 4, calculable: true, orient: 'horizontal', left: 'center', bottom: '5%', inRange: { color: ['#faf9f5', '#d97757'] } },
  series: [{
    type: 'heatmap',
    data: [],
    label: { show: true },
    itemStyle: { borderColor: '#fff', borderWidth: 2, borderRadius: 4 },
    emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.2)' } }
  }]
});

const formatTime = (time) => time ? time.replace('T', ' ') : '-'

onMounted(async () => {
  try {
    const res = await request.get('/statistics/dashboard')
    if (res.data.code === 0) {
      const data = res.data.data
      kpi.value = data.kpi || kpi.value
      recentLogs.value = data.recentLogs || []

      sexOption.value.series[0].data = (data.genderDistribution || []).map(item => ({ name: item.name, value: item.value }))
      deptOption.value.xAxis.data = (data.deptDistribution || []).map(item => item.name)
      deptOption.value.series[0].data = (data.deptDistribution || []).map(item => item.value)
      ageOption.value.xAxis.data = (data.ageDistribution || []).map(item => item.name)
      ageOption.value.series[0].data = (data.ageDistribution || []).map(item => item.value)
      courseAvgOption.value.xAxis.data = (data.avgScoreByCourse || []).map(item => item.name)
      courseAvgOption.value.series[0].data = (data.avgScoreByCourse || []).map(item => item.value)
      gradeOption.value.series[0].data = (data.gradeDistribution || []).map(item => ({ name: item.name, value: item.value }))

      const heatmapData = (data.scheduleHeatmap || []).map(item => [Number(item.weekDay) - 1, Number(item.section) - 1, Number(item.value)])
      scheduleOption.value.series[0].data = heatmapData
    }
  } catch (e) {
    ElMessage.error('加载数据大屏失败')
  } finally {
    loading.value = false
  }
  try {
    const nres = await request.get('/notice/latest', { params: { limit: 5 } })
    if (nres.data.code === 0) {
      notices.value = nres.data.data || []
    }
  } catch (e) {}
});
</script>
<template>
  <div class="dashboard" v-loading="loading">
    <div class="dashboard-title">
      <div class="title-text">教务数据统计</div>
      <div class="title-sub">学生 · 教师 · 课程 · 班级 全景概览</div>
    </div>

    <div class="stats-cards">
      <div class="stat-glass stat-primary">
        <el-icon :size="32"><User /></el-icon>
        <div class="stat-num">{{ kpi.studentCount }}</div>
        <div class="stat-label">学生总数</div>
      </div>
      <div class="stat-glass stat-success">
        <el-icon :size="32"><UserFilled /></el-icon>
        <div class="stat-num">{{ kpi.teacherCount }}</div>
        <div class="stat-label">教师总数</div>
      </div>
      <div class="stat-glass stat-warning">
        <el-icon :size="32"><Notebook /></el-icon>
        <div class="stat-num">{{ kpi.courseCount }}</div>
        <div class="stat-label">课程总数</div>
      </div>
      <div class="stat-glass stat-info">
        <el-icon :size="32"><School /></el-icon>
        <div class="stat-num">{{ kpi.clazzCount }}</div>
        <div class="stat-label">班级总数</div>
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-glass notice-panel">
        <div class="log-title">最新公告</div>
        <div class="notice-list">
          <div v-for="n in notices" :key="n.id" class="notice-item">
            <span class="notice-title">{{ n.title }}</span>
            <span class="notice-meta">{{ n.publisher || '-' }} · {{ formatTime(n.publishTime || n.createTime) }}</span>
          </div>
          <div v-if="notices.length === 0" class="log-empty">暂无公告</div>
        </div>
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-glass">
        <v-chart :option="sexOption" autoresize style="height: 320px;" />
      </div>
      <div class="chart-glass">
        <v-chart :option="gradeOption" autoresize style="height: 320px;" />
      </div>
      <div class="chart-glass log-panel">
        <div class="log-title">最近操作</div>
        <div class="log-list">
          <div v-for="log in recentLogs" :key="log.createTime + log.operation" class="log-item">
            <span class="log-user">{{ log.username }}</span>
            <span class="log-op">{{ log.operation }}</span>
            <span class="log-time">{{ formatTime(log.createTime) }}</span>
          </div>
          <div v-if="recentLogs.length === 0" class="log-empty">暂无操作记录</div>
        </div>
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-glass chart-full">
        <v-chart :option="deptOption" autoresize style="height: 320px;" />
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-glass">
        <v-chart :option="ageOption" autoresize style="height: 320px;" />
      </div>
      <div class="chart-glass">
        <v-chart :option="courseAvgOption" autoresize style="height: 320px;" />
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-glass chart-full">
        <v-chart :option="scheduleOption" autoresize style="height: 360px;" />
      </div>
    </div>
  </div>
</template>
<style scoped>
.dashboard {
  width: 100%;
  max-width: 1200px;
  padding: 8px;
}
.dashboard-title {
  text-align: left;
  margin-bottom: 20px;
  padding: 16px 20px;
  background: #ffffff;
  border: 1px solid var(--glass-border);
  border-radius: 8px;
}
.title-text {
  font-family: var(--font-serif);
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: 1px;
}
.title-sub {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 6px;
  letter-spacing: 1px;
}
.stats-cards {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}
.stat-glass {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  background: #ffffff;
  border: 1px solid var(--glass-border);
  border-radius: 8px;
  padding: 20px 12px;
  box-shadow: 0 1px 4px rgba(61, 57, 41, 0.04);
  transition: box-shadow 0.2s ease;
  color: var(--primary);
}
.stat-glass:hover {
  box-shadow: 0 2px 12px rgba(61, 57, 41, 0.07);
}
.stat-primary { color: #d97757; }
.stat-success { color: #6b8e5a; }
.stat-warning { color: #c09a3e; }
.stat-info { color: #9c9687; }
.stat-num {
  font-family: var(--font-serif);
  font-size: 32px;
  font-weight: 600;
  margin-top: 8px;
  color: var(--text-primary);
}
.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 4px;
  font-weight: 500;
}
.charts-row {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}
.chart-glass {
  flex: 1;
  min-width: 0;
  background: #ffffff;
  border: 1px solid var(--glass-border);
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(61, 57, 41, 0.04);
}
.chart-full {
  flex: 1;
}
.log-panel {
  min-width: 260px;
  max-width: 320px;
  display: flex;
  flex-direction: column;
}
.log-title {
  font-family: var(--font-serif);
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
  text-align: center;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--glass-border-light);
}
.log-list {
  flex: 1;
  overflow-y: auto;
  max-height: 260px;
  padding-right: 4px;
}
.log-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 10px;
  margin-bottom: 8px;
  background: var(--bg-gradient-start);
  border-radius: 4px;
  font-size: 12px;
  color: var(--text-secondary);
}
.log-user {
  font-family: var(--font-mono);
  font-weight: 600;
  color: var(--blue-600);
  min-width: 50px;
}
.log-op {
  flex: 1;
  text-align: center;
}
.log-time {
  font-family: var(--font-mono);
  color: var(--text-muted);
  font-size: 11px;
  min-width: 70px;
  text-align: right;
}
.log-empty {
  text-align: center;
  color: var(--text-muted);
  padding: 20px 0;
}
.notice-panel {
  min-width: 0;
}
.notice-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.notice-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 9px 12px;
  background: var(--bg-gradient-start);
  border-radius: 4px;
  font-size: 13px;
}
.notice-title {
  font-weight: 600;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.notice-meta {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--text-muted);
  flex-shrink: 0;
}
</style>
