<template>
  <div class="hall-page">
    <!-- 顶部导航栏：磨砂玻璃+海洋蓝 -->
    <div class="hall-header">
      <div class="header-left">海渔合规管理平台 - 功能大厅</div>
      <div class="header-right">
        <el-dropdown trigger="hover">
          <span class="user-info">
            <el-avatar :icon="User" class="user-avatar" />
            {{ userInfo.realName || userInfo.username }}
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </span>
          <template v-slot:dropdown>
            <el-dropdown-menu>
              <el-dropdown-item disabled>账号：{{ userInfo.username }}</el-dropdown-item>
              <el-dropdown-item disabled>角色：{{ userInfo.role === 2 ? '管理员' : '渔民' }}</el-dropdown-item>
              <el-dropdown-item disabled>手机号：{{ userInfo.phone || '未绑定' }}</el-dropdown-item>
              <el-divider />
              <el-dropdown-item @click="handleLogout" style="color: #f56c6c;">
                <el-icon></el-icon> 退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 主体功能卡片：优化布局+均匀分布 -->
    <div class="hall-main">
      <div class="func-card-container">
        <!-- 功能1：基础合规查询 -->
        <div class="func-card active" @click="toFunc('compliance')">
          <div class="func-icon"><el-icon><Search /></el-icon></div>
          <div class="func-name">基础合规查询模块</div>
          <div class="func-desc">海域/时间/鱼种查询 · 政策口语化解读</div>
        </div>

        <!-- 功能2：物种识别与合规校验 -->
        <el-tooltip content="功能暂未开发，敬请期待！" placement="top">
          <div class="func-card disabled">
            <div class="func-icon"><el-icon><Camera /></el-icon></div>
            <div class="func-name">物种识别与合规校验模块</div>
            <div class="func-desc">AI识别 · 误捕应急指引</div>
          </div>
        </el-tooltip>
        <!-- 功能3：捕捞日志与合规自查 -->
<div class="func-card active" @click="toFunc('fishingLog')">
  <div class="func-icon"><el-icon><Document /></el-icon></div>
  <div class="func-name">捕捞日志与合规自查模块</div>
  <div class="func-desc">日志录入 · 自动校验 · 报告导出</div>
</div>

        <!-- 功能4：系统管理模块（管理员可见） -->
        <div v-if="userInfo.role === 2">
          <el-tooltip content="功能暂未开发，敬请期待！" placement="top">
            <div class="func-card disabled">
              <div class="func-icon"><el-icon><Setting /></el-icon></div>
              <div class="func-name">系统管理模块</div>
              <div class="func-desc">基础数据管理 · 用户/日志管理</div>
            </div>
          </el-tooltip>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElDropdown, ElAvatar } from 'element-plus'
import { User, ArrowDown, Search, Camera, Document, Setting } from '@element-plus/icons-vue'
import userApi from '@/api/user'
import { useRouter } from 'vue-router'

const router = useRouter()
const userInfo = ref({
  username: '',
  realName: '',
  role: 0,
  phone: ''
})

onMounted(() => {
  loadUserInfo()
})

const loadUserInfo = async () => {
  try {
    const res = await userApi.getUserInfo()
    if (res.code === 200) {
      userInfo.value = res.data
    } else {
      ElMessage.error(res.msg)
      sessionStorage.removeItem('loginUserId')
      router.push('/login')
    }
  } catch (error) {
    ElMessage.error('获取个人信息失败')
    sessionStorage.removeItem('loginUserId')
    router.push('/login')
  }
}

const toFunc = (path) => {
  router.push(`/${path}`)
}

const handleLogout = async () => {
  try {
    const res = await userApi.logout()
    if (res.code === 200) {
      ElMessage.success(res.msg)
    }
    sessionStorage.removeItem('loginUserId')
    router.push('/login')
  } catch (error) {
    ElMessage.error('退出登录失败，强制退出')
    sessionStorage.removeItem('loginUserId')
    router.push('/login')
  }
}
</script>

<style scoped>
.hall-page {
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #f0f8ff, #e6f7ff);
  overflow: hidden;
}

/* 顶部导航：磨砂玻璃效果 */
.hall-header {
  height: 64px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 40px;
  border-bottom: 1px solid rgba(64, 158, 255, 0.1);
}

.header-left {
  font-size: 18px;
  font-weight: bold;
  color: #0066cc;
  letter-spacing: 1px;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #0052aa;
  font-size: 14px;
}

.user-avatar {
  margin-right: 10px;
  --el-avatar-bg-color: #409eff;
}

/* 主体区域：优化内边距，让卡片不贴边 */
.hall-main {
  flex: 1;
  padding: 40px 60px;
  overflow: auto;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 核心：卡片容器 - 网格布局实现均匀分布 */
.func-card-container {
  width: 100%;
  max-width: 1200px; /* 限制最大宽度，避免大屏下卡片过宽 */
  display: grid;
  grid-template-columns: repeat(2, 1fr); /* 固定2列 */
  grid-template-rows: repeat(2, 1fr);    /* 固定2行 */
  gap: 30px; /* 关键：统一卡片间距（横向+纵向），替代原有的gutter */
  align-items: stretch; /* 让卡片高度自适应行高 */
}

/* 功能卡片基础样式 */
.func-card {
  height: 100%; /* 自适应容器行高，不再固定高度 */
  min-height: 220px; /* 最小高度，避免卡片过矮 */
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: all 0.4s ease;
  color: #fff;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
}

/* 激活态卡片：海洋渐变+流动动效 */
.func-card.active {
  background: linear-gradient(135deg, #0066cc, #409eff, #66b1ff);
  background-size: 200% 200%;
  animation: wave-flow 8s ease infinite;
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.2);
}

/* 渐变流动动画 */
@keyframes wave-flow {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

/* 悬浮效果：上移+波浪阴影 */
.func-card.active:hover {
  transform: translateY(-8px);
  box-shadow: 0 15px 30px rgba(64, 158, 255, 0.3), 0 0 20px rgba(102, 177, 255, 0.2);
}

/* 禁用态卡片：浅灰磨砂 */
.func-card.disabled {
  background: rgba(245, 247, 250, 0.8);
  backdrop-filter: blur(5px);
  color: #90b8e8;
  cursor: not-allowed;
  border: 1px solid rgba(64, 158, 255, 0.1);
}

.func-icon {
  font-size: 56px;
  margin-bottom: 24px;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.1));
}

.func-name {
  font-size: 22px;
  font-weight: bold;
  margin-bottom: 12px;
  letter-spacing: 1px;
}

.func-desc {
  font-size: 14px;
  opacity: 0.9;
  text-align: center;
  padding: 0 20px;
  line-height: 1.5;
}

/* 下拉菜单样式优化 */
:deep(.el-dropdown-menu) {
  --el-dropdown-menu-border-radius: 8px;
  --el-dropdown-menu-bg-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(64, 158, 255, 0.2);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

:deep(.el-divider) {
  --el-divider-color: rgba(64, 158, 255, 0.1);
}

/* 响应式适配：小屏时卡片宽度自适应 */
@media (max-width: 992px) {
  .func-card-container {
    grid-template-columns: 1fr; /* 小屏改为1列 */
    gap: 20px;
  }
  .hall-main {
    padding: 20px 30px;
  }
}
</style>