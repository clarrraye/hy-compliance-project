<template>
  <div class="compliance-query-page">
    <!-- 页面标题：海洋蓝+加粗 -->
    <div class="page-title">海渔合规基础查询</div>

    <!-- 选项卡：海洋主题卡片样式 -->
    <el-tabs v-model="activeTab" type="card" class="query-tabs" style="margin: 20px 0;">
      <el-tab-pane label="禁渔规则查询" name="banRule">
        <!-- 禁渔规则查询：3大条件（海域+时间+鱼种） -->
        <el-card shadow="hover" class="query-card">
          <el-form :inline="true" :model="banQueryForm" class="query-form">
            <el-form-item label="选择海域" class="form-item">
              <el-select
                v-model="banQueryForm.seaId"
                placeholder="请选择海域"
                clearable
                style="width: 220px;"
                class="query-select"
              >
                <el-option
                  v-for="sea in filteredSeaList"
                  :key="sea.seaId"
                  :label="sea.seaName"
                  :value="sea.seaId"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="选择月份" class="form-item">
              <el-select
                v-model="banQueryForm.month"
                placeholder="请选择查询月份"
                clearable
                style="width: 220px;"
                class="query-select"
              >
                <el-option
                  v-for="month in monthList"
                  :key="month.value"
                  :label="month.label"
                  :value="month.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="选择鱼种" class="form-item">
              <el-select
                v-model="banQueryForm.speciesId"
                placeholder="请选择鱼种"
                clearable
                style="width: 220px;"
                class="query-select"
              >
                <el-option
                  v-for="species in filteredSpeciesList"
                  :key="species.speciesId"
                  :label="species.speciesName"
                  :value="species.speciesId"
                />
              </el-select>
            </el-form-item>
            <el-form-item class="form-item">
              <el-button type="primary" @click="queryBanRule" class="query-btn">查询</el-button>
              <el-button @click="resetBanForm" class="reset-btn">重置</el-button>
            </el-form-item>
          </el-form>

          <!-- 禁渔规则查询结果 -->
          <el-table :data="banRuleList" border style="margin-top: 20px;" stripe v-loading="banRuleLoading" class="result-table">
            <el-table-column prop="seaName" label="海域" align="center" />
            <el-table-column prop="speciesName" label="鱼种" align="center" />
            <el-table-column prop="banStartTime" label="禁渔开始时间" align="center" />
            <el-table-column prop="banEndTime" label="禁渔结束时间" align="center" />
            <el-table-column prop="specRequire" label="规格要求" align="center" />
            <el-table-column prop="punishDesc" label="违规处罚" align="center" />
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="渔业政策查询" name="policy">
        <!-- 政策查询：关键词搜索 -->
        <el-card shadow="hover" class="query-card">
          <el-form :inline="true" :model="policyQueryForm" class="query-form">
            <el-form-item label="关键词搜索" class="form-item">
              <el-input
                v-model="policyQueryForm.keyword"
                placeholder="请输入政策关键词（如禁渔期、网目规格）"
                style="width: 450px;"
                clearable
                class="query-input"
              />
            </el-form-item>
            <el-form-item class="form-item">
              <el-button type="primary" @click="searchPolicy" class="query-btn">搜索</el-button>
            </el-form-item>
          </el-form>

          <!-- 政策查询结果（优化：悬浮显示全文） -->
          <el-table :data="policyList" border style="margin-top: 20px;" stripe v-loading="policyLoading" class="result-table">
            <el-table-column prop="policyTitle" label="政策标题" align="center" />
            <el-table-column prop="publishUnit" label="发布单位" align="center" width="150" />
            <el-table-column prop="publishTime" label="发布时间" align="center" width="180">
              <template #default="scope">
                {{ scope.row.publishTime ? scope.row.publishTime.substring(0,10) : '' }}
              </template>
            </el-table-column>
            <!-- 官方条文列：悬浮显示全文 -->
            <el-table-column label="官方条文" align="center">
              <template #default="scope">
                <el-tooltip
                  class="item"
                  effect="light"
                  :content="scope.row.officialContent || '无'"
                  placement="top"
                  :disabled="!scope.row.officialContent || scope.row.officialContent.length < 10"
                >
                  <div class="text-overflow">{{ scope.row.officialContent || '无' }}</div>
                </el-tooltip>
              </template>
            </el-table-column>
            <!-- 口语化解读列：悬浮显示全文 -->
            <el-table-column label="口语化解读" align="center">
              <template #default="scope">
                <el-tooltip
                  class="item"
                  effect="light"
                  :content="scope.row.simpleContent || '无'"
                  placement="top"
                  :disabled="!scope.row.simpleContent || scope.row.simpleContent.length < 10"
                >
                  <div class="text-overflow">{{ scope.row.simpleContent || '无' }}</div>
                </el-tooltip>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="渔具合规查询" name="gear">
        <!-- 渔具查询：直接展示所有渔具信息 -->
        <el-card shadow="hover" class="query-card">
          <el-table :data="gearList" border stripe style="margin-top: 10px;" v-loading="gearLoading" class="result-table">
            <el-table-column prop="gearName" label="渔具名称" align="center" width="150" />
            <el-table-column label="是否允许" align="center" width="120">
              <template #default="scope">
                <el-tag :type="scope.row.isAllow === 1 ? 'success' : 'danger'" class="status-tag">
                  {{ scope.row.isAllow === 1 ? '允许' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注/规格要求" align="center" show-overflow-tooltip />
            <el-table-column prop="punishDesc" label="违规处罚" align="center" show-overflow-tooltip />
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElTag } from 'element-plus'
import complianceApi from '@/api/compliance'

// 选项卡激活项
const activeTab = ref('banRule')

// 加载状态
const banRuleLoading = ref(false)
const policyLoading = ref(false)
const gearLoading = ref(false)

// 1. 禁渔规则查询相关 - 初始值设为null
const banQueryForm = ref({
  seaId: null,
  month: null,
  speciesId: null
})
const seaList = ref([]) // 海域原始数据列表
const speciesList = ref([]) // 鱼种原始数据列表
const banRuleList = ref([]) // 禁渔规则结果
// 月份下拉列表
const monthList = ref([
  { label: '1月', value: '1月' },{ label: '2月', value: '2月' },{ label: '3月', value: '3月' },
  { label: '4月', value: '4月' },{ label: '5月', value: '5月' },{ label: '6月', value: '6月' },
  { label: '7月', value: '7月' },{ label: '8月', value: '8月' },{ label: '9月', value: '9月' },
  { label: '10月', value: '10月' },{ label: '11月', value: '11月' },{ label: '12月', value: '12月' }
])

// 2. 政策查询相关
const policyQueryForm = ref({
  keyword: ''
})
const policyList = ref([]) // 政策结果

// 3. 渔具查询相关
const gearList = ref([]) // 渔具结果

// 计算属性过滤逻辑
const filteredSeaList = computed(() => {
  return seaList.value.filter(item => {
    return !!item && (item.seaId !== null && item.seaId !== undefined) && !!item.seaName
  })
})
const filteredSpeciesList = computed(() => {
  return speciesList.value.filter(item => {
    return !!item && (item.speciesId !== null && item.speciesId !== undefined) && !!item.speciesName
  })
})

// 页面加载时初始化数据
onMounted(() => {
  loadSeaAndSpecies()
  loadFishingGear()
})

// 海域/鱼种加载
const loadSeaAndSpecies = async () => {
  try {
    const seaRes = await complianceApi.listSeaArea()
    const speciesRes = await complianceApi.listSpecies()
    seaList.value = Array.isArray(seaRes) ? seaRes : []
    speciesList.value = Array.isArray(speciesRes) ? speciesRes : []
  } catch (error) {
    ElMessage.error('加载海域/鱼种数据失败，请检查网络或后端')
    seaList.value = []
    speciesList.value = []
    console.error('加载失败原因：', error)
  }
}

// 加载渔具数据
const loadFishingGear = async () => {
  gearLoading.value = true
  try {
    const res = await complianceApi.listFishingGear()
    const gearData = Array.isArray(res) ? res : []
    gearList.value = gearData.filter(item => item && item.gearName)
  } catch (error) {
    ElMessage.error('加载渔具合规数据失败：' + (error.message || '网络异常'))
    gearList.value = []
  } finally {
    gearLoading.value = false
  }
}

// 禁渔规则查询
const queryBanRule = async () => {
  if (!banQueryForm.value.seaId && !banQueryForm.value.month && !banQueryForm.value.speciesId) {
    ElMessage.warning('请至少选择一个查询条件（海域/月份/鱼种）')
    return
  }
  banRuleLoading.value = true
  try {
    const queryParams = {}
    if (banQueryForm.value.seaId) queryParams.seaId = banQueryForm.value.seaId
    if (banQueryForm.value.month) queryParams.month = banQueryForm.value.month
    if (banQueryForm.value.speciesId) queryParams.speciesId = banQueryForm.value.speciesId
    const res = await complianceApi.queryBanRule(queryParams)
    banRuleList.value = Array.isArray(res) ? res : []
    if (banRuleList.value.length === 0) {
      ElMessage.info('未查询到相关禁渔规则，请更换条件重试')
    }
  } catch (error) {
    ElMessage.error('查询禁渔规则失败：' + (error.message || '网络异常'))
    banRuleList.value = []
  } finally {
    banRuleLoading.value = false
  }
}

// 重置禁渔查询表单
const resetBanForm = () => {
  banQueryForm.value = {
    seaId: null,
    month: null,
    speciesId: null
  }
  banRuleList.value = []
}

// 政策搜索
const searchPolicy = async () => {
  if (!policyQueryForm.value.keyword?.trim()) {
    ElMessage.warning('请输入有效的搜索关键词')
    return
  }
  policyLoading.value = true
  try {
    const res = await complianceApi.searchPolicy(policyQueryForm.value.keyword.trim())
    policyList.value = Array.isArray(res) ? res : []
    if (policyList.value.length === 0) {
      ElMessage.info('未查询到相关渔业政策，请更换关键词重试')
    }
  } catch (error) {
    ElMessage.error('搜索渔业政策失败：' + (error.message || '网络异常'))
    policyList.value = []
  } finally {
    policyLoading.value = false
  }
}
</script>

<style scoped>
.compliance-query-page {
  padding: 30px 40px;
  box-sizing: border-box;
  min-height: calc(100vh - 64px);
  background: linear-gradient(180deg, #e6f7ff, #f0f8ff);
}
/* 页面标题 */
.page-title {
  font-size: 22px;
  font-weight: bold;
  color: #0066cc;
  margin-bottom: 24px;
  letter-spacing: 1px;
}
/* 选项卡样式优化 */
:deep(.query-tabs) {
  --el-tabs-card-border-color: rgba(64, 158, 255, 0.2);
  --el-tabs-active-color: #0066cc;
}
:deep(.el-tabs__item) {
  font-size: 14px;
  padding: 8px 20px;
  transition: all 0.3s ease;
}
:deep(.el-tabs__item.is-active) {
  background: linear-gradient(90deg, #0066cc, #409eff);
  color: #fff !important;
  border: none !important;
}
/* 查询卡片：磨砂玻璃+圆角 */
.query-card {
  --el-card-border-radius: 12px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(64, 158, 255, 0.1);
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.08);
}
/* 查询表单 */
.query-form {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}
.form-item {
  margin-right: 24px !important;
  margin-bottom: 16px !important;
}
:deep(.form-item .el-form-item__label) {
  color: #0052aa;
  font-size: 14px;
  font-weight: 500;
}
/* 选择器/输入框样式 */
.query-select, .query-input {
  --el-input-border-radius: 8px;
  --el-input-border-color: #cce5ff;
  --el-input-focus-border-color: #409eff;
}
/* 按钮样式：海洋渐变 */
.query-btn {
  background: linear-gradient(90deg, #0066cc, #409eff);
  border: none;
  border-radius: 8px;
  padding: 8px 24px;
  transition: all 0.3s ease;
}
.query-btn:hover {
  background: linear-gradient(90deg, #0052aa, #3393ee);
  transform: scale(1.05);
}
.reset-btn {
  border-radius: 8px;
  padding: 8px 24px;
  border: 1px solid #cce5ff;
  color: #0066cc;
  transition: all 0.3s ease;
}
.reset-btn:hover {
  background: #e6f7ff;
  border-color: #409eff;
}
/* 结果表格样式 */
.result-table {
  --el-table-border-radius: 8px;
  --el-table-header-text-color: #0052aa;
  --el-table-row-hover-bg-color: #e6f7ff;
  --el-table-stripe-bg-color: #f8fcff;
  font-size: 14px;
}
:deep(.el-table__header) {
  background: rgba(64, 158, 255, 0.05);
}
/* 文字溢出省略（关键） */
.text-overflow {
  width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  padding: 0 4px;
  cursor: help; /* 鼠标悬浮显示帮助光标，提示可查看全文 */
}
/* 优化tooltip样式，贴合海洋主题 */
:deep(.el-tooltip__popper) {
  --el-tooltip-bg-color: rgba(255, 255, 255, 0.95);
  --el-tooltip-text-color: #0066cc;
  --el-tooltip-border-color: rgba(64, 158, 255, 0.2);
  backdrop-filter: blur(8px);
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.15);
  max-width: 500px; /* 限制tooltip最大宽度，避免内容过宽 */
  padding: 8px 12px;
  font-size: 14px;
  line-height: 1.5;
}
/* 状态标签优化 */
.status-tag {
  border-radius: 4px;
  padding: 2px 8px;
  font-size: 12px;
}
/* 加载样式优化 */
:deep(.el-loading-mask) {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(5px);
}
:deep(.el-loading-spinner__path) {
  stroke: #409eff;
}
</style>