<template>
  <div class="hall-page">
    <!-- 顶部导航栏 -->
    <div class="hall-header">
      <div class="header-left">
        <div class="wave-icon">
          <span></span><span></span><span></span><span></span><span></span>
        </div>
        海渔合规管理平台 - 功能大厅
      </div>
      <div class="header-right">
        <el-dropdown trigger="click">
          <span class="user-info">
            <div class="avatar-wrapper">
              <el-avatar :icon="User" class="user-avatar" />
              <span class="online-dot"></span>
            </div>
            {{ userInfo.realName || userInfo.username }}
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </span>
          <template v-slot:dropdown>
            <el-dropdown-menu>
              <el-dropdown-item disabled>
                <strong>账号：</strong>{{ userInfo.username }}
              </el-dropdown-item>
              <el-dropdown-item disabled>
                <strong>角色：</strong>{{ userInfo.role === 2 ? '管理员' : '渔民' }}
              </el-dropdown-item>
              <el-dropdown-item disabled>
                <strong>手机号：</strong>{{ userInfo.phone || '未绑定' }}
              </el-dropdown-item>
              <el-divider />
              <el-dropdown-item @click="handleLogout" style="color: #f56c6c;">
                <el-icon><SwitchButton /></el-icon> 退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 主体布局：左侧功能菜单 + 右侧内容区 -->
    <div class="hall-main">
      <!-- 左侧功能菜单 -->
      <div class="sidebar">
        <div class="menu-item" v-stagger="0" :class="{ active: activeMenu === 'compliance' }" @click="navigateTo('compliance')">
          <span class="menu-icon-emoji menu-icon-pulse">🛡️</span>
          <span>基础合规查询</span>
        </div>
        <div class="menu-item" v-stagger="70" :class="{ active: activeMenu === 'species' }" @click="navigateTo('species')">
          <span class="menu-icon-emoji menu-icon-wiggle">🐟</span>
          <span>物种识别校验</span>
        </div>
        <div class="menu-item" v-stagger="140" :class="{ active: activeMenu === 'fishingLog' }" @click="navigateTo('fishingLog')">
          <span class="menu-icon-emoji menu-icon-bob">⚓</span>
          <span>捕捞日志管理</span>
        </div>
        <div v-if="userInfo.role === 2" class="menu-item" v-stagger="210" :class="{ active: activeMenu === 'system' }" @click="navigateTo('system')">
          <span class="menu-icon-emoji menu-icon-spin">⚙️</span>
          <span>系统管理</span>
        </div>
      </div>

      <!-- 右侧内容区 -->
      <div class="content-area">
        <div class="visualization-section">
          <div class="time-filter" style="margin-bottom: 20px;">
            <el-radio-group v-model="timeRange" @change="updateVisualization">
              <el-radio-button label="day">今日</el-radio-button>
              <el-radio-button label="week">近一周</el-radio-button>
              <el-radio-button label="month">近一个月</el-radio-button>
              <el-radio-button label="year">近一年</el-radio-button>
            </el-radio-group>
          </div>

          <!-- 统计数字摘要行（数字滚动动效） -->
          <div class="stats-row">
            <div class="stat-card" v-stagger="0">
              <div class="stat-number" ref="statTotal">0</div>
              <div class="stat-label">总日志数</div>
            </div>
            <div class="stat-card" v-stagger="80">
              <div class="stat-number stat-success" ref="statCompliant">0</div>
              <div class="stat-label">合规</div>
            </div>
            <div class="stat-card" v-stagger="160">
              <div class="stat-number stat-danger" ref="statUncompliant">0</div>
              <div class="stat-label">违规</div>
            </div>
            <div class="stat-card" v-stagger="240">
              <div class="stat-number stat-primary" ref="statRate">0%</div>
              <div class="stat-label">合规率</div>
            </div>
          </div>

          <el-row :gutter="20">
            <el-col :span="12">
              <div class="chart-card" v-stagger="60">
                <div class="chart-card-inner">
                  <h3>合规情况统计</h3>
                  <div id="compliancePie" style="width: 100%; height: 300px"></div>
                </div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="chart-card" v-stagger="200">
                <div class="chart-card-inner">
                  <h3>各捕捞鱼种重量 (kg)</h3>
                  <div id="speciesWeightBar" style="width: 100%; height: 300px"></div>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage, ElDropdown, ElAvatar } from 'element-plus'
