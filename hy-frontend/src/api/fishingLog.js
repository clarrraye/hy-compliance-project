import request from '@/utils/request'

export default {
  // 保存捕捞日志（POST传参）
  saveLog(data) {
    return request.post('/fishingLog/save', data)
  },
  // 获取日志列表（GET传参）
  getLogList(params) {
    return request.get('/fishingLog/list', { params })
  },
  // 获取合规可视化数据（GET传参）
  getChartData(params) {
    return request.get('/fishingLog/chartData', { params })
  },
  // 导出自查报告（GET传参 + 二进制流响应）
  exportReport(params) {
    return request.get('/fishingLog/export', { 
      params,
      responseType: 'blob' // 关键：指定响应类型为二进制流
    })
  },
  // 新增：删除日志（DELETE传参）
  deleteLog(logId) {
    return request.delete(`/fishingLog/delete/${logId}`)
  }
}