<script setup>
import request from "@/utils/request.js";
import { ref, reactive } from "vue";
import { ElMessage } from 'element-plus'

const user = JSON.parse(localStorage.getItem('user') || '{}')

const form = reactive({
  uid: user.uid || '',
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const rules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 3, message: '密码至少3位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.newPassword) {
          callback(new Error('两次密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
}

const formRef = ref(null)

const submit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    const result = await request.put("/password", null, {
      params: {
        uid: form.uid,
        oldPassword: form.oldPassword,
        newPassword: form.newPassword,
      }
    })
    if (result.data.code === 0) {
      ElMessage.success("密码修改成功，请重新登录")
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      setTimeout(() => {
        window.location.href = '/#/login'
      }, 1500)
    } else {
      ElMessage.error(result.data.message || "修改失败")
    }
  })
}
</script>
<template>
  <div class="contain">
    <el-card class="glass-card" style="width: 450px">
      <template #header>
        <span class="card-title">修改密码</span>
      </template>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="form.oldPassword" type="password" class="glass-input-sm"/>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="form.newPassword" type="password" class="glass-input-sm"/>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" class="glass-input-sm"/>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submit" class="btn-glass-primary">确认修改</el-button>
          <el-button @click="form = { uid: user.uid, oldPassword: '', newPassword: '', confirmPassword: '' }" class="btn-glass-outline">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>
<style scoped>
.contain {
  display: flex;
  justify-content: center;
  padding: 30px;
  width: 100%;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
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
:deep(.btn-glass-primary) {
  background: linear-gradient(135deg, var(--blue-400), var(--blue-500)) !important;
  border: none !important;
  border-radius: 10px !important;
  box-shadow: 0 4px 14px rgba(74, 144, 226, 0.25);
  transition: all 0.25s ease;
}
:deep(.btn-glass-primary:hover) {
  transform: translateY(-1px);
  box-shadow: 0 6px 18px rgba(74, 144, 226, 0.35);
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
:deep(.el-form-item__label) {
  color: var(--text-secondary);
  font-weight: 500;
}
</style>
