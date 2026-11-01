<script setup>
import request from "@/utils/request.js";
import { ref, onMounted } from "vue";
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const loading = ref(false)
const courses = ref([])
const teachers = ref([])
const semesters = ref([])

const dialogVisible = ref(false)
const saving = ref(false)
const isEdit = ref(false)
const form = ref({})

const examTypes = ['期末', '补考', '重修']

const getData = async () => {
  loading.value = true
  try {
    const [examResult, courseResult, teacherResult, semesterResult] = await Promise.all([
      request.get('/exam/list'),
      request.get('/course/list'),
      request.get('/teacher/list'),
      request.get('/semester/list')
    ])
    if (examResult.data.code === 0) list.value = examResult.data.data || []
    if (courseResult.data.code === 0) courses.value = courseResult.data.data || []
    if (teacherResult.data.code === 0) teachers.value = teacherResult.data.data || []
    if (semesterResult.data.code === 0) semesters.value = semesterResult.data.data || []
  } finally {
    loading.value = false
  }
}

const currentSemesterId = () => {
  const current = semesters.value.find(s => s.isCurrent === 1)
  return current ? current.id : 0
}

const openAdd = () => {
  isEdit.value = false
  form.value = {
    courseId: '',
    semesterId: currentSemesterId(),
    examDate: '',
    startSection: 1,
    endSection: 2,
    room: '',
    invigilatorUid: '',
    type: '期末'
  }
  dialogVisible.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  form.value = {
    id: row.id,
    courseId: row.courseId,
    semesterId: row.semesterId || 0,
    examDate: row.examDate,
    startSection: row.startSection,
    endSection: row.endSection,
    room: row.room,
    invigilatorUid: row.invigilatorUid || '',
    type: row.type
  }
  dialogVisible.value = true
}

const save = async () => {
  if (!form.value.courseId) {
    ElMessage.warning('请选择课程')
    return
  }
  if (!form.value.examDate) {
    ElMessage.warning('请选择考试日期')
    return
  }
  if (!form.value.room) {
    ElMessage.warning('请填写考场')
    return
  }
  saving.value = true
  try {
    const result = isEdit.value
      ? await request.put('/exam', form.value)
      : await request.post('/exam', form.value)
    if (result.data.code === 0) {
      dialogVisible.value = false
      ElMessage.success(isEdit.value ? '考试安排已更新' : '考试安排已新增')
      await getData()
    } else {
      ElMessage.error(result.data.message || '保存失败')
    }
  } finally {
    saving.value = false
  }
}

const removeRow = (row) => {
  ElMessageBox.confirm(`确认删除「${row.courseName}」的考试安排（${row.examDate} 第${row.startSection}-${row.endSection}节）？`, '删除考试安排', {
    type: 'warning',
    confirmButtonText: '确认删除',
    cancelButtonText: '取消'
  }).then(async () => {
    const result = await request.delete(`/exam/${row.id}`)
    if (result.data.code === 0) {
      ElMessage.success('已删除')
      await getData()
    } else {
      ElMessage.error(result.data.message || '删除失败')
    }
  }).catch(() => {})
}

