<template>
  <div class="login-page">
    <!-- 动态海浪 + 气泡 Canvas 背景 -->
    <canvas ref="oceanCanvas" class="ocean-canvas"></canvas>

    <!-- 登录卡片 -->
    <div class="login-card">
      <!-- 扫光动画边框层 -->
      <div class="card-glow-border"></div>

      <!-- Logo 区域 -->
      <div class="login-logo">
        <span class="logo-icon">⚓</span>
      </div>
      <div class="login-title">海渔合规管理平台</div>
      <div class="login-subtitle">Marine Fishery Compliance System</div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-width="0px"
        class="login-form"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入登录账号"
            prefix-icon="User"
            clearable
            class="ocean-input"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入登录密码"
            prefix-icon="Lock"
            clearable
            class="ocean-input"
          />
        </el-form-item>
        <el-form-item>
          <!-- 水波纹按钮：点击时触发 ripple -->
          <button class="login-btn" @click="handleLoginWithRipple" :disabled="loading">
            <span v-if="!loading">登 录</span>
            <span v-else class="btn-loading">
              <span class="dot"></span><span class="dot"></span><span class="dot"></span>
            </span>
            <span class="ripple-container" ref="rippleContainer"></span>
          </button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import userApi from '@/api/user'
import { useRouter } from 'vue-router'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)
const loginForm = ref({ username: '', password: '' })
const loginRules = ref({
  username: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入登录密码', trigger: 'blur' }]
})

// =============== Canvas 海浪 + 气泡 ===============
const oceanCanvas = ref(null)
let animFrameId = null

// 海浪配置
const waveLayers = [
  { color: 'rgba(0,212,255,0.30)', amplitude: 28, frequency: 0.018, speed: 0.6,  offset: 0,   yBase: 0.72 },
  { color: 'rgba(0,150,200,0.20)', amplitude: 20, frequency: 0.024, speed: -0.4, offset: 2.1, yBase: 0.78 },
  { color: 'rgba(0,80,150,0.15)',  amplitude: 14, frequency: 0.030, speed: 0.8,  offset: 4.2, yBase: 0.83 },
]

// 气泡配置
const BUBBLE_COUNT = 28
let bubbles = []

function initBubbles(w, h) {
  bubbles = Array.from({ length: BUBBLE_COUNT }, () => createBubble(w, h, true))
}

function createBubble(w, h, randomY = false) {
  const r = 2 + Math.random() * 6           // 2~8px 半径
  return {
    x:       Math.random() * w,
    y:       randomY ? Math.random() * h : h + r,
    r,
    speed:   0.3 + Math.random() * 0.7,
    opacity: 0.2 + Math.random() * 0.4,
    drift:   (Math.random() - 0.5) * 0.4,   // 左右漂移
    phase:   Math.random() * Math.PI * 2,
  }
}

function drawOcean() {
  const canvas = oceanCanvas.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  const W = canvas.width
  const H = canvas.height
  const t = performance.now() / 1000

  ctx.clearRect(0, 0, W, H)

  // --- 背景深海渐变 ---
  const bg = ctx.createLinearGradient(0, 0, W * 0.6, H)
  bg.addColorStop(0,   '#0a0e27')
  bg.addColorStop(0.5, '#0d2137')
  bg.addColorStop(1,   '#0a1628')
  ctx.fillStyle = bg
  ctx.fillRect(0, 0, W, H)

  // --- 深海光晕（中心散射光） ---
  const radial = ctx.createRadialGradient(W * 0.5, H * 0.3, 0, W * 0.5, H * 0.3, W * 0.55)
  radial.addColorStop(0,   'rgba(0,180,255,0.06)')
  radial.addColorStop(0.5, 'rgba(0,100,200,0.03)')
  radial.addColorStop(1,   'rgba(0,0,0,0)')
  ctx.fillStyle = radial
  ctx.fillRect(0, 0, W, H)

  // --- 海浪（3层，从下往上） ---
  ;[...waveLayers].reverse().forEach(wave => {
    const yBase = H * wave.yBase
    ctx.beginPath()
    ctx.moveTo(0, H)
    for (let x = 0; x <= W; x += 4) {
      const y = yBase +
        Math.sin(x * wave.frequency + t * wave.speed + wave.offset) * wave.amplitude +
        Math.sin(x * wave.frequency * 1.7 + t * wave.speed * 0.6 + wave.offset * 1.3) * (wave.amplitude * 0.4)
      if (x === 0) ctx.lineTo(x, y)
      else ctx.lineTo(x, y)
    }
    ctx.lineTo(W, H)
    ctx.closePath()
    ctx.fillStyle = wave.color
    ctx.fill()
  })

  // --- 气泡 ---
  bubbles.forEach((b, i) => {
    // 更新位置
    b.y     -= b.speed
    b.x     += b.drift + Math.sin(t * 0.8 + b.phase) * 0.3
    b.phase += 0.01

    // 超出屏幕顶部则重置到底部
    if (b.y + b.r < 0) {
      bubbles[i] = createBubble(W, H)
    }

    // 绘制气泡
    ctx.beginPath()
    ctx.arc(b.x, b.y, b.r, 0, Math.PI * 2)
    ctx.strokeStyle = `rgba(0,212,255,${b.opacity})`
    ctx.lineWidth = 1
    ctx.stroke()

    // 高光
    ctx.beginPath()
    ctx.arc(b.x - b.r * 0.3, b.y - b.r * 0.3, b.r * 0.25, 0, Math.PI * 2)
    ctx.fillStyle = `rgba(255,255,255,${b.opacity * 0.6})`
    ctx.fill()
  })

  animFrameId = requestAnimationFrame(drawOcean)
}