import { User, ArrowDown, SwitchButton } from '@element-plus/icons-vue'
import userApi from '@/api/user'
import fishingLogApi from '@/api/fishingLog'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { countUp } from '@/utils/countUp'

const router = useRouter()
const userInfo = ref({
  username: '',
  realName: '',
  role: 0,
  phone: ''
})

// 时间筛选
const timeRange = ref('month')
// 当前激活菜单
const activeMenu = ref('')

// 图表实例
let compliancePieChart = null
let speciesWeightBarChart = null

// 统计数字 DOM 引用
const statTotal = ref(null)
const statCompliant = ref(null)
const statUncompliant = ref(null)
const statRate = ref(null)

// 加载用户信息
onMounted(async () => {
  await loadUserInfo()
  // 初始化可视化图表
  await updateVisualization()
  
  // 添加窗口大小变化监听器
  window.addEventListener('resize', handleResize)
})

// 组件卸载前清理资源
onBeforeUnmount(() => {
  // 销毁图表实例
  if (compliancePieChart) compliancePieChart.dispose()
  if (speciesWeightBarChart) speciesWeightBarChart.dispose()
  
  // 移除事件监听器
  window.removeEventListener('resize', handleResize)
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

const navigateTo = (path) => {
  activeMenu.value = path
  router.push(`/${path}`)
}

const updateVisualization = async () => {
  const userId = sessionStorage.getItem('loginUserId')
  if (!userId) {
    ElMessage.warning('请先登录')
    return
  }

  try {
    // 调用后端接口获取图表数据
    const res = await fishingLogApi.getChartData({ userId, timeRange: timeRange.value })
    
    // 处理后端返回的数据
    const chartData = res?.code === 200 ? (res.data || {}) : (res || {})
    if (!chartData) {
      ElMessage.warning('后端未返回可视化统计数据')
      return
    }

    // 转换数据格式以供ECharts使用
    const finalEchartsData = {
      pieData: [
        { name: '合规', value: chartData.compliantCount || 0 },
        { name: '违规', value: chartData.uncompliantCount || 0 }
      ],
      barXData: (chartData.speciesWeightData || []).map(item => item.name || '未知鱼种'),
      barYData: (chartData.speciesWeightData || []).map(item => item.value || 0)
    }

    // 初始化或更新图表
    await nextTick()
    initCharts(finalEchartsData)
    // 统计数字滚动动效
    const total = (chartData.compliantCount || 0) + (chartData.uncompliantCount || 0)
    const rate = total > 0 ? Math.round((chartData.compliantCount || 0) / total * 100) : 0
    countUp(statTotal.value, total)
    countUp(statCompliant.value, chartData.compliantCount || 0)
    countUp(statUncompliant.value, chartData.uncompliantCount || 0)
    countUp(statRate.value, rate, { suffix: '%' })
  } catch (error) {
    console.error('获取可视化数据失败：', error)
    ElMessage.error('获取可视化数据失败')
  }
}

const initCharts = (chartData) => {
  // 初始化合规饼图
  if (compliancePieChart) compliancePieChart.dispose()
  compliancePieChart = echarts.init(document.getElementById('compliancePie'))
  const pieOption = {
    backgroundColor: 'transparent',
    color: ['#00d4aa', '#ff4d6b'],
    tooltip: {
      trigger: 'item',
      formatter: '{b}：{c} 条 ({d}%)',
      backgroundColor: 'rgba(0,20,50,0.92)',
      borderColor: 'rgba(0,212,255,0.4)',
      textStyle: { color: '#b0d8f0' }
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: ['合规', '违规'],
      textStyle: { color: '#7ab8d8' }
    },
    title: { text: chartData?.pieData?.length ? '' : '暂无合规统计数据', left: 'center', top: '45%', textStyle: { color: '#7ab8d8', fontSize: 14 } },
    series: [{
      name: '合规状态',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '50%'],
      data: chartData?.pieData || [],
      label: { show: true, position: 'outside', color: '#b0d8f0' },
      labelLine: { lineStyle: { color: 'rgba(0,212,255,0.5)' } },
      itemStyle: {
        shadowBlur: 15,
        shadowColor: '#00d4ff',
        borderColor: 'rgba(0,10,30,0.8)',
        borderWidth: 2
      },
      silent: !chartData?.pieData?.length
    }]
  }
  compliancePieChart.setOption(pieOption)

  // 初始化鱼种重量柱状图
  if (speciesWeightBarChart) speciesWeightBarChart.dispose()
  speciesWeightBarChart = echarts.init(document.getElementById('speciesWeightBar'))
  const barOption = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>{a}: {c} kg',
      backgroundColor: 'rgba(0,20,50,0.92)',
      borderColor: 'rgba(0,212,255,0.4)',
      textStyle: { color: '#b0d8f0' }
    },
    title: { text: chartData?.barXData?.length ? '' : '暂无鱼种重量统计数据', left: 'center', top: '45%', textStyle: { color: '#7ab8d8', fontSize: 14 } },
    grid: { left: '3%', right: '4%', bottom: '15%', top: '15%', containLabel: true },
    xAxis: {
      type: 'category',
      data: chartData?.barXData || [],
      show: !!chartData?.barXData?.length,
      axisLabel: { rotate: 45, color: '#7ab8d8' },
      axisLine: { lineStyle: { color: 'rgba(0,212,255,0.3)' } },
      axisTick: { lineStyle: { color: 'rgba(0,212,255,0.2)' } }
    },
    yAxis: {
      type: 'value',
      name: '重量(kg)',
      show: !!chartData?.barXData?.length,
      nameTextStyle: { color: '#7ab8d8' },
      axisLabel: { color: '#7ab8d8' },
      axisLine: { lineStyle: { color: 'rgba(0,212,255,0.3)' } },
      splitLine: { lineStyle: { color: 'rgba(0,212,255,0.1)', type: 'dashed' } }
    },
    series: [{
      name: '鱼种重量',
      type: 'bar',
      data: chartData?.barYData || [],
      barWidth: '60%',
      silent: !chartData?.barXData?.length,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#00d4ff' },
          { offset: 0.5, color: '#0099cc' },
          { offset: 1, color: '#003d7a' }
        ]),
        shadowBlur: 8,
        shadowColor: 'rgba(0,212,255,0.4)',
        borderRadius: [4, 4, 0, 0]
      },
      emphasis: {
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#33eaff' },
            { offset: 1, color: '#0077bb' }
          ])
        }
      }
    }]
  }
  speciesWeightBarChart.setOption(barOption)
}

