<template>
  <div class="system-management">
    <!-- 顶部导航栏 -->
    <div class="system-header">
      <div class="header-left">
        <div class="wave-icon">
          <span class="bar"></span><span class="bar"></span><span class="bar"></span>
          <span class="bar"></span><span class="bar"></span>
        </div>
        <span class="header-title">海渔合规管理平台 <span class="header-sep">—</span> 系统管理</span>
      </div>
      <div class="header-right">
        <el-button class="btn-back" @click="goBack">返回功能大厅</el-button>
        <el-dropdown trigger="hover">
          <span class="user-info">
            <div class="avatar-wrapper">
              <el-avatar :icon="User" class="user-avatar" />
              <span class="online-dot"></span>
            </div>
            <span class="user-name-text">{{ userInfo.realName || userInfo.username }}</span>
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </span>
          <template v-slot:dropdown>
            <el-dropdown-menu>
              <el-dropdown-item disabled>账号：{{ userInfo.username }}</el-dropdown-item>
              <el-dropdown-item disabled>角色：管理员</el-dropdown-item>
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

    <!-- 主体内容 -->
    <div class="system-main">
      <!-- 选项卡 -->
      <el-tabs v-model="activeTab" class="system-tabs">
        <!-- 用户管理 -->
        <el-tab-pane label="用户管理" name="userManagement">
          <div class="tab-content">
            <!-- 搜索和操作栏 -->
            <div class="toolbar">
              <el-input
                v-model="userSearchKeyword"
                placeholder="搜索用户名或真实姓名"
                style="width: 300px"
                clearable
                @clear="loadUserList"
                @keyup.enter="loadUserList"
              >
                <template #append>
                  <el-button :icon="Search" @click="loadUserList" />
                </template>
              </el-input>
              <el-button class="btn-add" :icon="Plus" @click="showAddUserDialog">
                添加用户
              </el-button>
            </div>

            <!-- 用户列表 -->
            <el-table
              class="user-table"
              :data="userList"
              v-loading="userLoading"
              style="width: 100%"
              border
            >
              <el-table-column prop="userId" label="用户ID" width="80" />
              <el-table-column prop="username" label="用户名" width="120" />
              <el-table-column prop="realName" label="真实姓名" width="120" />
              <el-table-column prop="phone" label="手机号" width="130" />
              <el-table-column prop="role" label="角色" width="115">
                <template #default="scope">
                  <span v-if="scope.row.role === 2" class="role-cell role-admin">
                    <span class="role-icon">⚓</span>管理员
                  </span>
                  <span v-else class="role-cell role-user">
                    <span class="role-icon">🌊</span>渔民
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="isEnable" label="状态" width="80">
                <template #default="scope">
                  <span :class="['custom-switch', scope.row.isEnable === 1 ? 'switch-on' : 'switch-off']">
                    <span class="switch-thumb"></span>
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="创建时间" width="160">
                <template #default="scope">
                  {{ scope.row.createTime ? new Date(scope.row.createTime).toLocaleString() : '-' }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" fixed="right">
                <template #default="scope">
                  <div class="action-btns">
                    <el-button class="icon-btn edit-btn" icon="Edit" @click="showEditUserDialog(scope.row)" title="编辑" />
                    <el-button class="icon-btn del-btn" icon="Delete" @click="handleDeleteUser(scope.row.userId)" title="删除" />
                  </div>
                </template>
              </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="pagination">
              <el-pagination
                v-model:current-page="userPage"
                v-model:page-size="userPageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="userTotal"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="loadUserList"
                @current-change="loadUserList"
              />
            </div>
          </div>
        </el-tab-pane>

        <!-- 捕捞日志管理 -->
        <el-tab-pane label="捕捞日志管理" name="logManagement">
          <div class="tab-content">
            <!-- 搜索和操作栏 -->
            <div class="toolbar">
              <el-input
                v-model="logSearchDate"
                placeholder="选择日期"
                style="width: 200px"
                type="date"
                @change="loadLogList"
              />
                <el-select
                v-model="logSearchSeaId"
                placeholder="选择海域"
                style="width: 200px"
                clearable
                @change="loadLogList"
              >
                <el-option label="全部海域" value="" />
                <el-option 
                  v-for="sea in seaList" 
                  :key="sea.seaId" 
                  :label="sea.seaName" 
                  :value="sea.seaId" 
                />
                <el-option 
                  v-if="seaList.length === 0" 
                  label="暂无海域数据" 
                  value="" 
                  disabled 
                />
              </el-select>
              <el-select
                v-model="logSearchCompliant"
                placeholder="合规状态"
                style="width: 120px"
                clearable
                @change="loadLogList"
              >
                <el-option label="全部" value="" />
                <el-option label="合规" :value="1" />
                <el-option label="违规" :value="0" />
              </el-select>
            </div>
            <!-- 日志列表 -->
<el-table
  class="log-table"
  :data="logList"
  v-loading="logLoading"
  style="width: 100%"
  border
>
  <el-table-column prop="logId" label="日志ID" width="80" />
  <el-table-column prop="userId" label="用户ID" width="80" />
  <el-table-column prop="seaName" label="捕捞海域" width="120" />
  <el-table-column prop="fishingDate" label="捕捞日期" width="120" />
  <el-table-column prop="fishingGear" label="渔具类型" width="120" />
  <el-table-column prop="isCompliant" label="合规状态" width="100">
    <template #default="scope">
      <div class="status-cell">
        <span :class="scope.row.isCompliant === 1 ? 'status-dot status-dot--success' : 'status-dot status-dot--danger'"></span>
        <span :class="scope.row.isCompliant === 1 ? 'status-text--success' : 'status-text--danger'">
          {{ scope.row.isCompliant === 1 ? '合规' : '违规' }}
        </span>
      </div>
    </template>
  </el-table-column>
  <el-table-column prop="uncompliantReason" label="违规原因" show-overflow-tooltip />
  <el-table-column label="创建时间" width="160">
  <template #default="scope">
    {{ scope.row.createTime ? new Date(scope.row.createTime).toLocaleString() : '-' }}
  </template>
</el-table-column>
  <el-table-column label="操作" width="80" fixed="right">
    <template #default="scope">
      <div class="action-btns">
        <el-button class="icon-btn del-btn" icon="Delete" @click="handleDeleteLog(scope.row.logId)" title="删除" />
      </div>
    </template>
  </el-table-column>
</el-table>

            <!-- 分页 -->
            <div class="pagination">
              <el-pagination
                v-model:current-page="logPage"
                v-model:page-size="logPageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="logTotal"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="loadLogList"
                @current-change="loadLogList"
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 添加/编辑用户对话框 -->
    <el-dialog
      v-model="userDialogVisible"
      :title="userDialogTitle"
      width="500px"
      @close="resetUserForm"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userFormRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="isAddUser">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="userForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-radio-group v-model="userForm.role">
            <el-radio :label="0">普通用户</el-radio>
            <el-radio :label="2">管理员</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="isEnable">
          <el-radio-group v-model="userForm.isEnable">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveUser">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted , watch} from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Search, Plus, ArrowDown } from '@element-plus/icons-vue'
import userApi from '@/api/user'
import systemApi from '@/api/system'
import complianceApi from '@/api/compliance'

const router = useRouter()

// 用户信息
const userInfo = ref({})

// 选项卡
const activeTab = ref('userManagement')

// 用户管理相关数据
const userList = ref([])
const userLoading = ref(false)
const userPage = ref(1)
const userPageSize = ref(10)
const userTotal = ref(0)
const userSearchKeyword = ref('')

// 日志管理相关数据
const logList = ref([])
const logLoading = ref(false)
const logPage = ref(1)
const logPageSize = ref(10)
const logTotal = ref(0)
const logSearchDate = ref('')
const logSearchSeaId = ref('')
const logSearchCompliant = ref('')
const seaList = ref([])

// 用户对话框相关
const userDialogVisible = ref(false)
const isAddUser = ref(true)
const userDialogTitle = ref('添加用户')
const userFormRef = ref()
const userForm = reactive({
  userId: null,
  username: '',
  password: '',
  realName: '',
  phone: '',
  role: 0,
  isEnable: 1
})

// 表单验证规则
const userFormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}

