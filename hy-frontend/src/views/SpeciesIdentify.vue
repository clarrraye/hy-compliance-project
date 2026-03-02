<template>
  <div class="species-identify-container">
    <el-page-header @back="goBack" content="智能物种识别与合规检测" class="page-header" />
    
    <div class="content-wrapper" v-loading="loading" element-loading-text="正在智能识别与合规检测...">
      <el-card class="upload-card">
        <template #header>
          <div class="card-header">
            <span>上传渔获照片</span>
          </div>
        </template>
        
        <el-upload
          class="upload-demo"
          drag
          action="#"
          :http-request="handleUpload"
          :before-upload="beforeUpload"
          :show-file-list="false"
        >
          <div v-if="imageUrl" class="image-preview">
            <el-image :src="imageUrl" fit="contain" style="max-height: 200px;" />
          </div>
          <div v-else class="upload-placeholder">
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              拖拽图片到此处或 <em>点击上传</em>
            </div>
            <div class="el-upload__tip">
              支持 JPG/PNG 格式，单张不超过 5MB
            </div>
          </div>
        </el-upload>
      </el-card>

      <el-card v-if="result" class="result-card" :class="statusClass">
        <template #header>
          <div class="card-header">
            <span>识别与合规结果</span>
            <el-tag :type="statusTagType" effect="dark">{{ result.complianceStatus === 0 ? '禁止捕捞' : (result.complianceStatus === 2 ? '限制捕捞/警告' : '合规可捕') }}</el-tag>
          </div>
        </template>
        
        <div class="result-content">
          <div class="info-row">
            <span class="label">识别物种：</span>
            <span class="value species-name">{{ result.speciesName }}</span>
          </div>
          <div class="info-row">
            <span class="label">可信度：</span>
            <el-progress :percentage="Math.round(result.confidence * 100)" :status="confidenceStatus" style="width: 200px;" />
          </div>
          <div class="info-row">
            <span class="label">保护级别：</span>
            <span class="value">{{ result.protectLevel }}</span>
          </div>
          
          <el-divider />
          
          <div class="advice-section">
            <div class="label">合规建议：</div>
            <el-alert
              :title="result.complianceAdvice"
              :type="statusAlertType"
              :closable="false"
              show-icon
            >
              <template #default>
                <div v-if="result.confidence >= 0.3 && result.confidence < 0.8" style="margin-top: 10px; font-size: 12px; color: #666;">
                   * 提示：当前识别置信度较低（{{ (result.confidence * 100).toFixed(1) }}%），建议上传更清晰的图片或多角度拍摄以确认。如果该物种可能为相似的保护动物，请务必按严格标准执行。
                </div>
              </template>
            </el-alert>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElLoading } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue' // 需确保已引入图标
import { identifySpecies } from '@/api/species'

const router = useRouter()
const imageUrl = ref('')
const result = ref(null)
const loading = ref(false)

const goBack = () => {
  router.push('/hall')
}

const beforeUpload = (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('上传图片只能是 JPG/PNG 格式!')
  }
  if (!isLt5M) {
    ElMessage.error('上传图片大小不能超过 5MB!')
  }
  return isImage && isLt5M
}

const handleUpload = async (options) => {
  const file = options.file
  imageUrl.value = URL.createObjectURL(file)
  result.value = null // 重置结果
  loading.value = true
  
  try {
    const res = await identifySpecies(file)
    if (res.code === 200) {
      result.value = res.data
      ElMessage.success('识别成功')
    } else {
      ElMessage.error(res.msg || '识别失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('请求失败，请检查网络或稍后重试')
  } finally {
    loading.value = false
  }
}

// 计算属性用于动态样式
const statusClass = computed(() => {
  if (!result.value) return ''
  const s = result.value.complianceStatus
  return s === 0 ? 'status-danger' : (s === 2 ? 'status-warning' : 'status-success')
})

const statusTagType = computed(() => {
  if (!result.value) return 'info'
  const s = result.value.complianceStatus
  return s === 0 ? 'danger' : (s === 2 ? 'warning' : 'success')
})

const statusAlertType = computed(() => {
  if (!result.value) return 'info'
  const s = result.value.complianceStatus
  return s === 0 ? 'error' : (s === 2 ? 'warning' : 'success')
})

const confidenceStatus = computed(() => {
  if (!result.value) return ''
  const c = result.value.confidence
  if (c > 0.9) return 'success'
  if (c > 0.7) return 'warning'
  return 'exception'
})
</script>

<style scoped>
.species-identify-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
}

.content-wrapper {
  display: flex;
  flex-direction: column;
  gap: 20px;
  position: relative;
}

.upload-card .el-upload {
  width: 100%;
}

.upload-demo .el-upload-dragger {
  width: 100%;
}

.image-preview {
  padding: 10px;
  display: flex;
  justify-content: center;
}

.result-card {
  transition: all 0.3s;
}

.status-danger {
  border-left: 5px solid #F56C6C;
}

.status-warning {
  border-left: 5px solid #E6A23C;
}

.status-success {
  border-left: 5px solid #67C23A;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  font-size: 16px;
}

.info-row .label {
  width: 100px;
  color: #606266;
  font-weight: bold;
}

.species-name {
  font-size: 20px;
  color: #303133;
  font-weight: bold;
}

.loading-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.7);
  z-index: 100;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>
