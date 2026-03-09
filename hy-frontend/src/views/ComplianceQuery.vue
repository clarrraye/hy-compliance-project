<template>
  <div class="compliance-query-page">
    <!-- 顶部标题横幅 -->
    <div class="page-banner">
      <div class="banner-content">
        <div class="banner-icon">🛡️</div>
        <div>
          <h1 class="banner-title">基础合规查询</h1>
          <p class="banner-sub">海域禁渔规则 · 渔业政策 · 渔具合规</p>
        </div>
      </div>
      <canvas ref="bannerCanvas" class="banner-wave"></canvas>
    </div>

    <!-- 选项卡 -->
    <el-tabs v-model="activeTab" class="ocean-tabs">
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
            <template #empty>
              <div class="table-empty">
                <svg width="80" height="70" viewBox="0 0 80 70" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M8,55 Q20,47 32,55 Q44,63 56,55 Q68,47 75,51" stroke="rgba(0,212,255,0.35)" stroke-width="1.5" fill="none" stroke-linecap="round"/>
                  <path d="M5,63 Q18,56 30,63 Q42,70 54,63 Q66,56 74,60" stroke="rgba(0,212,255,0.2)" stroke-width="1.5" fill="none" stroke-linecap="round"/>
                  <ellipse cx="42" cy="32" rx="16" ry="9" fill="rgba(0,130,180,0.45)" stroke="rgba(0,212,255,0.5)" stroke-width="1"/>
                  <path d="M58,32 L66,24 L66,40 Z" fill="rgba(0,130,180,0.45)" stroke="rgba(0,212,255,0.5)" stroke-width="1"/>
                  <circle cx="32" cy="29" r="2" fill="rgba(0,212,255,0.7)"/>
                  <circle cx="27" cy="19" r="1.8" stroke="rgba(0,212,255,0.35)" stroke-width="1" fill="none"/>
                  <circle cx="22" cy="13" r="1.3" stroke="rgba(0,212,255,0.25)" stroke-width="1" fill="none"/>
                </svg>
                <p>暂无查询结果，请选择查询条件</p>
              </div>
            </template>
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

          <!-- 政策查询结果：卡片瀑布流 -->
          <div v-loading="policyLoading" class="policy-grid">
            <template v-if="!policyLoading && policyList.length === 0">
              <div class="policy-empty">
                <svg width="80" height="70" viewBox="0 0 80 70" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M8,55 Q20,47 32,55 Q44,63 56,55 Q68,47 75,51" stroke="rgba(0,212,255,0.35)" stroke-width="1.5" fill="none" stroke-linecap="round"/>
                  <path d="M5,63 Q18,56 30,63 Q42,70 54,63 Q66,56 74,60" stroke="rgba(0,212,255,0.2)" stroke-width="1.5" fill="none" stroke-linecap="round"/>
                  <ellipse cx="42" cy="32" rx="16" ry="9" fill="rgba(0,130,180,0.45)" stroke="rgba(0,212,255,0.5)" stroke-width="1"/>
                  <path d="M58,32 L66,24 L66,40 Z" fill="rgba(0,130,180,0.45)" stroke="rgba(0,212,255,0.5)" stroke-width="1"/>
                  <circle cx="32" cy="29" r="2" fill="rgba(0,212,255,0.7)"/>
                  <circle cx="27" cy="19" r="1.8" stroke="rgba(0,212,255,0.35)" stroke-width="1" fill="none"/>
                  <circle cx="22" cy="13" r="1.3" stroke="rgba(0,212,255,0.25)" stroke-width="1" fill="none"/>
                </svg>
                <p>暂无政策数据，请输入关键词搜索</p>
              </div>
            </template>
            <div v-for="(item, idx) in policyList" :key="idx" class="policy-card">
              <div class="policy-card-header">
                <span class="policy-title">{{ item.policyTitle }}</span>
              </div>
              <div class="policy-card-meta">
                <span class="policy-meta-tag">{{ item.publishUnit }}</span>
                <span class="policy-meta-tag">{{ item.publishTime ? item.publishTime.substring(0,10) : '未知日期' }}</span>
              </div>
              <div class="policy-card-body">
                <div class="policy-section">
                  <span class="policy-label">官方条文</span>
                  <p class="policy-content">{{ item.officialContent || '无' }}</p>
                </div>
                <div class="policy-section">
                  <span class="policy-label">口语化解读</span>
                  <p class="policy-content">{{ item.simpleContent || '无' }}</p>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="渔具合规查询" name="gear">
        <!-- 渔具查询：直接展示所有渔具信息 -->
        <el-card shadow="hover" class="query-card">
          <el-table :data="gearList" border stripe style="margin-top: 10px;" v-loading="gearLoading" class="result-table">
            <el-table-column prop="gearName" label="渔具名称" align="center" width="150" />
            <el-table-column label="是否允许" align="center" width="120">
              <template #default="scope">
                <el-tag
                  :type="scope.row.isAllow === 1 ? 'success' : 'danger'"
                  :class="scope.row.isAllow === 1 ? 'glow-tag glow-tag--success' : 'glow-tag glow-tag--danger'">
                  {{ scope.row.isAllow === 1 ? '✓ 允许' : '✗ 禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注/规格要求" align="center" show-overflow-tooltip />
            <el-table-column prop="punishDesc" label="违规处罚" align="center" show-overflow-tooltip />
            <template #empty>
              <div class="table-empty">
                <svg width="80" height="70" viewBox="0 0 80 70" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M8,55 Q20,47 32,55 Q44,63 56,55 Q68,47 75,51" stroke="rgba(0,212,255,0.35)" stroke-width="1.5" fill="none" stroke-linecap="round"/>
                  <path d="M5,63 Q18,56 30,63 Q42,70 54,63 Q66,56 74,60" stroke="rgba(0,212,255,0.2)" stroke-width="1.5" fill="none" stroke-linecap="round"/>
                  <ellipse cx="42" cy="32" rx="16" ry="9" fill="rgba(0,130,180,0.45)" stroke="rgba(0,212,255,0.5)" stroke-width="1"/>
                  <path d="M58,32 L66,24 L66,40 Z" fill="rgba(0,130,180,0.45)" stroke="rgba(0,212,255,0.5)" stroke-width="1"/>
                  <circle cx="32" cy="29" r="2" fill="rgba(0,212,255,0.7)"/>
                  <circle cx="27" cy="19" r="1.8" stroke="rgba(0,212,255,0.35)" stroke-width="1" fill="none"/>
                  <circle cx="22" cy="13" r="1.3" stroke="rgba(0,212,255,0.25)" stroke-width="1" fill="none"/>
                </svg>
                <p>暂无渔具数据</p>
              </div>
            </template>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { ElMessage, ElTag } from 'element-plus'
import complianceApi from '@/api/compliance'

// ===== Banner Canvas 海浪 =====
const bannerCanvas = ref(null)
let bannerAnimId = null
const bannerWaves = [
  { color: 'rgba(0,212,255,0.3)', amplitude: 10, frequency: 0.015, speed: 0.5,  offset: 0   },
  { color: 'rgba(0,150,200,0.2)', amplitude: 7,  frequency: 0.022, speed: -0.35, offset: 2.5 },
]
function drawBannerWave() {
  const canvas = bannerCanvas.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  const W = canvas.width
  const H = canvas.height
  const t = performance.now() / 1000
  ctx.clearRect(0, 0, W, H)
  ;[...bannerWaves].reverse().forEach(wave => {
    const yBase = H * 0.42
    ctx.beginPath()
    ctx.moveTo(0, H)
    for (let x = 0; x <= W; x += 3) {
      const y = yBase +
        Math.sin(x * wave.frequency + t * wave.speed + wave.offset) * wave.amplitude +
        Math.sin(x * wave.frequency * 1.8 + t * wave.speed * 0.5 + wave.offset * 1.4) * (wave.amplitude * 0.4)
      ctx.lineTo(x, y)
    }
    ctx.lineTo(W, H)
    ctx.closePath()
    ctx.fillStyle = wave.color
    ctx.fill()
  })
  bannerAnimId = requestAnimationFrame(drawBannerWave)
}
function initBannerCanvas() {
  const canvas = bannerCanvas.value
  if (!canvas) return
  canvas.width = canvas.parentElement.offsetWidth
  canvas.height = 70
  cancelAnimationFrame(bannerAnimId)
  drawBannerWave()
}

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
  initBannerCanvas()
  window.addEventListener('resize', initBannerCanvas)
})

