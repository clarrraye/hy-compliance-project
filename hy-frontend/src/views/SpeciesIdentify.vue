<template>
  <div class="species-identify-container">
    <el-page-header @back="goBack" content="智能物种识别与合规检测" class="page-header" />
    
    <div class="content-wrapper">
      <!-- 自定义海洋 loading 遮罩 -->
      <div v-if="loading" class="ocean-loading-overlay">
        <div class="wave-spinner">
          <div class="wave-ring"></div>
          <div class="wave-ring wave-ring--2"></div>
          <div class="wave-ring wave-ring--3"></div>
        </div>
        <div class="typing-text">{{ typingText }}</div>
      </div>

      <!-- 上传区域 -->
      <el-upload
        class="ocean-uploader"
        drag
        action="#"
        :http-request="handleUpload"
        :before-upload="beforeUpload"
        :show-file-list="false"
      >
        <div v-if="imageUrl" class="image-preview">
          <img :src="imageUrl" class="preview-img" />
        </div>
        <div v-else class="upload-placeholder">
          <div class="upload-icon-wrap">
            <el-icon class="upload-main-icon"><upload-filled /></el-icon>
          </div>
          <div class="upload-text">拖拽图片到此处或 <em>点击上传</em></div>
          <div class="upload-tip">支持 JPG/PNG 格式，单张不超过 5MB</div>
        </div>
      </el-upload>

      <!-- 识别结果卡片 -->
      <div v-if="result" class="result-card" :class="statusClass">
        <div class="result-status-bar"></div>
        <div class="result-body">
          <div class="result-header">
            <h2 class="species-name-title" :class="statusClass">{{ result.speciesName }}</h2>
            <div class="status-badge">
              <span :class="['status-dot', `dot-${statusClass.replace('status-', '')}`]"></span>
              <span :class="['status-label', `label-${statusClass.replace('status-', '')}`]">
                {{ result.complianceStatus === 0 ? '禁止捕捞' : (result.complianceStatus === 2 ? '限制捕捞/警告' : '合规可捕') }}
              </span>
            </div>
          </div>

          <div class="confidence-row">
            <span class="conf-label">识别置信度</span>
            <div class="conf-bar-track">
              <div class="conf-bar-fill" :style="{ width: confWidth + '%', background: confGradient }"></div>
            </div>
            <span class="conf-value">{{ confWidth }}%</span>
          </div>

          <div class="info-row">
            <span class="info-label">保护级别：</span>
            <span class="info-value">{{ result.protectLevel }}</span>
          </div>

          <div class="advice-card" :class="adviceCardClass">
            <div class="advice-icon">⚠️</div>
            <div class="advice-content">
              <div class="advice-title">合规建议</div>
              <div class="advice-text">{{ result.complianceAdvice }}</div>
              <div v-if="result.confidence >= 0.3 && result.confidence < 0.8" class="conf-warning-text">
                ※ 当前识别置信度较低（{{ (result.confidence * 100).toFixed(1) }}%），建议上传更清晰的图片或多角度拍摄以确认。
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { identifySpecies } from '@/api/species'

const router = useRouter()
const imageUrl = ref('')
const result = ref(null)
const loading = ref(false)

// ==== 打字机效果 ====
const typingText = ref('')
let typingIntervalId = null
function startTyping() {
  const msg = 'AI 正在分析中...'
  typingText.value = ''
  let i = 0
  typingIntervalId = setInterval(() => {
    if (i < msg.length) {
      typingText.value += msg[i++]
    } else {
      setTimeout(() => { typingText.value = ''; i = 0 }, 700)
    }
  }, 110)
}
function stopTyping() {
  clearInterval(typingIntervalId)
  typingIntervalId = null
}

// ==== 置信度进度条动画 ====
const confWidth = ref(0)
function animateConf(target) {
  confWidth.value = 0
  const targetVal = Math.round(target * 100)
  const step = Math.max(1, Math.round(targetVal / 40))
  let cur = 0
  const id = setInterval(() => {
    cur = Math.min(cur + step, targetVal)
    confWidth.value = cur
    if (cur >= targetVal) clearInterval(id)
  }, 18)
}
const confGradient = computed(() => {
  if (!result.value) return 'linear-gradient(90deg, #00aacc, #00d4aa)'
  const c = result.value.confidence * 100
  if (c < 50) return 'linear-gradient(90deg, #cc2222, #ff4444)'
  if (c < 75) return 'linear-gradient(90deg, #cc8800, #ffaa00)'
  return 'linear-gradient(90deg, #00aacc, #00d4aa)'
})

const goBack = () => router.push('/hall')

const beforeUpload = (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) ElMessage.error('上传图片只能是 JPG/PNG 格式!')
  if (!isLt5M) ElMessage.error('上传图片大小不能超过 5MB!')
  return isImage && isLt5M
}

