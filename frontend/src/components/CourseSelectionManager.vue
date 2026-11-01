<script setup>
import request from "@/utils/request.js";
import { ref, onMounted } from "vue";
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from "@element-plus/icons-vue";

const role = ref('')
const canDelete = () => role.value === 'professor' || role.value === 'admin'

/* 左侧：学生列表 */
const studentList = ref([])
const studentLoading = ref(false)
const stuPage = ref(1)
const stuSize = ref(8)
const stuTotal = ref(0)
const stuName = ref('')
const selectedStudent = ref(null)

const getStudents = async () => {
  studentLoading.value = true
  try {
    const params = { page: stuPage.value, size: stuSize.value }
    if (stuName.value) params.name = stuName.value
    const result = await request.get('/list', { params })
    if (result.data.code === 0) {
      studentList.value = result.data.data.list
      stuTotal.value = result.data.data.total
    }
  } catch (err) {
    console.error('获取学生失败:', err)
  } finally {
    studentLoading.value = false
  }
}

const searchStu = () => {
  stuPage.value = 1
  getStudents()
}

const resetStu = () => {
  stuName.value = ''
  stuPage.value = 1
  getStudents()
}

const handleStuPageChange = (page) => {
  stuPage.value = page
  getStudents()
}

const handleStuSizeChange = (size) => {
  stuSize.value = size
  stuPage.value = 1
  getStudents()
}

const selectStudent = (row) => {
  selectedStudent.value = row
  getSelections()
}

/* 右侧：课程列表 + 已选课程 */
const courseList = ref([])
const courseLoading = ref(false)
const selections = ref([])
const selectionLoading = ref(false)

const getCourses = async () => {
  courseLoading.value = true
  try {
    const result = await request.get('/course/list')
    if (result.data.code === 0) {
      courseList.value = result.data.data
    }
  } catch (err) {
    console.error('获取课程失败:', err)
  } finally {
    courseLoading.value = false
  }
}

const getSelections = async () => {
  if (!selectedStudent.value) return
  selectionLoading.value = true
  try {
    const result = await request.get('/selection/listByStudent', {
      params: { sid: selectedStudent.value.sid }
    })
    if (result.data.code === 0) {
      selections.value = result.data.data
    }
  } catch (err) {
    console.error('获取选课失败:', err)
  } finally {
    selectionLoading.value = false
  }
}

const addSelection = async (course) => {
  if (!selectedStudent.value) {
    ElMessage.warning('请先在左侧选择学生')
    return
  }
  const result = await request.post('/selection', {
    sid: selectedStudent.value.sid,
    courseId: course.courseId
  })
  if (result.data.code === 0) {
    ElMessage.success(`已为 ${selectedStudent.value.name} 选课：${course.name}`)
    await getSelections()
  } else {
    ElMessage.error(result.data.message || '选课失败')
  }
}

const removeSelection = (row) => {
  ElMessageBox.confirm(`是否确认退选课程「${row.courseName}」？`, {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    const result = await request.delete('/selection/' + row.id)
    if (result.data.code === 0) {
      await getSelections()
      ElMessage.success('退选成功')
    } else {
      ElMessage.error(result.data.message || '退选失败')
    }
  }).catch(() => {})
}

const isSelected = (courseId) => selections.value.some(s => s.courseId === courseId)

/* 退选审批 */
const activeTab = ref('select')
const applies = ref([])
const appliesLoading = ref(false)

const getApplies = async () => {
  appliesLoading.value = true
  try {
    const result = await request.get('/selectionApply/pending')
    if (result.data.code === 0) {
      applies.value = result.data.data
    }
  } catch (err) {
    console.error('获取退选申请失败:', err)
  } finally {
    appliesLoading.value = false
  }
}

const approveApply = async (row) => {
  const result = await request.put('/selectionApply/approve/' + row.id)
  if (result.data.code === 0) {
    ElMessage.success('已通过：选课记录已删除')
    await getApplies()
  } else {
    ElMessage.error(result.data.message || '操作失败')
  }
}

const rejectApply = async (row) => {
  const result = await request.put('/selectionApply/reject/' + row.id)
  if (result.data.code === 0) {
    ElMessage.success('已拒绝该退选申请')
    await getApplies()
  } else {
    ElMessage.error(result.data.message || '操作失败')
  }
}

const handleTabChange = (name) => {
  if (name === 'apply') {
    getApplies()
  }
}

onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    try {
      role.value = JSON.parse(userStr).role || ''
    } catch (e) {}
  }
  getStudents()
  getCourses()
})
</script>
<template>
  <div class="manager-page">
    <el-alert class="self-tip" type="info" :closable="false" show-icon
              title="学生自助选课入口已上线：学生可自行选课并提交退选申请，请在「退选审批」页签中审核。" />
    <el-tabs v-model="activeTab" class="glass-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="选课管理" name="select">
        <div class="selection-layout">
    <el-card class="glass-card stu-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">学生</span>
          <div class="contain-search">
            <el-input v-model="stuName" style="width: 110px" placeholder="姓名" clearable class="glass-input-sm" />
            <el-button :icon="Search" @click="searchStu" circle class="btn-glass-icon" />
            <el-button :icon="Refresh" @click="resetStu" circle class="btn-glass-icon" title="重置" />
          </div>
        </div>
      </template>
      <div v-loading="studentLoading" class="stu-list">
        <div v-if="studentList.length === 0" class="list-empty">暂无学生</div>
        <div
            v-for="stu in studentList"
            :key="stu.sid"
            class="stu-item"
            :class="{ active: selectedStudent && selectedStudent.sid === stu.sid }"
            @click="selectStudent(stu)"
        >
          <span class="stu-name">{{ stu.name }}</span>
          <span class="stu-sid">{{ stu.sid }}</span>
          <span class="stu-clazz">{{ stu.clazzName || '-' }}</span>
        </div>
      </div>
      <div style="display: flex; justify-content: center; margin-top: 12px;">
        <el-pagination
            v-model:current-page="stuPage"
            v-model:page-size="stuSize"
            :page-sizes="[8, 16, 32]"
            :total="stuTotal"
            layout="total, prev, pager, next"
            small
            @size-change="handleStuSizeChange"
            @current-change="handleStuPageChange"
            class="glass-pagination"
        />
      </div>
    </el-card>

    <el-card class="glass-card course-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">可选课程</span>
          <span class="card-sub">为「{{ selectedStudent ? selectedStudent.name : '未选择学生' }}」选课</span>
        </div>
      </template>
      <el-table :data="courseList" class="glass-table" v-loading="courseLoading" empty-text="暂无课程">
        <el-table-column prop="name" label="课程名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="credit" label="学分" width="70" />
        <el-table-column prop="hours" label="学时" width="70" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button
                size="small"
                class="btn-glass-edit"
                :disabled="isSelected(scope.row.courseId)"
                @click="addSelection(scope.row)"
            >{{ isSelected(scope.row.courseId) ? '已选' : '选课' }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="selected-title">已选课程</div>
      <el-table :data="selections" class="glass-table" v-loading="selectionLoading" empty-text="该学生尚未选课">
        <el-table-column prop="courseName" label="课程名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="credit" label="学分" width="70" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button
                v-if="canDelete()"
                size="small"
                class="btn-glass-danger"
                @click="removeSelection(scope.row)"
            >退选</el-button>
            <span v-else class="no-op">-</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
        </div>
      </el-tab-pane>

      <el-tab-pane label="退选审批" name="apply">
        <el-card class="glass-card apply-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">退选申请（待审批）</span>
            </div>
          </template>
          <el-table :data="applies" class="glass-table" v-loading="appliesLoading" empty-text="暂无待审批的退选申请">
            <el-table-column prop="id" label="单号" width="70" align="center" />
            <el-table-column prop="studentName" label="学生" width="100" />
            <el-table-column prop="clazzName" label="班级" width="110" />
            <el-table-column prop="courseName" label="课程" min-width="140" show-overflow-tooltip />
            <el-table-column prop="credit" label="学分" width="70" align="center" />
            <el-table-column prop="reason" label="退选原因" min-width="160" show-overflow-tooltip>
              <template #default="scope">{{ scope.row.reason || '-' }}</template>
            </el-table-column>
            <el-table-column label="申请时间" width="170" align="center">
              <template #default="scope">{{ scope.row.applyTime ? String(scope.row.applyTime).replace('T', ' ').slice(0, 19) : '-' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right" align="center">
              <template #default="scope">
                <el-button size="small" class="btn-glass-edit" @click="approveApply(scope.row)">通过</el-button>
                <el-button size="small" class="btn-glass-danger" @click="rejectApply(scope.row)">拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
<style scoped>
.manager-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  width: 100%;
  max-width: 1160px;
  margin: 0 auto;
}
.manager-page .el-tabs {
  width: 100%;
}
.self-tip {
  width: 100%;
  border-radius: 10px;
}
.apply-card {
  width: 100%;
}
:deep(.glass-tabs .el-tabs__item.is-active) {
  color: var(--primary);
}
:deep(.glass-tabs .el-tabs__active-bar) {
  background-color: var(--primary);
}
.selection-layout {
  display: flex;
  gap: 16px;
  justify-content: center;
  align-items: flex-start;
  width: 100%;
  max-width: 1100px;
}
.stu-card {
  width: 360px;
  flex-shrink: 0;
}
.course-card {
  flex: 1;
  min-width: 0;
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
.card-sub {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--text-muted);
}
.contain-search {
  display: flex;
  align-items: center;
  gap: 6px;
}
.stu-list {
  min-height: 240px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.stu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 12px;
  background: var(--bg-gradient-start);
  border: 1px solid transparent;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.stu-item:hover {
  border-color: var(--blue-300);
}
.stu-item.active {
  background: var(--blue-50);
  border-color: var(--primary);
}
.stu-name {
  font-weight: 600;
  color: var(--text-primary);
}
.stu-sid {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--blue-600);
}
.stu-clazz {
  margin-left: auto;
  font-size: 12px;
  color: var(--text-muted);
}
.list-empty {
  text-align: center;
  color: var(--text-muted);
  padding: 60px 0;
}
.selected-title {
  font-family: var(--font-serif);
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 18px 0 10px;
}
.no-op {
  color: var(--text-muted);
}
:deep(.glass-input-sm .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.5) !important;
  border: 1px solid var(--glass-border-light) !important;
  border-radius: 8px !important;
  box-shadow: none !important;
}
:deep(.btn-glass-icon) {
  background: rgba(255, 255, 255, 0.5) !important;
  border: 1px solid var(--glass-border-light) !important;
  color: var(--primary) !important;
}
:deep(.btn-glass-edit) {
  background: rgba(74, 144, 226, 0.1) !important;
  border: 1px solid rgba(74, 144, 226, 0.25) !important;
  color: var(--primary) !important;
  border-radius: 6px !important;
}
:deep(.btn-glass-edit.is-disabled) {
  background: rgba(0, 0, 0, 0.03) !important;
  color: var(--text-muted) !important;
  border-color: var(--glass-border-light) !important;
}
:deep(.btn-glass-danger) {
  background: rgba(245, 108, 108, 0.08) !important;
  border: 1px solid rgba(245, 108, 108, 0.2) !important;
  color: #f56c6c !important;
  border-radius: 6px !important;
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
</style>