<script setup>
import { ref, onMounted } from "vue";
import request from "@/utils/request.js";
import { ElMessage } from 'element-plus'
import { Printer, Document, Postcard } from '@element-plus/icons-vue'

const loading = ref(false)
const previewUrl = ref('')
const previewType = ref('')

const printPdf = async (type) => {
  loading.value = true
  try {
    const result = await request.get(`/print/${type}`, { responseType: 'blob' })
    const blob = new Blob([result.data], { type: 'application/pdf' })
    if (previewUrl.value) {
      URL.revokeObjectURL(previewUrl.value)
    }
    previewUrl.value = URL.createObjectURL(blob)
    previewType.value = type
  } catch (e) {
    ElMessage.error('生成证明失败')
  } finally {
    loading.value = false
  }
}

const download = () => {
  if (!previewUrl.value) return
  const link = document.createElement('a')
  link.href = previewUrl.value
  link.download = previewType.value === 'transcript' ? '成绩单.pdf' : '在读证明.pdf'
  link.click()
}

const print = () => {
  const iframe = document.getElementById('pdf-preview')
  if (iframe && iframe.contentWindow) {
    iframe.contentWindow.focus()
    iframe.contentWindow.print()
  }
}

onMounted(() => {})
</script>
<template>
  <div class="cert-page">
    <el-card class="glass-card" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><Printer /></el-icon>
          <span class="card-title">证明打印</span>
        </div>
      </template>

      <div class="cert-actions">
        <el-button type="primary" :icon="Document" :loading="loading" @click="printPdf('transcript')">生成成绩单</el-button>
        <el-button type="primary" plain :icon="Postcard" :loading="loading" @click="printPdf('enrollment')">生成在读证明</el-button>
        <el-button v-if="previewUrl" @click="download">下载 PDF</el-button>
        <el-button v-if="previewUrl" @click="print">打印</el-button>
      </div>

      <div v-if="previewUrl" class="preview-wrap">
        <iframe id="pdf-preview" :src="previewUrl" class="pdf-frame" title="证明预览"></iframe>
      </div>
      <div v-else class="preview-empty">
        <p>点击上方按钮生成成绩单或在读证明，可在线预览、下载或打印。</p>
        <p class="preview-tip">证明仅供本人使用，请注意保管个人信息。</p>
      </div>
    </el-card>
  </div>
</template>
<style scoped>
.cert-page {
  display: flex;
  justify-content: center;
  width: 100%;
}
.glass-card {
  width: 100%;
  max-width: 900px;
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
  align-items: center;
  gap: 8px;
}
.card-title {
  font-family: var(--font-serif);
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
}
.cert-actions {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}
.preview-wrap {
  border: 1px solid var(--glass-border-light);
  border-radius: 8px;
  overflow: hidden;
}
.pdf-frame {
  width: 100%;
  height: 620px;
  border: none;
}
.preview-empty {
  text-align: center;
  color: var(--text-secondary);
  padding: 40px 0;
  font-size: 14px;
}
.preview-tip {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 8px;
}
</style>
