<script setup>
import { ref, onMounted } from "vue"
import { ElMessage } from 'element-plus'
import {
  Bell,
  Calendar,
  TrendCharts,
  Reading,
  Star,
  Document,
  School,
  Remove,
  ArrowDown
} from '@element-plus/icons-vue'
import { useRouter } from "vue-router";

const router = useRouter()
const username = ref("")
const sid = ref("")

onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    try {
      const user = JSON.parse(userStr)
      username.value = user.username || '同学'
      sid.value = user.uid || ''
    } catch (e) {}
  }
})

const handleCommand = (command) => {
  if (command === "logout") {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    ElMessage.success("退出登录")
    router.push("/login")
  }
}
</script>
<template>
  <div class="common-layout">
    <el-container>
      <el-header class="header-contain">
        <div class="header-left">
          <el-icon :size="22"><School /></el-icon>
          <span class="system-title">高校教务管理系统 · 学生端</span>
        </div>
        <div class="header-right">
          <span class="welcome-text">欢迎，<em class="welcome-user">{{ username }}</em><span class="sid-tag">学号 {{ sid }}</span></span>
          <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link">
              <el-avatar :size="32" class="header-avatar"
                  src="https://cdn.phototourl.com/free/2026-06-17-0d67638e-894a-4202-9234-706e069377f6.jpg"
              />
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout" divided :icon="Remove">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-container class="body-container">
        <el-aside class="sidebar" width="200px">
          <el-menu
              router
              style="height: 100%; border-right: none;"
              default-active="/stu/notice"
              text-color="#6e6a5b"
              active-text-color="#d97757"
          >
            <el-menu-item index="/stu/notice">
              <el-icon><Bell /></el-icon>
              <span>公告通知</span>
            </el-menu-item>
            <el-menu-item index="/stu/schedule">
              <el-icon><Calendar /></el-icon>
              <span>我的课表</span>
            </el-menu-item>
            <el-menu-item index="/stu/score">
              <el-icon><TrendCharts /></el-icon>
              <span>我的成绩</span>
            </el-menu-item>
            <el-menu-item index="/stu/selection">
              <el-icon><Reading /></el-icon>
              <span>自助选课</span>
            </el-menu-item>
            <el-menu-item index="/stu/evaluation">
              <el-icon><Star /></el-icon>
              <span>教学评教</span>
            </el-menu-item>
            <el-menu-item index="/stu/exam">
              <el-icon><Document /></el-icon>
              <span>考试安排</span>
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
  background: var(--bg-gradient-start);
  position: absolute;
  inset: 0;
}
.common-layout .el-container {
  width: 100%;
  height: 100%;
}

.header-contain {
  background: #ffffff;
  border-bottom: 1px solid var(--glass-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 10;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--primary);
}
.system-title {
  font-family: var(--font-serif);
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: 1px;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 14px;
}
.welcome-text {
  font-size: 14px;
  color: var(--text-secondary);
}
.welcome-user {
  font-family: var(--font-mono);
  font-style: normal;
  font-size: 13px;
  color: var(--text-primary);
  background: var(--bg-gradient-end);
  border-radius: 3px;
  padding: 1px 6px;
}
.sid-tag {
  margin-left: 8px;
  font-size: 12px;
  color: var(--blue-600);
  background: var(--blue-50);
  border: 1px solid var(--blue-100);
  border-radius: 3px;
  padding: 1px 6px;
}
.header-avatar {
  cursor: pointer;
}
.el-dropdown-link {
  display: flex;
  align-items: center;
  outline: none;
}

.sidebar {
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: var(--blur-md);
  -webkit-backdrop-filter: var(--blur-md);
  border-right: 1px solid var(--glass-border);
}
.sidebar :deep(.el-menu) {
  background: transparent;
}
.sidebar :deep(.el-menu-item.is-active) {
  background: rgba(217, 119, 87, 0.08);
  border-right: 2px solid var(--primary);
}

.body-container {
  height: calc(100% - 60px);
}
.contain_main {
  padding: 20px;
  overflow: auto;
}
.footer-contain {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 40px !important;
  font-size: 12px;
  color: var(--text-muted);
}
</style>