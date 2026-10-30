import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import { resolve } from 'path'

export default defineConfig({
  plugins: [
    vue(),
    // element-plus 按需自动引入（含 v-loading 指令）
    Components({
      resolvers: [ElementPlusResolver()]
    })
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5173,
    host: '0.0.0.0'
  },
  build: {
    rollupOptions: {
      output: {
        // 按包拆分大依赖：echarts 与 zrender 分开，保证单 chunk < 500KB
        manualChunks(id) {
          if (id.includes('node_modules/echarts')) return 'echarts'
          if (id.includes('node_modules/zrender')) return 'zrender'
          if (id.includes('node_modules/element-plus')) return 'element-plus'
          if (id.includes('node_modules/@element-plus/icons-vue')) return 'element-plus-icons'
        }
      }
    },
    chunkSizeWarningLimit: 500
  }
})