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

// ── v-stagger：卡片从下方交错淡入（IntersectionObserver 驱动）──
app.directive('stagger', {
  mounted(el, binding) {
    const delay = Number(binding.value) || 0
    el.style.opacity = '0'
    el.style.transform = 'translateY(26px)'
    el.style.transition = [
      `opacity 0.52s cubic-bezier(0.4,0,0.2,1) ${delay}ms`,
      `transform 0.52s cubic-bezier(0.4,0,0.2,1) ${delay}ms`
    ].join(', ')
    const observer = new IntersectionObserver(([entry]) => {
      if (entry.isIntersecting) {
        el.style.opacity = '1'
        el.style.transform = 'translateY(0)'
        observer.disconnect()
      }
    }, { threshold: 0.08 })
    observer.observe(el)
  }
})

app.use(router).use(ElementPlus).mount('#app')
