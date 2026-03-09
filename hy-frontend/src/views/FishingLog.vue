<template>
  <div class="fishing-log-page">
    <!-- 顶部导航栏：磨砂玻璃风格 -->
    <div class="log-header">
      <div class="header-left">海渔合规管理平台 - 捕捞日志与合规自查</div>
      <div class="header-right">
        <el-button class="btn-add" icon="Plus" @click="showAddDialog = true">新增捕捞日志</el-button>
        <el-button class="btn-export" icon="Download" @click="exportReport" :disabled="!logList.length">导出自查报告</el-button>
        <el-button class="btn-chart" icon="PieChart" @click="showChartDialog = true" :disabled="!logList.length">合规数据可视化</el-button>
      </div>
    </div>

    <!-- 日志列表区域 -->
    <div class="log-main">
      <!-- 列表查询条件 -->
      <div class="log-search">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="捕捞日期：">
            <el-date-picker
              v-model="searchForm.fishingDate"
              type="date"
              placeholder="选择日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              :disabled-date="(time) => time > Date.now()" 
            />
          </el-form-item>
          <el-form-item label="海域：">
            <el-select v-model="searchForm.seaId" placeholder="选择海域" clearable>
              <el-option
                v-for="sea in seaList"
                :key="sea.seaId"
                :label="sea.seaName"
                :value="sea.seaId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="合规状态：">
            <el-select v-model="searchForm.isCompliant" placeholder="全部" clearable>
              <el-option label="合规" value="1" />
              <el-option label="违规" value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="getLogList">查询</el-button>
            <el-button icon="Refresh" @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 日志列表：已添加?.空值防护 -->
      <div class="log-table">
        <el-table
          v-loading="loading"
          :data="logList"
          border
          stripe
          style="width: 100%"
          highlight-current-row
          empty-text="暂无捕捞日志，点击右上角「新增捕捞日志」创建第一条日志吧！"
        >
          <el-table-column prop="logId" label="日志编号" width="95" align="center">
            <template #default="scope">
              <span class="log-id-cell">
                <span class="log-id-icon">🌊</span>
                <span>#{{ scope.row.logId }}</span>
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="seaName" label="捕捞海域" width="150" align="center" />
          <el-table-column prop="fishingDate" label="捕捞日期" width="120" align="center" />
          <el-table-column prop="fishingGear" label="渔具类型" width="120" align="center" />
          <el-table-column
            prop="isCompliant"
            label="合规状态"
            width="100"
            align="center"
          >
            <template #default="scope">
              <div class="status-cell">
                <span :class="scope.row?.isCompliant === 1 ? 'status-dot status-dot--success' : 'status-dot status-dot--danger'"></span>
                <span :class="scope.row?.isCompliant === 1 ? 'status-text--success' : 'status-text--danger'">
                  {{ scope.row?.isCompliant === 1 ? '合规' : '违规' }}
                </span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="uncompliantReason" label="违规原因" min-width="200" />
          <el-table-column label="录入时间" width="180" align="center">
            <template #default="scope">
              <!-- 把ISO格式转成本地时间格式 -->
              {{ scope.row.createTime ? new Date(scope.row.createTime).toLocaleString() : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="90" align="center">
            <template #default="scope">
              <div class="action-btns">
                <el-button class="icon-btn view-btn" icon="Search" @click="viewLog(scope.row)" title="查看详情" />
                <el-button class="icon-btn del-btn" icon="Delete" @click="deleteLog(scope.row.logId)" title="删除" />
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页：无数据时隐藏 -->
        <el-pagination
          v-if="total > 0"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pageNum"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          class="pagination"
        />
      </div>
    </div>

    <!-- 新增/编辑日志弹窗 -->
    <el-dialog
      v-model="showAddDialog"
      title="新增捕捞日志"
      width="70%"
      destroy-on-close
      draggable
    >
      <el-form
        ref="logFormRef"
        :model="logForm"
        :rules="logRules"
        label-width="100px"
        class="log-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="捕捞海域：" prop="seaId">
              <el-select v-model="logForm.seaId" placeholder="请选择海域" style="width: 100%" :disabled="!seaList.length">
                <el-option
                  v-for="sea in seaList"
                  :key="sea.seaId"
                  :label="sea.seaName"
                  :value="sea.seaId"
                />
                <el-option v-if="!seaList.length" label="暂无海域数据，请先在后台配置" value="" disabled />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="捕捞日期：" prop="fishingDate">
              <el-date-picker
                v-model="logForm.fishingDate"
                type="date"
                placeholder="选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                :disabled-date="(time) => time > Date.now()" 
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="渔具类型：" prop="fishingGear">
          <el-input v-model="logForm.fishingGear" placeholder="请输入渔具类型（如：拖网、刺网、围网）" />
        </el-form-item>

        <!-- 物种明细 -->
        <el-form-item label="捕捞物种：" :disabled="!speciesAllList.length">
          <el-table
            :data="speciesList"
            border
            style="width: 100%"
            @row-click="handleSpeciesRowClick"
          >
            <el-table-column label="#" width="48" align="center">
              <template #default="scope">
                <span class="species-badge">{{ scope.$index + 1 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="物种名称" width="180">
              <template #default="scope">
                <el-select
                  v-model="scope.row.speciesId"
                  placeholder="选择物种"
                  style="width: 100%"
                >
                  <el-option
                    v-for="specie in speciesAllList"
                    :key="specie.speciesId"
                    :label="specie.speciesName"
                    :value="specie.speciesId"
                  />
                  <el-option v-if="!speciesAllList.length" label="暂无鱼种数据，请先在后台配置" value="" disabled />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="捕捞数量" width="120">
              <template #default="scope">
                <el-input
                  v-model="scope.row.catchNum"
                  placeholder="输入数量（如：50）"
                  type="number"
                  min="0"
                  step="1"
                />
              </template>
            </el-table-column>
              <el-table-column label="捕捞规格" width="120">
              <template #default="scope">
                <el-input
                  v-model="scope.row.catchSpec"
                  placeholder="如：15kg（选填）"
                />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center">
              <template #default="scope">
                <el-button
                  type="text"
                  icon="Delete"
                  style="color: #f56c6c"
                  @click="deleteSpecies(scope.row)"
                >删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-button
            class="add-species-btn"
            icon="Plus"
            @click="addSpecies"
            :disabled="!speciesAllList.length"
          >
            {{ speciesAllList.length ? '添加物种' : '暂无鱼种数据，无法添加' }}
          </el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="submitLog" :disabled="!seaList.length || !speciesAllList.length">
          {{ (seaList.length && speciesAllList.length) ? '提交' : '请先配置海域/鱼种数据' }}
        </el-button>
      </template>
    </el-dialog>

        <!-- 数据可视化弹窗：无日志时隐藏 -->
    <el-dialog
      v-model="showChartDialog"
      title="合规数据可视化"
      width="80%"
      destroy-on-close
      draggable
    >
      <div class="chart-container">
        <!-- 时间筛选器 -->
        <div class="time-filter" style="margin-bottom: 20px;">
          <el-radio-group v-model="timeRange" @change="openChartDialog" class="time-filter-group">
            <el-radio-button label="day">今日</el-radio-button>
            <el-radio-button label="week">近一周</el-radio-button>
            <el-radio-button label="month">近一个月</el-radio-button>
            <el-radio-button label="year">近一年</el-radio-button>
          </el-radio-group>
        </div>
        
        <el-row :gutter="20" class="chart-row">
          <el-col :span="12">
            <div class="chart-card">
              <h3>合规情况</h3>
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
    </el-dialog>
  </div>
</template>

<script setup>
// 核心Vue API导入
import { ref, reactive, onMounted, onBeforeUnmount, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'

// 接口导入
import fishingLogApi from '@/api/fishingLog'
import complianceApi from '@/api/compliance'

// 基础状态
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const logList = ref([])
const seaList = ref([])
const speciesAllList = ref([])

// 时间筛选
const timeRange = ref('month')  // 默认为近一个月

// ECharts实例：防止内存泄漏
let compliancePieChart = null
let speciesWeightBarChart = null  // 替换原来的多个图表实例

// 查询表单
const searchForm = reactive({
  fishingDate: '',
  seaId: '',
  isCompliant: ''
})

// 弹窗控制
const showAddDialog = ref(false)
const showChartDialog = ref(false)

// 新增日志表单
const logFormRef = ref(null)
const logForm = reactive({
  seaId: '',
  fishingDate: '',
  fishingGear: '',
  speciesList: []
})
const speciesList = ref([
  { speciesId: '', catchNum: '', catchSpec: '' }
])

// 表单校验规则
const logRules = reactive({
  seaId: [{ required: true, message: '请选择捕捞海域', trigger: 'change' }],
  fishingDate: [{ required: true, message: '请选择捕捞日期', trigger: 'change' }],
  fishingGear: [{ required: true, message: '请输入渔具类型', trigger: 'blur' }]
})

// 页面初始化
onMounted(async () => {
  await Promise.all([
    getSeaList(),
    getSpeciesList()
  ])
  getLogList()
})

// 页面销毁
onBeforeUnmount(() => {
  if (compliancePieChart) compliancePieChart.dispose()
  if (speciesWeightBarChart) speciesWeightBarChart.dispose()
  window.removeEventListener('resize', resizeCharts)
})

// 获取海域列表
const getSeaList = async () => {
  try {
    const res = await complianceApi.listSeaArea()
    seaList.value = Array.isArray(res) ? res : []
  } catch (error) {
    ElMessage.error('获取海域列表失败，请检查接口或后台数据')
    seaList.value = []
    console.error('海域加载失败：', error)
  }
}

// 获取鱼种列表
const getSpeciesList = async () => {
  try {
    const res = await complianceApi.listSpecies()
    speciesAllList.value = Array.isArray(res) ? res : []
  } catch (error) {
    ElMessage.error('获取鱼种列表失败，请检查接口或后台数据')
    speciesAllList.value = []
    console.error('鱼种加载失败：', error)
  }
}

// ########### 核心修复：获取日志列表（精准解析后端返回格式）###########
const getLogList = async () => {
  loading.value = true
  try {
    const userId = sessionStorage.getItem('loginUserId')
    if (!userId) {
      ElMessage.warning('请先登录后再操作')
      loading.value = false
      return
    }
    const params = {
      userId,
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      ...searchForm
    }
    const res = await fishingLogApi.getLogList(params)
    // 核心修改：注释过滤逻辑，直接赋值，不过滤null对象
    if (res && res.code === 200) {
      // 删掉过滤代码，直接使用后端返回的data
      logList.value = Array.isArray(res.data) ? res.data : []
      total.value = Number(res.total) || 0
      console.log("后端返回的日志列表：", logList.value); // 打印确认列表有数据
    } else {
      logList.value = []
      total.value = 0
    }
  } catch (error) {
    ElMessage.error('获取日志列表失败，请检查网络或接口')
    logList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 重置查询条件
const resetSearch = () => {
  searchForm.fishingDate = ''
  searchForm.seaId = ''
  searchForm.isCompliant = ''
  pageNum.value = 1
  getLogList()
}

// 分页事件
const handleSizeChange = (val) => {
  pageSize.value = val
  getLogList()
}
const handleCurrentChange = (val) => {
  pageNum.value = val
  getLogList()
}

// 物种明细操作
const addSpecies = () => {
  speciesList.value.push({ speciesId: '', catchNum: '', catchSpec: '' })
}
const deleteSpecies = (row) => {
  const index = speciesList.value.findIndex(item => item === row)
  if (index > -1) {
    speciesList.value.splice(index, 1)
    if (speciesList.value.length === 0) {
      speciesList.value.push({ speciesId: '', catchNum: '', catchSpec: '' })
    }
  }
}
const handleSpeciesRowClick = () => {}

// 提交日志
const submitLog = async () => {
  if (!logFormRef.value) return
  const userId = sessionStorage.getItem('loginUserId')
  if (!userId) {
    ElMessage.warning('请先登录后再提交日志')
    return
  }
  try {
    await logFormRef.value.validate()
    const validSpecies = speciesList.value.filter(item => 
      item.speciesId && item.catchNum !== '' && item.catchNum >= 0
    )
    if (validSpecies.length === 0) {
      ElMessage.warning('请至少添加一种捕捞物种并填写有效数量（≥0）')
      return
    }
    const submitData = {
      userId,
      seaId: logForm.seaId,
      fishingDate: logForm.fishingDate,
      fishingGear: logForm.fishingGear,
      speciesList: validSpecies
    }
    const res = await fishingLogApi.saveLog(submitData)
    if (res?.code === 200 || res?.success) {
      ElMessage.success('日志提交成功！')
      showAddDialog.value = false
      logForm.seaId = ''
      logForm.fishingDate = ''
      logForm.fishingGear = ''
      speciesList.value = [{ speciesId: '', catchNum: '', catchSpec: '' }]
      // 提交成功后强制刷新列表（关键）
      getLogList()
    } else {
      ElMessage.error(res?.msg || '日志提交失败，请稍后重试')
    }
  } catch (error) {
    ElMessage.error('表单填写不完整，请检查必填项')
  }
}

// 查看日志详情（已添加?.空值防护）
const viewLog = (row) => {
  if (!row) return
  // 新增：时间格式化逻辑，和列表页保持一致
  const formatCreateTime = (time) => {
    return time ? new Date(time).toLocaleString() : '暂无'
  }
  ElMessageBox.alert(`
    <div style="text-align: left; line-height: 1.8;">
      <p><b>日志ID：</b>${row?.logId || '暂无'}</p>
      <p><b>捕捞海域：</b>${row?.seaName || '暂无'}</p>
      <p><b>捕捞日期：</b>${row?.fishingDate || '暂无'}</p>
      <p><b>渔具类型：</b>${row?.fishingGear || '暂无'}</p>
      <p><b>合规状态：</b>${row?.isCompliant === 1 ? '<span style="color: #67c23a;">合规</span>' : '<span style="color: #f56c6c;">违规</span>'}</p>
      <p><b>违规原因：</b>${row?.uncompliantReason || '无'}</p>
      <!-- 核心修改：调用格式化函数处理createTime -->
      <p><b>录入时间：</b>${formatCreateTime(row?.createTime)}</p>
    </div>
  `, '日志详情', {
    dangerouslyUseHTMLString: true,
    width: '400px',
    confirmButtonText: '确定'
  })
}

// 替换原有deleteLog方法
const deleteLog = async (logId) => {
  // 二次确认弹窗
  try {
    await ElMessageBox.confirm(
      '确定删除该日志吗？删除后数据不可恢复！',
      '删除确认',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用删除接口（增加超时和错误捕获）
    const res = await fishingLogApi.deleteLog(logId)
    console.log('后端删除接口返回原始数据：', res) // 打印返回数据，方便排查
    
    // 兼容所有返回格式：只要接口不报错，就判定为删除成功
    // 场景1：后端返回标准格式 {code:200, msg:'成功'}
    if (res?.code === 200 || res?.success) {
      ElMessage.success(res?.msg || '日志删除成功！')
    }
    // 场景2：后端返回空值/纯字符串/其他格式（只要没抛错就认为成功）
    else if (res !== undefined && res !== null) {
      ElMessage.success('日志删除成功！')
    }
    // 刷新列表
    getLogList()
  } catch (error) {
    // 取消删除时不提示错误
    if (error.type !== 'cancel') {
      // 兼容后端各种错误返回格式
      let errorMsg = '删除失败：'
      if (error.response) {
        // 有响应但状态码非200
        errorMsg += error.response.data?.msg || error.response.data || '服务器返回异常'
      } else if (error.message) {
        // 网络/超时错误
        errorMsg += error.message
      } else {
        errorMsg += '数据格式异常'
      }
      ElMessage.error(errorMsg)
    }
  }
}

// 导出自查报告
const exportReport = async () => {
  const userId = sessionStorage.getItem('loginUserId')
  if (!userId) {
    ElMessage.warning('请先登录')
    return
  }
  if (!logList.value.length) {
    ElMessage.warning('暂无日志数据，无法导出报告')
    return
  }
  try {
    loading.value = true
    const res = await fishingLogApi.exportReport({ userId })
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `捕捞合规自查报告_${new Date().toLocaleDateString().replace(/\//g, '-')}.xlsx`
    a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('自查报告导出成功！')
  } catch (error) {
    ElMessage.error('报告导出失败，请检查接口或网络')
    console.error('导出失败：', error)
  } finally {
    loading.value = false
  }
}

// ECharts自适应
const resizeCharts = () => {
  if (compliancePieChart) compliancePieChart.resize()
  if (speciesWeightBarChart) speciesWeightBarChart.resize()
}

// 初始化ECharts图表（只包含合规饼图和鱼种重量柱状图）
const initCharts = (chartData) => {
  // 先销毁旧实例，避免重复创建导致的图表异常
  if (compliancePieChart) compliancePieChart.dispose()
  if (speciesWeightBarChart) speciesWeightBarChart.dispose()

  // 1. 合规/违规饼图（增加无数据提示）
  compliancePieChart = echarts.init(document.getElementById('compliancePie'))
  const pieOption = {
    backgroundColor: 'transparent',
    color: ['#00dd77', '#ff5555'],
    tooltip: {
      trigger: 'item',
      formatter: '{b}：{c} 条 ({d}%)',
      backgroundColor: 'rgba(0,15,45,0.9)',
      borderColor: 'rgba(0,180,255,0.3)',
      textStyle: { color: '#e0f4ff' }
    },
    legend: { orient: 'vertical', left: 'left', data: ['合规', '违规'], textStyle: { color: '#88bbdd' } },
    title: { text: chartData?.pieData?.length ? '' : '暂无合规统计数据', left: 'center', top: '45%', textStyle: { color: '#557799' } },
    series: [{
      name: '合规状态',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['55%', '50%'],
      data: chartData?.pieData || [],
      label: { show: true, position: 'outside', color: '#88ccdd' },
      labelLine: { lineStyle: { color: 'rgba(0,180,255,0.4)' } },
      silent: !chartData?.pieData?.length
    }]
  }
  compliancePieChart.setOption(pieOption)

  // 2. 各捕捞鱼种重量柱状图（增加无数据提示）
  speciesWeightBarChart = echarts.init(document.getElementById('speciesWeightBar'))
  const barOption = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>{a}: {c} kg',
      backgroundColor: 'rgba(0,15,45,0.9)',
      borderColor: 'rgba(0,180,255,0.3)',
      textStyle: { color: '#e0f4ff' }
    },
    title: { text: chartData?.barXData?.length ? '' : '暂无鱼种重量统计数据', left: 'center', top: '45%', textStyle: { color: '#557799' } },
    grid: { left: '3%', right: '4%', bottom: '15%', top: '15%', containLabel: true },
    xAxis: {
      type: 'category',
      data: chartData?.barXData || [],
      show: chartData?.barXData?.length,
      axisLabel: { rotate: 45, color: '#88bbdd' },
      axisLine: { lineStyle: { color: 'rgba(0,150,200,0.4)' } },
      splitLine: { lineStyle: { color: 'rgba(0,100,160,0.2)' } }
    },
    yAxis: {
      type: 'value',
      name: '重量(kg)',
      show: chartData?.barXData?.length,
      nameTextStyle: { color: '#88bbdd' },
      axisLabel: { color: '#88bbdd' },
      axisLine: { lineStyle: { color: 'rgba(0,150,200,0.4)' } },
      splitLine: { lineStyle: { color: 'rgba(0,100,160,0.2)' } }
    },
    series: [{
      name: '鱼种重量',
      type: 'bar',
      data: chartData?.barYData || [],
      barWidth: '60%',
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#00d4ff' },
          { offset: 1, color: '#004488' }
        ])
      },
      silent: !chartData?.barXData?.length
    }]
  }
  speciesWeightBarChart.setOption(barOption)

  // 重新绑定自适应事件
  window.addEventListener('resize', resizeCharts)
}

// 打开可视化弹窗（核心修复：格式转换，对接后端ChartDataVO格式）
const openChartDialog = async () => {
  const userId = sessionStorage.getItem('loginUserId')
  if (!userId || !logList.value.length) {
    ElMessage.warning('暂无日志数据，无法生成可视化图表')
    return
  }
  try {
    loading.value = true
    // 调用后端接口，传递时间范围参数
    const res = await fishingLogApi.getChartData({ userId, timeRange: timeRange.value })
    console.log('后端可视化原始数据：', res) // 打印原始数据，方便排查
    // 兼容后端统一返回格式（code=200取data）
    const chartData = res?.code === 200 ? (res.data || {}) : (res || {})
    if (!chartData) {
      ElMessage.warning('后端未返回可视化统计数据')
      return
    }

    // ########### 核心：格式转换 → 后端ChartDataVO → 前端ECharts格式 ###########
    const finalEchartsData = {
      // 1. 饼图数据（合规/违规）：转成[{name: '合规', value: 数}, {name: '违规', value: 数}]
      pieData: [
        { name: '合规', value: chartData.compliantCount || 0 },
        { name: '违规', value: chartData.uncompliantCount || 0 }
      ],
      // 2. 柱状图数据（鱼种重量）：barXData=鱼种名，barYData=重量
      barXData: (chartData.speciesWeightData || []).map(item => item.name || '未知鱼种'),
      barYData: (chartData.speciesWeightData || []).map(item => item.value || 0)
    }
    console.log('转换后ECharts数据：', finalEchartsData) // 打印转换后数据

    // 传入转换后的数据初始化图表
    initCharts(finalEchartsData)
  } catch (error) {
    console.error('可视化接口调用失败：', error)
    ElMessage.error('获取可视化数据失败，请检查后端接口')
    // 报错时兜底空数据，避免图表崩溃
    initCharts({
      pieData: [],
      barXData: [],
      barYData: []
    })
  } finally {
    loading.value = false
  }
}

// 监听可视化弹窗显示
watch(
  () => showChartDialog.value,
  (newVal) => {
    if (newVal && logList.value.length) {
      // 延迟从100ms改为300ms，确保弹窗DOM和图表容器完全渲染
      setTimeout(() => openChartDialog(), 300)
    }
  }
)
</script>

<style scoped>
/* ===== 页面基础 ===== */
.fishing-log-page {
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* ===== 顶部栏 ===== */
.log-header {
  height: 64px;
  background: rgba(0,10,30,0.92);
  backdrop-filter: blur(16px);
  box-shadow: 0 2px 20px rgba(0,0,0,0.5);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 40px;
  border-bottom: 1px solid rgba(0,212,255,0.25);
  flex-shrink: 0;
}
.header-left {
  font-size: 17px;
  font-weight: 700;
  color: #e8f6ff;
  letter-spacing: 2px;
  text-shadow: 0 0 12px rgba(0,212,255,0.4);
}
.header-right {
  display: flex;
  gap: 10px;
}

/* 新增按钮（青色渐变） */
.btn-add {
  background: linear-gradient(135deg, #00d4ff, #0099cc) !important;
  border: none !important;
  color: #fff !important;
  font-weight: 600 !important;
  box-shadow: 0 2px 12px rgba(0,212,255,0.35) !important;
  transition: transform 0.2s, box-shadow 0.2s !important;
}
.btn-add:hover { transform: translateY(-1px) !important; box-shadow: 0 4px 18px rgba(0,212,255,0.55) !important; }

/* 导出按钮（深蓝渐变） */
.btn-export {
  background: linear-gradient(135deg, #0055aa, #003377) !important;
  border: 1px solid rgba(0,120,220,0.5) !important;
  color: #aaddff !important;
  font-weight: 600 !important;
  transition: transform 0.2s, box-shadow 0.2s !important;
}
.btn-export:hover:not(.is-disabled) { transform: translateY(-1px) !important; box-shadow: 0 4px 18px rgba(0,80,180,0.5) !important; }

/* 可视化按钮（深蓝紫渐变） */
.btn-chart {
  background: linear-gradient(135deg, #1a3d6e, #0d2447) !important;
  border: 1px solid rgba(0,160,220,0.4) !important;
  color: #88ccff !important;
  font-weight: 600 !important;
  transition: transform 0.2s, box-shadow 0.2s !important;
}
.btn-chart:hover:not(.is-disabled) { transform: translateY(-1px) !important; box-shadow: 0 4px 18px rgba(0,100,200,0.4) !important; }

/* ===== 主内容区 ===== */
.log-main {
  flex: 1;
  padding: 20px 40px;
  overflow-y: auto;
}

/* ===== 查询区 ===== */
.log-search {
  background: rgba(0,15,45,0.82);
  padding: 15px 20px;
  border-radius: 10px;
  margin-bottom: 18px;
  border: 1px solid rgba(0,150,200,0.2);
  backdrop-filter: blur(8px);
}
.search-form {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

/* ===== 表格区 ===== */
.log-table {
  background: rgba(0,15,45,0.82);
  padding: 20px;
  border-radius: 10px;
  border: 1px solid rgba(0,150,200,0.2);
  backdrop-filter: blur(8px);
}

/* logId 海洋编号 */
.log-id-cell {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #66d8ff;
  font-weight: 600;
  font-size: 13px;
}

/* 合规状态 */
.status-cell {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-weight: 600;
  font-size: 13px;
}
.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.status-dot--success {
  background: #00ff88;
  animation: pulse-green 1.8s ease-in-out infinite;
}
.status-dot--danger {
  background: #ff4444;
  animation: pulse-red 1.8s ease-in-out infinite;
}
.status-text--success { color: #00dd77; }
.status-text--danger  { color: #ff5555; }

@keyframes pulse-green {
  0%, 100% { box-shadow: 0 0 4px rgba(0,255,136,0.6); transform: scale(1); }
  50%       { box-shadow: 0 0 10px rgba(0,255,136,0.9); transform: scale(1.3); }
}
@keyframes pulse-red {
  0%, 100% { box-shadow: 0 0 4px rgba(255,68,68,0.6); transform: scale(1); }
  50%       { box-shadow: 0 0 10px rgba(255,68,68,0.9); transform: scale(1.3); }
}

/* 表格深海主题覆盖 */
:deep(.log-table .el-table),
:deep(.log-table .el-table tr),
:deep(.log-table .el-table th.el-table__cell),
:deep(.log-table .el-table td.el-table__cell) {
  background-color: transparent !important;
}
:deep(.log-table .el-table) {
  --el-table-bg-color: rgba(0,15,50,0.7) !important;
  --el-table-tr-bg-color: rgba(0,15,50,0.7) !important;
  --el-table-header-bg-color: rgba(0,25,70,0.9) !important;
  --el-table-row-hover-bg-color: rgba(0,160,220,0.18) !important;
  --el-table-border-color: rgba(0,130,180,0.2) !important;
  --el-table-text-color: #b8dff0 !important;
  --el-table-header-text-color: #66d8ff !important;
  color: #b8dff0 !important;
}
:deep(.log-table .el-table th.el-table__cell) {
  background-color: rgba(0,25,70,0.9) !important;
  color: #66d8ff !important;
  border-bottom-color: rgba(0,130,180,0.3) !important;
  font-weight: 600 !important;
  letter-spacing: 0.5px !important;
}
:deep(.log-table .el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell) {
  background-color: rgba(0,30,75,0.6) !important;
}
:deep(.log-table .el-table__body tr.hover-row > td.el-table__cell) {
  background-color: rgba(0,160,220,0.18) !important;
  color: #00d4ff !important;
}
:deep(.log-table .el-table__empty-block) {
  background-color: rgba(0,15,50,0.7) !important;
}
:deep(.log-table .el-table__empty-text) {
  color: #5577aa !important;
}
:deep(.log-table .el-table__border-left-patch),
:deep(.log-table .el-table__inner-wrapper::before),
:deep(.log-table .el-table__border-bottom-patch) {
  background-color: rgba(0,130,180,0.25) !important;
}

/* 操作按钮行 */
.action-btns {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

/* 图标操作按钮 */
.icon-btn {
  width: 32px !important;
  height: 32px !important;
  padding: 0 !important;
  border-radius: 8px !important;
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
  transition: transform 0.2s, box-shadow 0.2s !important;
  font-size: 15px !important;
}
.view-btn {
  background: rgba(0,100,200,0.25) !important;
  border: 1px solid rgba(0,160,255,0.35) !important;
  color: #66ccff !important;
}
.view-btn:hover { transform: scale(1.18) !important; background: rgba(0,130,220,0.4) !important; box-shadow: 0 0 10px rgba(0,180,255,0.4) !important; }
.del-btn {
  background: rgba(180,30,30,0.2) !important;
  border: 1px solid rgba(255,80,80,0.3) !important;
  color: #ff6666 !important;
}
.del-btn:hover { transform: scale(1.18) !important; background: rgba(200,40,40,0.35) !important; box-shadow: 0 0 10px rgba(255,80,80,0.4) !important; }

.pagination { margin-top: 20px; text-align: right; }

/* ===== 弹窗 ===== */
:deep(.el-dialog) {
  background: rgba(0,12,35,0.95) !important;
  border: 1px solid rgba(0,180,255,0.25) !important;
  backdrop-filter: blur(20px) !important;
  border-radius: 12px !important;
}
:deep(.el-dialog__header) {
  border-bottom: 1px solid rgba(0,150,220,0.2) !important;
  padding: 18px 24px 14px !important;
}
:deep(.el-dialog__title) {
  color: #66e8ff !important;
  font-size: 17px !important;
  font-weight: 700 !important;
  letter-spacing: 2px !important;
}

/* 表单样式 */
.log-form {
  max-height: 500px;
  overflow-y: auto;
  padding-right: 10px;
}

/* 弹窗内物种表格深海主题覆盖 */
:deep(.log-form .el-table),
:deep(.log-form .el-table tr),
:deep(.log-form .el-table th.el-table__cell),
:deep(.log-form .el-table td.el-table__cell) {
  background-color: transparent !important;
}
:deep(.log-form .el-table) {
  --el-table-bg-color: rgba(0,20,60,0.6) !important;
  --el-table-tr-bg-color: rgba(0,20,60,0.6) !important;
  --el-table-header-bg-color: rgba(0,30,80,0.85) !important;
  --el-table-row-hover-bg-color: rgba(0,130,200,0.18) !important;
  --el-table-border-color: rgba(0,120,180,0.25) !important;
  --el-table-text-color: #b8dff0 !important;
  --el-table-header-text-color: #66d8ff !important;
  color: #b8dff0 !important;
}
:deep(.log-form .el-table th.el-table__cell) {
  background-color: rgba(0,30,80,0.85) !important;
  color: #66d8ff !important;
  border-bottom-color: rgba(0,120,180,0.3) !important;
  font-weight: 600 !important;
}
:deep(.log-form .el-table__body tr.hover-row > td.el-table__cell) {
  background-color: rgba(0,130,200,0.18) !important;
}
:deep(.log-form .el-table__empty-block) {
  background-color: rgba(0,20,60,0.6) !important;
}
:deep(.log-form .el-table__inner-wrapper::before) {
  background-color: rgba(0,120,180,0.25) !important;
}

/* 弹窗内表单控件深海主题 */
:deep(.el-dialog .el-input__wrapper),
:deep(.el-dialog .el-textarea__inner) {
  background-color: rgba(0,20,60,0.7) !important;
  box-shadow: 0 0 0 1px rgba(0,150,220,0.3) inset !important;
}
:deep(.el-dialog .el-input__wrapper:hover),
:deep(.el-dialog .el-textarea__inner:hover) {
  box-shadow: 0 0 0 1px rgba(0,200,255,0.5) inset !important;
}
:deep(.el-dialog .el-input__wrapper.is-focus),
:deep(.el-dialog .el-textarea__inner:focus) {
  box-shadow: 0 0 0 1px rgba(0,212,255,0.7) inset !important;
}
:deep(.el-dialog .el-input__inner),
:deep(.el-dialog .el-textarea__inner) {
  color: #cceeff !important;
  background-color: transparent !important;
}
:deep(.el-dialog .el-input__inner::placeholder),
:deep(.el-dialog .el-textarea__inner::placeholder) {
  color: rgba(100,170,210,0.5) !important;
}
/* select 触发器 */
:deep(.el-dialog .el-select .el-input__wrapper) {
  background-color: rgba(0,20,60,0.7) !important;
}
/* date-picker 触发器 */
:deep(.el-dialog .el-date-editor .el-input__wrapper) {
  background-color: rgba(0,20,60,0.7) !important;
}
:deep(.el-dialog .el-date-editor .el-input__prefix-inner .el-icon),
:deep(.el-dialog .el-input__suffix-inner .el-icon) {
  color: #55aacc !important;
}
/* form-item label */
:deep(.el-dialog .el-form-item__label) {
  color: #88ccdd !important;
}
:deep(.el-form-item__error) {
  color: #ff5555 !important;
  text-shadow: 0 0 8px rgba(255,60,60,0.6) !important;
  font-weight: 500 !important;
}

/* 物种序号徽章 */
.species-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0088cc, #005599);
  color: #aaddff;
  font-size: 11px;
  font-weight: 700;
  box-shadow: 0 0 6px rgba(0,150,220,0.4);
}

/* 添加物种虚线按钮 */
.add-species-btn {
  margin-top: 10px !important;
  width: 100% !important;
  border: 2px dashed rgba(0,180,255,0.45) !important;
  background: rgba(0,50,100,0.15) !important;
  color: #66ccff !important;
  border-radius: 8px !important;
  font-size: 14px !important;
  transition: border-style 0.2s, border-color 0.2s, background 0.2s !important;
}
.add-species-btn:hover:not(.is-disabled) {
  border-style: solid !important;
  border-color: rgba(0,212,255,0.7) !important;
  background: rgba(0,70,130,0.25) !important;
  color: #00d4ff !important;
}
.add-species-btn.is-disabled { opacity: 0.4 !important; }

/* ===== 可视化弹窗 ===== */
.chart-container { padding: 10px; }
.time-filter {
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
}

/* 药丸形时间筛选器 */
:deep(.time-filter-group .el-radio-button__inner) {
  background: rgba(0,30,70,0.7) !important;
  border-color: rgba(0,150,220,0.35) !important;
  color: #88bbdd !important;
  transition: all 0.25s !important;
}
:deep(.time-filter-group .el-radio-button:first-child .el-radio-button__inner) {
  border-radius: 20px 0 0 20px !important;
}
:deep(.time-filter-group .el-radio-button:last-child .el-radio-button__inner) {
  border-radius: 0 20px 20px 0 !important;
}
:deep(.time-filter-group .el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(90deg, #00aacc, #0077aa) !important;
  border-color: #00aacc !important;
  color: #fff !important;
  box-shadow: 0 0 12px rgba(0,170,204,0.45) !important;
}

/* 图表卡片 */
.chart-card {
  background: rgba(0,15,45,0.85);
  border: 1px solid rgba(0,150,200,0.2);
  padding: 16px;
  border-radius: 10px;
  margin-bottom: 16px;
}
.chart-card h3 {
  margin: 0 0 12px 0;
  font-size: 15px;
  color: #66e8ff;
  font-weight: 700;
  letter-spacing: 1px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(0,150,200,0.2);
}

/* 图表间分隔线 */
:deep(.chart-row > .el-col:first-child) {
  border-right: 1px solid rgba(0,150,200,0.15);
}

/* ===== 响应式 ===== */
@media (max-width: 992px) {
  .log-header { padding: 0 20px; }
  .header-left { font-size: 15px; }
  .log-main { padding: 10px 15px; }
  :deep(.chart-row > .el-col:first-child) { border-right: none; border-bottom: 1px solid rgba(0,150,200,0.15); }
  :deep(.el-dialog) { width: 95% !important; }
}
</style>

<!-- 日历弹出面板（Teleport 到 body，需全局样式覆盖） -->
<style>
.el-picker-panel {
  background: linear-gradient(145deg, rgba(0, 18, 52, 0.97), rgba(0, 10, 35, 0.99)) !important;
  border: 1px solid rgba(0, 212, 255, 0.28) !important;
  border-radius: 12px !important;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.7), 0 0 30px rgba(0, 80, 160, 0.2) !important;
  color: #c8e8ff !important;
}

/* 顶部月份/年份导航 */
.el-date-picker__header {
  color: #88ccee !important;
}
.el-date-picker__header .el-date-picker__header-label {
  color: #a8deff !important;
  font-weight: 600 !important;
}
.el-date-picker__header .el-date-picker__header-label:hover {
  color: #00d4ff !important;
}
.el-picker-panel__icon-btn {
  color: #55aacc !important;
}
.el-picker-panel__icon-btn:hover {
  color: #00d4ff !important;
}

/* 星期行 */
.el-date-table th {
  color: rgba(0, 212, 255, 0.6) !important;
  border-bottom: 1px solid rgba(0, 212, 255, 0.15) !important;
  font-weight: 600 !important;
  background: transparent !important;
}

/* 日期单元格 */
.el-date-table td {
  background: transparent !important;
}
.el-date-table td .el-date-table-cell {
  background: transparent !important;
}
.el-date-table td .el-date-table-cell__text {
  color: #c0e0f8 !important;
  border-radius: 6px !important;
  transition: all 0.2s !important;
}

/* 鼠标悬停 */
.el-date-table td:not(.disabled):not(.current):hover .el-date-table-cell__text {
  background: rgba(0, 180, 255, 0.18) !important;
  color: #00d4ff !important;
}

/* 今日 */
.el-date-table td.today .el-date-table-cell__text {
  color: #00d4ff !important;
  font-weight: 700 !important;
  border: 1px solid rgba(0, 212, 255, 0.5) !important;
  background: rgba(0, 180, 255, 0.1) !important;
}

/* 已选中 */
.el-date-table td.current:not(.disabled) .el-date-table-cell__text,
.el-date-table td.selected .el-date-table-cell__text {
  background: linear-gradient(135deg, #00b4cc, #006688) !important;
  color: #fff !important;
  box-shadow: 0 0 10px rgba(0, 200, 220, 0.5) !important;
  border-radius: 6px !important;
}

/* 禁用日期 */
.el-date-table td.disabled .el-date-table-cell__text,
.el-date-table td.disabled {
  background: transparent !important;
  color: rgba(100, 150, 180, 0.35) !important;
  cursor: not-allowed !important;
}

/* 上/下月灰色日期 */
.el-date-table td.prev-month .el-date-table-cell__text,
.el-date-table td.next-month .el-date-table-cell__text {
  color: rgba(100, 160, 200, 0.35) !important;
}

/* 底部按钮栏 */
.el-picker-panel__footer {
  background: rgba(0, 15, 45, 0.9) !important;
  border-top: 1px solid rgba(0, 212, 255, 0.15) !important;
}
.el-picker-panel__footer .el-button--text {
  color: #55aacc !important;
}
.el-picker-panel__footer .el-button--text:hover {
  color: #00d4ff !important;
}
.el-picker-panel__footer .el-button--default {
  background: rgba(0, 40, 80, 0.7) !important;
  border-color: rgba(0, 180, 220, 0.4) !important;
  color: #88ccee !important;
}
.el-picker-panel__footer .el-button--primary {
  background: linear-gradient(135deg, #00b4cc, #006688) !important;
  border: none !important;
  color: #fff !important;
}

/* panel 内的输入框（时间选择器等） */
.el-picker-panel .el-input__wrapper {
  background: rgba(0, 20, 55, 0.7) !important;
  box-shadow: 0 0 0 1px rgba(0, 180, 220, 0.3) inset !important;
}
.el-picker-panel .el-input__inner {
  color: #c0e4ff !important;
  background: transparent !important;
}

/* 年月选择下拉 */
.el-date-picker__header-label {
  color: #a8deff !important;
}
</style>