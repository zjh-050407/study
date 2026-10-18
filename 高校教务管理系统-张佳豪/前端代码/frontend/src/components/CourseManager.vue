<script setup>
import request from "@/utils/request.js";
import { ref } from "vue";
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from "@element-plus/icons-vue";

const tableData = ref([])

const getData = async () => {
  try {
    const result = await request.get('/course/list')
    if (result.data.code === 0) {
      tableData.value = result.data.data
    }
  } catch (err) {
    console.error('获取数据失败:', err)
  }
}
getData()

const dialogFormVisible = ref(false)
const addVisible = ref(false)
const formLabelWidth = '100px'

const form = ref({
  courseId: '',
  name: '',
  credit: 3.0,
  hours: 48,
})

const rules = {
  name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  credit: [{ required: true, message: '请输入学分', trigger: 'blur' }],
  hours: [{ required: true, message: '请输入学时', trigger: 'blur' }],
}

const handleEdit = (index, row) => {
  form.value = { ...row }
  dialogFormVisible.value = true
}

const editConfirm = async () => {
  const result = await request.put("/course", form.value)
  if (result.data.code === 0) {
    dialogFormVisible.value = false
    await getData()
    ElMessage.success("更新成功")
  } else {
    ElMessage.error("更新失败")
  }
}

const handleAdd = () => {
  form.value = { courseId: '', name: '', credit: 3.0, hours: 48 }
  addVisible.value = true
}

const addConfirm = async () => {
  const result = await request.post("/course", form.value)
  if (result.data.code === 0) {
    addVisible.value = false
    await getData()
    ElMessage.success("新增成功")
  } else {
    ElMessage.error("新增失败")
  }
}

const handleDelete = async (index, row) => {
  ElMessageBox.confirm('是否确认删除该课程？', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    const result = await request.delete("/course/" + row.courseId)
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
    <el-card class="glass-card" style="max-width: 700px">
      <template #header>
        <div class="card-header">
          <span class="card-title">课程管理</span>
          <el-button :icon="Plus" round @click="handleAdd" class="btn-neu-add">新增课程</el-button>
        </div>
      </template>
      <el-table :data="tableData" style="width: 100%" class="glass-table">
        <el-table-column prop="courseId" label="编号" width="80" />
        <el-table-column prop="name" label="课程名称" width="180" />
        <el-table-column prop="credit" label="学分" width="100" />
        <el-table-column prop="hours" label="学时" width="100" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" class="btn-glass-edit" @click="handleEdit(scope.$index, scope.row)">编辑</el-button>
            <el-button size="small" class="btn-glass-danger" @click="handleDelete(scope.$index, scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogFormVisible" title="编辑课程" width="500" class="glass-dialog">
      <el-form :model="form" :rules="rules" label-width="100px">
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="form.name" class="glass-input-sm"/>
        </el-form-item>
        <el-form-item label="学分" prop="credit">
          <el-input-number v-model="form.credit" :min="0.5" :max="10" :step="0.5" />
        </el-form-item>
        <el-form-item label="学时" prop="hours">
          <el-input-number v-model="form.hours" :min="8" :max="160" :step="8" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogFormVisible = false" class="btn-glass-outline">取消</el-button>
        <el-button type="primary" @click="editConfirm" class="btn-glass-primary">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="addVisible" title="新增课程" width="500" class="glass-dialog">
      <el-form :model="form" :rules="rules" label-width="100px">
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="form.name" class="glass-input-sm"/>
        </el-form-item>
        <el-form-item label="学分" prop="credit">
          <el-input-number v-model="form.credit" :min="0.5" :max="10" :step="0.5" />
        </el-form-item>
        <el-form-item label="学时" prop="hours">
          <el-input-number v-model="form.hours" :min="8" :max="160" :step="8" />
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
