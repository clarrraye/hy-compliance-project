<template>
  <div class="fishing-log-page">
    <!-- 顶部导航栏：磨砂玻璃风格 -->
    <div class="log-header">
      <div class="header-left">海渔合规管理平台 - 捕捞日志与合规自查</div>
      <div class="header-right">
        <el-button type="primary" icon="Plus" @click="showAddDialog = true">新增捕捞日志</el-button>
        <el-button type="success" icon="Download" @click="exportReport" :disabled="!logList.length">导出自查报告</el-button>
        <el-button type="info" icon="PieChart" @click="showChartDialog = true" :disabled="!logList.length">合规数据可视化</el-button>
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
          <el-table-column prop="logId" label="日志ID" width="80" align="center" />
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
              <el-tag :type="scope.row?.isCompliant === 1 ? 'success' : 'danger'">
                {{ scope.row?.isCompliant === 1 ? '合规' : '违规' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="uncompliantReason" label="违规原因" min-width="200" />
          <el-table-column label="录入时间" width="180" align="center">
            <template #default="scope">
              <!-- 把ISO格式转成本地时间格式 -->
              {{ scope.row.createTime ? new Date(scope.row.createTime).toLocaleString() : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" align="center">
            <template #default="scope">
              <el-button type="text" icon="Edit" @click="viewLog(scope.row)">查看详情</el-button>
              <!-- 新增：删除按钮 -->
              <el-button type="text" icon="Delete" style="color: #f56c6c" @click="deleteLog(scope.row.logId)">删除</el-button>
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
                  placeholder="如：15cm/500g（选填）"
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
            type="text"
            icon="Plus"
            style="margin-top: 10px"
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
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="chart-card">
              <h3>合规/违规数量统计</h3>
              <div id="compliancePie" style="width: 100%; height: 300px"></div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="chart-card">
              <h3>各海域合规率</h3>
              <div id="seaRateBar" style="width: 100%; height: 300px"></div>
            </div>
          </el-col>
          <el-col :span="24">
            <div class="chart-card">
              <h3>物种捕捞数量统计</h3>
              <div id="speciesCatchLine" style="width: 100%; height: 300px"></div>
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

// ECharts实例：防止内存泄漏
let compliancePieChart = null
let seaRateBarChart = null
let speciesCatchLineChart = null

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
  if (seaRateBarChart) seaRateBarChart.dispose()
  if (speciesCatchLineChart) speciesCatchLineChart.dispose()
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
  if (seaRateBarChart) seaRateBarChart.resize()
  if (speciesCatchLineChart) speciesCatchLineChart.resize()
}

// 初始化ECharts图表
// 初始化ECharts图表（修复：增加空数据提示+实例唯一+适配无数据场景）
const initCharts = (chartData) => {
  // 先销毁旧实例，避免重复创建导致的图表异常
  if (compliancePieChart) compliancePieChart.dispose()
  if (seaRateBarChart) seaRateBarChart.dispose()
  if (speciesCatchLineChart) speciesCatchLineChart.dispose()

  // 1. 合规/违规饼图（增加无数据提示）
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
      // 无数据时隐藏饼图
      silent: !chartData?.pieData?.length
    }]
  }
  compliancePieChart.setOption(pieOption)

  // 2. 各海域合规率柱状图（增加无数据提示）
  seaRateBarChart = echarts.init(document.getElementById('seaRateBar'))
  const barOption = {
    color: ['#409eff'],
    tooltip: { trigger: 'axis', formatter: '{b}：{c}%' },
    title: { text: chartData?.barXData?.length ? '' : '暂无海域统计数据', left: 'center', top: '45%', textStyle: { color: '#999' } },
    xAxis: { type: 'category', data: chartData?.barXData || [], show: chartData?.barXData?.length },
    yAxis: { type: 'value', max: 100, min: 0, name: '合规率(%)', show: chartData?.barXData?.length },
    series: [{ 
      name: '合规率', 
      type: 'bar', 
      data: chartData?.barYData || [], 
      barWidth: '40%',
      silent: !chartData?.barXData?.length
    }]
  }
  seaRateBarChart.setOption(barOption)

  // 3. 物种捕捞数量折线图（增加无数据提示）
  speciesCatchLineChart = echarts.init(document.getElementById('speciesCatchLine'))
  const lineOption = {
    color: ['#67c23a'],
    tooltip: { trigger: 'axis', formatter: '{b}：{c} kg' },
    legend: { data: ['捕捞数量'], right: 10, show: chartData?.lineXData?.length },
    title: { text: chartData?.lineXData?.length ? '' : '暂无物种捕捞统计数据', left: 'center', top: '45%', textStyle: { color: '#999' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: chartData?.lineXData || [], show: chartData?.lineXData?.length },
    yAxis: { type: 'value', name: '捕捞数量(kg)', show: chartData?.lineXData?.length },
    series: [{
      name: '捕捞数量',
      type: 'line',
      smooth: true,
      data: chartData?.lineYData || [],
      lineStyle: { width: 2 },
      itemStyle: { radius: 4 },
      silent: !chartData?.lineXData?.length
    }]
  }
  speciesCatchLineChart.setOption(lineOption)

  // 重新绑定自适应事件
  window.addEventListener('resize', resizeCharts)
}

// 打开可视化弹窗
// 打开可视化弹窗（核心修复：格式转换，对接后端ChartDataVO格式）
const openChartDialog = async () => {
  const userId = sessionStorage.getItem('loginUserId')
  if (!userId || !logList.value.length) {
    ElMessage.warning('暂无日志数据，无法生成可视化图表')
    return
  }
  try {
    loading.value = true
    // 调用后端接口（原有代码保留）
    const res = await fishingLogApi.getChartData({ userId, timeRange: 'month' })
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
      // 2. 柱状图数据（海域合规率）：barXData=海域名，barYData=合规率
      barXData: (chartData.compliantRateBySea || []).map(item => item.name || '未配置海域'),
      barYData: (chartData.compliantRateBySea || []).map(item => item.rate || 0),
      // 3. 折线图数据（物种捕捞量）：lineXData=物种名，lineYData=捕捞量
      lineXData: (chartData.speciesCatchData || []).map(item => item.name || '未知物种'),
      lineYData: (chartData.speciesCatchData || []).map(item => item.value || 0)
    }
    console.log('转换后ECharts数据：', finalEchartsData) // 打印转换后数据

    // 传入转换后的数据初始化图表（原有initCharts保留，无需改）
    initCharts(finalEchartsData)
  } catch (error) {
    console.error('可视化接口调用失败：', error)
    ElMessage.error('获取可视化数据失败，请检查后端接口')
    // 报错时兜底空数据，避免图表崩溃
    initCharts({
      pieData: [],
      barXData: [],
      barYData: [],
      lineXData: [],
      lineYData: []
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
.fishing-log-page {
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #f0f8ff, #e6f7ff);
  overflow: hidden;
}

.log-header {
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
.header-right {
  display: flex;
  gap: 10px;
}

.log-main {
  flex: 1;
  padding: 20px 40px;
  overflow-y: auto;
}

.log-search {
  background: rgba(255, 255, 255, 0.9);
  padding: 15px 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
.search-form {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.log-table {
  background: rgba(255, 255, 255, 0.9);
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
.pagination {
  margin-top: 20px;
  text-align: right;
}

.log-form {
  max-height: 500px;
  overflow-y: auto;
  padding-right: 10px;
}

.chart-container {
  padding: 10px;
}
.chart-card {
  background: rgba(255, 255, 255, 0.9);
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
.chart-card h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #0066cc;
  font-weight: bold;
}

/* 响应式适配 */
@media (max-width: 992px) {
  .log-header {
    padding: 0 20px;
  }
  .header-left {
    font-size: 16px;
  }
  .header-right .el-button {
    padding: 6px 8px;
    font-size: 12px;
  }
  .log-main {
    padding: 10px 15px;
  }
  .chart-container .el-row {
    flex-direction: column;
  }
  .chart-container .el-col {
    width: 100% !important;
    margin-bottom: 15px;
  }
  .el-dialog {
    width: 95% !important;
  }
}
</style>