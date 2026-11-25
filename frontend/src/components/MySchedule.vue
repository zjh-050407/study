<script setup>
import { ref, onMounted } from "vue";
import request from "@/utils/request.js";
import ScheduleGrid from "@/components/ScheduleGrid.vue";

const slots = ref([])
const loading = ref(false)

const getData = async () => {
  loading.value = true
  try {
    const result = await request.get('/schedule/my')
    if (result.data.code === 0) {
      slots.value = result.data.data || []
    }
  } catch (err) {
    console.error('获取课表失败:', err)
  } finally {
    loading.value = false
  }
}
onMounted(getData)
</script>
<template>
  <ScheduleGrid :slots="slots" :loading="loading" title="我的课表"
                subtitle="按当前学期选课 × 排课生成（含未分学期旧数据）" />
</template>