onMounted(getData)
</script>
<template>
  <div class="exam-page">
    <el-card class="glass-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">考务排考</span>
          <span class="card-tip">同课程不重复排考；同考场同时段、同监考教师同时段自动冲突校验</span>
        </div>
      </template>
      <div class="toolbar">
        <el-button type="primary" class="btn-glass-primary" @click="openAdd">新增考试安排</el-button>
      </div>
      <el-table :data="list" class="glass-table" v-loading="loading" empty-text="暂无考试安排">
        <el-table-column prop="courseName" label="课程" min-width="140" show-overflow-tooltip />
        <el-table-column label="学期" width="180" align="center">
          <template #default="scope">{{ scope.row.semesterName || '未分学期' }}</template>
        </el-table-column>
        <el-table-column prop="examDate" label="考试日期" width="110" align="center" />
        <el-table-column label="节次" width="90" align="center">
          <template #default="scope">{{ scope.row.startSection }}-{{ scope.row.endSection }}节</template>
        </el-table-column>
        <el-table-column prop="room" label="考场" width="90" align="center" />
        <el-table-column label="监考教师" width="100" align="center">
          <template #default="scope">{{ scope.row.invigilatorName || '-' }}</template>
        </el-table-column>
        <el-table-column label="类型" width="80" align="center">
          <template #default="scope">
            <el-tag size="small" effect="light" :type="scope.row.type === '期末' ? 'primary' : scope.row.type === '补考' ? 'warning' : 'info'">
              {{ scope.row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right" align="center">
          <template #default="scope">
            <el-button size="small" class="btn-glass-edit" @click="openEdit(scope.row)">编辑</el-button>
            <el-button size="small" class="btn-glass-danger" @click="removeRow(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑考试安排' : '新增考试安排'" width="520" class="glass-dialog">
      <el-form label-width="90px">
        <el-form-item label="课程">
          <el-select v-model="form.courseId" placeholder="请选择课程" class="picker-full">
            <el-option v-for="item in courses" :key="item.courseId" :label="item.name" :value="item.courseId" />
          </el-select>
        </el-form-item>
        <el-form-item label="学期">
          <el-select v-model="form.semesterId" placeholder="请选择学期" class="picker-full">
            <el-option v-for="item in semesters" :key="item.id" :label="item.name" :value="item.id" />
            <el-option label="未分学期" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="考试日期">
          <el-date-picker v-model="form.examDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" class="picker-full" />
        </el-form-item>
        <el-form-item label="节次">
          <div class="section-row">
            <el-input-number v-model="form.startSection" :min="1" :max="12" />
            <span class="section-sep">至</span>
            <el-input-number v-model="form.endSection" :min="1" :max="12" />
          </div>
        </el-form-item>
        <el-form-item label="考场">
          <el-input v-model="form.room" placeholder="如：A101" class="picker-full" />
        </el-form-item>
        <el-form-item label="监考教师">
          <el-select v-model="form.invigilatorUid" placeholder="请选择监考教师（可空）" clearable class="picker-full">
            <el-option v-for="item in teachers" :key="item.tid" :label="item.name" :value="item.tid" />
          </el-select>
        </el-form-item>
        <el-form-item label="考试类型">
          <el-select v-model="form.type" class="picker-full">
            <el-option v-for="t in examTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false" class="btn-glass-outline">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save" class="btn-glass-primary">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<style scoped>
.exam-page {
  display: flex;
  justify-content: center;
  width: 100%;
}
.exam-page .glass-card {
  width: 100%;
  max-width: 1100px;
}
.glass-card {
  background: var(--glass-bg-strong);
  backdrop-filter: var(--blur-md);
  -webkit-backdrop-filter: var(--blur-md);
  border: 1px solid var(--glass-border);
  border-radius: 16px;
  box-shadow: var(--glass-shadow);
}
.card-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}
.card-tip {
  font-size: 12px;
  color: var(--text-muted);
}
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
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
:deep(.btn-glass-edit) {
  background: rgba(255, 255, 255, 0.45) !important;
  border: 1px solid var(--glass-border) !important;
  color: var(--blue-600) !important;
  border-radius: 6px !important;
}
:deep(.btn-glass-danger) {
  background: rgba(245, 108, 108, 0.08) !important;
  border: 1px solid rgba(245, 108, 108, 0.2) !important;
  color: #f56c6c !important;
  border-radius: 6px !important;
}
.picker-full {
  width: 100%;
}
.section-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.section-sep {
  color: var(--text-secondary);
}
:deep(.glass-dialog .el-dialog) {
  background: var(--glass-bg-strong);
  backdrop-filter: var(--blur-lg);
  -webkit-backdrop-filter: var(--blur-lg);
  border: 1px solid var(--glass-border);
  border-radius: 16px;
  box-shadow: var(--glass-shadow);
}
</style>