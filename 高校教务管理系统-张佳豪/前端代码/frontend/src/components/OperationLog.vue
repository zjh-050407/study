<script setup>
import request from "@/utils/request.js";
import { ref, onMounted, computed } from "vue";
import { ElMessage } from 'element-plus'
import { Search, Delete } from "@element-plus/icons-vue";

const tableData = ref([])
const allData = ref([])
const loading = ref(false)

const getData = async () => {
  loading.value = true
  try {
    const result = await request.get('/operationLog/list')
    if (result.data.code === 0) {
      allData.value = result.data.data || []
      tableData.value = allData.value
    }
  } catch (err) {
    ElMessage.error('获取操作日志失败')
  } finally {
    loading.value = false
  }
}

onMounted(getData)

const searchKeyword = ref('')

const doSearch = () => {
  const kw = searchKeyword.value.trim()
  if (!kw) {
    tableData.value = allData.value
    return
  }
  tableData.value = allData.value.filter(item =>
    (item.username && item.username.includes(kw)) ||
    (item.operation && item.operation.includes(kw)) ||
    (item.method && item.method.includes(kw)) ||
    (item.ip && item.ip.includes(kw))
  )
}

const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ')
}

const isLongParams = (params) => params && params.length > 60

const clearAll = async () => {
  ElMessage.warning('日志清空功能未启用')
}
</script>
<template>
  <div class="contain">
    <el-card class="glass-card" style="max-width: 1100px; width: 100%">
      <template #header>
        <div class="card-header">
          <div class="card-title">
            <span>操作日志审计</span>
            <span class="sub-title">仅管理员可见</span>
          </div>
          <div class="contain-search">
            <el-input v-model="searchKeyword" style="width: 240px" placeholder="搜索用户/操作/方法/IP" clearable class="glass-input-sm"/>
            <el-button :icon="Search" @click="doSearch" circle class="btn-glass-icon"/>
            <el-button :icon="Delete" @click="clearAll" circle class="btn-glass-icon" title="清空日志" />
          </div>
        </div>
      </template>
      <el-table :data="tableData" style="width: 100%" class="glass-table" v-loading="loading" :scroll-x="false">
        <el-table-column prop="id" label="编号" width="70" />
        <el-table-column prop="username" label="操作用户" width="120" />
        <el-table-column prop="operation" label="操作描述" width="120" />
        <el-table-column prop="method" label="目标方法" width="260" show-overflow-tooltip />
        <el-table-column prop="params" label="请求参数" min-width="200" show-overflow-tooltip>
          <template #default="scope">
            <el-tooltip v-if="isLongParams(scope.row.params)" :content="scope.row.params" placement="top" effect="light">
              <span>{{ scope.row.params.substring(0, 60) + '...' }}</span>
            </el-tooltip>
            <span v-else>{{ scope.row.params || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP 地址" width="140" />
        <el-table-column prop="createTime" label="操作时间" width="170">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
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
.sub-title {
  font-size: 12px;
  font-weight: 400;
  color: var(--text-muted);
  background: rgba(103, 194, 58, 0.1);
  padding: 2px 8px;
  border-radius: 10px;
}
.contain-search {
  display: flex;
  align-items: center;
  gap: 10px;
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
:deep(.glass-card .el-card__body) {
  background: transparent;
}

:deep(.btn-glass-icon) {
  background: rgba(255, 255, 255, 0.5) !important;
  border: 1px solid var(--glass-border-light) !important;
  color: var(--primary) !important;
  transition: all 0.2s;
}
:deep(.btn-glass-icon:hover) {
  background: rgba(255, 255, 255, 0.7) !important;
  border-color: var(--blue-300) !important;
}
:deep(.glass-input-sm .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.5) !important;
  border: 1px solid var(--glass-border-light) !important;
  border-radius: 8px !important;
  box-shadow: none !important;
  transition: all 0.2s;
}
:deep(.glass-input-sm .el-input__wrapper:hover) {
  border-color: var(--blue-300) !important;
}
:deep(.glass-input-sm .el-input__wrapper.is-focus) {
  border-color: var(--primary) !important;
  box-shadow: 0 0 0 3px rgba(74, 144, 226, 0.1) !important;
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
:deep(.glass-table td.el-table__cell) {
  color: var(--text-secondary);
}
</style>
