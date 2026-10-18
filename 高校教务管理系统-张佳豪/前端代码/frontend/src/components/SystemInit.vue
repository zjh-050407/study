<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import request from '@/utils/request.js'

const router = useRouter()
const confirmText = ref('')
const loading = ref(false)

const handleInit = async () => {
  if (confirmText.value !== '确认初始化') {
    ElMessage.warning('请输入"确认初始化"以验证操作')
    return
  }

  try {
    await ElMessageBox.confirm(
      '此操作将清空所有业务数据（院系、班级、学生、教师、课程、排课、成绩）并恢复为初始状态。管理员账号不受影响。是否继续？',
      '危险操作确认',
      {
        confirmButtonText: '确认初始化',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
  } catch {
    return
  }

  loading.value = true
  try {
    const result = await request.post('/init/reset')
    if (result.data.code === 0) {
      ElMessage.success('系统初始化完成，即将刷新页面')
      setTimeout(() => {
        router.push('/home/t1')
        location.reload()
      }, 1500)
    } else {
      ElMessage.error(result.data.message || '初始化失败')
    }
  } catch (e) {
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="contain">
    <el-card class="glass-card" style="max-width: 560px">
      <template #header>
        <div class="card-header">
          <span class="card-title">系统初始化</span>
        </div>
      </template>

      <div class="warning-block">
        <el-icon size="20" color="#e6a23c"><Warning /></el-icon>
        <div class="warning-text">
          <p>此操作将清空所有业务数据并恢复为系统初始状态。</p>
          <p>受影响的数据包括：院系、班级、学生、教师、课程、排课、成绩。</p>
          <p>管理员账号和密码将保留，不受影响。</p>
        </div>
      </div>

      <el-form label-width="120px" style="margin-top: 20px">
        <el-form-item label="验证操作">
          <el-input
            v-model="confirmText"
            placeholder='请输入"确认初始化"'
            class="glass-input"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="danger"
            class="btn-glass-danger"
            :loading="loading"
            @click="handleInit"
          >
            执行初始化
          </el-button>
        </el-form-item>
      </el-form>
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

.warning-block {
  background: rgba(230, 162, 60, 0.08);
  border: 1px solid rgba(230, 162, 60, 0.25);
  border-radius: 12px;
  padding: 16px;
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.warning-text p {
  margin: 0 0 6px 0;
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.6;
}

.warning-text p:last-child {
  margin-bottom: 0;
}

:deep(.glass-input .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.5) !important;
  border: 1px solid var(--glass-border-light) !important;
  border-radius: 10px !important;
  box-shadow: none !important;
}

.btn-glass-danger {
  background: linear-gradient(135deg, #f56c6c, #e64c4c) !important;
  border: none !important;
  border-radius: 10px !important;
  padding: 10px 28px !important;
  font-weight: 600;
  box-shadow: 0 4px 16px rgba(245, 108, 108, 0.3);
  transition: all 0.25s ease;
}
.btn-glass-danger:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(245, 108, 108, 0.4);
}
</style>
