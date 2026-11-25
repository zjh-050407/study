<script setup>
import { ref, onMounted, computed } from "vue";
import request from "@/utils/request.js";
import { useRouter } from "vue-router";
import { Bell, BellFilled, Document, Stamp } from '@element-plus/icons-vue'

const props = defineProps({
  /** 消息中心页面路径（管理端 /home/message，学生端 /stu/message） */
  centerPath: { type: String, default: '/home/message' },
})

const router = useRouter()
const total = ref(0)
const todo = ref({})
const list = ref([])
const popVisible = ref(false)

const totalLabel = computed(() => (total.value > 99 ? '99+' : String(total.value)))

const loadBadge = async () => {
  try {
    const result = await request.get('/message/unreadCount')
    if (result.data.code === 0) {
      const d = result.data.data || {}
      total.value = (d.messageUnread || 0) + (d.noticeUnread || 0)
      todo.value = d.todo || {}
    }
  } catch (e) {}
}

const loadList = async () => {
  try {
    const result = await request.get('/message/list', { params: { page: 1, size: 5 } })
    if (result.data.code === 0) {
      list.value = result.data.data.list || []
    }
  } catch (e) {}
}

const onPopShow = () => {
  if (popVisible.value) {
    loadList()
  }
}

const readOne = async (m) => {
  try {
    await request.put(`/message/${m.id}/read`)
    m.isRead = 1
    loadBadge()
  } catch (e) {}
}

const readAll = async () => {
  try {
    await request.put('/message/readAll')
    list.value = []
    loadBadge()
  } catch (e) {}
}

const readNotices = async () => {
  try {
    await request.put('/message/noticeRead')
    loadBadge()
  } catch (e) {}
}

const goCenter = () => {
  popVisible.value = false
  router.push(props.centerPath)
}

const typeLabel = { system: '系统', approval: '审批', notice: '通知', selection: '选课' }

onMounted(loadBadge)
</script>
<template>
  <el-popover placement="bottom-end" :width="360" trigger="click" v-model:visible="popVisible" @show="onPopShow">
    <template #reference>
      <span class="bell-ref">
        <el-badge :value="total" :hidden="total === 0" :max="99" class="bell-badge">
          <el-icon :size="19" class="bell-icon"><Bell /></el-icon>
        </el-badge>
      </span>
    </template>
    <div class="msg-pop">
      <div class="msg-head">
        <span class="msg-title">消息通知</span>
        <span class="msg-actions">
          <el-link type="primary" :underline="false" size="small" @click="readNotices">公告已读</el-link>
          <el-link type="primary" :underline="false" size="small" @click="readAll">全部已读</el-link>
          <el-link type="primary" :underline="false" size="small" @click="goCenter">进入中心</el-link>
        </span>
      </div>
      <!-- 待办提示 -->
      <div v-if="todo.applyPending || todo.registerPending" class="todo-strip">
        <el-icon><Stamp /></el-icon>
        <span>
          待办：
          <template v-if="todo.registerPending">注册审批 {{ todo.registerPending }} 条</template>
          <template v-if="todo.registerPending && todo.applyPending">，</template>
          <template v-if="todo.applyPending">退选申请 {{ todo.applyPending }} 条</template>
        </span>
      </div>
      <div class="msg-list">
        <div v-if="list.length === 0" class="msg-empty">暂无消息</div>
        <div v-for="m in list" :key="m.id" class="msg-item" :class="{ unread: m.isRead === 0 }" @click="readOne(m)">
          <el-icon class="msg-dot"><Document /></el-icon>
          <div class="msg-body">
            <div class="msg-item-title">{{ m.title }}</div>
            <div class="msg-item-content">{{ m.content }}</div>
            <div class="msg-item-time">{{ m.createTime }}</div>
          </div>
        </div>
      </div>
    </div>
  </el-popover>
</template>
<style scoped>
.bell-ref {
  display: inline-flex;
  align-items: center;
  cursor: pointer;
  padding: 4px;
}
.bell-icon {
  color: var(--text-secondary);
}
.bell-ref:hover .bell-icon {
  color: var(--primary);
}
.msg-pop {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.msg-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--glass-border-light);
  padding-bottom: 8px;
}
.msg-title {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 14px;
}
.msg-actions {
  display: flex;
  gap: 10px;
}
.todo-strip {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--primary);
  background: var(--bg-gradient-end);
  border-radius: 6px;
  padding: 6px 10px;
}
.msg-list {
  max-height: 300px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.msg-empty {
  text-align: center;
  color: var(--text-muted);
  font-size: 12px;
  padding: 20px 0;
}
.msg-item {
  display: flex;
  gap: 8px;
  padding: 8px;
  border-radius: 8px;
  cursor: pointer;
  border: 1px solid transparent;
}
.msg-item:hover {
  background: var(--bg-gradient-start);
}
.msg-item.unread {
  background: var(--blue-50);
}
.msg-dot {
  color: var(--blue-400);
  margin-top: 2px;
  flex-shrink: 0;
}
.msg-body {
  flex: 1;
  min-width: 0;
}
.msg-item-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}
.msg-item-content {
  font-size: 12px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.msg-item-time {
  font-family: var(--font-mono);
  font-size: 10px;
  color: var(--text-muted);
}
</style>
