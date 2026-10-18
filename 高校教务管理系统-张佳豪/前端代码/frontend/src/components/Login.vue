<template>
  <div class="contain">
    <div class="login-glass">
      <el-form
          ref="ruleFormRef"
          style="max-width: 400px;"
          :model="ruleForm"
          status-icon
          label-width="auto"
          class="demo-ruleForm"
          :rules="rules"
      >
        <div class="login-title-wrap">
          <h3 class="login-title">用户登陆</h3>
        </div>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="ruleForm.username" placeholder="请输入用户名" class="glass-input"/>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="ruleForm.password" type="password" placeholder="请输入密码" autocomplete="off" class="glass-input"/>
        </el-form-item>
        <el-form-item>
          <div class="contain-button">
            <el-button type="primary" class="btn-glass" @click="login">登陆</el-button>
            <el-button class="btn-glass-outline" @click="openRegister">注册</el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>

    <el-dialog v-model="registerVisible" title="用户注册" width="420px" class="glass-dialog" :close-on-click-modal="false">
      <el-form :model="registerForm" :rules="registerRules" ref="registerRef" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名" class="glass-input-sm"/>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" autocomplete="off" class="glass-input-sm"/>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" autocomplete="off" class="glass-input-sm"/>
        </el-form-item>
        <el-form-item label="申请角色" prop="role">
          <el-select v-model="registerForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="讲师" value="lecturer" />
            <el-option label="教授" value="professor" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="registerVisible = false" class="btn-glass-outline">取消</el-button>
          <el-button type="primary" @click="doRegister" class="btn-glass-primary">提交申请</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue'
import type { FormInstance } from 'element-plus'
import { ElMessage } from "element-plus";
import { useRouter } from "vue-router"
import request from "@/utils/request.js";

const ruleFormRef = ref<FormInstance>()
const ruleForm = ref({
  username: '',
  password: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 3, message: '密码至少3位', trigger: 'blur' }],
}

const router = useRouter()

const login = async () => {
  if (!ruleFormRef.value) return
  await ruleFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      const result = await request.get("/login", { params: ruleForm.value })
      if (result.data.code === 0) {
        const data = result.data.data
        localStorage.setItem('token', data.token)
        localStorage.setItem('user', JSON.stringify(data))
        router.push("/home")
        ElMessage.success("登陆成功")
      } else {
        ElMessage.error(result.data.message || "登陆失败")
      }
    } catch (e) {
      ElMessage.error("网络错误")
    }
  })
}

const registerVisible = ref(false)
const registerRef = ref<FormInstance>()
const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: '',
  role: '',
})

const registerRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 3, message: '密码至少3位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: Function) => {
        if (value !== registerForm.value.password) {
          callback(new Error('两次密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  role: [{ required: true, message: '请选择申请角色', trigger: 'change' }],
}

const openRegister = () => {
  registerForm.value = { username: '', password: '', confirmPassword: '', role: '' }
  registerVisible.value = true
}

const doRegister = async () => {
  if (!registerRef.value) return
  await registerRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      const result = await request.post("/register", null, {
        params: {
          username: registerForm.value.username,
          password: registerForm.value.password,
          role: registerForm.value.role,
        }
      })
      if (result.data.code === 0) {
        registerVisible.value = false
        ElMessage.success("注册申请已提交，请等待管理员审批")
      } else {
        ElMessage.error(result.data.message || "注册失败")
      }
    } catch (e) {
      ElMessage.error("网络错误")
    }
  })
}
</script>
<style scoped>
.contain {
  position: absolute;
  inset: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  background: url('@/assets/background.png') center/cover no-repeat;
  perspective: 1200px;
  overflow: hidden;
}

.contain::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse at center, rgba(220, 245, 255, 0.15) 0%, rgba(180, 220, 255, 0.35) 100%);
  pointer-events: none;
}

/* 3D 淡蓝色玻璃登录卡片 */
.login-glass {
  position: relative;
  z-index: 1;
  background: linear-gradient(
    135deg,
    rgba(210, 247, 255, 0.55) 0%,
    rgba(185, 235, 255, 0.45) 50%,
    rgba(165, 225, 255, 0.4) 100%
  );
  backdrop-filter: blur(22px) saturate(150%);
  -webkit-backdrop-filter: blur(22px) saturate(150%);
  border: 1px solid rgba(255, 255, 255, 0.55);
  border-radius: 24px;
  width: 85%;
  max-width: 460px;
  padding: 40px 36px;
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.7),
    inset 0 -1px 0 rgba(120, 190, 255, 0.25),
    0 24px 48px rgba(31, 110, 200, 0.18),
    0 12px 24px rgba(31, 110, 200, 0.12),
    0 0 0 1px rgba(255, 255, 255, 0.25);
  transform: rotateX(6deg) rotateY(-4deg) translateZ(20px);
  transform-style: preserve-3d;
  transition: transform 0.4s cubic-bezier(0.23, 1, 0.32, 1), box-shadow 0.4s ease;
}

