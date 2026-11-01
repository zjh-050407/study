<script setup>
import { Tools } from '@element-plus/icons-vue'

defineProps({
  title: {
    type: String,
    default: '功能模块',
  },
  desc: {
    type: String,
    default: '该模块正在建设中，敬请期待。',
  },
})
</script>
<template>
  <div class="contain">
    <el-card class="glass-card" style="max-width: 560px">
      <div class="placeholder-body">
        <div class="placeholder-icon">
          <el-icon :size="40"><Tools /></el-icon>
        </div>
        <div class="placeholder-title">{{ title }}</div>
        <div class="placeholder-desc">{{ desc }}</div>
      </div>
    </el-card>
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

.glass-card {
  background: var(--glass-bg-strong);
  backdrop-filter: var(--blur-md);
  -webkit-backdrop-filter: var(--blur-md);
  border: 1px solid var(--glass-border);
  border-radius: 16px;
  box-shadow: var(--glass-shadow);
  width: 100%;
}

.placeholder-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48px 24px;
  text-align: center;
}
.placeholder-icon {
  color: var(--primary);
  opacity: 0.7;
  margin-bottom: 16px;
}
.placeholder-title {
  font-family: var(--font-serif);
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 10px;
}
.placeholder-desc {
  font-size: 13px;
  color: var(--text-muted);
  line-height: 1.6;
}
</style>