import { createRouter, createWebHistory } from 'vue-router'
// 导入新增的捕捞日志组件
import ComplianceQuery from '@/views/ComplianceQuery.vue'
import LoginPage from '@/views/LoginPage.vue'
import HallIndex from '@/views/HallIndex.vue'
import FishingLog from '@/views/FishingLog.vue' // 新增
import SystemManagement from '@/views/SystemManagement.vue'

// 路由规则配置
const routes = [
  {
    path: '/login',
    name: 'LoginPage',
    component: LoginPage,
    meta: { title: '海渔合规平台-登录' }
  },
  {
    path: '/hall',
    name: 'HallIndex',
    component: HallIndex,
    meta: { title: '海渔合规平台-功能大厅', requireAuth: true }
  },
  {
    path: '/compliance',
    name: 'ComplianceQuery',
    component: ComplianceQuery,
    meta: { title: '基础合规查询', requireAuth: true }
  },
  // 新增捕捞日志路由
  {
    path: '/fishingLog',
    name: 'FishingLog',
    component: FishingLog,
    meta: { title: '捕捞日志与合规自查', requireAuth: true }
  },
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/system',
    name: 'SystemManagement',
    component: SystemManagement,
    meta: { title: '系统管理', requireAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.title) document.title = to.meta.title
  if (to.meta.requireAuth) {
    const loginUserId = sessionStorage.getItem('loginUserId')
    if (loginUserId) {
      next()
    } else {
      next('/login')
    }
  } else {
    next()
  }
})

export default router