<script setup>
import {
  DataLine,
  Menu as IconMenu,
  User,
  Setting,
  UserFilled,
  Stamp,
  Remove,
  School,
  Reading,
  Notebook,
  Warning,
  Document,
  WarningFilled
} from '@element-plus/icons-vue'
import { ref, onMounted } from "vue"
import { ElMessage } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { useRouter } from "vue-router";

const router = useRouter()
const username = ref("")
const role = ref("")

const roleLabel = { lecturer: '讲师', professor: '教授', admin: '管理员' }

onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    const user = JSON.parse(userStr)
    username.value = user.username || 'admin'
    role.value = user.role || ''
  }
})

const handleCommand = (command) => {
  if (command === "logout") {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    ElMessage.success("退出登陆")
    router.push("/login")
  }
  if (command === "modify") {
    router.push("/home/t3")
  }
}
</script>
<template>
  <div class="common-layout">
    <el-container>
      <el-header class="header-contain">
        <div class="contain-title">欢迎回来，{{ username }} <span v-if="role" class="role-tag">[{{ roleLabel[role] || role }}]</span></div>
        <div class="contain-down">
          <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link">
              <el-avatar :size="36"
                  src="https://cdn.phototourl.com/free/2026-06-17-0d67638e-894a-4202-9234-706e069377f6.jpg"
              />
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="modify" :icon="Stamp">修改密码</el-dropdown-item>
                <el-dropdown-item command="logout" divided :icon="Remove">退出登陆</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-container>
        <el-aside class="sidebar-glass" width="200px">
          <el-menu
              router
              active-text-color="#4a90e2"
              background-color="transparent"
              style="height: 100%; border-right: none;"
              default-active="/home/t1"
              text-color="#3a5a7c"
          >
            <el-menu-item index="/home/t1">
              <el-icon><DataLine /></el-icon>
              <span>仪表盘</span>
            </el-menu-item>
            <el-sub-menu index="1">
              <template #title>
                <el-icon><School /></el-icon>
                <span>教务管理</span>
              </template>
              <el-menu-item index="/home/department" v-if="role === 'professor' || role === 'admin'">院系管理</el-menu-item>
              <el-menu-item index="/home/clazz">班级管理</el-menu-item>
              <el-menu-item index="/home/studentManager">学生管理</el-menu-item>
              <el-menu-item index="/home/t2" v-if="role === 'professor' || role === 'admin'">教师管理</el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="2">
              <template #title>
                <el-icon><Notebook /></el-icon>
                <span>教学管理</span>
              </template>
              <el-menu-item index="/home/course">课程管理</el-menu-item>
              <el-menu-item index="/home/courseArrange">排课管理</el-menu-item>
              <el-menu-item index="/home/score">成绩管理</el-menu-item>
            <el-menu-item index="/home/scoreWarning" style="color: #f56c6c;">
              <el-icon><WarningFilled /></el-icon>
              <span>成绩预警</span>
            </el-menu-item>
          </el-sub-menu>
            <el-menu-item index="/home/systemInit" style="color: #e6a23c;" v-if="role === 'admin'">
              <el-icon><Warning /></el-icon>
              <span>系统初始化</span>
            </el-menu-item>
            <el-menu-item index="/home/registerApproval" style="color: #67c23a;" v-if="role === 'admin'">
              <el-icon><UserFilled /></el-icon>
              <span>注册审批</span>
            </el-menu-item>
            <el-menu-item index="/home/operationLog" v-if="role === 'admin'">
              <el-icon><Document /></el-icon>
              <span>操作日志</span>
            </el-menu-item>
            <el-menu-item index="/home/t3">
              <el-icon><Stamp /></el-icon>
              <span>修改密码</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-container>
          <el-main class="contain_main">
            <router-view></router-view>
          </el-main>
          <el-footer class="footer-contain">
            Copyright © 2025 GGS. 保留所有权利。
          </el-footer>
        </el-container>
      </el-container>
    </el-container>
  </div>
</template>
<style scoped>
.common-layout {
  background: linear-gradient(135deg, var(--bg-gradient-start) 0%, var(--bg-gradient-end) 50%, #f5faff 100%);
  position: absolute;
  inset: 0;
}
.common-layout .el-container {
  width: 100%;
  height: 100%;
}

/* 玻璃头部 */
.header-contain {
  background: var(--glass-bg-strong);
  backdrop-filter: var(--blur-md);
  -webkit-backdrop-filter: var(--blur-md);
  border-bottom: 1px solid var(--glass-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: var(--glass-shadow-sm);
  z-index: 10;
}
.contain-title {
  margin-left: 200px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: 0.5px;
}
.contain-down {
  display: flex;
  align-items: center;
  margin-right: 40px;
}
.el-dropdown-link {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 4px 10px;
  border-radius: 20px;
  transition: background 0.2s;
}
.el-dropdown-link:hover {
  background: rgba(140, 190, 255, 0.15);
}

/* 玻璃侧边栏 */
.sidebar-glass {
  background: var(--sidebar-bg);
  backdrop-filter: var(--blur-md);
  -webkit-backdrop-filter: var(--blur-md);
  border-right: 1px solid var(--glass-border);
}

:deep(.el-menu-item:hover) {
  background: rgba(140, 190, 255, 0.18) !important;
}
:deep(.el-sub-menu__title:hover) {
  background: rgba(140, 190, 255, 0.18) !important;
}
:deep(.el-menu-item.is-active) {
  background: rgba(74, 144, 226, 0.1) !important;
  font-weight: 600;
}

/* 主内容区 */
.contain_main {
  background: transparent;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 20px;
}

/* 玻璃底部 */
.footer-contain {
  background: var(--glass-bg);
  backdrop-filter: var(--blur-sm);
  -webkit-backdrop-filter: var(--blur-sm);
  border-top: 1px solid var(--glass-border-light);
  display: flex;
  justify-content: center;
  align-items: center;
  color: var(--text-muted);
  font-size: 12px;
  height: 36px !important;
}
</style>
