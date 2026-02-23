<template>
  <div class="login-page">
    <el-card class="login-card" shadow="hover">
      <div class="login-title">海渔合规管理平台</div>
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
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入登录密码"
            prefix-icon="Lock"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" class="login-btn">
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <!-- 波纹装饰 -->
    <div class="wave wave1"></div>
    <div class="wave wave2"></div>
    <div class="wave wave3"></div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import userApi from '@/api/user'
import { useRouter } from 'vue-router'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)
const loginForm = ref({
  username: '',
  password: ''
})
const loginRules = ref({
  username: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入登录密码', trigger: 'blur' }]
})

const handleLogin = async () => {
  const valid = await loginFormRef.value.validate()
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
</script>

<style scoped>
.login-page {
  width: 100vw;
  height: 100vh;
  background: linear-gradient(180deg, #e6f7ff, #b3e5fc);
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
}
.login-card {
  width: 400px;
  padding: 40px 30px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(64, 158, 255, 0.1);
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.1);
  z-index: 10;
}
.login-title {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  color: #0066cc;
  margin-bottom: 30px;
  letter-spacing: 2px;
}
.login-form {
  margin-top: 10px;
}
.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  background: linear-gradient(90deg, #0066cc, #409eff);
  border: none;
  border-radius: 8px;
  transition: all 0.3s ease;
}
.login-btn:hover {
  background: linear-gradient(90deg, #0052aa, #3393ee);
  transform: scale(1.02);
}
/* 波纹动效 */
.wave {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  animation: wave-animate 15s infinite linear;
  z-index: 1;
}
.wave1 {
  width: 800px;
  height: 800px;
  bottom: -400px;
  left: -200px;
}
.wave2 {
  width: 600px;
  height: 600px;
  bottom: -300px;
  right: -100px;
  animation-delay: 3s;
}
.wave3 {
  width: 700px;
  height: 700px;
  bottom: -350px;
  left: 30%;
  animation-delay: 6s;
}
@keyframes wave-animate {
  0% {
    transform: translate(0, 0) rotate(0deg);
  }
  50% {
    transform: translate(20px, -20px) rotate(180deg);
  }
  100% {
    transform: translate(0, 0) rotate(360deg);
  }
}
:deep(.el-input) {
  --el-input-border-radius: 8px;
  --el-input-border-color: #cce5ff;
  --el-input-focus-border-color: #409eff;
}
:deep(.el-input__prefix) {
  color: #0066cc;
}
</style>