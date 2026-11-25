<script setup>
import { ref, onMounted, computed } from "vue";
import request from "@/utils/request.js";
import { ElMessage } from 'element-plus'
import { useRouter } from "vue-router";
import { Bell, Stamp, Document } from '@element-plus/icons-vue'

const router = useRouter()

const user = JSON.parse(localStorage.getItem('user') || '{}')
const role = computed(() => user.role || '')

const activeTab = ref('message')
const messages = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const notices = ref([])
const todo = ref({})

const loadTodo = async () => {
  try {
    const result = await request.get('/message/unreadCount')
    if (result.data.code === 0) {
      todo.value = result.data.data.todo || {}
    }
  } catch (e) {}
}

const loadMessages = async () => {
  loading.value = true
  try {
    const result = await request.get('/message/list', { params: { page: page.value, size: size.value } })
    if (result.data.code === 0) {
      messages.value = result.data.data.list || []
      total.value = result.data.data.total || 0
    }
  } catch (e) {
    ElMessage.error('加载消息失败')
  } finally {
    loading.value = false
  }
}

const loadNotices = async () => {
  try {
    const result = await request.get('/notice/list', { params: { page: 1, size: 20 } })
    if (result.data.code === 0) {
      notices.value = result.data.data.list || []
    }
  } catch (e) {}
}

const readOne = async (m) => {
  try {
    await request.put(`/message/${m.id}/read`)
    m.isRead = 1
    loadTodo()
  } catch (e) {}
}

const readAll = async () => {
  try {
    await request.put('/message/readAll')
    ElMessage.success('已全部标记为已读')
    loadMessages()
    loadTodo()
  } catch (e) {}
}

const readNotices = async () => {
  try {
    await request.put('/message/noticeRead')
    ElMessage.success('公告已标记为已读')
    loadTodo()
  } catch (e) {}
}

const goApply = () => router.push('/home/courseSelection')
const goRegister = () => router.push('/home/registerApproval')

const typeLabel = { system: '系统', approval: '审批', notice: '通知', selection: '选课' }

onMounted(() => {
  loadMessages()
  loadNotices()
  loadTodo()
})
</script>
<template>
  <div class="center-page">
    <el-card class="glass-card" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><Bell /></el-icon>
          <span class="card-title">消息中心</span>
        </div>
      </template>
      <el-tabs v-model="activeTab">
        <!-- 通知 tab -->
        <el-tab-pane label="我的消息" name="message">
          <div class="toolbar">
            <el-button size="small" @click="readAll">全部已读</el-button>
          </div>
          <el-table :data="messages" v-loading="loading" class="glass-table" empty-text="暂无消息"
                    :header-cell-style="{ textAlign: 'center' }">
            <el-table-column label="类型" width="90" align="center">
              <template #default="scope">
                <el-tag size="small" :type="scope.row.isRead === 0 ? 'warning' : 'info'">
                  {{ typeLabel[scope.row.type] || scope.row.type }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="title" label="标题" min-width="160" />
            <el-table-column prop="content" label="内容" min-width="240" show-overflow-tooltip />
            <el-table-column prop="createTime" label="时间" width="170" align="center" />
            <el-table-column label="操作" width="90" align="center">
              <template #default="scope">
                <el-link v-if="scope.row.isRead === 0" type="primary" :underline="false" @click="readOne(scope.row)">标为已读</el-link>
                <span v-else class="read-tag">已读</span>
              </template>
            </el-table-column>
          </el-table>
          <div class="pager">
            <el-pagination layout="prev, pager, next, total" :total="total" :page-size="size"
                           v-model:current-page="page" @current-change="loadMessages" />
          </div>
        </el-tab-pane>

        <!-- 公告 tab -->
        <el-tab-pane label="公告" name="notice">
          <div class="toolbar">
            <el-button size="small" @click="readNotices">公告标为已读</el-button>
          </div>
          <div class="notice-list">
            <div v-for="n in notices" :key="n.id" class="notice-item">
              <div class="notice-title">{{ n.title }}</div>
              <div class="notice-meta">{{ n.publisher }} · {{ n.publishTime || n.createTime }}</div>
              <div class="notice-content">{{ n.content }}</div>
            </div>
            <div v-if="notices.length === 0" class="empty-tip">暂无公告</div>
          </div>
        </el-tab-pane>

        <!-- 待办 tab（管理端） -->
        <el-tab-pane v-if="role === 'admin' || role === 'professor'" label="待办" name="todo">
          <div class="todo-cards">
            <div v-if="role === 'admin'" class="todo-card" @click="goRegister">
              <el-icon :size="22"><Document /></el-icon>
              <div class="todo-info">
                <div class="todo-name">注册审批</div>
                <div class="todo-desc">{{ todo.registerPending || 0 }} 条待审批的注册申请</div>
              </div>
              <el-button size="small" type="primary" plain>去处理</el-button>
            </div>
            <div class="todo-card" @click="goApply">
              <el-icon :size="22"><Stamp /></el-icon>
              <div class="todo-info">
                <div class="todo-name">退选申请审批</div>
                <div class="todo-desc">{{ todo.applyPending || 0 }} 条待审批的退选申请</div>
              </div>
              <el-button size="small" type="primary" plain>去处理</el-button>
            </div>
            <div v-if="!todo.registerPending && !todo.applyPending" class="empty-tip">暂无待办事项</div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>
<style scoped>
.center-page {
  display: flex;
  justify-content: center;
  width: 100%;
}
.glass-card {
  width: 100%;
  max-width: 1000px;
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
.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
}
.card-title {
  font-family: var(--font-serif);
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
}
.toolbar {
  margin-bottom: 10px;
  display: flex;
  justify-content: flex-end;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
:deep(.glass-table) {
  --el-table-header-bg-color: rgba(220, 240, 255, 0.5);
  --el-table-border-color: var(--glass-border-light);
}
.read-tag {
  font-size: 12px;
  color: var(--text-muted);
}
.notice-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.notice-item {
  border: 1px solid var(--glass-border-light);
  border-radius: 10px;
  padding: 12px 14px;
  background: rgba(255, 255, 255, 0.5);
}
.notice-title {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 14px;
}
.notice-meta {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--text-muted);
  margin: 4px 0;
}
.notice-content {
  font-size: 13px;
  color: var(--text-secondary);
  white-space: pre-wrap;
}
.todo-cards {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
}
.todo-card {
  display: flex;
  align-items: center;
  gap: 12px;
  border: 1px solid var(--glass-border-light);
  border-radius: 12px;
  padding: 14px 18px;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.5);
  transition: all 0.2s;
  color: var(--primary);
  min-width: 300px;
}
.todo-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(74, 144, 226, 0.15);
  border-color: var(--blue-300);
}
.todo-info {
  flex: 1;
}
.todo-name {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 14px;
}
.todo-desc {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 2px;
}
.empty-tip {
  color: var(--text-muted);
  font-size: 13px;
  padding: 20px 0;
  text-align: center;
  width: 100%;
}
</style>
