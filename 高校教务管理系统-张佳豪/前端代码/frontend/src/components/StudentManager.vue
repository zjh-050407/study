<script setup>
import request from "@/utils/request.js";
import { ref, onMounted } from "vue";
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Refresh, Download, Upload } from "@element-plus/icons-vue";

const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(5)
const total = ref(0)

const searchForm = ref({
  name: "",
  cname: "",
})

const getData = async () => {
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (searchForm.value && searchForm.value.name) {
      params.name = searchForm.value.name
    }
    if (searchForm.value && searchForm.value.cname) {
      params.cname = searchForm.value.cname
    }
    const result = await request.get('/list', { params })
    if (result.data.code === 0) {
      tableData.value = result.data.data.list
      total.value = result.data.data.total
    }
  } catch (err) {
    console.error('获取数据失败:', err)
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

const search = async () => {
  currentPage.value = 1
  await getData()
}

const resetSearch = async () => {
  searchForm.value = { name: "", cname: "" }
  await getData()
}

onMounted(() => {
  getData()
})

const dialogFormVisible = ref(false)
const addVisible = ref(false)
const formLabelWidth = '140px'
const form = ref({
  sid: '',
  name: '',
  age: '',
  sex: '',
  cid: '',
})

const clazzOpinions = ref([])
const sexOpinions = ["男", "女"]

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  age: [{ required: true, message: '请输入年龄', trigger: 'blur' }],
  sex: [{ required: true, message: '请选择性别', trigger: 'change' }],
  cid: [{ required: true, message: '请选择班级', trigger: 'change' }],
}

onMounted(async () => {
  try {
    const res = await request.get('/clazz/list')
    if (res.data.code === 0) {
      clazzOpinions.value = res.data.data
    }
  } catch (e) {}
})

const handleEdit = (index, row) => {
  form.value = { ...row }
  dialogFormVisible.value = true
}

const editConfirm = async () => {
  const result = await request.put("/stu2", form.value)
  if (result.data.code === 0) {
    dialogFormVisible.value = false
    await getData()
    ElMessage.success("更新成功")
  } else {
    ElMessage.error("更新失败")
  }
}

const add = () => {
  form.value = {
    sid: "",
    name: "",
    age: "",
    sex: "",
    cid: ""
  }
  addVisible.value = true
}

const addConfirm = async () => {
  const result = await request.post("/stu", form.value)
  if (result.data.code === 0) {
    addVisible.value = false
    await getData()
    ElMessage.success("新增成功")
  } else {
    ElMessage.error("新增失败")
  }
}

const downloadBlob = (url, filename) => {
  request.get(url, { responseType: 'blob' }).then(res => {
    const blob = new Blob([res.data])
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = filename
    link.click()
  }).catch(() => {
    ElMessage.error('下载失败')
  })
}

const downloadTemplate = () => {
  downloadBlob('/student/template', '学生导入模板.xlsx')
}

const exportStudent = () => {
  downloadBlob('/student/export', '学生信息.xlsx')
}

const handleUpload = async (options) => {
  const formData = new FormData()
  formData.append('file', options.file)
  try {
    const result = await request.post('/student/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (result.data.code === 0) {
      const data = result.data.data
      ElMessage.success(`导入成功 ${data.successCount} 条，失败 ${data.errorCount} 条`)
      if (data.errors && data.errors.length > 0) {
        console.warn('导入错误：', data.errors)
      }
      await getData()
    } else {
      ElMessage.error(result.data.message || '导入失败')
    }
  } catch (e) {
    ElMessage.error('导入请求失败')
  }
}

