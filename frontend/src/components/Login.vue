<template>
  <div class="contain">
    <div class="login-panel">
      <div class="login-header">
        <div class="login-school">高校教务管理系统</div>
        <div class="login-sub">Academic Affairs Management System</div>
      </div>
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
          <h3 class="login-title">用户登录</h3>
        </div>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="ruleForm.username" placeholder="请输入用户名"/>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="ruleForm.password" type="password" placeholder="请输入密码" autocomplete="off"/>
        </el-form-item>
        <el-form-item>
          <div class="contain-button">
            <el-button type="primary" @click="login">登录</el-button>
            <el-button @click="openRegister">注册</el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>

    <el-dialog v-model="registerVisible" title="用户注册" width="420px" :close-on-click-modal="false">
      <el-form :model="registerForm" :rules="registerRules" ref="registerRef" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名"/>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" autocomplete="off"/>
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
          <el-button @click="registerVisible = false">取消</el-button>
          <el-button type="primary" @click="doRegister">提交申请</el-button>
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
        ElMessage.success("登录成功")
      } else {
        ElMessage.error(result.data.message || "登录失败")
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
    { min: 6, message: '密码长度至少 6 位', trigger: 'blur' }
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
  if (!registerForm.value.password || registerForm.value.password.length < 6) {
    ElMessage.warning('密码长度至少 6 位')
    return
  }
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
  background: linear-gradient(180deg, #f5f4ee 0%, #faf9f5 100%);
  overflow: hidden;
}

/* 卡片后方的柔和暖色光晕，模拟 Claude 官网的克制氛围 */
.contain::before {
  content: '';
  position: absolute;
  top: 12%;
  left: 50%;
  transform: translateX(-50%);
  width: 720px;
  height: 480px;
  background: radial-gradient(ellipse at center, rgba(217, 119, 87, 0.08) 0%, rgba(217, 119, 87, 0) 70%);
  pointer-events: none;
}

/* 白色登录面板 */
.login-panel {
  position: relative;
  z-index: 1;
  background: #ffffff;
  border: 1px solid var(--glass-border);
  border-radius: 8px;
  width: 420px;
  max-width: 88%;
  padding: 0 36px 32px;
  box-shadow: 0 4px 20px rgba(61, 57, 41, 0.06);
}

.login-header {
  text-align: center;
  padding: 28px 0 20px;
  margin: 0 -36px;
  border-bottom: 1px solid var(--glass-border-light);
  margin-bottom: 24px;
}
.login-school {
  font-family: var(--font-serif);
  font-size: 23px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: 3px;
}
/* 标题下的陶土橙强调线 */
.login-school::after {
  content: '';
  display: block;
  width: 34px;
  height: 2px;
  margin: 12px auto 0;
  background: var(--primary);
}
.login-sub {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 10px;
  letter-spacing: 2px;
  text-transform: uppercase;
}

.login-title-wrap {
  text-align: center;
  margin-bottom: 20px;
  width: 100%;
}
.login-title {
  display: inline-block;
  font-family: var(--font-serif);
  color: var(--text-primary);
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 1px;
}

.contain-button {
  width: 100%;
  display: flex;
  justify-content: center;
  gap: 16px;
  padding-top: 8px;
}

.contain-button .el-button {
  min-width: 110px;
}

:deep(.el-form-item__label) {
  color: var(--text-secondary);
  font-weight: 500;
}
</style>
