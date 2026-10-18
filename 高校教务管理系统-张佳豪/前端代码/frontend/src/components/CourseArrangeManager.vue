<script setup>
import request from "@/utils/request.js";
import { ref, onMounted } from "vue";
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from "@element-plus/icons-vue";

const tableData = ref([])
const courseList = ref([])
const teacherList = ref([])

const getData = async () => {
  try {
    const [arrangeRes, courseRes, teacherRes] = await Promise.all([
      request.get('/courseArrange/list'),
      request.get('/course/list'),
      request.get('/teacher/list')
    ])
    if (arrangeRes.data.code === 0) {
      tableData.value = arrangeRes.data.data
    }
    if (courseRes.data.code === 0) {
      courseList.value = courseRes.data.data
    }
    if (teacherRes.data.code === 0) {
      teacherList.value = teacherRes.data.data
    }
  } catch (err) {
    console.error('获取数据失败:', err)
  }
}
onMounted(getData)

const addVisible = ref(false)
const editVisible = ref(false)

const form = ref({
  courseId: '',
  tid: '',
  classroom: '',
  weekDay: 1,
  section: 1,
})

const editForm = ref({
  arrangeId: '',
  courseId: '',
  tid: '',
  classroom: '',
  weekDay: 1,
  section: 1,
})

const weekDayOptions = [
  { label: '周一', value: 1 },
  { label: '周二', value: 2 },
  { label: '周三', value: 3 },
  { label: '周四', value: 4 },
  { label: '周五', value: 5 },
  { label: '周六', value: 6 },
  { label: '周日', value: 7 },
]

const sectionOptions = [
  { label: '第1节', value: 1 },
  { label: '第2节', value: 2 },
  { label: '第3节', value: 3 },
  { label: '第4节', value: 4 },
  { label: '第5节', value: 5 },
]

const handleAdd = () => {
  form.value = { courseId: '', tid: '', classroom: '', weekDay: 1, section: 1 }
  addVisible.value = true
}

const addConfirm = async () => {
  if (!form.value.courseId || !form.value.tid || !form.value.classroom) {
    ElMessage.warning('请填写完整信息')
    return
  }
  const result = await request.post("/courseArrange", form.value)
  if (result.data.code === 0) {
    addVisible.value = false
    await getData()
    ElMessage.success("新增成功")
  } else {
    ElMessage.error(result.data.message || "新增失败")
  }
}

const handleEdit = (index, row) => {
  editForm.value = {
    arrangeId: row.arrangeId,
    courseId: row.courseId,
    tid: row.tid,
    classroom: row.classroom,
    weekDay: row.weekDay,
    section: row.section,
  }
  editVisible.value = true
}

const editConfirm = async () => {
  if (!editForm.value.courseId || !editForm.value.tid || !editForm.value.classroom) {
    ElMessage.warning('请填写完整信息')
    return
  }
  const result = await request.put("/courseArrange", editForm.value)
  if (result.data.code === 0) {
    editVisible.value = false
    await getData()
    ElMessage.success("更新成功")
  } else {
    ElMessage.error(result.data.message || "更新失败")
  }
}

