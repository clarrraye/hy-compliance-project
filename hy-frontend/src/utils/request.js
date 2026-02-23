import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router' // 新增：引入路由，用于401重定向

// 创建axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
})

// 请求拦截器（原有，不动）
request.interceptors.request.use(
  (config) => {
    return config
  },
  (error) => {
    ElMessage.error('请求发送失败，请检查网络')
    return Promise.reject(error)
  }
)

// 响应拦截器（🔥 仅修改此处，新增401处理，其余不动）
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (Array.isArray(res) || (typeof res === 'object' && res !== null)) {
      return res
    } else {
      ElMessage.error('后端返回数据格式异常')
      return Promise.reject(new Error('数据格式异常'))
    }
  },
  (error) => {
    // 🔥 新增：处理401未登录（后端拦截器/接口返回的401）
    if (error.response && error.response.status === 401) {
      ElMessage.warning('登录状态已过期，请重新登录')
      // 清除登录态，重定向到登录页
      sessionStorage.removeItem('loginUserId')
      router.push('/login')
    } else {
      ElMessage.error(error.message || '网络异常，请检查后端是否启动')
    }
    return Promise.reject(error)
  }
)

export default request