<script setup>
import request from "@/utils/request.js";
import { ref, onMounted } from "vue";
import VChart from "vue-echarts";
import { use } from "echarts/core";
import { CanvasRenderer } from "echarts/renderers";
import { PieChart, BarChart, HeatmapChart } from "echarts/charts";
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent, VisualMapComponent } from "echarts/components";
import { ElMessage } from 'element-plus'

use([CanvasRenderer, PieChart, BarChart, HeatmapChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent, VisualMapComponent]);

const loading = ref(true)
const kpi = ref({ studentCount: 0, teacherCount: 0, courseCount: 0, clazzCount: 0 })
const recentLogs = ref([])

const sexOption = ref({
  title: { text: '学生性别分布', left: 'center', textStyle: { color: '#1a3a5c', fontSize: 16, fontWeight: 600 } },
  tooltip: { trigger: 'item', backgroundColor: 'rgba(255,255,255,0.9)', borderColor: 'rgba(140,190,255,0.3)', textStyle: { color: '#1a3a5c' } },
  legend: { orient: 'vertical', left: 'left', textStyle: { color: '#5a7a9c' } },
  series: [{
    type: 'pie',
    radius: ['40%', '65%'],
    itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
    data: [],
    emphasis: { itemStyle: { shadowBlur: 15, shadowOffsetX: 0, shadowColor: 'rgba(74,144,226,0.3)' } },
    color: ['#4a90e2', '#f56c6c', '#93c5fd']
  }]
});

const deptOption = ref({
  title: { text: '各院系学生人数', left: 'center', textStyle: { color: '#1a3a5c', fontSize: 16, fontWeight: 600 } },
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, backgroundColor: 'rgba(255,255,255,0.9)', borderColor: 'rgba(140,190,255,0.3)', textStyle: { color: '#1a3a5c' } },
  xAxis: { type: 'category', data: [], axisLabel: { color: '#5a7a9c' }, axisLine: { lineStyle: { color: 'rgba(140,190,255,0.3)' } } },
  yAxis: { type: 'value', axisLabel: { color: '#5a7a9c' }, splitLine: { lineStyle: { color: 'rgba(140,190,255,0.15)' } } },
  series: [{ type: 'bar', data: [], itemStyle: { color: '#4a90e2', borderRadius: [6, 6, 0, 0] }, barWidth: '50%' }]
});

const ageOption = ref({
  title: { text: '学生年龄分布', left: 'center', textStyle: { color: '#1a3a5c', fontSize: 16, fontWeight: 600 } },
  tooltip: { trigger: 'axis', backgroundColor: 'rgba(255,255,255,0.9)', borderColor: 'rgba(140,190,255,0.3)', textStyle: { color: '#1a3a5c' } },
  xAxis: { type: 'category', data: [], axisLabel: { color: '#5a7a9c' }, axisLine: { lineStyle: { color: 'rgba(140,190,255,0.3)' } } },
  yAxis: { type: 'value', axisLabel: { color: '#5a7a9c' }, splitLine: { lineStyle: { color: 'rgba(140,190,255,0.15)' } } },
  series: [{ type: 'bar', data: [], itemStyle: { color: '#6da8f5', borderRadius: [6, 6, 0, 0] }, barWidth: '50%' }]
});

const courseAvgOption = ref({
  title: { text: '课程平均分 TOP5', left: 'center', textStyle: { color: '#1a3a5c', fontSize: 16, fontWeight: 600 } },
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, backgroundColor: 'rgba(255,255,255,0.9)', borderColor: 'rgba(140,190,255,0.3)', textStyle: { color: '#1a3a5c' } },
  xAxis: { type: 'category', data: [], axisLabel: { color: '#5a7a9c', rotate: 20 }, axisLine: { lineStyle: { color: 'rgba(140,190,255,0.3)' } } },
  yAxis: { type: 'value', min: 0, max: 100, axisLabel: { color: '#5a7a9c' }, splitLine: { lineStyle: { color: 'rgba(140,190,255,0.15)' } } },
  series: [{ type: 'bar', data: [], itemStyle: { color: '#93c5fd', borderRadius: [6, 6, 0, 0] }, barWidth: '50%' }]
});