onBeforeUnmount(() => {
  cancelAnimationFrame(bannerAnimId)
  window.removeEventListener('resize', initBannerCanvas)
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
/* ===== 页面容器 ===== */
.compliance-query-page {
  min-height: calc(100vh - 64px);
  box-sizing: border-box;
  overflow-x: hidden;
}

/* ===== 顶部横幅 ===== */
.page-banner {
  position: relative;
  padding: 36px 60px 72px;
  background: linear-gradient(135deg, rgba(0,30,80,0.92) 0%, rgba(0,55,110,0.85) 50%, rgba(0,25,70,0.92) 100%);
  overflow: hidden;
}

.page-banner::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -10%;
  width: 120%;
  height: 180%;
  background: radial-gradient(ellipse at 40% 50%, rgba(0,212,255,0.1) 0%, transparent 65%);
  pointer-events: none;
}

.banner-content {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 18px;
}

.banner-icon {
  font-size: 40px;
  line-height: 1;
  animation: banner-float 3s ease-in-out infinite;
  filter: drop-shadow(0 0 14px rgba(0,212,255,0.7));
  flex-shrink: 0;
}

@keyframes banner-float {
  0%, 100% { transform: translateY(0); }
  50%       { transform: translateY(-7px); }
}

.banner-title {
  font-size: 26px;
  font-weight: 800;
  letter-spacing: 4px;
  margin: 0;
  background: linear-gradient(90deg, #66e8ff, #00d4ff 40%, #aaf4ff);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  filter: drop-shadow(0 0 10px rgba(0,212,255,0.5));
}

.banner-sub {
  margin: 6px 0 0;
  font-size: 13px;
  color: rgba(176,216,240,0.65);
  letter-spacing: 2px;
}

.banner-wave {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 70px;
  z-index: 1;
  display: block;
}

/* ===== Tabs ===== */
.ocean-tabs {
  padding: 20px 40px 24px;
}

:deep(.ocean-tabs .el-tabs__nav-wrap::after) {
  background-color: rgba(0,212,255,0.15) !important;
  height: 1px;
}

:deep(.ocean-tabs .el-tabs__item) {
  color: #7ab8d8 !important;
  font-size: 15px;
  padding: 0 28px;
  height: 44px;
  line-height: 44px;
  transition: all 0.3s ease;
}

:deep(.ocean-tabs .el-tabs__item:hover) {
  color: #00d4ff !important;
  text-shadow: 0 0 8px rgba(0,212,255,0.4);
}

:deep(.ocean-tabs .el-tabs__item.is-active) {
  color: #00d4ff !important;
  font-weight: 600;
  text-shadow: 0 0 10px rgba(0,212,255,0.6);
}

:deep(.ocean-tabs .el-tabs__active-bar) {
  background: linear-gradient(90deg, transparent, #00d4ff, transparent) !important;
  box-shadow: 0 0 10px rgba(0,212,255,0.9), 0 0 20px rgba(0,212,255,0.4) !important;
  height: 3px !important;
  border-radius: 2px !important;
  transition: width 0.3s ease, transform 0.3s ease !important;
}

/* ===== 查询卡片 ===== */
.query-card {
  --el-card-bg-color: rgba(0,15,45,0.82) !important;
  background: rgba(0,15,45,0.82) !important;
  backdrop-filter: blur(12px) !important;
  -webkit-backdrop-filter: blur(12px) !important;
  border: 1px solid rgba(0,212,255,0.2) !important;
  border-radius: 12px !important;
  box-shadow: 0 4px 24px rgba(0,0,0,0.3), inset 0 1px 0 rgba(0,212,255,0.08) !important;
}

:deep(.query-card .el-card__body) {
  background: rgba(0,15,45,0.82) !important;
  padding: 24px !important;
}

/* ===== 查询表单 ===== */
.query-form {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  padding-bottom: 18px;
  border-bottom: 1px solid rgba(0,212,255,0.12);
  margin-bottom: 20px;
}

:deep(.query-form .el-form-item) {
  margin-right: 20px !important;
  margin-bottom: 8px !important;
}

:deep(.query-form .el-form-item__label) {
  color: #7ab8d8 !important;
  font-size: 14px;
}

/* ===== 结果表格 ===== */
.result-table {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.result-table .el-table__row:nth-child(odd) td.el-table__cell) {
  background: rgba(0,20,55,0.5) !important;
  color: #b0d8f0 !important;
}

:deep(.result-table .el-table__row:nth-child(even) td.el-table__cell) {
  background: rgba(0,35,75,0.4) !important;
  color: #b0d8f0 !important;
}

/* EP 通过 .hover-row class 控制 hover 背景，而非 :hover 伪类 */
:deep(.result-table .el-table__body tr.hover-row > td.el-table__cell) {
  background: rgba(0,160,220,0.18) !important;
  color: #00d4ff !important;
}

:deep(.result-table .el-table__body tr.hover-row > td.el-table__cell:first-child) {
  box-shadow: inset 3px 0 0 #00d4ff !important;
}

/* ===== 表格空数据状态 ===== */
.table-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 20px;
}

.table-empty p {
  margin-top: 14px;
  font-size: 13px;
  color: rgba(122,184,216,0.65);
  letter-spacing: 1px;
}

/* ===== 发光状态标签 ===== */
.glow-tag {
  border-radius: 5px !important;
  padding: 3px 10px !important;
  font-size: 12px !important;
  font-weight: 600 !important;
  letter-spacing: 0.5px;
  transition: box-shadow 0.3s ease;
}

.glow-tag--success {
  background: rgba(0,212,170,0.15) !important;
  border-color: rgba(0,212,170,0.55) !important;
  color: #00e6bb !important;
  box-shadow: 0 0 8px rgba(0,212,170,0.45), inset 0 0 6px rgba(0,212,170,0.08) !important;
}

.glow-tag--success:hover {
  box-shadow: 0 0 16px rgba(0,212,170,0.7) !important;
}

.glow-tag--danger {
  background: rgba(255,80,80,0.15) !important;
  border-color: rgba(255,80,80,0.55) !important;
  color: #ff7070 !important;
  box-shadow: 0 0 8px rgba(255,80,80,0.45), inset 0 0 6px rgba(255,80,80,0.08) !important;
}

.glow-tag--danger:hover {
  box-shadow: 0 0 16px rgba(255,80,80,0.7) !important;
}

/* ===== 文字溢出省略 ===== */
.text-overflow {
  width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  padding: 0 4px;
  cursor: help;
}

/* ===== 政策卡片瀑布流 ===== */
.policy-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 20px;
  margin-top: 20px;
  min-height: 200px;
}

.policy-card {
  background: rgba(0,22,58,0.75);
  border: 1px solid rgba(0,212,255,0.2);
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  box-shadow: 0 4px 16px rgba(0,0,0,0.25);
  backdrop-filter: blur(10px);
}

.policy-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 32px rgba(0,0,0,0.4);
}