function resizeCanvas() {
  const canvas = oceanCanvas.value
  if (!canvas) return
  canvas.width  = window.innerWidth
  canvas.height = window.innerHeight
  initBubbles(canvas.width, canvas.height)
}

// =============== 水波纹点击效果 ===============
const rippleContainer = ref(null)

function handleLoginWithRipple(e) {
  // 触发水波纹
  const btn = e.currentTarget
  const rect = btn.getBoundingClientRect()
  const ripple = document.createElement('span')
  const size = Math.max(rect.width, rect.height) * 2
  const x = e.clientX - rect.left - size / 2
  const y = e.clientY - rect.top  - size / 2
  ripple.style.cssText = `
    position: absolute;
    width:  ${size}px;
    height: ${size}px;
    left:   ${x}px;
    top:    ${y}px;
    border-radius: 50%;
    background: rgba(255,255,255,0.35);
    transform: scale(0);
    animation: ripple-expand 0.6s ease-out forwards;
    pointer-events: none;
  `
  btn.appendChild(ripple)
  setTimeout(() => ripple.remove(), 650)

  handleLogin()
}

// =============== 登录逻辑 ===============
const handleLogin = async () => {
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await userApi.login(loginForm.value)
    if (res.code === 200) {
      ElMessage.success(res.msg)
      sessionStorage.setItem('loginUserId', res.data)
      router.push('/hall')
    } else {
      ElMessage.error(res.msg)
    }
  } catch (error) {
    ElMessage.error('登录失败，请检查网络或后端状态')
    console.error('登录异常：', error)
  } finally {
    loading.value = false
  }
}

// =============== 生命周期 ===============
onMounted(() => {
  resizeCanvas()
  drawOcean()
  window.addEventListener('resize', resizeCanvas)
})

onBeforeUnmount(() => {
  if (animFrameId) cancelAnimationFrame(animFrameId)
  window.removeEventListener('resize', resizeCanvas)
})
</script>

<style scoped>
/* ===== 页面容器 ===== */
.login-page {
  width: 100vw;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
  background: #0a0e27; /* fallback */
}

/* ===== Canvas 海浪背景 ===== */
.ocean-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

/* ===== 登录卡片 ===== */
.login-card {
  position: relative;
  z-index: 10;
  width: 420px;
  padding: 44px 36px 36px;
  border-radius: 16px;
  background: rgba(0, 20, 50, 0.75);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(0, 212, 255, 0.4);
  box-shadow:
    0 0 40px rgba(0, 212, 255, 0.15),
    0 20px 60px rgba(0, 0, 0, 0.5),
    inset 0 1px 0 rgba(0, 212, 255, 0.2);
  /* 卡片入场动画 */
  animation: card-enter 0.7s cubic-bezier(0.22, 1, 0.36, 1) both;
}

@keyframes card-enter {
  from { opacity: 0; transform: translateY(30px) scale(0.96); }
  to   { opacity: 1; transform: translateY(0)   scale(1);     }
}

/* 扫光动画边框层 */
.card-glow-border {
  position: absolute;
  inset: -1px;
  border-radius: 16px;
  background: linear-gradient(
    130deg,
    transparent 0%,
    transparent 35%,
    rgba(0, 212, 255, 0.5) 50%,
    transparent 65%,
    transparent 100%
  );
  background-size: 300% 300%;
  animation: border-sweep 4s linear infinite;
  pointer-events: none;
  z-index: -1;
  opacity: 0.6;
}

@keyframes border-sweep {
  0%   { background-position: 200% 0%; }
  100% { background-position: -100% 0%; }
}

