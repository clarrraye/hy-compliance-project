<template>
  <div class="system-management">
    <!-- 顶部导航栏 -->
    <div class="system-header">
      <div class="header-left">海渔合规管理平台 - 系统管理</div>
      <div class="header-right">
        <el-button type="primary" @click="goBack">返回功能大厅</el-button>
        <el-dropdown trigger="hover">
          <span class="user-info">
            <el-avatar :icon="User" class="user-avatar" />
            {{ userInfo.realName || userInfo.username }}
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
      <el-tabs v-model="activeTab" type="card" class="system-tabs">
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
              <el-button type="primary" :icon="Plus" @click="showAddUserDialog">
                添加用户
              </el-button>
            </div>

            <!-- 用户列表 -->
            <el-table
              :data="userList"
              v-loading="userLoading"
              style="width: 100%"
              border
            >
              <el-table-column prop="userId" label="用户ID" width="80" />
              <el-table-column prop="username" label="用户名" width="120" />
              <el-table-column prop="realName" label="真实姓名" width="120" />
              <el-table-column prop="phone" label="手机号" width="130" />
              <el-table-column prop="role" label="角色" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.role === 2 ? 'success' : 'info'">
                    {{ scope.row.role === 2 ? '管理员' : '普通用户' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="isEnable" label="状态" width="80">
                <template #default="scope">
                  <el-tag :type="scope.row.isEnable === 1 ? 'success' : 'danger'">
                    {{ scope.row.isEnable === 1 ? '启用' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="创建时间" width="160">
                <template #default="scope">
                  {{ scope.row.createTime ? new Date(scope.row.createTime).toLocaleString() : '-' }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="scope">
                  <el-button size="small" @click="showEditUserDialog(scope.row)">
                    编辑
                  </el-button>
                  <el-button
                    size="small"
                    type="danger"
                    @click="handleDeleteUser(scope.row.userId)"
                  >
                    删除
                  </el-button>
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
      <el-tag :type="scope.row.isCompliant === 1 ? 'success' : 'danger'">
        {{ scope.row.isCompliant === 1 ? '合规' : '违规' }}
      </el-tag>
    </template>
  </el-table-column>
  <el-table-column prop="uncompliantReason" label="违规原因" show-overflow-tooltip />
  <el-table-column label="创建时间" width="160">
  <template #default="scope">
    {{ scope.row.createTime ? new Date(scope.row.createTime).toLocaleString() : '-' }}
  </template>
</el-table-column>
  <el-table-column label="操作" width="100" fixed="right">
    <template #default="scope">
      <el-button
        size="small"
        type="danger"
        @click="handleDeleteLog(scope.row.logId)"
      >
        删除
      </el-button>
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
.system-management {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.system-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.header-left {
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.user-avatar {
  margin-right: 8px;
}

.system-main {
  padding: 20px;
}

.system-tabs {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.tab-content {
  padding: 20px;
}

.toolbar {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  align-items: center;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