const gradeOption = ref({
  title: { text: '成绩等级分布', left: 'center', textStyle: { color: '#1a3a5c', fontSize: 16, fontWeight: 600 } },
  tooltip: { trigger: 'item', backgroundColor: 'rgba(255,255,255,0.9)', borderColor: 'rgba(140,190,255,0.3)', textStyle: { color: '#1a3a5c' } },
  legend: { orient: 'vertical', left: 'left', textStyle: { color: '#5a7a9c' } },
  series: [{
    type: 'pie',
    radius: ['40%', '65%'],
    itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
    data: [],
    emphasis: { itemStyle: { shadowBlur: 15, shadowOffsetX: 0, shadowColor: 'rgba(74,144,226,0.3)' } },
    color: ['#4a90e2', '#6da8f5', '#93c5fd', '#f56c6c']
  }]
});

const scheduleOption = ref({
  title: { text: '排课时间热力图', left: 'center', textStyle: { color: '#1a3a5c', fontSize: 16, fontWeight: 600 } },
  tooltip: { position: 'top', backgroundColor: 'rgba(255,255,255,0.9)', borderColor: 'rgba(140,190,255,0.3)', textStyle: { color: '#1a3a5c' } },
  grid: { height: '60%', top: '20%' },
  xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五'], splitArea: { show: true } },
  yAxis: { type: 'category', data: ['第1节', '第2节', '第3节', '第4节'], splitArea: { show: true } },
  visualMap: { min: 0, max: 4, calculable: true, orient: 'horizontal', left: 'center', bottom: '5%', inRange: { color: ['#e0f2fe', '#4a90e2'] } },
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
});
</script>
<template>
  <div class="dashboard" v-loading="loading">
    <div class="dashboard-title">
      <div class="title-text">教务数据驾驶舱</div>
      <div class="title-sub">实时统计 · 智能分析 · 可视化展示</div>
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
  text-align: center;
  margin-bottom: 24px;
}
.title-text {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: 1px;
}
.title-sub {
  font-size: 13px;
  color: var(--text-muted);
  margin-top: 6px;
}
.stats-cards {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}
.stat-glass {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  background: var(--glass-bg-strong);
  backdrop-filter: var(--blur-md);
  -webkit-backdrop-filter: var(--blur-md);
  border: 1px solid var(--glass-border);
  border-radius: 16px;
  padding: 20px 12px;
  box-shadow: var(--glass-shadow);
  transition: transform 0.25s ease;
  color: var(--primary);
}
.stat-glass:hover {
  transform: translateY(-3px);
}
.stat-primary { color: #4a90e2; }
.stat-success { color: #67c23a; }
.stat-warning { color: #e6a23c; }
.stat-info { color: #909399; }
.stat-num {
  font-size: 36px;
  font-weight: 700;
  margin-top: 8px;
  color: var(--text-primary);
}
.stat-label {
  font-size: 13px;
  color: var(--text-muted);
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
  background: var(--glass-bg-strong);
  backdrop-filter: var(--blur-md);
  -webkit-backdrop-filter: var(--blur-md);
  border: 1px solid var(--glass-border);
  border-radius: 16px;
  padding: 16px;
  box-shadow: var(--glass-shadow);
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
  font-size: 16px;
  font-weight: 600;
  color: #1a3a5c;
  margin-bottom: 12px;
  text-align: center;
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
  background: rgba(255, 255, 255, 0.4);
  border-radius: 8px;
  font-size: 12px;
  color: var(--text-secondary);
}
.log-user {
  font-weight: 600;
  color: var(--primary);
  min-width: 50px;
}
.log-op {
  flex: 1;
  text-align: center;
}
.log-time {
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
</style>
