import request from '@/utils/request'

export default {
  // 用户管理相关接口（管理员功能）
  getUserList(params) {
    return request.get('/user/list', { params })
  },
  
  addUser(data) {
    return request.post('/user/add', data)
  },
  
  editUser(data) {
    return request.post('/user/edit', data)
  },
  
  deleteUser(userId) {
    return request.post('/user/delete', { userId })
  },
  
  // 捕捞日志管理（管理员功能）
  getAllFishingLogs(params) {
    return request.get('/system/fishingLogs', { params })
  },
  
  deleteFishingLog(logId) {
    return request.post('/system/fishingLogs/delete', { logId })
  }
}