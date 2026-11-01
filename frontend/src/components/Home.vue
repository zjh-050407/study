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
  WarningFilled,
  Bell,
  Star,
  Timer,
  Finished,
  Collection,
  Switch
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
    ElMessage.success("退出登录")
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
        <div class="header-left">
          <el-icon :size="22"><School /></el-icon>
          <span class="system-title">高校教务管理系统</span>
        </div>
        <div class="header-right">
          <span class="welcome-text">欢迎，<em class="welcome-user">{{ username }}</em><span v-if="role" class="role-tag">{{ roleLabel[role] || role }}</span></span>
          <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link">
              <el-avatar :size="32" class="header-avatar"
                  src="https://cdn.phototourl.com/free/2026-06-17-0d67638e-894a-4202-9234-706e069377f6.jpg"
              />
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="modify" :icon="Stamp">修改密码</el-dropdown-item>
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
              default-active="/home/t1"
              text-color="#6e6a5b"
              active-text-color="#d97757"
          >
            <el-menu-item index="/home/t1">
              <el-icon><DataLine /></el-icon>
              <span>数据统计</span>
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
              <el-menu-item index="/home/semester" v-if="role === 'professor' || role === 'admin'">学期管理</el-menu-item>
              <el-menu-item index="/home/courseArrange">排课管理</el-menu-item>
              <el-menu-item index="/home/courseSelection">
                <el-icon><Reading /></el-icon>
                <span>选课管理</span>
              </el-menu-item>
              <el-menu-item index="/home/score">成绩管理</el-menu-item>
              <el-menu-item index="/home/scoreWarning">
                <el-icon><WarningFilled /></el-icon>
                <span>成绩预警</span>
              </el-menu-item>
              <el-menu-item index="/home/evaluation">
                <el-icon><Star /></el-icon>
                <span>教学评价</span>
              </el-menu-item>
              <el-menu-item index="/home/examArrange" v-if="role === 'professor' || role === 'admin'">
                <el-icon><Timer /></el-icon>
                <span>考务排考</span>
              </el-menu-item>
              <el-menu-item index="/home/graduateAudit" v-if="role === 'professor' || role === 'admin'">
                <el-icon><Finished /></el-icon>
                <span>毕业审核</span>
              </el-menu-item>
              <el-menu-item index="/home/trainingPlan" v-if="role === 'professor' || role === 'admin'">
                <el-icon><Collection /></el-icon>
                <span>培养方案</span>
              </el-menu-item>
              <el-menu-item index="/home/statusChange" v-if="role === 'professor' || role === 'admin'">
                <el-icon><Switch /></el-icon>
                <span>学籍异动</span>
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item index="/home/systemInit" v-if="role === 'admin'">
              <el-icon><Warning /></el-icon>
              <span>系统初始化</span>
            </el-menu-item>
            <el-menu-item index="/home/registerApproval" v-if="role === 'admin'">
              <el-icon><UserFilled /></el-icon>
              <span>注册审批</span>
            </el-menu-item>
            <el-menu-item index="/home/operationLog" v-if="role === 'admin'">
              <el-icon><Document /></el-icon>
              <span>操作日志</span>
            </el-menu-item>
            <el-menu-item index="/home/notice">
              <el-icon><Bell /></el-icon>
              <span>公告管理</span>
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
  background: var(--bg-gradient-start);
  position: absolute;
  inset: 0;
}
.common-layout .el-container {
  width: 100%;
  height: 100%;
}

/* 暖白头部 */
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
.role-tag {
  margin-left: 8px;
  font-size: 12px;
  color: var(--blue-600);
  background: var(--blue-50);
  border: 1px solid var(--blue-100);
  border-radius: 3px;
  padding: 1px 8px;
}
.el-dropdown-link {
  display: flex;
  align-items: center;
  gap: 2px;
  cursor: pointer;
}
.header-avatar {
  background: var(--blue-100);
}

/* 暖白侧边栏 */
.sidebar {
  background: #ffffff;
  border-right: 1px solid var(--glass-border);
}

:deep(.el-menu) {
  background: #ffffff;
}
:deep(.el-sub-menu__title:hover) {
  background: var(--bg-gradient-start);
}
:deep(.el-menu-item:hover) {
  background: var(--bg-gradient-start);
}
:deep(.el-menu-item.is-active) {
  background: var(--blue-50);
  border-right: 2px solid var(--primary);
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

/* 底部 */
.footer-contain {
  font-family: var(--font-mono);
  font-size: 11px;
  background: #ffffff;
  border-top: 1px solid var(--glass-border-light);
  display: flex;
  justify-content: center;
  align-items: center;
  color: var(--text-muted);
  height: 36px !important;
}
</style>