const handleUpload = async (options) => {
  imageUrl.value = URL.createObjectURL(options.file)
  result.value = null
  loading.value = true
  startTyping()
  try {
    const res = await identifySpecies(options.file)
    if (res.code === 200) {
      result.value = res.data
      animateConf(res.data.confidence)
      ElMessage.success('识别成功')
    } else {
      ElMessage.error(res.msg || '识别失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('请求失败，请检查网络或稍后重试')
  } finally {
    loading.value = false
    stopTyping()
  }
}

const statusClass = computed(() => {
  if (!result.value) return ''
  const s = result.value.complianceStatus
  return s === 0 ? 'status-danger' : (s === 2 ? 'status-warning' : 'status-success')
})

const adviceCardClass = computed(() => {
  if (!result.value) return ''
  const s = result.value.complianceStatus
  return s === 0 ? 'advice-danger' : (s === 2 ? 'advice-warning' : 'advice-success')
})
</script>

<style scoped>
/* ===== 容器 ===== */
.species-identify-container {
  padding: 24px 40px;
  max-width: 860px;
  margin: 0 auto;
  min-height: calc(100vh - 64px);
  box-sizing: border-box;
}

:deep(.page-header .el-page-header__back .el-icon) { color: #66d8ff; }
:deep(.page-header .el-page-header__title) { color: #88ccdd; }
:deep(.page-header .el-page-header__content) {
  color: #e8f6ff;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 2px;
  text-shadow: 0 0 12px rgba(0,212,255,0.4);
}
:deep(.page-header .el-page-header__separator) { background: rgba(0,150,200,0.3); }

.content-wrapper {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 24px;
  margin-top: 20px;
}

/* ===== 自定义 loading 遮罩 ===== */
.ocean-loading-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,10,30,0.78);
  backdrop-filter: blur(6px);
  z-index: 9999;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 28px;
}
.wave-spinner {
  position: relative;
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.wave-ring {
  position: absolute;
  width: 56px;
  height: 56px;
  border: 2.5px solid rgba(0,212,255,0.75);
  border-radius: 50%;
  animation: wave-expand 2s ease-out infinite;
}
.wave-ring--2 { animation-delay: -0.67s; }
.wave-ring--3 { animation-delay: -1.33s; }
@keyframes wave-expand {
  0%   { transform: scale(0.3); opacity: 1; }
  100% { transform: scale(2.4); opacity: 0; }
}
.typing-text {
  font-size: 16px;
  color: #66e8ff;
  letter-spacing: 3px;
  font-weight: 600;
  text-shadow: 0 0 12px rgba(0,212,255,0.6);
  min-width: 148px;
  text-align: center;
}

/* ===== 上传区域 ===== */
:deep(.ocean-uploader .el-upload) { width: 100%; display: block; }
:deep(.ocean-uploader .el-upload-dragger) {
  width: 100%;
  height: 220px;
  border: 2px dashed rgba(0,212,255,0.4) !important;
  border-radius: 16px !important;
  background: rgba(0,15,50,0.6) !important;
  background-image:
    radial-gradient(ellipse at 25% 35%, rgba(0,160,255,0.09) 0%, transparent 55%),
    radial-gradient(ellipse at 75% 70%, rgba(0,100,200,0.07) 0%, transparent 50%) !important;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: border-color 0.25s, background 0.25s, box-shadow 0.25s !important;
}
:deep(.ocean-uploader .el-upload-dragger:hover) {
  border-style: solid !important;
  border-color: rgba(0,212,255,0.65) !important;
  background-color: rgba(0,25,65,0.7) !important;
}
:deep(.ocean-uploader .el-upload-dragger.is-dragover) {
  background-color: rgba(0,212,255,0.13) !important;
  border-style: solid !important;
  border-color: #00d4ff !important;
  box-shadow: 0 0 22px rgba(0,212,255,0.35) !important;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  pointer-events: none;
}
.upload-icon-wrap {
  width: 68px;
  height: 68px;
  border-radius: 50%;
  background: rgba(0,100,180,0.2);
  border: 1px solid rgba(0,180,255,0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s;
}
:deep(.ocean-uploader .el-upload-dragger:hover) .upload-icon-wrap {
  animation: icon-shake 0.55s ease-in-out;
}
.upload-main-icon {
  font-size: 28px;
  color: #00d4ff;
  filter: drop-shadow(0 0 8px rgba(0,212,255,0.55));
}
@keyframes icon-shake {
  0%,100% { transform: translateY(0); }
  30%      { transform: translateY(-5px); }
  70%      { transform: translateY(4px); }
}
.upload-text { font-size: 15px; color: #88bbdd; }
.upload-text em { color: #00d4ff; font-style: normal; font-weight: 600; }
.upload-tip { font-size: 12px; color: rgba(100,160,200,0.6); }

/* 图片预览 */
.image-preview {
  width: 100%;
  display: flex;
  justify-content: center;
  padding: 10px;
}
.preview-img {
  max-height: 196px;
  max-width: 100%;
  border-radius: 12px;
  border: 2px solid rgba(0,180,255,0.4);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  cursor: zoom-in;
}
.preview-img:hover {
  transform: scale(1.02);
  box-shadow: 0 0 20px rgba(0,180,255,0.4);
}

/* ===== 识别结果卡片 ===== */
.result-card {
  display: flex;
  border-radius: 14px;
  overflow: hidden;
  background: rgba(0,15,50,0.82);
  border: 1px solid rgba(0,150,200,0.2);
  backdrop-filter: blur(12px);
}
.result-status-bar { width: 5px; flex-shrink: 0; }
.status-danger  .result-status-bar { background: linear-gradient(180deg, #ff4444, #cc2222); }
.status-warning .result-status-bar { background: linear-gradient(180deg, #ffaa00, #cc8800); }
.status-success .result-status-bar { background: linear-gradient(180deg, #00dd77, #00aa55); }

.result-body {
  flex: 1;
  padding: 24px 28px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

/* 物种名称标题行 */
.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10px;
}
.species-name-title {
  margin: 0;
  font-size: 26px;
  font-weight: 800;
  letter-spacing: 3px;
}
.species-name-title.status-danger  { color: #ff6666; filter: drop-shadow(0 0 8px rgba(255,60,60,0.6)); }
.species-name-title.status-warning { color: #ffcc44; filter: drop-shadow(0 0 8px rgba(255,180,0,0.6)); }
.species-name-title.status-success { color: #44ee99; filter: drop-shadow(0 0 8px rgba(0,220,100,0.6)); }

/* 状态徽章 */
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 5px 14px;
  border-radius: 20px;
  background: rgba(0,20,55,0.7);
  border: 1px solid rgba(0,150,200,0.25);
}
.status-dot { width: 9px; height: 9px; border-radius: 50%; flex-shrink: 0; }
.dot-danger  { background: #ff4444; animation: pulse-dot-red   1.6s ease-in-out infinite; }
.dot-warning { background: #ffaa00; animation: pulse-dot-amber 1.6s ease-in-out infinite; }
.dot-success { background: #00dd77; animation: pulse-dot-green 1.6s ease-in-out infinite; }
@keyframes pulse-dot-red   { 0%,100%{ box-shadow: 0 0 4px rgba(255,68,68,0.5) }  50%{ box-shadow: 0 0 11px rgba(255,68,68,1) } }
@keyframes pulse-dot-amber { 0%,100%{ box-shadow: 0 0 4px rgba(255,160,0,0.5) }  50%{ box-shadow: 0 0 11px rgba(255,160,0,1) } }
@keyframes pulse-dot-green { 0%,100%{ box-shadow: 0 0 4px rgba(0,221,119,0.5) } 50%{ box-shadow: 0 0 11px rgba(0,221,119,1) } }

.status-label { font-size: 14px; font-weight: 700; }
.label-danger  { color: #ff6666; text-shadow: 0 0 8px rgba(255,60,60,0.55); }
.label-warning { color: #ffcc44; text-shadow: 0 0 8px rgba(255,180,0,0.55); }
.label-success { color: #44ee99; text-shadow: 0 0 8px rgba(0,220,100,0.55); }

/* 置信度进度条 */
.confidence-row {
  display: flex;
  align-items: center;
  gap: 14px;
}
.conf-label { color: #88bbdd; font-size: 14px; white-space: nowrap; }
.conf-bar-track {
  flex: 1;
  height: 8px;
  border-radius: 4px;
  background: rgba(0,50,100,0.5);
  overflow: hidden;
}
.conf-bar-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.05s linear;
}
.conf-value { color: #cceeff; font-size: 14px; font-weight: 700; min-width: 36px; text-align: right; }

/* 信息行 */
.info-row { display: flex; align-items: center; gap: 10px; }
.info-label { color: #88bbdd; font-size: 14px; white-space: nowrap; }
.info-value  { color: #cceeff; font-size: 14px; }

/* 应急指引卡片 */
.advice-card {
  display: flex;
  gap: 14px;
  padding: 16px 18px;
  border-radius: 10px;
  border: 1px solid;
}
.advice-danger  { background: rgba(80,10,10,0.45);  border-color: rgba(255,100,0,0.55); }
.advice-warning { background: rgba(60,40,0,0.45);   border-color: rgba(255,170,0,0.55); }
.advice-success { background: rgba(0,50,30,0.45);   border-color: rgba(0,200,100,0.45); }

.advice-icon  { font-size: 22px; flex-shrink: 0; line-height: 1.4; }
.advice-title { font-size: 13px; font-weight: 700; color: #88ccdd; margin-bottom: 6px; letter-spacing: 1px; }
.advice-text  { font-size: 14px; color: #cce8f0; line-height: 1.7; }
.conf-warning-text { margin-top: 8px; font-size: 12px; color: rgba(255,200,100,0.75); }
</style>
