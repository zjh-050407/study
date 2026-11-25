<script setup>
import { ref, onMounted, computed } from "vue";
import request from "@/utils/request.js";
import { ElMessage } from 'element-plus'
import ScheduleGrid from "@/components/ScheduleGrid.vue";

const user = JSON.parse(localStorage.getItem('user') || '{}')
const isManager = computed(() => user.role === 'admin' || user.role === 'professor')

const slots = ref([])
const loading = ref(false)
const teachers = ref([])
const tid = ref(null)
const teacherName = ref('')

const loadTeachers = async () => {
  try {
    const result = await request.get('/teacher/list', { params: { page: 1, size: 100 } })
    if (result.data.code === 0) {
      teachers.value = result.data.data.list || result.data.data || []
    }
  } catch (e) {}
}

const getData = async () => {
  loading.value = true
  try {
    const params = {}
    if (tid.value != null) params.tid = tid.value
    const result = await request.get('/schedule/grid/teacher', { params })
    if (result.data.code === 0) {
      slots.value = result.data.data || []
    } else {
      ElMessage.error(result.data.message || '加载课表失败')
    }
  } catch (e) {
    ElMessage.error('加载课表失败')
  } finally {
    loading.value = false
  }
}

const onTeacherChange = () => {
  const t = teachers.value.find(x => x.tid === tid.value)
  teacherName.value = t ? t.name : ''
  getData()
}

onMounted(async () => {
  if (isManager.value) {
    await loadTeachers()
    // 管理员无教师档案时默认查看第一位教师
    if (user.role === 'admin' && teachers.value.length > 0) {
      tid.value = teachers.value[0].tid
      const t = teachers.value[0]
      teacherName.value = t ? t.name : ''
    }
  }
  getData()
})
</script>
<template>
  <div class="page-wrap">
    <div v-if="isManager" class="toolbar">
      <span class="toolbar-label">查看教师：</span>
      <el-select v-model="tid" style="width: 220px" placeholder="选择教师" @change="onTeacherChange">
        <el-option v-for="t in teachers" :key="t.tid" :label="t.name" :value="t.tid" />
      </el-select>
    </div>
    <ScheduleGrid :slots="slots" :loading="loading"
                  :title="teacherName ? `课表 · ${teacherName}` : '我的课表'"
                  subtitle="按当前学期排课生成（含未分学期旧数据）" />
  </div>
</template>
<style scoped>
.page-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  width: 100%;
}
.toolbar {
  width: 100%;
  max-width: 1100px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.toolbar-label {
  font-size: 13px;
  color: var(--text-secondary);
}
</style>
