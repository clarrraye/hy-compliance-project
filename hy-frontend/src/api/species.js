import request from '@/utils/request'

// 物种识别接口
export function identifySpecies(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/species/identify',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
