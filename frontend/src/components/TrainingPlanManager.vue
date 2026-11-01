<script setup>
import request from "@/utils/request.js";
import { ref, onMounted } from "vue";
import { ElMessage, ElMessageBox } from 'element-plus'

const plans = ref([])
const loading = ref(false)
const clazzes = ref([])
const courses = ref([])
const courseTypes = ['必修', '限选', '任选']

/* ============ 方案 CRUD ============ */
const planDialogVisible = ref(false)
const planSaving = ref(false)
const isPlanEdit = ref(false)
const planForm = ref({})

const getData = async () => {
  loading.value = true
  try {
    const [planResult, clazzResult, courseResult] = await Promise.all([
      request.get('/training/plan/list'),
      request.get('/clazz/list'),
      request.get('/course/list')
    ])
    if (planResult.data.code === 0) plans.value = planResult.data.data || []
    if (clazzResult.data.code === 0) clazzes.value = clazzResult.data.data || []
    if (courseResult.data.code === 0) courses.value = courseResult.data.data || []
  } finally {
    loading.value = false
  }
}

const openPlanAdd = () => {
  isPlanEdit.value = false
  planForm.value = { cid: '', name: '', totalCreditRequired: 0, requiredCreditRequired: 0, electiveCreditRequired: 0 }
  planDialogVisible.value = true
}

const openPlanEdit = (row) => {
  isPlanEdit.value = true
  planForm.value = {
    id: row.id,
    cid: row.cid,
    name: row.name || '',
    totalCreditRequired: row.totalCreditRequired ?? 0,
    requiredCreditRequired: row.requiredCreditRequired ?? 0,
    electiveCreditRequired: row.electiveCreditRequired ?? 0
  }
  planDialogVisible.value = true
}

const savePlan = async () => {
  if (!planForm.value.cid) {
    ElMessage.warning('请选择班级')
    return
  }
  planSaving.value = true
  try {
    const result = isPlanEdit.value
      ? await request.put('/training/plan', planForm.value)
      : await request.post('/training/plan', planForm.value)
    if (result.data.code === 0) {
      planDialogVisible.value = false
      ElMessage.success(isPlanEdit.value ? '培养方案已更新' : '培养方案已创建')
      await getData()
    } else {
      ElMessage.error(result.data.message || '保存失败')
    }
  } finally {
    planSaving.value = false
  }
}

const removePlan = (row) => {
  ElMessageBox.confirm(`确认删除「${row.clazzName}」的培养方案？方案下全部课程配置将一并删除。`, '删除培养方案', {
    type: 'warning',
    confirmButtonText: '确认删除',
    cancelButtonText: '取消'
  }).then(async () => {
    const result = await request.delete(`/training/plan/${row.id}`)
    if (result.data.code === 0) {
      ElMessage.success('已删除')
      await getData()
    } else {
      ElMessage.error(result.data.message || '删除失败')
    }
  }).catch(() => {})
}

/* ============ 方案课程 ============ */
const courseDialogVisible = ref(false)
const courseDialogLoading = ref(false)
const currentPlan = ref(null)
const planCourses = ref([])
const courseForm = ref({})
const courseSaving = ref(false)
const isCourseEdit = ref(false)

const openCourses = async (row) => {
  currentPlan.value = row
  courseDialogVisible.value = true
  courseDialogLoading.value = true
  try {
    const result = await request.get(`/training/plan/${row.id}/courses`)
    if (result.data.code === 0) {
      planCourses.value = result.data.data || []
    }
  } finally {
    courseDialogLoading.value = false
  }
}

const openCourseAdd = () => {
  isCourseEdit.value = false
  courseForm.value = { courseId: '', type: '必修', requireCredit: 0 }
}

const openCourseEdit = (row) => {
  isCourseEdit.value = true
  courseForm.value = { id: row.id, courseId: row.courseId, type: row.type, requireCredit: row.requireCredit }
}

const saveCourse = async () => {
  if (!courseForm.value.courseId) {
    ElMessage.warning('请选择课程')
    return
  }
  courseSaving.value = true
  try {
    const result = isCourseEdit.value
      ? await request.put('/training/plan/course', courseForm.value)
      : await request.post('/training/plan/course', { ...courseForm.value, planId: currentPlan.value.id })
    if (result.data.code === 0) {
      courseForm.value = {}
      ElMessage.success(isCourseEdit.value ? '课程配置已更新' : '课程已添加')
      await openCourses(currentPlan.value)
    } else {
      ElMessage.error(result.data.message || '保存失败')
    }
  } finally {
    courseSaving.value = false
  }
}

const removeCourse = (row) => {
  ElMessageBox.confirm(`确认从方案中移除「${row.courseName}」？`, '移除课程', {
    type: 'warning',
    confirmButtonText: '确认移除',
    cancelButtonText: '取消'
  }).then(async () => {
    const result = await request.delete(`/training/plan/course/${row.id}`)
    if (result.data.code === 0) {
      ElMessage.success('已移除')
      await openCourses(currentPlan.value)
    } else {
      ElMessage.error(result.data.message || '移除失败')
    }
  }).catch(() => {})
}

const typeTag = (t) => t === '必修' ? 'danger' : t === '限选' ? 'warning' : 'info'