// 初始化
onMounted(() => {
  loadUserInfo()
  loadUserList()
  loadSeaList()
  loadLogList() 
})
// 添加标签页变化监听
watch(
  () => activeTab.value,
  (newTab) => {
    if (newTab === 'logManagement') {
      loadLogList()
    }
  }
)

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const response = await userApi.getUserInfo()
    if (response.code === 200) {
      userInfo.value = response.data
      // 检查是否为管理员
      if (userInfo.value.role !== 2) {
        ElMessage.error('无权限访问系统管理模块')
        router.push('/hall')
      }
    } else {
      ElMessage.error('获取用户信息失败')
      router.push('/login')
    }
  } catch (error) {
    ElMessage.error('获取用户信息失败')
    router.push('/login')
  }
}

// 加载用户列表
const loadUserList = async () => {
  userLoading.value = true
  try {
    const params = {
      page: userPage.value,
      size: userPageSize.value,
      keyword: userSearchKeyword.value
    }
    const response = await systemApi.getUserList(params)
    if (response.code === 200) {
      userList.value = response.data
      userTotal.value = response.total
    } else {
      ElMessage.error(response.msg || '获取用户列表失败')
    }
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  } finally {
    userLoading.value = false
  }
}

// 加载海域列表
const loadSeaList = async () => {
  try {
    const response = await complianceApi.listSeaArea()
    console.log('海域列表API响应:', response) // 添加调试信息
    // 根据API返回格式调整处理方式
    if (Array.isArray(response)) {
      // 如果API返回的是数组（如用户日志页面的情况）
      seaList.value = response
    } else if (response && response.code === 200) {
      // 如果API返回的是包含code和data的对象
      seaList.value = response.data || []
    } else {
      // 其他情况设为空数组
      seaList.value = []
    }
    console.log('加载海域列表成功:', seaList.value) // 添加调试信息
  } catch (error) {
    console.error('获取海域列表失败:', error)
    seaList.value = [] // 确保列表为空
  }
}

