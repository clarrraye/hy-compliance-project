import request from '@/utils/request'

// 修改system.js文件
export default {
  // 用户管理相关接口（直接使用SysUserController中的接口）
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
  
  // 捕捞日志管理（使用FishingLogController中的管理员接口）
  getAllFishingLogs(params) {
    return request.get('/fishingLog/admin/all', { params })
  },
  
  deleteFishingLog(logId) {
    return request.post('/fishingLog/admin/delete', { logId })
  }
}