onMounted(getData)
</script>
<template>
  <div class="plan-page">
    <el-card class="glass-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">培养方案</span>
          <span class="card-tip">按班级配置课程体系（必修/限选/任选 + 学分要求），毕业审核按方案校验；无方案的班级回退总学分模式</span>
        </div>
      </template>
      <div class="toolbar">
        <el-button type="primary" class="btn-glass-primary" @click="openPlanAdd">新增培养方案</el-button>
      </div>
      <el-table :data="plans" class="glass-table" v-loading="loading" empty-text="暂无培养方案">
        <el-table-column prop="id" label="方案ID" width="80" align="center" />
        <el-table-column label="班级" width="150" align="center">
          <template #default="scope">{{ scope.row.clazzName || '-' }}</template>
        </el-table-column>
        <el-table-column label="方案名称" min-width="150">
          <template #default="scope">{{ scope.row.name || '-' }}</template>
        </el-table-column>
        <el-table-column label="总学分要求" width="110" align="center">
          <template #default="scope">{{ scope.row.totalCreditRequired }}</template>
        </el-table-column>
        <el-table-column label="必修学分要求" width="110" align="center">
          <template #default="scope">{{ scope.row.requiredCreditRequired }}</template>
        </el-table-column>
        <el-table-column label="选修学分要求" width="110" align="center">
          <template #default="scope">{{ scope.row.electiveCreditRequired }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="scope">
            <el-button size="small" class="btn-glass-edit" @click="openCourses(scope.row)">课程设置</el-button>
            <el-button size="small" class="btn-glass-edit" @click="openPlanEdit(scope.row)">编辑</el-button>
            <el-button size="small" class="btn-glass-danger" @click="removePlan(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="planDialogVisible" :title="isPlanEdit ? '编辑培养方案' : '新增培养方案'" width="520" class="glass-dialog">
      <el-form label-width="110px">
        <el-form-item label="适用班级">
          <el-select v-model="planForm.cid" placeholder="请选择班级" class="picker-full" :disabled="isPlanEdit">
            <el-option v-for="c in clazzes" :key="c.cid" :label="c.name" :value="c.cid" />
          </el-select>
        </el-form-item>
        <el-form-item label="方案名称">
          <el-input v-model="planForm.name" placeholder="选填，如：计科2301培养方案" class="picker-full" />
        </el-form-item>
        <el-form-item label="总学分要求">
          <el-input-number v-model="planForm.totalCreditRequired" :min="0" :step="5" :precision="1" />
        </el-form-item>
        <el-form-item label="必修学分要求">
          <el-input-number v-model="planForm.requiredCreditRequired" :min="0" :step="5" :precision="1" />
        </el-form-item>
        <el-form-item label="选修学分要求">
          <el-input-number v-model="planForm.electiveCreditRequired" :min="0" :step="5" :precision="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="planDialogVisible = false" class="btn-glass-outline">取消</el-button>
        <el-button type="primary" :loading="planSaving" @click="savePlan" class="btn-glass-primary">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="courseDialogVisible" :title="`课程设置 · ${currentPlan?.clazzName || ''}`" width="700" class="glass-dialog">
      <div class="toolbar">
        <el-button size="small" type="primary" class="btn-glass-primary" @click="openCourseAdd">添加课程</el-button>
      </div>
      <div v-if="courseForm.courseId !== undefined" class="course-form">
        <el-select v-model="courseForm.courseId" placeholder="选择课程" class="course-select" :disabled="isCourseEdit">
          <el-option v-for="c in courses" :key="c.courseId" :label="`${c.name}（${c.credit ?? '-'}学分）`" :value="c.courseId" />
        </el-select>
        <el-select v-model="courseForm.type" placeholder="课程类型" class="type-select">
          <el-option v-for="t in courseTypes" :key="t" :label="t" :value="t" />
        </el-select>
        <el-input-number v-model="courseForm.requireCredit" :min="0" :step="0.5" :precision="1" />
        <el-button size="small" type="primary" :loading="courseSaving" class="btn-glass-primary"
                   @click="saveCourse">{{ isCourseEdit ? '保存修改' : '确定添加' }}</el-button>
        <el-button size="small" class="btn-glass-outline" @click="courseForm = {}">取消</el-button>
      </div>
      <el-table :data="planCourses" v-loading="courseDialogLoading" size="small" empty-text="方案暂无课程">
        <el-table-column prop="courseName" label="课程" min-width="150" />
        <el-table-column label="类型" width="90" align="center">
          <template #default="scope">
            <el-tag size="small" effect="light" :type="typeTag(scope.row.type)">{{ scope.row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requireCredit" label="学分要求" width="100" align="center" />
        <el-table-column label="操作" width="140" align="center">
          <template #default="scope">
            <el-button size="small" class="btn-glass-edit" @click="openCourseEdit(scope.row)">编辑</el-button>
            <el-button size="small" class="btn-glass-danger" @click="removeCourse(scope.row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>
<style scoped>
.plan-page {
  display: flex;
  justify-content: center;
  width: 100%;
}
.plan-page .glass-card {
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
.course-form {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.course-select {
  width: 240px;
}
.type-select {
  width: 120px;
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
:deep(.glass-dialog .el-dialog) {
  background: var(--glass-bg-strong);
  backdrop-filter: var(--blur-lg);
  -webkit-backdrop-filter: var(--blur-lg);
  border: 1px solid var(--glass-border);
  border-radius: 16px;
  box-shadow: var(--glass-shadow);
}
</style>