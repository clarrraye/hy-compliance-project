<template>
  <div class="hall-page">
    <!-- 顶部导航栏：磨砂玻璃+海洋蓝 -->
    <div class="hall-header">
      <div class="header-left">海渔合规管理平台 - 功能大厅</div>
      <div class="header-right">
        <el-dropdown trigger="click">
          <span class="user-info">
            <el-avatar :icon="User" class="user-avatar" />
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
        <div class="menu-item" @click="navigateTo('compliance')">
          <el-icon><Search /></el-icon>
          <span>基础合规查询</span>
        </div>
        <div class="menu-item" @click="navigateTo('species')">
          <el-icon><Camera /></el-icon>
          <span>物种识别校验</span>
        </div>
        <div class="menu-item" @click="navigateTo('fishingLog')">
          <el-icon><Document /></el-icon>
          <span>捕捞日志管理</span>
        </div>
        <div v-if="userInfo.role === 2" class="menu-item" @click="navigateTo('system')">
          <el-icon><Setting /></el-icon>
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
          
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="chart-card">
                <h3>合规情况统计</h3>
                <div id="compliancePie" style="width: 100%; height: 300px"></div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="chart-card">
                <h3>各捕捞鱼种重量 (kg)</h3>
                <div id="speciesWeightBar" style="width: 100%; height: 300px"></div>
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
import { User, ArrowDown, Search, Camera, Document, Setting, SwitchButton } from '@element-plus/icons-vue'
import userApi from '@/api/user'
import fishingLogApi from '@/api/fishingLog'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'

const router = useRouter()
const userInfo = ref({
  username: '',
  realName: '',
  role: 0,
  phone: ''
})

// 时间筛选
const timeRange = ref('month')

// 图表实例
let compliancePieChart = null
let speciesWeightBarChart = null

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
    color: ['#67c23a', '#f56c6c'],
    tooltip: { trigger: 'item', formatter: '{b}：{c} 条 ({d}%)' },
    legend: { orient: 'vertical', left: 'left', data: ['合规', '违规'] },
    title: { text: chartData?.pieData?.length ? '' : '暂无合规统计数据', left: 'center', top: '45%', textStyle: { color: '#999' } },
    series: [{
      name: '合规状态',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '50%'],
      data: chartData?.pieData || [],
      label: { show: true, position: 'outside' },
      silent: !chartData?.pieData?.length
    }]
  }
  compliancePieChart.setOption(pieOption)

  // 初始化鱼种重量柱状图
  if (speciesWeightBarChart) speciesWeightBarChart.dispose()
  speciesWeightBarChart = echarts.init(document.getElementById('speciesWeightBar'))
  const barOption = {
    color: ['#409eff'],
    tooltip: { trigger: 'axis', formatter: '{b}<br/>{a}: {c} kg' },
    title: { text: chartData?.barXData?.length ? '' : '暂无鱼种重量统计数据', left: 'center', top: '45%', textStyle: { color: '#999' } },
    grid: { left: '3%', right: '4%', bottom: '15%', top: '15%', containLabel: true },
    xAxis: { 
      type: 'category', 
      data: chartData?.barXData || [], 
      show: chartData?.barXData?.length,
      axisLabel: { rotate: 45 }
    },
    yAxis: { 
      type: 'value', 
      name: '重量(kg)',
      show: chartData?.barXData?.length 
    },
    series: [{ 
      name: '鱼种重量', 
      type: 'bar', 
      data: chartData?.barYData || [], 
      barWidth: '60%',
      silent: !chartData?.barXData?.length
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
  z-index: 10;
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

/* 主体布局：左侧菜单 + 右侧内容 */
.hall-main {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* 左侧菜单 */
.sidebar {
  width: 240px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-right: 1px solid rgba(64, 158, 255, 0.1);
  padding: 20px 0;
  display: flex;
  flex-direction: column;
  gap: 5px;
  overflow-y: auto;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 14px 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #303133;
  font-size: 15px;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 14px 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #303133;
  font-size: 15px;
  transform-origin: center; /* 添加这行 */
}

.menu-item:hover {
  background: rgba(64, 158, 255, 0.1);
  color: #409eff;
  font-weight: bold;        /* 添加这行使字体加粗 */
  transform: scale(1.03);  /* 添加这行使卡片放大 */
}

.menu-item .el-icon {
  margin-right: 10px;
  width: 24px;
  text-align: center;
}

/* 右侧内容区 */
.content-area {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: rgba(248, 250, 252, 0.3);
}

.visualization-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.chart-card {
  background: #fafafa;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.chart-card h3 {
  margin: 0 0 15px 0;
  font-size: 16px;
  color: #303133;
  text-align: center;
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
</style>