.policy-card-header {
  margin-bottom: 10px;
}

.policy-title {
  font-size: 15px;
  font-weight: 600;
  color: #00d4ff;
  text-shadow: 0 0 8px rgba(0,212,255,0.3);
  line-height: 1.5;
  display: block;
}

.policy-card-meta {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}

.policy-meta-tag {
  background: rgba(0,212,255,0.08);
  border: 1px solid rgba(0,212,255,0.2);
  border-radius: 4px;
  padding: 2px 8px;
  font-size: 11px;
  color: #7ab8d8;
}

.policy-card-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.policy-section {
  border-left: 2px solid rgba(0,212,255,0.3);
  padding-left: 12px;
}

.policy-section:last-child {
  border-left-color: rgba(0,180,130,0.45);
}

.policy-label {
  display: inline-block;
  font-size: 11px;
  color: #00d4ff;
  background: rgba(0,212,255,0.1);
  border-radius: 3px;
  padding: 1px 6px;
  margin-bottom: 5px;
  letter-spacing: 0.5px;
}

.policy-section:last-child .policy-label {
  color: #00d4aa;
  background: rgba(0,212,170,0.1);
}

.policy-content {
  font-size: 13px;
  color: #b0d8f0;
  line-height: 1.65;
  word-break: break-all;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin: 0;
}

.policy-section:last-child .policy-content {
  color: #a0e8d0;
}

.policy-empty {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 50px 20px;
}

.policy-empty p {
  margin-top: 16px;
  font-size: 13px;
  color: rgba(122,184,216,0.65);
  letter-spacing: 1px;
}
</style>