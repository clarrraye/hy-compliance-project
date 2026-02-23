import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
// 引入Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// 引入Element Plus图标（可选）
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 🔥 新增：全局捕获ResizeObserver兼容性报错，阻止控制台显示
window.addEventListener('error', (e) => {
  if (e.message.includes('ResizeObserver loop completed with undelivered notifications.')) {
    e.preventDefault() // 阻止错误冒泡
    e.stopPropagation() // 停止事件传播
  }
})

const app = createApp(App)
// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
app.use(router).use(ElementPlus).mount('#app')
