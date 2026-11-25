<script setup>
import request from "@/utils/request.js";
import { ref, onMounted } from "vue";
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from "@element-plus/icons-vue";

const rounds = ref([])
const loading = ref(false)
const semesters = ref([])
const semesterId = ref(null)
const dialogVisible = ref(false)
const saving = ref(false)

const form = ref({ name: '', type: 1, maxWishes: 3 })

const typeMap = { 1: '初选（志愿投档）', 2: '补退选（先到先得）' }
const typeTag = { 1: 'primary', 2: 'success' }
const statusMap = { 0: '未开始', 1: '进行中', 2: '已结束' }
const statusTag = { 0: 'info', 1: 'warning', 2: 'success' }

const loadSemesters = async () => {
  try {
    const result = await request.get('/semester/list')
    if (result.data.code === 0) {
      semesters.value = result.data.data || []
      const current = semesters.value.find(s => s.isCurrent === 1)
      semesterId.value = current ? current.id : (semesters.value.length > 0 ? semesters.value[semesters.value.length - 1].id : 0)
    }
  } catch (e) {}
}

const getRounds = async () => {
  loading.value = true
  try {
    const result = await request.get('/selection/round/list', { params: { semesterId: semesterId.value } })
    if (result.data.code === 0) {
      rounds.value = result.data.data || []
    }
  } catch (e) {
    ElMessage.error('加载轮次失败')
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  form.value = { name: '', type: 1, maxWishes: 3 }
  dialogVisible.value = true
}

const save = async () => {
  if (!form.value.name.trim()) {
    ElMessage.warning('请填写轮次名称')
    return
  }
  saving.value = true
  try {
    const result = await request.post('/selection/round', { ...form.value, semesterId: semesterId.value })
    if (result.data.code === 0) {
      ElMessage.success('创建成功')
      dialogVisible.value = false
      getRounds()
    } else {
      ElMessage.error(result.data.message || '创建失败')
    }
  } catch (e) {
    ElMessage.error('网络错误')
  } finally {
    saving.value = false
  }
}

const changeStatus = async (row, status) => {
  const label = status === 1 ? '开始' : '结束'
  try {
    await ElMessageBox.confirm(`确定${label}轮次「${row.name}」？`, '提示', { type: 'warning' })
    const result = await request.put(`/selection/round/${row.id}/status`, null, { params: { status } })
    if (result.data.code === 0) {
      ElMessage.success(`${label}成功`)
      getRounds()
    } else {
      ElMessage.error(result.data.message || '操作失败')
    }
  } catch (e) {}
}

const allocate = async (row) => {
  try {
    await ElMessageBox.confirm(`确定对「${row.name}」执行投档？投档后轮次将结束，学生按志愿顺序录取。`, '提示', { type: 'warning' })
    const result = await request.post(`/selection/round/${row.id}/allocate`)
    if (result.data.code === 0) {
      const d = result.data.data
      ElMessage.success(`投档完成：参与 ${d.total} 人，录取 ${d.admitted} 人，未录取 ${d.failed} 人`)
      getRounds()
    } else {
      ElMessage.error(result.data.message || '投档失败')
    }
  } catch (e) {}
}

const onSemesterChange = () => getRounds()

onMounted(async () => {
  await loadSemesters()
  getRounds()
})
</script>
<template>
  <div class="round-page">
    <el-card class="glass-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">选课轮次管理</span>
          <div class="header-actions">
            <el-select v-model="semesterId" style="width: 240px" @change="onSemesterChange">
              <el-option v-for="s in semesters" :key="s.id" :label="s.name" :value="s.id" />
            </el-select>
            <el-button :icon="Refresh" circle @click="getRounds" />
            <el-button type="primary" :icon="Plus" @click="openCreate">新建轮次</el-button>
          </div>
        </div>
      </template>

      <el-alert class="tip-alert" type="info" :closable="false" show-icon
                title="初选轮次：学生提交志愿，轮次结束后执行投档录取；补退选轮次：学生先到先得自助选课。同类型同时只能有一个进行中轮次。" />

      <el-table :data="rounds" class="glass-table" v-loading="loading" empty-text="暂无轮次"
                :header-cell-style="{ textAlign: 'center' }">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="name" label="轮次名称" min-width="170" show-overflow-tooltip />
        <el-table-column label="类型" width="150" align="center">
          <template #default="scope">
            <el-tag :type="typeTag[scope.row.type]" size="small">{{ typeMap[scope.row.type] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="scope">
            <el-tag :type="statusTag[scope.row.status]" size="small">{{ statusMap[scope.row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="志愿上限" width="90" align="center">
          <template #default="scope">{{ scope.row.type === 1 ? scope.row.maxWishes : '-' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" align="center">
          <template #default="scope">{{ scope.row.createTime ? String(scope.row.createTime).replace('T', ' ').slice(0, 19) : '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="scope">
            <el-button v-if="scope.row.status === 0" size="small" type="warning" plain @click="changeStatus(scope.row, 1)">开始</el-button>
            <el-button v-if="scope.row.status === 1 && scope.row.type === 1" size="small" type="primary" plain @click="allocate(scope.row)">投档</el-button>
            <el-button v-if="scope.row.status === 1 && scope.row.type === 2" size="small" type="danger" plain @click="changeStatus(scope.row, 2)">结束</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新建选课轮次" width="460" :close-on-click-modal="false">
      <el-form :model="form" label-width="90px">
        <el-form-item label="轮次名称">
          <el-input v-model="form.name" placeholder="如：2026-2027学年第一学期初选" />
        </el-form-item>
        <el-form-item label="轮次类型">
          <el-radio-group v-model="form.type">
            <el-radio :value="1">初选（志愿投档）</el-radio>
            <el-radio :value="2">补退选（先到先得）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.type === 1" label="志愿上限">
          <el-input-number v-model="form.maxWishes" :min="1" :max="10" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<style scoped>
.round-page {
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
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}
.header-actions {
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
.tip-alert {
  margin-bottom: 12px;
}
:deep(.glass-table) {
  --el-table-header-bg-color: rgba(220, 240, 255, 0.5);
  --el-table-border-color: var(--glass-border-light);
  border-radius: 8px;
  overflow: hidden;
}
</style>