// 加载日志列表
const loadLogList = async () => {
  logLoading.value = true
  try {
    const params = {
      page: logPage.value,
      size: logPageSize.value,
      fishingDate: logSearchDate.value,
      seaId: logSearchSeaId.value,
      isCompliant: logSearchCompliant.value
    }
    const response = await systemApi.getAllFishingLogs(params)
    if (response.code === 200) {
      logList.value = response.data
      logTotal.value = response.total
    } else {
      ElMessage.error(response.msg || '获取日志列表失败')
      // 设置为空数组以确保表格清空
      logList.value = []
      logTotal.value = 0
    }
  } catch (error) {
    ElMessage.error('获取日志列表失败')
    // 设置为空数组以确保表格清空
    logList.value = []
    logTotal.value = 0
    console.error('获取日志列表失败:', error)
  } finally {
    logLoading.value = false
  }
}

// 显示添加用户对话框
const showAddUserDialog = () => {
  isAddUser.value = true
  userDialogTitle.value = '添加用户'
  userDialogVisible.value = true
}

// 显示编辑用户对话框
const showEditUserDialog = (user) => {
  isAddUser.value = false
  userDialogTitle.value = '编辑用户'
  Object.assign(userForm, user)
  userDialogVisible.value = true
}

// 重置用户表单
const resetUserForm = () => {
  userFormRef.value?.resetFields()
  Object.assign(userForm, {
    userId: null,
    username: '',
    password: '',
    realName: '',
    phone: '',
    role: 0,
    isEnable: 1
  })
}

// 保存用户
const handleSaveUser = async () => {
  try {
    await userFormRef.value.validate()
    
    let response
    if (isAddUser.value) {
      response = await systemApi.addUser(userForm)
    } else {
      response = await systemApi.editUser(userForm)
    }
    
    if (response.code === 200) {
      ElMessage.success('保存成功')
      userDialogVisible.value = false
      loadUserList()
    } else {
      ElMessage.error(response.msg || '保存失败')
    }
  } catch (error) {
    ElMessage.error('表单验证失败')
  }
}