const handleResize = () => {
  if (compliancePieChart) compliancePieChart.resize()
  if (speciesWeightBarChart) speciesWeightBarChart.resize()
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
/* ===== 整体页面 ===== */
.hall-page {
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* ===== 顶部导航栏 ===== */
.hall-header {
  height: 64px;
  background: transparent;
  border-bottom: 1px solid rgb(1, 111, 255);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 40px;
  z-index: 100;
  position: relative;
  flex-shrink: 0;
}

.header-left {
  font-size: 18px;
  font-weight: bold;
  letter-spacing: 2px;
  color: #00d4ff;
  display: flex;
  align-items: center;
  gap: 12px;
  text-shadow: 0 0 10px rgba(0, 212, 255, 0.5);
}

/* 海浪波纹 loading 图标 */
.wave-icon {
  display: inline-flex;
  align-items: flex-end;
  gap: 4px;
  height: 26px;
  overflow: visible;
}

.wave-icon span {
  display: inline-block;
  width: 4px;
  background: linear-gradient(to top, #00aaee, #66eeff);
  border-radius: 3px;
  box-shadow: 0 0 8px #00d4ff, 0 0 14px rgba(0, 212, 255, 0.5);
  transform-origin: bottom center;
  animation: wave-bar 1.3s ease-in-out infinite;
}

.wave-icon span:nth-child(1) { height: 10px; animation-delay: 0s; }
.wave-icon span:nth-child(2) { height: 20px; animation-delay: 0.18s; }
.wave-icon span:nth-child(3) { height: 13px; animation-delay: 0.36s; }
.wave-icon span:nth-child(4) { height: 22px; animation-delay: 0.54s; }
.wave-icon span:nth-child(5) { height: 9px;  animation-delay: 0.72s; }

@keyframes wave-bar {
  0%, 100% { transform: scaleY(0.45); opacity: 0.6; }
  50%       { transform: scaleY(1.25); opacity: 1; }
}

/* ===== 用户信息区域 ===== */
.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #e8f6ff;
  font-size: 14px;
  font-weight: 500;
  gap: 8px;
  padding: 6px 14px;
  border-radius: 8px;
  border: 1px solid transparent;
  transition: all 0.3s ease;
  text-shadow: 0 0 6px rgba(0, 212, 255, 0.3);
}

.user-info:hover {
  background: rgba(0, 212, 255, 0.1);
  border-color: rgba(0, 212, 255, 0.35);
  color: #ffffff;
  text-shadow: 0 0 10px rgba(0, 212, 255, 0.6);
}

.avatar-wrapper {
  position: relative;
  display: inline-flex;
  flex-shrink: 0;
}

.user-avatar {
  --el-avatar-bg-color: rgba(0, 60, 120, 0.9) !important;
  border: 2px solid rgba(0, 212, 255, 0.5) !important;
  border-radius: 50% !important;
  box-shadow: 0 0 8px rgba(0, 212, 255, 0.3);
  transition: border-color 0.3s, box-shadow 0.4s;
}

.user-info:hover .user-avatar {
  border-color: #00d4ff !important;
  box-shadow: 0 0 14px rgba(0, 212, 255, 0.7);
  animation: avatar-halo-spin 3s linear infinite;
}

@keyframes avatar-halo-spin {
  0%   { box-shadow: 0 0 14px rgba(0,212,255,0.7),  2px  0   6px rgba(0,212,255,0.4); }
  25%  { box-shadow: 0 0 14px rgba(0,212,255,0.7),  0    2px 6px rgba(0,212,255,0.4); }
  50%  { box-shadow: 0 0 14px rgba(0,212,255,0.7), -2px  0   6px rgba(0,212,255,0.4); }
  75%  { box-shadow: 0 0 14px rgba(0,212,255,0.7),  0   -2px 6px rgba(0,212,255,0.4); }
  100% { box-shadow: 0 0 14px rgba(0,212,255,0.7),  2px  0   6px rgba(0,212,255,0.4); }
}

.online-dot {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 10px;
  height: 10px;
  background: #00ff88;
  border-radius: 50%;
  border: 2px solid rgba(0, 10, 30, 0.9);
  box-shadow: 0 0 6px rgba(0, 255, 136, 0.8);
  animation: dot-pulse 2s ease-in-out infinite;
}

@keyframes dot-pulse {
  0%, 100% { box-shadow: 0 0 6px rgba(0,255,136,0.8); transform: scale(1); }
  50%       { box-shadow: 0 0 12px rgba(0,255,136,1), 0 0 22px rgba(0,255,136,0.4); transform: scale(1.15); }
}

/* ===== 主体布局 ===== */
.hall-main {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* ===== 左侧菜单 ===== */
.sidebar {
  width: 240px;
  background: rgba(0, 15, 40, 0.8);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-right: 1px solid var(--ocean-border, rgba(0, 212, 255, 0.3));
  padding: 20px 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow-y: auto;
  flex-shrink: 0;
}

.menu-item {
  position: relative;
  display: flex;
  align-items: center;
  padding: 13px 20px;
  margin: 3px 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.3s ease, color 0.3s ease, box-shadow 0.3s ease;
  color: #a8d8f0;
  font-size: 15px;
  font-weight: 500;
  overflow: hidden;
  background: rgba(0, 40, 80, 0.4);
  border: 1px solid rgba(0, 150, 200, 0.2);
}

/* 左侧竖条高亮 */
.menu-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 2px;
  height: 55%;
  background: #00d4ff;
  border-radius: 2px;
  opacity: 0;
  transition: width 0.25s ease, height 0.25s ease, opacity 0.25s ease, box-shadow 0.25s ease;
}

.menu-item:hover::before {
  width: 4px;
  height: 75%;
  opacity: 1;
  box-shadow: 0 0 10px rgba(0, 212, 255, 0.9), 0 0 20px rgba(0, 212, 255, 0.4);
}

.menu-item.active::before {
  width: 3px;
  height: 65%;
  opacity: 1;
  box-shadow: 0 0 8px rgba(0, 212, 255, 0.7);
}

.menu-item:hover {
  background: rgba(0, 140, 200, 0.18);
  border-color: rgba(0, 212, 255, 0.4);
  color: #00d4ff;
  box-shadow: 0 0 12px rgba(0, 212, 255, 0.15);
  text-shadow: 0 0 8px rgba(0, 212, 255, 0.4);
}

.menu-item.active {
  background: linear-gradient(90deg, rgba(0, 180, 255, 0.25), rgba(0, 80, 140, 0.15));
  border-color: rgba(0, 212, 255, 0.5);
  color: #00d4ff;
  font-weight: 600;
  box-shadow: 0 0 14px rgba(0, 212, 255, 0.2);
}

/* 菜单 emoji 图标 */
.menu-icon-emoji {
  font-size: 18px;
  margin-right: 10px;
  width: 26px;
  text-align: center;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s ease;
  line-height: 1;
  flex-shrink: 0;
}

/* hover 时各图标动效 */
.menu-item:hover .menu-icon-pulse  { animation: icon-pulse  0.5s ease forwards; }
.menu-item:hover .menu-icon-bob    { animation: icon-bob    0.6s ease forwards; }
.menu-item:hover .menu-icon-wiggle { animation: icon-wiggle 0.5s ease forwards; }
.menu-item:hover .menu-icon-spin   { animation: icon-spin-half 0.5s ease forwards; }

@keyframes icon-pulse {
  0%   { transform: scale(1); }
  50%  { transform: scale(1.35); }
  100% { transform: scale(1.12); }
}

@keyframes icon-bob {
  0%   { transform: translateY(0); }
  40%  { transform: translateY(-5px); }
  70%  { transform: translateY(-2px); }
  100% { transform: translateY(-3px); }
}

@keyframes icon-wiggle {
  0%   { transform: rotate(0deg); }
  25%  { transform: rotate(-18deg); }
  75%  { transform: rotate(18deg); }
  100% { transform: rotate(0deg); }
}

@keyframes icon-spin-half {
  from { transform: rotate(0deg); }
  to   { transform: rotate(180deg); }
}

/* ===== 右侧内容区 ===== */
.content-area {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.visualization-section {
  background: rgba(0, 15, 40, 0.6);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 24px;
  border: 1px solid rgba(0, 212, 255, 0.15);
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.3);
}

/* ===== 图表卡片 - 旋转 conic 流水边框 ===== */
.chart-card {
  position: relative;
  border-radius: 10px;
  padding: 1px;
  margin-bottom: 20px;
  overflow: hidden;
}

.chart-card::before {
  content: '';
  position: absolute;
  width: 200%;
  height: 200%;
  top: -50%;
  left: -50%;
  background: conic-gradient(
    from 0deg,
    transparent 0deg,
    rgba(0, 212, 255, 0.9) 40deg,
    rgba(0, 150, 200, 0.3) 80deg,
    transparent 160deg,
    rgba(0, 80, 160, 0.5) 250deg,
    rgba(0, 212, 255, 0.8) 320deg,
    transparent 360deg
  );
  animation: rotate-flow 4s linear infinite;
  z-index: 0;
}

@keyframes rotate-flow {
  from { transform: rotate(0deg); }
  to   { transform: rotate(360deg); }
}

.chart-card-inner {
  position: relative;
  z-index: 1;
  background: rgba(0, 20, 55, 0.88);
  border-radius: 9px;
  padding: 16px;
}

.chart-card-inner h3 {
  margin: 0 0 15px 0;
  font-size: 16px;
  color: #00d4ff;
  text-align: center;
  text-shadow: 0 0 8px rgba(0, 212, 255, 0.5);
  letter-spacing: 1px;
}

/* ===== 下拉菜单深海主题 ===== */
:deep(.el-dropdown-menu) {
  background: rgba(0, 20, 50, 0.95) !important;
  backdrop-filter: blur(15px);
  border: 1px solid rgba(0, 212, 255, 0.3) !important;
  border-radius: 8px !important;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.5), 0 0 20px rgba(0, 212, 255, 0.1) !important;
}

:deep(.el-dropdown-menu__item) {
  color: #b0d8f0 !important;
}

:deep(.el-dropdown-menu__item:not(.is-disabled):hover) {
  background: rgba(0, 212, 255, 0.1) !important;
  color: #00d4ff !important;
}

:deep(.el-divider) {
  --el-divider-color: rgba(0, 212, 255, 0.2);
  margin: 6px 0;
}

/* 时间筛选 radio-button */
:deep(.el-radio-button__inner) {
  background: rgba(0, 20, 50, 0.7) !important;
  border-color: rgba(0, 212, 255, 0.3) !important;
  color: #7ab8d8 !important;
  transition: all 0.3s;
}

:deep(.el-radio-button__inner:hover) {
  color: #00d4ff !important;
  border-color: rgba(0, 212, 255, 0.5) !important;
}

:deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, #0055aa, #0099cc) !important;
  border-color: #00d4ff !important;
  box-shadow: -1px 0 0 0 #00d4ff, 0 0 10px rgba(0, 212, 255, 0.4) !important;
  color: #fff !important;
}

/* ===== 统计数字摘要行 ===== */
.stats-row {
  display: flex;
  gap: 16px;
  margin-bottom: 22px;
}
.stat-card {
  flex: 1;
  background: rgba(0, 15, 45, 0.75);
  border: 1px solid rgba(0, 212, 255, 0.18);
  border-radius: 10px;
  padding: 14px 12px;
  text-align: center;
  backdrop-filter: blur(8px);
}
.stat-number {
  font-size: 30px;
  font-weight: 700;
  color: #c8e8ff;
  font-variant-numeric: tabular-nums;
  letter-spacing: 1px;
  line-height: 1;
}
.stat-success { color: #00ff88; text-shadow: 0 0 14px rgba(0, 255, 136, 0.5); }
.stat-danger  { color: #ff6688; text-shadow: 0 0 14px rgba(255, 68, 104, 0.5); }
.stat-primary { color: #00d4ff; text-shadow: 0 0 14px rgba(0, 212, 255, 0.5); }
.stat-label {
  font-size: 11px;
  color: rgba(150, 200, 240, 0.65);
  margin-top: 6px;
  letter-spacing: 2px;
  text-transform: uppercase;
}
</style>
