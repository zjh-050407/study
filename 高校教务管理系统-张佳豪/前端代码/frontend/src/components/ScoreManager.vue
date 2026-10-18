<script setup>
import request from "@/utils/request.js";
import { ref, onMounted, computed } from "vue";
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Download } from "@element-plus/icons-vue";

const tableData = ref([])
const allData = ref([])

const getData = async () => {
  try {
    const result = await request.get('/score/list')
    if (result.data.code === 0) {
      allData.value = result.data.data
      tableData.value = result.data.data
    }
  } catch (err) {
    console.error('获取数据失败:', err)
  }
}
getData()

const searchKeyword = ref('')

const computedTotal = computed(() => {
  const u = Number(form.value.usual || 0)
  const m = Number(form.value.mid || 0)
  const f = Number(form.value.finalScore || 0)
  return (u * 0.3 + m * 0.2 + f * 0.5).toFixed(1)
})

const computedGp = computed(() => {
  const total = parseFloat(computedTotal.value)
  if (total >= 90) return '4.0'
  if (total >= 85) return '3.7'
  if (total >= 82) return '3.3'
  if (total >= 78) return '3.0'
  if (total >= 75) return '2.7'
  if (total >= 72) return '2.3'
  if (total >= 68) return '2.0'
  if (total >= 64) return '1.5'
  if (total >= 60) return '1.0'
  return '0.0'
})

const doSearch = () => {
  const kw = searchKeyword.value.trim()
  if (!kw) {
    tableData.value = allData.value
    return
  }
  tableData.value = allData.value.filter(item =>
    (item.studentName && item.studentName.includes(kw)) ||
    (item.courseName && item.courseName.includes(kw)) ||
    (item.clazzName && item.clazzName.includes(kw))
  )
}

const dialogFormVisible = ref(false)
const addVisible = ref(false)
const formLabelWidth = '100px'

const form = ref({
  scoreId: '',
  sid: '',
  courseId: '',
  usual: 0,
  mid: 0,
  finalScore: 0,
})