// 删除用户
const handleDeleteUser = async (userId) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
      type: 'warning'
    })
    
    const response = await systemApi.deleteUser(userId)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadUserList()
    } else {
      ElMessage.error(response.msg || '删除失败')
    }
  } catch (error) {
    // 用户取消删除
  }
}

// 删除日志
const handleDeleteLog = async (logId) => {
  try {
    await ElMessageBox.confirm('确定要删除该日志吗？', '提示', {
      type: 'warning'
    })
    
    const response = await systemApi.deleteFishingLog(logId)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadLogList()
    } else {
      ElMessage.error(response.msg || '删除失败')
    }
  } catch (error) {
    // 用户取消删除
  }
}

// 返回功能大厅
const goBack = () => {
  router.push('/hall')
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      type: 'warning'
    })
    
    await userApi.logout()
    sessionStorage.removeItem('loginUserId')
    ElMessage.success('退出成功')
    router.push('/login')
  } catch (error) {
    // 用户取消退出
  }
}
</script>

<style scoped>
/* ===== 整体容器 ===== */
.system-management {
  min-height: 100vh;
}

/* ===== 顶部导航栏（与 HallIndex 一致） ===== */
.system-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 32px;
  height: 64px;
  background: rgba(0, 10, 30, 0.92);
  backdrop-filter: blur(16px);
  border-bottom: 1px solid rgba(0, 212, 255, 0.25);
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.5);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

