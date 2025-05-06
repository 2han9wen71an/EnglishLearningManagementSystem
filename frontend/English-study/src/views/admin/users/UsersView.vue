<template>
  <div class="admin-users">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>用户管理</h2>
          <el-button type="primary" @click="handleAddUser">
            <el-icon><Plus /></el-icon>添加用户
          </el-button>
        </div>
      </template>

      <!-- 搜索和筛选区域 -->
      <div class="search-container">
        <el-input
          v-model="searchQuery"
          placeholder="搜索用户名/邮箱"
          class="search-input"
          clearable
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-select v-model="roleFilter" placeholder="角色筛选" clearable @change="handleSearch">
          <el-option
            v-for="item in roleOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>

        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>

      <!-- 表格区域 -->
      <el-table
        :data="userList"
        border
        stripe
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="userId" label="ID" width="80" />
        <el-table-column prop="userName" label="用户名" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column label="角色" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.role === 1 ? 'danger' : 'success'">
              {{ scope.row.role === 1 ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.activeStatus === 1 ? 'success' : 'info'">
              {{ scope.row.activeStatus === 1 ? '已激活' : '未激活' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              @click="handleEditUser(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDeleteUser(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 用户表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '添加用户' : '编辑用户'"
      width="500px"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userFormRules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="userName">
          <el-input v-model="userForm.userName" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="dialogType === 'add'">
          <el-input v-model="userForm.password" placeholder="请输入密码" type="password" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择角色">
            <el-option label="普通用户" :value="0" />
            <el-option label="管理员" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="activeStatus">
          <el-select v-model="userForm.activeStatus" placeholder="请选择状态">
            <el-option label="未激活" :value="0" />
            <el-option label="已激活" :value="1" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitUserForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { getUserList, addUser, updateUser, deleteUser } from '@/api/user'

// 用户列表数据
const userList = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchQuery = ref('')
const roleFilter = ref('')

// 角色选项
const roleOptions = [
  { value: 0, label: '普通用户' },
  { value: 1, label: '管理员' }
]

// 对话框相关
const dialogVisible = ref(false)
const dialogType = ref('add') // 'add' 或 'edit'
const userFormRef = ref<FormInstance>()
const userForm = reactive({
  userId: 0,
  userName: '',
  email: '',
  password: '',
  role: 0,
  activeStatus: 1
})

// 表单验证规则
const userFormRules = reactive<FormRules>({
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  activeStatus: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
})

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true
  try {
    // 构建查询参数
    const params: any = {
      page: currentPage.value,
      size: pageSize.value
    }

    // 添加搜索条件
    if (searchQuery.value) {
      params.query = searchQuery.value
    }

    // 添加角色筛选
    if (roleFilter.value !== '') {
      params.role = roleFilter.value
    }

    // 调用API获取用户列表
    const response = await getUserList(params)

    if (response.success) {
      // 更新用户列表和总数
      if (response.data && Array.isArray(response.data.data)) {
        userList.value = response.data.data || []
        total.value = response.data.total || userList.value.length
      } else {
        // 兼容旧格式
        userList.value = response.data || []
        total.value = response.total || userList.value.length
      }
    } else {
      ElMessage.error(response.message || '获取用户列表失败')
      userList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败，请稍后重试')
    userList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchUserList()
}

// 分页相关
const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchUserList()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchUserList()
}

// 添加用户
const handleAddUser = () => {
  dialogType.value = 'add'
  userForm.userId = 0
  userForm.userName = ''
  userForm.email = ''
  userForm.password = ''
  userForm.role = 0
  userForm.activeStatus = 1
  dialogVisible.value = true
}

// 编辑用户
const handleEditUser = (row: any) => {
  dialogType.value = 'edit'
  userForm.userId = row.userId
  userForm.userName = row.userName
  userForm.email = row.email
  userForm.password = '' // 编辑时不显示密码
  userForm.role = row.role
  userForm.activeStatus = row.activeStatus
  dialogVisible.value = true
}

// 删除用户
const handleDeleteUser = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除用户 ${row.userName} 吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      // 调用API删除用户
      const response = await deleteUser(row.userId)
      if (response.success) {
        ElMessage.success('删除用户成功')
        fetchUserList()
      } else {
        ElMessage.error(response.message || '删除用户失败')
      }
    } catch (error) {
      console.error('删除用户失败:', error)
      ElMessage.error('删除用户失败，请稍后重试')
    }
  }).catch(() => {
    // 取消删除
  })
}

// 提交用户表单
const submitUserForm = async () => {
  if (!userFormRef.value) return

  await userFormRef.value.validate(async (valid, fields) => {
    if (valid) {
      try {
        if (dialogType.value === 'add') {
          // 添加用户
          const response = await addUser(userForm)
          if (response.success) {
            ElMessage.success('添加用户成功')
            dialogVisible.value = false
            fetchUserList()
          } else {
            ElMessage.error(response.message || '添加用户失败')
          }
        } else {
          // 更新用户
          const response = await updateUser(userForm.userId, userForm)
          if (response.success) {
            ElMessage.success('更新用户成功')
            dialogVisible.value = false
            fetchUserList()
          } else {
            ElMessage.error(response.message || '更新用户失败')
          }
        }
      } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error('操作失败，请稍后重试')
      }
    } else {
      console.log('表单验证失败', fields)
    }
  })
}

// 初始化
onMounted(() => {
  fetchUserList()
})
</script>

<style scoped>
.admin-users {
  padding: 20px 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-weight: 600;
}

.search-container {
  display: flex;
  margin-bottom: 20px;
  gap: 10px;
}

.search-input {
  width: 300px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
