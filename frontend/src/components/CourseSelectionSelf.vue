<script setup>
import request from "@/utils/request.js";
import { ref, onMounted, computed } from "vue";
import { ElMessage } from 'element-plus'

const activeTab = ref('courses')
const courseList = ref([])
const mySelections = ref([])
const myApplies = ref([])
const loading = ref(false)

// 批次O：选课轮次
const roundInfo = ref({ first: null, second: null })
const wishList = ref([])
const wishPick = ref([])   // 正在挑选的志愿课程（有序）

const firstRound = computed(() => roundInfo.value.first)
const secondRound = computed(() => roundInfo.value.second)

const applyDialogVisible = ref(false)
const applyCourse = ref(null)
const applyReason = ref('')

const getCourses = async () => {
  loading.value = true
  try {
    const result = await request.get('/selection/self/courses')
    if (result.data.code === 0) {
      roundInfo.value = result.data.data.round || { first: null, second: null }
      courseList.value = result.data.data.list || []
    }
  } catch (err) {
    console.error('获取课程失败:', err)
  } finally {
    loading.value = false
  }
}

const getMySelections = async () => {
  const result = await request.get('/selection/self/list')
  if (result.data.code === 0) {
    mySelections.value = result.data.data || []
  }
}

const getMyApplies = async () => {
  const result = await request.get('/selectionApply/my')
  if (result.data.code === 0) {
    myApplies.value = result.data.data || []
  }
}

const getMyWishes = async () => {
  if (!firstRound.value) {
    wishList.value = []
    return
  }
  try {
    const result = await request.get('/selection/wish/my', { params: { roundId: firstRound.value.id } })
    if (result.data.code === 0) {
      wishList.value = result.data.data || []
      // 同步已提交志愿到挑选区
      wishPick.value = wishList.value.map(w => courseList.value.find(c => c.courseId === w.courseId)).filter(Boolean)
    }
  } catch (e) {}
}

const refreshAll = async () => {
  await Promise.all([getCourses(), getMySelections(), getMyApplies()])
  await getMyWishes()
}

/* ---- 补退选：即时选课 ---- */
const selectCourse = async (course) => {
  const result = await request.post('/selection/self', { courseId: course.courseId })
  if (result.data.code === 0) {
    ElMessage.success(`选课成功：${course.name}`)
    await refreshAll()
  } else {
    ElMessage.error(result.data.message || '选课失败')
  }
}

/* ---- 初选：志愿挑选与提交 ---- */
const maxWishes = computed(() => {
  const m = firstRound.value?.maxWishes
  return m || 3
})

const toggleWish = (course) => {
  const idx = wishPick.value.findIndex(c => c.courseId === course.courseId)
  if (idx >= 0) {
    wishPick.value.splice(idx, 1)
  } else {
    if (wishPick.value.length >= maxWishes.value) {
      ElMessage.warning(`志愿最多 ${maxWishes.value} 门，请先移除其他志愿`)
      return
    }
    wishPick.value.push(course)
  }
}

const wishIndex = (courseId) => {
  const idx = wishPick.value.findIndex(c => c.courseId === courseId)
  return idx >= 0 ? idx + 1 : 0
}

const moveWish = (idx, dir) => {
  const target = idx + dir
  if (target < 0 || target >= wishPick.value.length) return
  const arr = wishPick.value
  const tmp = arr[idx]
  arr[idx] = arr[target]
  arr[target] = tmp
}

const submitWishes = async () => {
  if (wishPick.value.length === 0) {
    ElMessage.warning('请先挑选志愿课程')
    return
  }
  const result = await request.post('/selection/wish', {
    roundId: firstRound.value.id,
    courseIds: wishPick.value.map(c => c.courseId),
  })
  if (result.data.code === 0) {
    ElMessage.success('志愿已提交，轮次结束后统一投档')
    await getMyWishes()
  } else {
    ElMessage.error(result.data.message || '提交失败')
  }
}

const openApply = (row) => {
  applyCourse.value = row
  applyReason.value = ''
  applyDialogVisible.value = true
}

const submitApply = async () => {
  const result = await request.post('/selectionApply', {
    courseId: applyCourse.value.courseId,
    reason: applyReason.value
  })
  if (result.data.code === 0) {
    applyDialogVisible.value = false
    ElMessage.success('退选申请已提交，等待教师审批')
    await getMyApplies()
  } else {
    ElMessage.error(result.data.message || '提交失败')
  }
}

const statusMap = {
  0: { text: '待审批', type: 'warning' },
  1: { text: '已通过', type: 'success' },
  2: { text: '已拒绝', type: 'danger' },
}