const handleDelete = async (index, row) => {
  ElMessageBox.confirm(
      '是否确认删除',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(async () => {
    const result = await request.delete("/stu?sid=" + row.sid)
    if (result.data.code === 0) {
      await getData()
      ElMessage.success("删除成功")
    } else {
      ElMessage.error("服务出错，删除失败")
    }
  }).catch(() => {
    ElMessage.info("撤销删除")
  })
}
</script>
<template>
  <div class="contain">
    <el-card class="glass-card" style="max-width: 850px">
      <template #header>
        <div class="card-header">
          <div class="contain-search">
            <el-input v-model="searchForm.name" style="width: 140px" placeholder="请输入姓名" clearable class="glass-input-sm"/>
            <el-input v-model="searchForm.cname" style="width: 140px" placeholder="请输入班级" clearable class="glass-input-sm"/>
            <el-button :icon="Search" @click="search" circle class="btn-glass-icon"/>
            <el-button :icon="Refresh" @click="resetSearch" circle class="btn-glass-icon" title="重置"/>
          </div>
          <div class="contain-actions">
            <el-upload
              :http-request="handleUpload"
              :show-file-list="false"
              accept=".xlsx,.xls"
              class="upload-inline"
            >
              <el-button :icon="Upload" round class="btn-glass-outline">导入</el-button>
            </el-upload>
            <el-button :icon="Download" round @click="downloadTemplate" class="btn-glass-outline">模板</el-button>
            <el-button :icon="Download" round @click="exportStudent" class="btn-glass-outline">导出</el-button>
            <el-button :icon="Plus" round @click="add" class="btn-neu-add">新增</el-button>
          </div>
        </div>
      </template>
      <el-table :data="tableData" style="width: 100%" class="glass-table">
        <el-table-column prop="sid" label="学号" width="80" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="age" label="年龄" width="80" />
        <el-table-column prop="sex" label="性别" width="80" />
        <el-table-column prop="clazzName" label="班级" width="120" />
        <el-table-column prop="deptName" label="院系" width="140" />
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="small" class="btn-glass-edit" @click="handleEdit(scope.$index, scope.row)">编辑</el-button>
            <el-button size="small" class="btn-glass-danger" @click="handleDelete(scope.$index, scope.row)">删除</el-button>
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
    <el-dialog v-model="dialogFormVisible" title="编辑学生" width="500" class="glass-dialog">
      <el-form :model="form">
        <el-form-item label="姓名" :label-width="formLabelWidth">
          <el-input v-model="form.name" autocomplete="off" class="glass-input-sm"/>
        </el-form-item>
        <el-form-item label="年龄" :label-width="formLabelWidth">
          <el-input-number v-model="form.age" :step="2" />
        </el-form-item>
        <el-form-item label="性别" :label-width="formLabelWidth">
          <el-select v-model="form.sex" placeholder="请选择性别" class="glass-select">
            <el-option v-for="sex in sexOpinions" :key="sex" :label="sex" :value="sex" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级" :label-width="formLabelWidth">
          <el-select v-model="form.cid" placeholder="请选择班级" class="glass-select">
            <el-option v-for="clazz in clazzOpinions"
                       :key="clazz.cid"
                       :label="clazz.name"
                       :value="clazz.cid" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogFormVisible = false" class="btn-glass-outline">取消</el-button>
          <el-button type="primary" @click="editConfirm" class="btn-glass-primary">确定</el-button>
        </div>
      </template>
    </el-dialog>
    <el-dialog v-model="addVisible" title="新增学生" width="500" class="glass-dialog">
      <el-form :model="form">
        <el-form-item label="姓名" :label-width="formLabelWidth">
          <el-input v-model="form.name" autocomplete="off" class="glass-input-sm"/>
        </el-form-item>
        <el-form-item label="年龄" :label-width="formLabelWidth">
          <el-input-number v-model="form.age" :step="2" />
        </el-form-item>
        <el-form-item label="性别" :label-width="formLabelWidth">
          <el-select v-model="form.sex" placeholder="请选择性别" class="glass-select">
            <el-option v-for="sex in sexOpinions" :key="sex" :label="sex" :value="sex" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级" :label-width="formLabelWidth">
          <el-select v-model="form.cid" placeholder="请选择班级" class="glass-select">
            <el-option v-for="clazz in clazzOpinions"
                       :key="clazz.cid"
                       :label="clazz.name"
                       :value="clazz.cid" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="addVisible = false" class="btn-glass-outline">取消</el-button>
          <el-button type="primary" @click="addConfirm" class="btn-glass-primary">确定</el-button>
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
.upload-inline {
  display: inline-block;
}

/* 玻璃按钮 */
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

/* 玻璃输入框 */
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

/* 玻璃表格 */
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

/* 分页 */
:deep(.glass-pagination .el-pagination__total,
.glass-pagination .el-pagination__jump) {
  color: var(--text-secondary);
}
:deep(.glass-pagination .el-pager li) {
  background: rgba(255, 255, 255, 0.4);
  border: 1px solid var(--glass-border-light);
  border-radius: 6px;
  color: var(--text-secondary);
  transition: all 0.2s;
}
:deep(.glass-pagination .el-pager li:hover) {
  background: rgba(255, 255, 255, 0.6);
  color: var(--primary);
}
:deep(.glass-pagination .el-pager li.is-active) {
  background: var(--primary);
  color: #fff;
  border-color: var(--primary);
}
:deep(.glass-pagination .btn-prev,
.glass-pagination .btn-next) {
  background: rgba(255, 255, 255, 0.4);
  border: 1px solid var(--glass-border-light);
  border-radius: 6px;
}
:deep(.glass-pagination .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.4) !important;
  border: 1px solid var(--glass-border-light) !important;
  border-radius: 6px !important;
  box-shadow: none !important;
}

/* 对话框 */
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
  padding: 20px 24px;
}
:deep(.glass-dialog .el-dialog__title) {
  color: var(--text-primary);
  font-weight: 600;
}
:deep(.glass-dialog .el-dialog__body) {
  color: var(--text-secondary);
  padding: 24px;
}
:deep(.glass-dialog .el-dialog__footer) {
  border-top: 1px solid var(--glass-border-light);
  padding: 16px 24px;
}
</style>
