<script setup>
import { ref, onMounted } from "vue";
import request from "@/utils/request.js";
import { ElMessage, ElMessageBox } from 'element-plus'
import { Calendar } from '@element-plus/icons-vue'

const semesters = ref([])
const semesterId = ref(null)
const weeks = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref(null)

const form = ref({ weekNo: 1, startDate: '', endDate: '', note: '' })

const loadSemesters = async () => {
  try {
    const result = await request.get('/semester/list')
    if (result.data.code === 0) {
      semesters.value = result.data.data || []
      const current = semesters.value.find(s => s.isCurrent === 1 || s.isCurrent === true)
      if (current) {
        semesterId.value = current.id
        loadWeeks()
      } else if (semesters.value.length > 0) {
        semesterId.value = semesters.value[semesters.value.length - 1].id
        loadWeeks()
      }
    }
  } catch (e) {}
}

const loadWeeks = async () => {
  loading.value = true
  try {
    const result = await request.get('/calendar/list', { params: { semesterId: semesterId.value } })
    if (result.data.code === 0) {
      weeks.value = result.data.data || []
    }
  } catch (e) {
    ElMessage.error('加载校历失败')
  } finally {
    loading.value = false
  }
}

const onSemesterChange = () => loadWeeks()

const openAdd = () => {
  editingId.value = null
  const nextNo = weeks.value.length > 0 ? Math.max(...weeks.value.map(w => w.weekNo)) + 1 : 1
  form.value = { weekNo: nextNo, startDate: '', endDate: '', note: '' }
  dialogVisible.value = true
}

const openEdit = (row) => {
  editingId.value = row.id
  form.value = { weekNo: row.weekNo, startDate: row.startDate, endDate: row.endDate, note: row.note || '' }
  dialogVisible.value = true
}

const save = async () => {
  if (!form.value.weekNo) {
    ElMessage.warning('请填写教学周')
    return
  }
  saving.value = true
  try {
    const body = {
      weekNo: form.value.weekNo,
      startDate: form.value.startDate || null,
      endDate: form.value.endDate || null,
      note: form.value.note || null,
    }
    let result
    if (editingId.value) {
      result = await request.put('/calendar', { ...body, id: editingId.value, semesterId: semesterId.value })
    } else {
      result = await request.post('/calendar', { ...body, semesterId: semesterId.value })
    }
    if (result.data.code === 0) {
      ElMessage.success('保存成功')
      dialogVisible.value = false
      loadWeeks()
    } else {
      ElMessage.error(result.data.message || '保存失败')
    }
  } catch (e) {
    ElMessage.error('网络错误')
  } finally {
    saving.value = false
  }
}

const remove = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除第 ${row.weekNo} 周的校历记录？`, '提示', { type: 'warning' })
    const result = await request.delete(`/calendar/${row.id}`)
    if (result.data.code === 0) {
      ElMessage.success('已删除')
      loadWeeks()
    } else {
      ElMessage.error(result.data.message || '删除失败')
    }
  } catch (e) {}
}

onMounted(loadSemesters)
</script>
<template>
  <div class="calendar-page">
    <el-card class="glass-card" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><Calendar /></el-icon>
          <span class="card-title">校历（教学周历）</span>
        </div>
      </template>
      <div class="toolbar">
        <el-select v-model="semesterId" style="width: 260px" @change="onSemesterChange" placeholder="选择学期">
          <el-option v-for="s in semesters" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
        <el-button type="primary" @click="openAdd">新增教学周</el-button>
      </div>
      <el-table :data="weeks" v-loading="loading" class="glass-table" empty-text="暂无校历数据"
                :header-cell-style="{ textAlign: 'center' }">
        <el-table-column prop="weekNo" label="教学周" width="100" align="center">
          <template #default="scope">第 {{ scope.row.weekNo }} 周</template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="140" align="center" />
        <el-table-column prop="endDate" label="结束日期" width="140" align="center" />
        <el-table-column prop="note" label="备注" min-width="200" />
        <el-table-column label="操作" width="160" align="center">
          <template #default="scope">
            <el-button size="small" @click="openEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="remove(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑教学周' : '新增教学周'" width="440px" :close-on-click-modal="false">
      <el-form :model="form" label-width="90px">
        <el-form-item label="教学周">
          <el-input-number v-model="form.weekNo" :min="1" :max="30" />
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="选择开始日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" placeholder="选择结束日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.note" placeholder="如：考试周/运动会" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<style scoped>
.calendar-page {
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
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 8px;
}
:deep(.glass-table) {
  --el-table-header-bg-color: rgba(220, 240, 255, 0.5);
  --el-table-border-color: var(--glass-border-light);
}
</style>
