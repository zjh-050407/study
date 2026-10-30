<script setup>
import request from "@/utils/request.js";
import { ref, onMounted } from "vue";
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from "@element-plus/icons-vue";

const tableData = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const role = ref('')

const typeLabel = { system: '系统', academic: '教务', exam: '考试', activity: '活动' }

const getData = async () => {
  loading.value = true
  try {
    const result = await request.get('/notice/list', {
      params: { page: currentPage.value, size: pageSize.value }
    })
    if (result.data.code === 0) {
      tableData.value = result.data.data.list
      total.value = result.data.data.total
    }
  } catch (err) {
    console.error('获取数据失败:', err)
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  getData()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  getData()
}

onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    try {
      role.value = JSON.parse(userStr).role || ''
    } catch (e) {}
  }
  getData()
})

const canPublish = () => role.value === 'professor' || role.value === 'admin'

const dialogVisible = ref(false)
const form = ref({ title: '', content: '' })

const publish = () => {
  form.value = { title: '', content: '' }
  dialogVisible.value = true
}

const publishConfirm = async () => {
  if (!form.value.title.trim()) {
    ElMessage.warning('请输入公告标题')
    return
  }
  if (!form.value.content.trim()) {
    ElMessage.warning('请输入公告内容')
    return
  }
  const result = await request.post('/notice', form.value)
  if (result.data.code === 0) {
    dialogVisible.value = false
    currentPage.value = 1
    await getData()
    ElMessage.success('发布成功')
  } else {
    ElMessage.error(result.data.message || '发布失败')
  }
}

const handleDelete = async (row) => {
  ElMessageBox.confirm('是否确认删除该公告？', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    const result = await request.delete('/notice/' + row.id)
    if (result.data.code === 0) {
      await getData()
      ElMessage.success('删除成功')
    } else {
      ElMessage.error(result.data.message || '删除失败')
    }
  }).catch(() => {})
}

const formatTime = (time) => time ? time.replace('T', ' ') : '-'
</script>
<template>
  <div class="contain">
    <el-card class="glass-card" style="max-width: 860px">
      <template #header>
        <div class="card-header">
          <span class="card-title">公告管理</span>
          <el-button v-if="canPublish()" :icon="Plus" round @click="publish" class="btn-neu-add">发布公告</el-button>
        </div>
      </template>
      <el-table :data="tableData" style="width: 100%" class="glass-table" v-loading="loading" empty-text="暂无公告">
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="content" label="内容" min-width="220" show-overflow-tooltip />
        <el-table-column label="类型" width="90">
          <template #default="scope">
            <span class="type-tag">{{ typeLabel[scope.row.type] || scope.row.type || '系统' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="publisher" label="发布人" width="110" />
        <el-table-column label="发布时间" width="170">
          <template #default="scope">
            {{ formatTime(scope.row.publishTime || scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column v-if="canPublish()" label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button size="small" class="btn-glass-danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="display: flex; justify-content: center; margin-top: 16px;">
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[5, 10, 20]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
            class="glass-pagination"
        />
      </div>
    </el-card>
    <el-dialog v-model="dialogVisible" title="发布公告" width="560" class="glass-dialog">
      <el-form :model="form" label-width="70px">
        <el-form-item label="标题">
          <el-input v-model="form.title" maxlength="100" show-word-limit placeholder="请输入公告标题" class="glass-input-sm" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="6" maxlength="1000" placeholder="请输入公告内容" class="glass-input-sm" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false" class="btn-glass-outline">取消</el-button>
          <el-button type="primary" @click="publishConfirm" class="btn-glass-primary">发布</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
<style scoped>
.contain {
  display: flex;
  justify-content: center;
  flex-direction: column;
  align-items: center;
  width: 100%;
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-title {
  font-family: var(--font-serif);
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

:deep(.btn-neu-add) {
  background: var(--primary) !important;
  border: none !important;
  border-radius: 8px !important;
  color: #fff !important;
  box-shadow: 0 4px 14px rgba(217, 119, 87, 0.25);
  transition: all 0.25s ease;
}
:deep(.btn-neu-add:hover) {
  transform: translateY(-1px);
  box-shadow: 0 6px 18px rgba(217, 119, 87, 0.35);
}
:deep(.btn-glass-primary) {
  background: linear-gradient(135deg, var(--blue-400), var(--blue-500)) !important;
  border: none !important;
  border-radius: 10px !important;
  box-shadow: 0 4px 14px rgba(74, 144, 226, 0.25);
}
:deep(.btn-glass-outline) {
  background: rgba(255, 255, 255, 0.4) !important;
  border: 1px solid var(--glass-border) !important;
  border-radius: 8px !important;
  color: var(--text-secondary);
}
:deep(.btn-glass-danger) {
  background: rgba(245, 108, 108, 0.08) !important;
  border: 1px solid rgba(245, 108, 108, 0.2) !important;
  color: #f56c6c !important;
  border-radius: 6px !important;
}
:deep(.glass-input-sm .el-input__wrapper),
:deep(.glass-input-sm .el-textarea__inner) {
  background: rgba(255, 255, 255, 0.5) !important;
  border: 1px solid var(--glass-border-light) !important;
  border-radius: 8px !important;
  box-shadow: none !important;
}
:deep(.glass-table) {
  --el-table-header-bg-color: rgba(220, 240, 255, 0.5);
  --el-table-row-hover-bg-color: rgba(140, 190, 255, 0.1);
  --el-table-border-color: var(--glass-border-light);
  border-radius: 8px;
  overflow: hidden;
}
:deep(.glass-table th.el-table__cell) {
  color: var(--text-primary);
  font-weight: 600;
}
:deep(.glass-pagination .el-pager li.is-active) {
  background: var(--primary);
  color: #fff;
  border-color: var(--primary);
}
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
}
:deep(.glass-dialog .el-dialog__title) {
  color: var(--text-primary);
  font-weight: 600;
}
:deep(.glass-dialog .el-dialog__footer) {
  border-top: 1px solid var(--glass-border-light);
}

.type-tag {
  font-size: 12px;
  color: var(--blue-600);
  background: var(--blue-50);
  border: 1px solid var(--blue-100);
  border-radius: 3px;
  padding: 1px 8px;
}
</style>