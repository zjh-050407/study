<script setup>
import { ref, onMounted, watch } from "vue";
import { useRoute } from "vue-router";
import request from "@/utils/request.js";
import { ElMessage } from 'element-plus'
import ScheduleGrid from "@/components/ScheduleGrid.vue";

const route = useRoute()
const classrooms = ref([])
const classroom = ref('')
const slots = ref([])
const loading = ref(false)

const loadClassrooms = async () => {
  try {
    const result = await request.get('/classroom/list', { params: { page: 1, size: 100 } })
    if (result.data.code === 0) {
      const data = result.data.data
      classrooms.value = data.list || data || []
    }
  } catch (e) {}
}

const getData = async () => {
  if (!classroom.value) {
    slots.value = []
    return
  }
  loading.value = true
  try {
    const result = await request.get('/schedule/grid/classroom', { params: { classroom: classroom.value } })
    if (result.data.code === 0) {
      slots.value = result.data.data || []
    } else {
      ElMessage.error(result.data.message || '加载失败')
    }
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const onClassroomChange = () => getData()

onMounted(async () => {
  await loadClassrooms()
  // 支持从教室管理页跳转带入 room 参数
  const room = route.query.room
  if (room) {
    classroom.value = String(room)
  } else if (classrooms.value.length > 0) {
    classroom.value = classrooms.value[0].name || classrooms.value[0].roomName || ''
  }
  getData()
})

watch(() => route.query.room, (val) => {
  if (val) {
    classroom.value = String(val)
    getData()
  }
})
</script>
<template>
  <div class="page-wrap">
    <div class="toolbar">
      <span class="toolbar-label">选择教室：</span>
      <el-select v-model="classroom" style="width: 220px" placeholder="选择教室" @change="onClassroomChange">
        <el-option v-for="c in classrooms" :key="c.id" :label="c.name" :value="c.name" />
      </el-select>
    </div>
    <ScheduleGrid :slots="slots" :loading="loading"
                  :title="classroom ? `教室课表 · ${classroom}` : '教室课表'"
                  subtitle="按当前学期排课生成" />
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