const rules = {
  sid: [{ required: true, message: '请选择学生', trigger: 'change' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
}

const studentOpinions = ref([])
const courseOpinions = ref([])

onMounted(async () => {
  try {
    const [stuRes, courseRes] = await Promise.all([
      request.get('/list', { params: { page: 1, size: 1000 } }),
      request.get('/course/list')
    ])
    if (stuRes.data.code === 0) {
      studentOpinions.value = stuRes.data.data.list || []
    }
    if (courseRes.data.code === 0) {
      courseOpinions.value = courseRes.data.data
    }
  } catch (e) {}
})

const handleEdit = (index, row) => {
  form.value = { ...row }
  dialogFormVisible.value = true
}

const editConfirm = async () => {
  const result = await request.put("/score", form.value)
  if (result.data.code === 0) {
    dialogFormVisible.value = false
    await getData()
    ElMessage.success("更新成功")
  } else {
    ElMessage.error("更新失败")
  }
}

const handleAdd = () => {
  form.value = { scoreId: '', sid: '', courseId: '', usual: 0, mid: 0, finalScore: 0 }
  addVisible.value = true
}

const addConfirm = async () => {
  const result = await request.post("/score", form.value)
  if (result.data.code === 0) {
    addVisible.value = false
    await getData()
    ElMessage.success("新增成功")
  } else {
    ElMessage.error("新增失败")
  }
}

const exportScore = () => {
  request.get('/score/export', { responseType: 'blob' }).then(res => {
    const blob = new Blob([res.data])
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = '成绩信息.xlsx'
    link.click()
  }).catch(() => {
    ElMessage.error('导出失败')
  })
}

const handleDelete = async (index, row) => {
  ElMessageBox.confirm('是否确认删除该成绩记录？', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    const result = await request.delete("/score/" + row.scoreId)
    if (result.data.code === 0) {
      await getData()
      ElMessage.success("删除成功")
    } else {
      ElMessage.error("删除失败")
    }
  }).catch(() => {})
}
</script>
<template>
  <div class="contain">
    <el-card class="glass-card" style="max-width: 1000px">
      <template #header>
        <div class="card-header">
          <div class="contain-search">
            <el-input v-model="searchKeyword" style="width: 200px" placeholder="搜索学生/课程/班级" clearable class="glass-input-sm"/>
            <el-button :icon="Search" @click="doSearch" circle class="btn-glass-icon"/>
          </div>
          <div class="contain-actions">
            <el-button :icon="Download" round @click="exportScore" class="btn-glass-outline">导出成绩</el-button>
            <el-button :icon="Plus" round @click="handleAdd" class="btn-neu-add">新增成绩</el-button>
          </div>
        </div>
      </template>
      <el-table :data="tableData" style="width: 100%" class="glass-table" :scroll-x="false">
        <el-table-column prop="scoreId" label="编号" width="70" />
        <el-table-column prop="studentName" label="学生姓名" width="110" />
        <el-table-column prop="clazzName" label="班级" width="120" />
        <el-table-column prop="courseName" label="课程" width="140" />
        <el-table-column prop="usual" label="平时" width="80" />
        <el-table-column prop="mid" label="期中" width="80" />
        <el-table-column prop="finalScore" label="期末" width="80" />
        <el-table-column prop="total" label="总评" width="80" />
        <el-table-column prop="gp" label="绩点" width="70" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="scope">
            <el-button size="small" class="btn-glass-edit" @click="handleEdit(scope.$index, scope.row)">编辑</el-button>
            <el-button size="small" class="btn-glass-danger" @click="handleDelete(scope.$index, scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogFormVisible" title="编辑成绩" width="500" class="glass-dialog">
      <el-form :model="form" :rules="rules" label-width="100px">
        <el-form-item label="学生" prop="sid">
          <el-select v-model="form.sid" placeholder="请选择学生" class="glass-select" disabled>
            <el-option v-for="stu in studentOpinions" :key="stu.sid" :label="stu.name" :value="stu.sid" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程" prop="courseId">
          <el-select v-model="form.courseId" placeholder="请选择课程" class="glass-select" disabled>
            <el-option v-for="c in courseOpinions" :key="c.courseId" :label="c.name" :value="c.courseId" />
          </el-select>
        </el-form-item>
        <el-form-item label="平时成绩">
          <el-input-number v-model="form.usual" :min="0" :max="100" :step="1" />
        </el-form-item>
        <el-form-item label="期中成绩">
          <el-input-number v-model="form.mid" :min="0" :max="100" :step="1" />
        </el-form-item>
        <el-form-item label="期末成绩">
          <el-input-number v-model="form.finalScore" :min="0" :max="100" :step="1" />
        </el-form-item>
        <el-form-item label="总评">
          <el-input v-model="computedTotal" disabled class="glass-input-sm" />
        </el-form-item>
        <el-form-item label="绩点">
          <el-input v-model="computedGp" disabled class="glass-input-sm" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogFormVisible = false" class="btn-glass-outline">取消</el-button>
        <el-button type="primary" @click="editConfirm" class="btn-glass-primary">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="addVisible" title="新增成绩" width="500" class="glass-dialog">
      <el-form :model="form" :rules="rules" label-width="100px">
        <el-form-item label="学生" prop="sid">
          <el-select v-model="form.sid" placeholder="请选择学生" class="glass-select">
            <el-option v-for="stu in studentOpinions" :key="stu.sid" :label="stu.name" :value="stu.sid" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程" prop="courseId">
          <el-select v-model="form.courseId" placeholder="请选择课程" class="glass-select">
            <el-option v-for="c in courseOpinions" :key="c.courseId" :label="c.name" :value="c.courseId" />
          </el-select>
        </el-form-item>
        <el-form-item label="平时成绩">
          <el-input-number v-model="form.usual" :min="0" :max="100" :step="1" />
        </el-form-item>
        <el-form-item label="期中成绩">
          <el-input-number v-model="form.mid" :min="0" :max="100" :step="1" />
        </el-form-item>
        <el-form-item label="期末成绩">
          <el-input-number v-model="form.finalScore" :min="0" :max="100" :step="1" />
        </el-form-item>
        <el-form-item label="总评">
          <el-input v-model="computedTotal" disabled class="glass-input-sm" />
        </el-form-item>
        <el-form-item label="绩点">
          <el-input v-model="computedGp" disabled class="glass-input-sm" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addVisible = false" class="btn-glass-outline">取消</el-button>
        <el-button type="primary" @click="addConfirm" class="btn-glass-primary">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<style scoped>
.contain {
  display: flex;
  justify-content: center;
  padding: 20px;
  width: 100%;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.contain-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}
.contain-search {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 玻璃卡片 */
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
:deep(.glass-card .el-card__body) {
  background: transparent;
}

:deep(.btn-glass-primary) {
  background: linear-gradient(135deg, var(--blue-400), var(--blue-500)) !important;
  border: none !important;
  border-radius: 10px !important;
  box-shadow: 0 4px 14px rgba(74, 144, 226, 0.25);
  transition: all 0.25s ease;
}
:deep(.btn-glass-primary:hover) {
  transform: translateY(-1px);
  box-shadow: 0 6px 18px rgba(74, 144, 226, 0.35);
}
:deep(.btn-glass-icon) {
  background: rgba(255, 255, 255, 0.5) !important;
  border: 1px solid var(--glass-border-light) !important;
  color: var(--primary) !important;
  transition: all 0.2s;
}
:deep(.btn-glass-icon:hover) {
  background: rgba(255, 255, 255, 0.7) !important;
  border-color: var(--blue-300) !important;
}
:deep(.btn-glass-outline) {
  background: rgba(255, 255, 255, 0.4) !important;
  border: 1px solid var(--glass-border) !important;
  border-radius: 8px !important;
  color: var(--text-secondary);
  transition: all 0.2s;
}
:deep(.btn-glass-outline:hover) {
  background: rgba(255, 255, 255, 0.6) !important;
  border-color: var(--blue-300) !important;
  color: var(--primary);
}
:deep(.btn-glass-edit) {
  background: rgba(74, 144, 226, 0.1) !important;
  border: 1px solid rgba(74, 144, 226, 0.25) !important;
  color: var(--primary) !important;
  border-radius: 6px !important;
  transition: all 0.2s;
}
:deep(.btn-glass-edit:hover) {
  background: rgba(74, 144, 226, 0.2) !important;
}
:deep(.btn-glass-danger) {
  background: rgba(245, 108, 108, 0.08) !important;
  border: 1px solid rgba(245, 108, 108, 0.2) !important;
  color: #f56c6c !important;
  border-radius: 6px !important;
  transition: all 0.2s;
}
:deep(.btn-glass-danger:hover) {
  background: rgba(245, 108, 108, 0.15) !important;
}
:deep(.glass-input-sm .el-input__wrapper),
:deep(.glass-select .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.5) !important;
  border: 1px solid var(--glass-border-light) !important;
  border-radius: 8px !important;
  box-shadow: none !important;
  transition: all 0.2s;
}
:deep(.glass-input-sm .el-input__wrapper:hover),
:deep(.glass-select .el-input__wrapper:hover) {
  border-color: var(--blue-300) !important;
}
:deep(.glass-input-sm .el-input__wrapper.is-focus),
:deep(.glass-select .el-input__wrapper.is-focus) {
  border-color: var(--primary) !important;
  box-shadow: 0 0 0 3px rgba(74, 144, 226, 0.1) !important;
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
:deep(.glass-table td.el-table__cell) {
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
:deep(.glass-dialog .el-dialog__header) {
  border-bottom: 1px solid var(--glass-border-light);
}
:deep(.glass-dialog .el-dialog__title) {
  color: var(--text-primary);
  font-weight: 600;
}
:deep(.glass-dialog .el-dialog__body) {
  color: var(--text-secondary);
}
:deep(.glass-dialog .el-dialog__footer) {
  border-top: 1px solid var(--glass-border-light);
}
</style>