const handleDelete = async (index, row) => {
  ElMessageBox.confirm('是否确认删除该排课记录？', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    const result = await request.delete("/courseArrange/" + row.arrangeId)
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
    <el-card class="glass-card" style="max-width: 800px">
      <template #header>
        <div class="card-header">
          <span class="card-title">排课管理</span>
          <el-button :icon="Plus" round @click="handleAdd" class="btn-neu-add">新增排课</el-button>
        </div>
      </template>
      <el-table :data="tableData" style="width: 100%" class="glass-table">
        <el-table-column prop="arrangeId" label="编号" width="70" />
        <el-table-column prop="courseName" label="课程名称" width="150" />
        <el-table-column prop="teacherName" label="授课教师" width="120" />
        <el-table-column prop="classroom" label="教室" width="100" />
        <el-table-column prop="weekDay" label="星期" width="80">
          <template #default="scope">
            {{ weekDayOptions.find(o => o.value === scope.row.weekDay)?.label || scope.row.weekDay }}
          </template>
        </el-table-column>
        <el-table-column prop="section" label="节次" width="80">
          <template #default="scope">
            {{ sectionOptions.find(o => o.value === scope.row.section)?.label || scope.row.section }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="small" class="btn-glass-edit" @click="handleEdit(scope.$index, scope.row)">编辑</el-button>
            <el-button size="small" class="btn-glass-danger" @click="handleDelete(scope.$index, scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="addVisible" title="新增排课" width="520" class="glass-dialog">
      <el-form :model="form" label-width="100px">
        <el-form-item label="课程" required>
          <el-select v-model="form.courseId" placeholder="请选择课程" class="glass-select" style="width: 100%">
            <el-option
              v-for="item in courseList"
              :key="item.courseId"
              :label="item.name"
              :value="item.courseId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="授课教师" required>
          <el-select v-model="form.tid" placeholder="请选择教师" class="glass-select" style="width: 100%">
            <el-option
              v-for="item in teacherList"
              :key="item.tid"
              :label="item.name"
              :value="item.tid"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="教室" required>
          <el-input v-model="form.classroom" placeholder="请输入教室" class="glass-input-sm"/>
        </el-form-item>
        <el-form-item label="星期" required>
          <el-select v-model="form.weekDay" placeholder="请选择星期" class="glass-select" style="width: 100%">
            <el-option
              v-for="item in weekDayOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="节次" required>
          <el-select v-model="form.section" placeholder="请选择节次" class="glass-select" style="width: 100%">
            <el-option
              v-for="item in sectionOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addVisible = false" class="btn-glass-outline">取消</el-button>
        <el-button type="primary" @click="addConfirm" class="btn-glass-primary">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editVisible" title="编辑排课" width="520" class="glass-dialog">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="课程" required>
          <el-select v-model="editForm.courseId" placeholder="请选择课程" class="glass-select" style="width: 100%">
            <el-option
              v-for="item in courseList"
              :key="item.courseId"
              :label="item.name"
              :value="item.courseId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="授课教师" required>
          <el-select v-model="editForm.tid" placeholder="请选择教师" class="glass-select" style="width: 100%">
            <el-option
              v-for="item in teacherList"
              :key="item.tid"
              :label="item.name"
              :value="item.tid"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="教室" required>
          <el-input v-model="editForm.classroom" placeholder="请输入教室" class="glass-input-sm"/>
        </el-form-item>
        <el-form-item label="星期" required>
          <el-select v-model="editForm.weekDay" placeholder="请选择星期" class="glass-select" style="width: 100%">
            <el-option
              v-for="item in weekDayOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="节次" required>
          <el-select v-model="editForm.section" placeholder="请选择节次" class="glass-select" style="width: 100%">
            <el-option
              v-for="item in sectionOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false" class="btn-glass-outline">取消</el-button>
        <el-button type="primary" @click="editConfirm" class="btn-glass-primary">确定</el-button>
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
.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
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
:deep(.glass-input-sm .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.5) !important;
  border: 1px solid var(--glass-border-light) !important;
  border-radius: 8px !important;
  box-shadow: none !important;
  transition: all 0.2s;
}
:deep(.glass-input-sm .el-input__wrapper:hover) {
  border-color: var(--blue-300) !important;
}
:deep(.glass-input-sm .el-input__wrapper.is-focus) {
  border-color: var(--primary) !important;
  box-shadow: 0 0 0 3px rgba(74, 144, 226, 0.1) !important;
}
:deep(.glass-select .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.5) !important;
  border: 1px solid var(--glass-border-light) !important;
  border-radius: 8px !important;
  box-shadow: none !important;
}
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