/* ===== Logo 区域 ===== */
.login-logo {
  text-align: center;
  margin-bottom: 10px;
}
.logo-icon {
  font-size: 40px;
  display: inline-block;
  animation: anchor-float 3s ease-in-out infinite;
  filter: drop-shadow(0 0 10px rgba(0, 212, 255, 0.8));
}

@keyframes anchor-float {
  0%, 100% { transform: translateY(0);    }
  50%       { transform: translateY(-6px); }
}

/* ===== 标题 ===== */
.login-title {
  font-size: 22px;
  font-weight: bold;
  text-align: center;
  letter-spacing: 3px;
  margin-bottom: 4px;
  /* 青白渐变文字 */
  background: linear-gradient(135deg, #ffffff 0%, #00d4ff 60%, #0099cc 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: none; /* 渐变文字不能用 text-shadow，用 filter 替代 */
  filter: drop-shadow(0 0 10px rgba(0, 212, 255, 0.6));
}

.login-subtitle {
  font-size: 11px;
  text-align: center;
  color: rgba(0, 212, 255, 0.5);
  letter-spacing: 2px;
  margin-bottom: 28px;
}

/* ===== 表单 ===== */
.login-form {
  margin-top: 0;
}

/* 覆盖 EP 表单项底部间距 */
:deep(.el-form-item) {
  margin-bottom: 20px;
}

/* ===== 输入框深海样式 ===== */
:deep(.ocean-input .el-input__wrapper) {
  background: rgba(0, 30, 60, 0.7) !important;
  border: 1px solid rgba(0, 212, 255, 0.35) !important;
  border-radius: 8px !important;
  box-shadow: none !important;
  transition: border-color 0.3s ease, box-shadow 0.3s ease !important;
  padding: 0 12px !important;
  height: 44px !important;
}
:deep(.ocean-input .el-input__wrapper:hover) {
  border-color: rgba(0, 212, 255, 0.6) !important;
}
:deep(.ocean-input .el-input__wrapper.is-focus) {
  border-color: #00d4ff !important;
  box-shadow: 0 0 8px rgba(0, 212, 255, 0.5), inset 0 0 6px rgba(0, 212, 255, 0.06) !important;
}
:deep(.ocean-input .el-input__inner) {
  color: #e0f0ff !important;
  font-size: 14px !important;
  height: 44px !important;
  line-height: 44px !important;
}
:deep(.ocean-input .el-input__inner::placeholder) {
  color: rgba(0, 180, 255, 0.4) !important;
}
:deep(.ocean-input .el-input__prefix .el-icon) {
  color: #00d4ff !important;
  filter: drop-shadow(0 0 4px rgba(0, 212, 255, 0.6));
}
:deep(.ocean-input .el-input__suffix .el-icon) {
  color: rgba(0, 180, 255, 0.5) !important;
}

/* ===== 登录按钮 ===== */
:deep(.el-form-item:last-child) {
  margin-bottom: 0;
}
:deep(.el-form-item:last-child .el-form-item__content) {
  margin-left: 0 !important;
}

.login-btn {
  position: relative;
  overflow: hidden;
  width: 100%;
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  color: #001a33;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  outline: none;
  /* 青蓝渐变 */
  background: linear-gradient(90deg, #0077b6 0%, #0099cc 45%, #00d4ff 100%);
  background-size: 200% 100%;
  background-position: 0% 50%;
  transition: background-position 0.5s ease, box-shadow 0.3s ease, transform 0.2s ease;
  box-shadow: 0 4px 15px rgba(0, 212, 255, 0.3);
}

.login-btn:hover:not(:disabled) {
  background-position: 100% 50%; /* 渐变方向微移 */
  box-shadow: 0 0 20px rgba(0, 212, 255, 0.6), 0 6px 20px rgba(0, 100, 200, 0.4);
  transform: translateY(-2px);
}

.login-btn:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 0 10px rgba(0, 212, 255, 0.4);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

/* 水波纹 keyframes（JS 动态注入 span 使用） */
@keyframes ripple-expand {
  0%   { transform: scale(0); opacity: 0.8; }
  100% { transform: scale(1); opacity: 0;   }
}

/* Loading 三点动画 */
.btn-loading {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}
.dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #001a33;
  animation: dot-bounce 1.2s ease-in-out infinite;
}
.dot:nth-child(1) { animation-delay: 0s;    }
.dot:nth-child(2) { animation-delay: 0.2s;  }
.dot:nth-child(3) { animation-delay: 0.4s;  }

@keyframes dot-bounce {
  0%, 80%, 100% { transform: scale(0.8); opacity: 0.5; }
  40%           { transform: scale(1.2); opacity: 1;   }
}

/* ===== 表单错误提示颜色 ===== */
:deep(.el-form-item__error) {
  color: #ff6b6b !important;
  padding-top: 3px !important;
}
</style>