const isApplied = (courseId) => myApplies.value.some(a => a.courseId === courseId && a.status === 0)

const isFull = (course) => {
  const capacity = Number(course.capacity || 0)
  const chosen = Number(course.chosenCount || 0)
  return capacity > 0 && chosen >= capacity
}

const remainOf = (course) => {
  const capacity = Number(course.capacity || 0)
  const chosen = Number(course.chosenCount || 0)
  return `${capacity - chosen} / ${capacity}`
}

onMounted(refreshAll)
</script>
<template>
  <div class="selection-page">
    <el-card class="glass-card" shadow="never">
      <!-- 批次O：轮次状态条 -->
      <div class="round-banner">
        <template v-if="firstRound">
          <el-alert type="primary" :closable="false" show-icon
                    :title="`「${firstRound.name}」进行中：请挑选最多 ${maxWishes} 门志愿课程并提交，轮次结束后统一投档`" />
        </template>
        <template v-else-if="secondRound">
          <el-alert type="success" :closable="false" show-icon
                    :title="`「${secondRound.name}」进行中：先到先得，可直接选课`" />
        </template>
        <template v-else>
          <el-alert type="warning" :closable="false" show-icon
                    title="当前不在选课时间（无进行中的选课轮次），无法自助选课" />
        </template>
      </div>

      <!-- 初选：志愿挑选区 -->
      <div v-if="firstRound" class="wish-bar">
        <div class="wish-pick">
          <span class="wish-label">我的志愿（按优先级排序）：</span>
          <el-tag v-for="(c, i) in wishPick" :key="c.courseId" class="wish-tag" type="warning" closable
                  @close="toggleWish(c)">
            {{ i + 1 }}. {{ c.name }}
            <el-button link size="small" @click.stop="moveWish(i, -1)" :disabled="i === 0">↑</el-button>
            <el-button link size="small" @click.stop="moveWish(i, 1)" :disabled="i === wishPick.length - 1">↓</el-button>
          </el-tag>
          <span v-if="wishPick.length === 0" class="wish-empty">尚未挑选志愿</span>
        </div>
        <el-button type="primary" size="small" @click="submitWishes">提交志愿</el-button>
      </div>

      <el-tabs v-model="activeTab" class="glass-tabs">
        <el-tab-pane label="全部课程" name="courses">
          <el-table :data="courseList" class="glass-table" v-loading="loading" empty-text="暂无课程">
            <el-table-column prop="name" label="课程名称" min-width="160" show-overflow-tooltip />
            <el-table-column label="学分" width="70" align="center">
              <template #default="scope">{{ scope.row.credit }}</template>
            </el-table-column>
            <el-table-column label="学时" width="70" align="center">
              <template #default="scope">{{ scope.row.hours }}</template>
            </el-table-column>
            <el-table-column label="余量" width="100" align="center">
              <template #default="scope">
                <el-tag v-if="isFull(scope.row)" type="danger" size="small">已满</el-tag>
                <span v-else class="quota-text">{{ remainOf(scope.row) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="任课教师" min-width="120" align="center">
              <template #default="scope">{{ scope.row.teacherNames || '-' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="140" fixed="right" align="center">
              <template #default="scope">
                <!-- 初选：挑选志愿 -->
                <el-button v-if="firstRound" size="small" class="btn-glass-edit"
                           :disabled="Number(scope.row.selected) > 0"
                           @click="toggleWish(scope.row)">
                  {{ wishIndex(scope.row.courseId) > 0 ? `志愿${wishIndex(scope.row.courseId)}` : '加志愿' }}
                </el-button>
                <!-- 补退选：即时抢课 -->
                <el-button v-else size="small" class="btn-glass-edit"
                           :disabled="!secondRound || Number(scope.row.selected) > 0 || isFull(scope.row)"
                           @click="selectCourse(scope.row)">
                  {{ Number(scope.row.selected) > 0 ? '已选' : (isFull(scope.row) ? '已满' : '选课') }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane v-if="firstRound" label="我的志愿" name="wishes">
          <el-table :data="wishList" class="glass-table" empty-text="尚未提交志愿">
            <el-table-column prop="priority" label="志愿序" width="80" align="center">
              <template #default="scope">第 {{ scope.row.priority }} 志愿</template>
            </el-table-column>
            <el-table-column prop="courseName" label="课程" min-width="160" show-overflow-tooltip />
            <el-table-column label="已选/容量" width="130" align="center">
              <template #default="scope">{{ scope.row.selectedCount }} / {{ scope.row.capacity }}</template>
            </el-table-column>
            <el-table-column prop="createTime" label="提交时间" width="170" align="center">
              <template #default="scope">{{ scope.row.createTime ? String(scope.row.createTime).replace('T', ' ').slice(0, 19) : '-' }}</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="我的课程" name="mine">
          <el-alert class="tip-alert" type="info" :closable="false" show-icon
                    title="退选需提交申请，经教师审批通过后生效" />
          <el-table :data="mySelections" class="glass-table" empty-text="你还没有选课">
            <el-table-column prop="courseName" label="课程名称" min-width="160" show-overflow-tooltip />
            <el-table-column label="学分" width="70" align="center">
              <template #default="scope">{{ scope.row.credit ?? '-' }}</template>
            </el-table-column>
            <el-table-column label="学期" width="160" align="center">
              <template #default="scope">{{ scope.row.semesterName || '未分学期' }}</template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right" align="center">
              <template #default="scope">
                <el-button
                    size="small"
                    class="btn-glass-danger"
                    :disabled="isApplied(scope.row.courseId)"
                    @click="openApply(scope.row)"
                >{{ isApplied(scope.row.courseId) ? '审批中' : '退选申请' }}</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="退选申请记录" name="applies">
          <el-table :data="myApplies" class="glass-table" empty-text="暂无退选申请">
            <el-table-column prop="id" label="单号" width="70" align="center" />
            <el-table-column prop="courseName" label="课程" min-width="140" show-overflow-tooltip />
            <el-table-column prop="reason" label="退选原因" min-width="180" show-overflow-tooltip>
              <template #default="scope">{{ scope.row.reason || '-' }}</template>
            </el-table-column>
            <el-table-column label="申请时间" width="170" align="center">
              <template #default="scope">{{ scope.row.applyTime ? String(scope.row.applyTime).replace('T', ' ').slice(0, 19) : '-' }}</template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="scope">
                <el-tag :type="statusMap[scope.row.status]?.type" size="small" effect="light">
                  {{ statusMap[scope.row.status]?.text }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="applyDialogVisible" title="提交退选申请" width="460" class="glass-dialog">
      <el-form label-width="90px">
        <el-form-item label="课程">
          <el-input :model-value="applyCourse?.courseName" disabled class="glass-input-sm" />
        </el-form-item>
        <el-form-item label="退选原因">
          <el-input v-model="applyReason" type="textarea" :rows="3" maxlength="200" show-word-limit
                    placeholder="请填写退选原因（选填）" class="glass-input-sm" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyDialogVisible = false" class="btn-glass-outline">取消</el-button>
        <el-button type="primary" @click="submitApply" class="btn-glass-primary">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<style scoped>
.selection-page {
  display: flex;
  justify-content: center;
  width: 100%;
}
.selection-page .glass-card {
  width: 100%;
  max-width: 900px;
}
.glass-card {
  background: var(--glass-bg-strong);
  backdrop-filter: var(--blur-md);
  -webkit-backdrop-filter: var(--blur-md);
  border: 1px solid var(--glass-border);
  border-radius: 16px;
  box-shadow: var(--glass-shadow);
}
:deep(.glass-tabs .el-tabs__item.is-active) {
  color: var(--primary);
}
:deep(.glass-tabs .el-tabs__active-bar) {
  background-color: var(--primary);
}
.tip-alert {
  margin-bottom: 12px;
}
.round-banner {
  margin-bottom: 12px;
}
.wish-bar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  border: 1px dashed var(--blue-300);
  border-radius: 10px;
  padding: 10px 14px;
  margin-bottom: 12px;
  background: rgba(74, 144, 226, 0.05);
  flex-wrap: wrap;
}
.wish-pick {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  flex: 1;
}
.wish-label {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 600;
}
.wish-tag {
  font-size: 12px;
}
.wish-empty {
  font-size: 12px;
  color: var(--text-muted);
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
:deep(.btn-glass-danger.is-disabled) {
  background: rgba(0, 0, 0, 0.03) !important;
  color: var(--text-muted) !important;
  border-color: var(--glass-border-light) !important;
}
:deep(.glass-input-sm .el-input__wrapper),
:deep(.glass-input-sm .el-textarea__inner) {
  background: rgba(255, 255, 255, 0.5) !important;
  border: 1px solid var(--glass-border-light) !important;
  border-radius: 8px !important;
  box-shadow: none !important;
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
:deep(.glass-dialog .el-dialog) {
  background: var(--glass-bg-strong);
  backdrop-filter: var(--blur-lg);
  -webkit-backdrop-filter: var(--blur-lg);
  border: 1px solid var(--glass-border);
  border-radius: 16px;
  box-shadow: var(--glass-shadow);
}
</style>