/* wave-bar 动画（与 HallIndex 一致） */
.wave-icon {
  display: flex;
  align-items: flex-end;
  gap: 3px;
  height: 22px;
}
.wave-icon .bar {
  display: inline-block;
  width: 4px;
  border-radius: 2px;
  background: linear-gradient(to top, #00aaee, #66eeff);
  animation: wave-bar 1.2s ease-in-out infinite;
}
.wave-icon .bar:nth-child(1) { height: 8px;  animation-delay: 0s;    }
.wave-icon .bar:nth-child(2) { height: 14px; animation-delay: 0.15s; }
.wave-icon .bar:nth-child(3) { height: 20px; animation-delay: 0.3s;  }
.wave-icon .bar:nth-child(4) { height: 14px; animation-delay: 0.45s; }
.wave-icon .bar:nth-child(5) { height: 8px;  animation-delay: 0.6s;  }
@keyframes wave-bar {
  0%, 100% { transform: scaleY(1);   opacity: 0.9; }
  50%       { transform: scaleY(1.7); opacity: 1;   }
}

.header-title {
  font-size: 18px;
  font-weight: 700;
  color: #e8f6ff;
  letter-spacing: 1px;
  text-shadow: 0 0 14px rgba(0, 212, 255, 0.5);
}
.header-sep { color: rgba(0, 212, 255, 0.5); margin: 0 6px; }

.header-right {
  display: flex;
  align-items: center;
  gap: 18px;
}

/* 返回按钮 */
.btn-back {
  background: linear-gradient(135deg, rgba(0, 60, 120, 0.7), rgba(0, 30, 70, 0.8)) !important;
  border: 1px solid rgba(0, 212, 255, 0.4) !important;
  color: #a8e6ff !important;
  border-radius: 8px !important;
  font-size: 13px !important;
  transition: all 0.25s !important;
}
.btn-back:hover {
  background: linear-gradient(135deg, rgba(0, 100, 180, 0.8), rgba(0, 60, 130, 0.85)) !important;
  border-color: rgba(0, 212, 255, 0.7) !important;
  color: #fff !important;
  box-shadow: 0 0 12px rgba(0, 212, 255, 0.35) !important;
}

/* 用户信息区 */
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 8px;
  border: 1px solid rgba(0, 212, 255, 0.2);
  transition: all 0.25s;
  color: #c8e8ff;
}
.user-info:hover {
  background: rgba(0, 212, 255, 0.1);
  border-color: rgba(0, 212, 255, 0.45);
}
.user-name-text { font-size: 14px; }
.avatar-wrapper {
  position: relative;
  display: inline-block;
}
.user-avatar {
  background: linear-gradient(135deg, #003366, #00224d) !important;
  border: 1px solid rgba(0, 212, 255, 0.4);
}
.online-dot {
  position: absolute;
  bottom: 0; right: 0;
  width: 9px; height: 9px;
  border-radius: 50%;
  background: #00ff88;
  border: 2px solid rgba(0, 10, 30, 0.92);
  animation: pulse-online 2s ease-in-out infinite;
}
@keyframes pulse-online {
  0%, 100% { box-shadow: 0 0 0 0 rgba(0,255,136,0.5); }
  50%       { box-shadow: 0 0 0 5px rgba(0,255,136,0);  }
}

/* ===== 主内容区 ===== */
.system-main {
  padding: 24px 40px;
}

/* ===== Tabs 深海风格 ===== */
:deep(.system-tabs .el-tabs__nav-wrap::after) {
  background-color: rgba(0, 212, 255, 0.15) !important;
  height: 1px !important;
}
:deep(.system-tabs .el-tabs__item) {
  color: rgba(180, 220, 255, 0.65) !important;
  font-weight: 500;
  transition: all 0.25s;
}
:deep(.system-tabs .el-tabs__item:hover) {
  color: #00d4ff !important;
}
:deep(.system-tabs .el-tabs__item.is-active) {
  color: #00d4ff !important;
  text-shadow: 0 0 10px rgba(0, 212, 255, 0.6);
}
:deep(.system-tabs .el-tabs__active-bar) {
  background: linear-gradient(90deg, #00d4ff, #0088cc) !important;
  height: 2px !important;
  box-shadow: 0 0 8px rgba(0, 212, 255, 0.5);
}
:deep(.system-tabs .el-tabs__content) {
  background: transparent !important;
}

/* ===== 工具栏 ===== */
.tab-content { padding: 20px 0; }
.toolbar {
  display: flex;
  gap: 14px;
  margin-bottom: 20px;
  align-items: center;
}

/* 添加按钮（青色渐变，与 FishingLog 一致） */
.btn-add {
  background: linear-gradient(135deg, #00b4cc 0%, #006688 100%) !important;
  border: none !important;
  color: #fff !important;
  border-radius: 8px !important;
  font-weight: 600 !important;
  box-shadow: 0 0 10px rgba(0, 200, 220, 0.3) !important;
  transition: all 0.25s !important;
}
.btn-add:hover {
  background: linear-gradient(135deg, #00d4ee 0%, #0088aa 100%) !important;
  box-shadow: 0 0 18px rgba(0, 212, 255, 0.55) !important;
  transform: translateY(-1px);
}

/* ===== 用户表格深海覆盖 ===== */
:deep(.user-table .el-table),
:deep(.user-table .el-table__inner-wrapper) {
  background: transparent !important;
  --el-table-bg-color: rgba(0, 15, 50, 0.7) !important;
  --el-table-tr-bg-color: rgba(0, 15, 50, 0.7) !important;
  --el-table-header-bg-color: rgba(0, 25, 70, 0.9) !important;
  --el-table-row-hover-bg-color: rgba(0, 160, 220, 0.18) !important;
  --el-table-border-color: rgba(0, 212, 255, 0.12) !important;
  --el-table-text-color: #c8e8ff !important;
  --el-table-header-text-color: #7dd8ff !important;
  border: 1px solid rgba(0, 212, 255, 0.18) !important;
  border-radius: 10px !important;
}
:deep(.user-table .el-table tr),
:deep(.user-table .el-table th),
:deep(.user-table .el-table td) {
  background-color: transparent !important;
  border-color: rgba(0, 212, 255, 0.1) !important;
}
:deep(.user-table .el-table__body tr.hover-row > td) {
  background-color: rgba(0, 160, 220, 0.18) !important;
}

/* ===== 日志表格深海覆盖 ===== */
:deep(.log-table .el-table),
:deep(.log-table .el-table__inner-wrapper) {
  background: transparent !important;
  --el-table-bg-color: rgba(0, 15, 50, 0.7) !important;
  --el-table-tr-bg-color: rgba(0, 15, 50, 0.7) !important;
  --el-table-header-bg-color: rgba(0, 25, 70, 0.9) !important;
  --el-table-row-hover-bg-color: rgba(0, 160, 220, 0.18) !important;
  --el-table-border-color: rgba(0, 212, 255, 0.12) !important;
  --el-table-text-color: #c8e8ff !important;
  --el-table-header-text-color: #7dd8ff !important;
  border: 1px solid rgba(0, 212, 255, 0.18) !important;
  border-radius: 10px !important;
}
:deep(.log-table .el-table tr),
:deep(.log-table .el-table th),
:deep(.log-table .el-table td) {
  background-color: transparent !important;
  border-color: rgba(0, 212, 255, 0.1) !important;
}
:deep(.log-table .el-table__body tr.hover-row > td) {
  background-color: rgba(0, 160, 220, 0.18) !important;
}

/* ===== 角色标签 ===== */
.role-cell {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 20px;
}
.role-icon { font-style: normal; }
.role-admin {
  color: #ffd060;
  background: rgba(255, 200, 50, 0.12);
  border: 1px solid rgba(255, 200, 50, 0.3);
  text-shadow: 0 0 8px rgba(255, 200, 0, 0.5);
}
.role-user {
  color: #00d4ff;
  background: rgba(0, 212, 255, 0.1);
  border: 1px solid rgba(0, 212, 255, 0.3);
  text-shadow: 0 0 8px rgba(0, 212, 255, 0.45);
}

/* ===== 自定义开关 ===== */
.custom-switch {
  display: inline-flex;
  align-items: center;
  width: 44px;
  height: 22px;
  border-radius: 11px;
  padding: 2px;
  cursor: default;
  transition: background 0.3s;
  position: relative;
  box-sizing: border-box;
}
.switch-on  { background: linear-gradient(90deg, #00b4cc, #0088aa); box-shadow: 0 0 8px rgba(0,212,255,0.4); }
.switch-off { background: rgba(60, 80, 110, 0.6); }
.switch-thumb {
  width: 18px; height: 18px;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.3);
  transition: transform 0.3s;
  position: absolute;
  top: 2px;
}
.switch-on  .switch-thumb { transform: translateX(22px); }
.switch-off .switch-thumb { transform: translateX(0);    }

/* ===== 合规状态脉冲点 ===== */
.status-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}
.status-dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  display: inline-block;
  flex-shrink: 0;
}
.status-dot--success {
  background: #00ff88;
  animation: pulse-green-sys 1.8s ease-in-out infinite;
}
.status-dot--danger {
  background: #ff4466;
  animation: pulse-red-sys 1.8s ease-in-out infinite;
}
@keyframes pulse-green-sys {
  0%, 100% { box-shadow: 0 0 0 0 rgba(0,255,136,0.5); transform: scale(1);   }
  50%       { box-shadow: 0 0 0 5px rgba(0,255,136,0); transform: scale(1.2); }
}
@keyframes pulse-red-sys {
  0%, 100% { box-shadow: 0 0 0 0 rgba(255,68,102,0.5); transform: scale(1);   }
  50%       { box-shadow: 0 0 0 5px rgba(255,68,102,0); transform: scale(1.2); }
}
.status-text--success { color: #00ff88; font-size: 13px; }
.status-text--danger  { color: #ff6688; font-size: 13px; }

/* ===== 图标操作按钮 ===== */
.action-btns {
  display: flex;
  align-items: center;
  gap: 8px;
}
.icon-btn {
  width: 32px !important;
  height: 32px !important;
  padding: 0 !important;
  border-radius: 8px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  transition: all 0.25s !important;
  border: 1px solid transparent !important;
}
.edit-btn {
  background: rgba(0, 150, 200, 0.15) !important;
  color: #00c8ff !important;
  border-color: rgba(0, 180, 220, 0.3) !important;
}
.edit-btn:hover {
  background: rgba(0, 180, 220, 0.25) !important;
  border-color: rgba(0, 212, 255, 0.6) !important;
  box-shadow: 0 0 12px rgba(0, 212, 255, 0.4) !important;
  transform: translateY(-1px);
}
.del-btn {
  background: rgba(200, 30, 60, 0.15) !important;
  color: #ff6688 !important;
  border-color: rgba(220, 50, 80, 0.3) !important;
}
.del-btn:hover {
  background: rgba(220, 50, 80, 0.28) !important;
  border-color: rgba(255, 80, 110, 0.6) !important;
  box-shadow: 0 0 12px rgba(255, 60, 90, 0.4) !important;
  transform: translateY(-1px);
}

/* ===== 弹窗深海玻璃态 ===== */
:deep(.el-dialog) {
  background: linear-gradient(145deg, rgba(0, 20, 55, 0.96), rgba(0, 10, 35, 0.98)) !important;
  border: 1px solid rgba(0, 212, 255, 0.25) !important;
  border-radius: 16px !important;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.7), 0 0 40px rgba(0, 100, 180, 0.15) !important;
  backdrop-filter: blur(20px) !important;
}
:deep(.el-dialog .el-dialog__title) {
  color: #a8e6ff !important;
  font-size: 16px !important;
  font-weight: 700 !important;
  letter-spacing: 1px !important;
}
:deep(.el-dialog .el-dialog__headerbtn .el-dialog__close) {
  color: rgba(0, 212, 255, 0.6) !important;
}
:deep(.el-dialog .el-dialog__headerbtn:hover .el-dialog__close) {
  color: #00d4ff !important;
}
:deep(.el-dialog .el-form-item__label) {
  color: #88ccee !important;
}
:deep(.el-dialog .el-input__wrapper) {
  background-color: rgba(0, 20, 60, 0.7) !important;
  box-shadow: 0 0 0 1px rgba(0, 212, 255, 0.3) inset !important;
}
:deep(.el-dialog .el-input__wrapper:hover),
:deep(.el-dialog .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px rgba(0, 212, 255, 0.7) inset !important;
}
:deep(.el-dialog .el-input__inner) {
  color: #d0eeff !important;
  background: transparent !important;
}
:deep(.el-dialog .el-radio__label) {
  color: #a0d4f0 !important;
}
:deep(.el-dialog .el-radio__input.is-checked .el-radio__inner) {
  background: #00d4ff !important;
  border-color: #00d4ff !important;
}
:deep(.el-dialog .el-dialog__footer .el-button--primary) {
  background: linear-gradient(135deg, #00b4cc, #006688) !important;
  border: none !important;
  color: #fff !important;
}
:deep(.el-dialog .el-dialog__footer .el-button--primary:hover) {
  background: linear-gradient(135deg, #00d4ee, #0088aa) !important;
  box-shadow: 0 0 16px rgba(0, 212, 255, 0.5) !important;
}
:deep(.el-dialog .el-dialog__footer .el-button:not(.el-button--primary)) {
  background: rgba(0, 40, 80, 0.6) !important;
  border-color: rgba(0, 212, 255, 0.25) !important;
  color: #88bbdd !important;
}

/* ===== 分页 ===== */
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
:deep(.el-pagination .el-pager li),
:deep(.el-pagination button) {
  background: rgba(0, 20, 55, 0.7) !important;
  color: #88ccee !important;
  border: 1px solid rgba(0, 212, 255, 0.2) !important;
  border-radius: 6px !important;
}
:deep(.el-pagination .el-pager li.is-active) {
  background: linear-gradient(135deg, #00b4cc, #006688) !important;
  color: #fff !important;
  border-color: transparent !important;
}
:deep(.el-pagination .el-pager li:hover),
:deep(.el-pagination button:hover) {
  color: #00d4ff !important;
  border-color: rgba(0, 212, 255, 0.5) !important;
}

/* ===== 搜索框深海样式 ===== */
:deep(.toolbar .el-input__wrapper) {
  background: rgba(0, 20, 55, 0.6) !important;
  box-shadow: 0 0 0 1px rgba(0, 212, 255, 0.25) inset !important;
  border-radius: 8px !important;
}
:deep(.toolbar .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(0, 212, 255, 0.5) inset !important;
}
:deep(.toolbar .el-input__inner) {
  color: #c8e8ff !important;
}
:deep(.toolbar .el-input__inner::placeholder) {
  color: rgba(100, 180, 220, 0.5) !important;
}
</style>
