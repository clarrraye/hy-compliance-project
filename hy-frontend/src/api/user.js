import request from '@/utils/request'

export default {
  // 登录接口：POST传参
  login(data) {
    return request.post('/user/login', data)
  },
  // 获取个人信息
  getUserInfo() {
    return request.get('/user/info')
  },
  // 退出登录
  logout() {
    return request.get('/user/logout')
  }
}