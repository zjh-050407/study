<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Close } from '@element-plus/icons-vue'
import request from '@/utils/request.js'

const tableData = ref([])
const loading = ref(false)

const fetchRequests = async () => {
  loading.value = true
  try {
    const result = await request.get('/register/requests')
    if (result.data.code === 0) {
      tableData.value = result.data.data
    }
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要通过用户「${row.username}」的注册申请吗？`, '审批确认', {
      confirmButtonText: '同意',
      cancelButtonText: '取消',
      type: 'success',
    })
  } catch {
    return
  }
  try {
    const result = await request.post('/register/approve', null, { params: { id: row.id } })
    if (result.data.code === 0) {
      ElMessage.success(`已通过 ${row.username} 的申请`)
      fetchRequests()
    } else {
      ElMessage.error(result.data.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('网络错误')
  }
}

const handleReject = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要拒绝用户「${row.username}」的注册申请吗？`, '审批确认', {
      confirmButtonText: '拒绝',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    const result = await request.post('/register/reject', null, { params: { id: row.id } })
    if (result.data.code === 0) {
      ElMessage.success(`已拒绝 ${row.username} 的申请`)
      fetchRequests()
    } else {
      ElMessage.error(result.data.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('网络错误')
  }
}

const roleLabel = { lecturer: '讲师', professor: '教授' }

onMounted(() => {
  fetchRequests()
})
</script>

<template>
  <div class="contain">
    <el-card class="glass-card" style="max-width: 750px">
      <template #header>
        <div class="card-header">
          <span class="card-title">注册审批</span>
          <el-button :icon="RefreshRight" round @click="fetchRequests" class="btn-glass-primary" :loading="loading">刷新</el-button>
        </div>
      </template>
      <el-table :data="tableData" style="width: 100%" class="glass-table" v-loading="loading" empty-text="暂无待审批的注册申请">
        <el-table-column prop="username" label="用户名" width="180" />
        <el-table-column label="申请角色" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.role === 'professor' ? 'warning' : 'info'" size="small">
              {{ roleLabel[scope.row.role] || scope.row.role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="200" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" type="success" class="btn-glass-approve" :icon="Check" @click="handleApprove(scope.row)">同意</el-button>
            <el-button size="small" type="danger" class="btn-glass-reject" :icon="Close" @click="handleReject(scope.row)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.contain {
  width: 100%;
  display: flex;
  justify-content: center;
  padding-top: 40px;
}

.btn-glass-primary {
  background: linear-gradient(135deg, var(--blue-400), var(--blue-500)) !important;
  border: none !important;
  border-radius: 10px !important;
  padding: 8px 20px !important;
  font-weight: 600;
  box-shadow: 0 4px 16px rgba(74, 144, 226, 0.3);
  transition: all 0.25s ease;
}
.btn-glass-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(74, 144, 226, 0.4);
}

.btn-glass-approve {
  background: linear-gradient(135deg, #67c23a, #5daf34) !important;
  border: none !important;
  border-radius: 8px !important;
  font-weight: 500;
  box-shadow: 0 3px 12px rgba(103, 194, 58, 0.3);
  transition: all 0.2s;
}
.btn-glass-approve:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(103, 194, 58, 0.4);
}

.btn-glass-reject {
  background: linear-gradient(135deg, #f56c6c, #e64c4c) !important;
  border: none !important;
  border-radius: 8px !important;
  font-weight: 500;
  box-shadow: 0 3px 12px rgba(245, 108, 108, 0.3);
  transition: all 0.2s;
}
.btn-glass-reject:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(245, 108, 108, 0.4);
}
</style>