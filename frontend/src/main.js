import '@fontsource/source-serif-4/400.css'
import '@fontsource/source-serif-4/600.css'
import '@fontsource/source-serif-4/700.css'
import '@fontsource/jetbrains-mono/400.css'
import '@fontsource/jetbrains-mono/500.css'
import { createApp } from 'vue'
import './assets/main.css'
import App from './App.vue'
import router from '@/router/index.js'

const app = createApp(App)
app.use(router)
app.mount('#app')