.login-glass::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 24px;
  background: linear-gradient(
    125deg,
    rgba(255, 255, 255, 0.55) 0%,
    rgba(255, 255, 255, 0.05) 45%,
    rgba(255, 255, 255, 0) 100%
  );
  pointer-events: none;
  z-index: -1;
}

.login-glass:hover {
  transform: rotateX(4deg) rotateY(-2deg) translateZ(32px) scale(1.01);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.75),
    inset 0 -1px 0 rgba(120, 190, 255, 0.3),
    0 32px 64px rgba(31, 110, 200, 0.22),
    0 16px 32px rgba(31, 110, 200, 0.16),
    0 0 0 1px rgba(255, 255, 255, 0.3);
}

.login-title-wrap {
  text-align: center;
  margin-bottom: 20px;
  width: 100%;
}
.login-title {
  display: inline-block;
  color: var(--text-primary);
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 2px;
}

/* 玻璃按钮 */
:deep(.btn-glass) {
  background: linear-gradient(135deg, var(--blue-400), var(--blue-500)) !important;
  border: none !important;
  border-radius: 10px !important;
  padding: 10px 32px !important;
  font-weight: 600;
  box-shadow: 0 4px 16px rgba(74, 144, 226, 0.3);
  transition: all 0.25s ease;
}
:deep(.btn-glass:hover) {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(74, 144, 226, 0.4);
}

:deep(.btn-glass-outline) {
  background: rgba(255, 255, 255, 0.4) !important;
  border: 1px solid var(--glass-border) !important;
  border-radius: 10px !important;
  padding: 10px 32px !important;
  color: var(--text-secondary);
  transition: all 0.25s ease;
}
:deep(.btn-glass-outline:hover) {
  background: rgba(255, 255, 255, 0.6) !important;
  border-color: var(--blue-300) !important;
  color: var(--primary);
}

:deep(.btn-glass-primary) {
  background: linear-gradient(135deg, var(--blue-400), var(--blue-500)) !important;
  border: none !important;
  border-radius: 10px !important;
  padding: 10px 24px !important;
  font-weight: 600;
  box-shadow: 0 4px 16px rgba(74, 144, 226, 0.3);
}

.contain-button {
  width: 100%;
  display: flex;
  justify-content: center;
  gap: 20px;
  padding-top: 8px;
}

/* 输入框玻璃化 */
:deep(.glass-input .el-input__wrapper),
:deep(.glass-input-sm .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.5) !important;
  border: 1px solid var(--glass-border-light) !important;
  border-radius: 10px !important;
  box-shadow: none !important;
  transition: all 0.2s;
}
:deep(.glass-input .el-input__wrapper:hover),
:deep(.glass-input-sm .el-input__wrapper:hover) {
  border-color: var(--blue-300) !important;
}
:deep(.glass-input .el-input__wrapper.is-focus),
:deep(.glass-input-sm .el-input__wrapper.is-focus) {
  border-color: var(--primary) !important;
  box-shadow: 0 0 0 3px rgba(74, 144, 226, 0.12) !important;
}

:deep(.el-form-item__label) {
  color: var(--text-secondary);
  font-weight: 500;
}

/* 注册对话框 */
:deep(.glass-dialog .el-dialog) {
  background: var(--glass-bg-strong);
  backdrop-filter: var(--blur-lg);
  -webkit-backdrop-filter: var(--blur-lg);
  border: 1px solid var(--glass-border);
  border-radius: 16px;
  box-shadow: var(--glass-shadow);
}
:deep(.glass-dialog .el-dialog__header) {
  border-bottom: 1px solid var(--glass-border-light);
  padding: 20px 24px;
}
:deep(.glass-dialog .el-dialog__title) {
  color: var(--text-primary);
  font-weight: 600;
}
:deep(.glass-dialog .el-dialog__body) {
  color: var(--text-secondary);
  padding: 24px;
}
:deep(.glass-dialog .el-dialog__footer) {
  border-top: 1px solid var(--glass-border-light);
  padding: 16px 24px;
}
</style>
