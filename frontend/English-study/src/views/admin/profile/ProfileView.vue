<template>
  <div class="admin-profile">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="profile-card">
          <div class="profile-header">
            <el-avatar :size="100" :src="userInfo.avatar" />
            <h2>{{ userInfo.userName }}</h2>
            <p>{{ userInfo.role === 1 ? '管理员' : '普通用户' }}</p>
          </div>
          <div class="profile-info">
            <p><el-icon><Message /></el-icon> {{ userInfo.email }}</p>
            <p><el-icon><Calendar /></el-icon> 注册时间：{{ userInfo.createTime }}</p>
            <p><el-icon><Timer /></el-icon> 上次登录：{{ userInfo.lastLoginTime }}</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <h2>个人信息</h2>
            </div>
          </template>
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
            <el-form-item label="头像">
              <el-upload
                class="avatar-uploader"
                action="/api/upload/avatar"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
              >
                <img v-if="userForm.avatar" :src="userForm.avatar" class="avatar" />
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitUserForm">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="mt-20">
          <template #header>
            <div class="card-header">
              <h2>修改密码</h2>
            </div>
          </template>
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordFormRules"
            label-width="100px"
          >
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入当前密码" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitPasswordForm">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Message, Calendar, Timer, Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import Cookies from 'js-cookie'
import { getUserInfo as fetchUserInfoApi, updateUserInfo, changePassword } from '@/api/user'

// 用户信息
const userInfo = reactive({
  userId: 0,
  userName: '',
  email: '',
  avatar: '',
  role: 1,
  createTime: '',
  lastLoginTime: ''
})

// 表单相关
const userFormRef = ref<FormInstance>()
const userForm = reactive({
  userName: '',
  email: '',
  avatar: ''
})

const userFormRules = reactive<FormRules>({
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
})

// 密码表单相关
const passwordFormRef = ref<FormInstance>()
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordFormRules = reactive<FormRules>({
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
})

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    const userId = Cookies.get('userId')
    if (!userId) {
      ElMessage.error('未找到用户ID，请重新登录')
      return
    }

    const response = await fetchUserInfoApi(Number(userId))
    if (response.success && response.data) {
      const userData = response.data

      // 更新用户信息
      Object.assign(userInfo, {
        userId: userData.userId,
        userName: userData.userName,
        email: userData.email,
        avatar: userData.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
        role: userData.role,
        createTime: userData.createTime || '未知',
        lastLoginTime: userData.lastLoginTime || '未知'
      })

      // 填充表单
      userForm.userName = userInfo.userName
      userForm.email = userInfo.email
      userForm.avatar = userInfo.avatar
    } else {
      ElMessage.error(response.message || '获取用户信息失败')
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败，请稍后重试')
  }
}

// 头像上传相关
const handleAvatarSuccess = (response: any, file: any) => {
  userForm.avatar = response.data.url
  ElMessage.success('上传成功')
}

const beforeAvatarUpload = (file: any) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('上传头像图片只能是JPG或PNG格式!')
  }
  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过2MB!')
  }
  return isJPG && isLt2M
}

// 提交用户表单
const submitUserForm = async () => {
  if (!userFormRef.value) return

  await userFormRef.value.validate(async (valid, fields) => {
    if (valid) {
      try {
        // 调用API更新用户信息
        const response = await updateUserInfo(userInfo.userId, {
          userName: userForm.userName,
          email: userForm.email,
          avatar: userForm.avatar
        })

        if (response.success) {
          ElMessage.success('保存成功')

          // 更新本地显示
          userInfo.userName = userForm.userName
          userInfo.email = userForm.email
          userInfo.avatar = userForm.avatar
        } else {
          ElMessage.error(response.message || '更新用户信息失败')
        }
      } catch (error) {
        console.error('更新用户信息失败:', error)
        ElMessage.error('更新用户信息失败，请稍后重试')
      }
    } else {
      console.log('表单验证失败', fields)
    }
  })
}

// 提交密码表单
const submitPasswordForm = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid, fields) => {
    if (valid) {
      try {
        // 调用API修改密码
        const response = await changePassword(userInfo.userId, {
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        })

        if (response.success) {
          ElMessage.success('密码修改成功')

          // 清空表单
          passwordForm.oldPassword = ''
          passwordForm.newPassword = ''
          passwordForm.confirmPassword = ''
        } else {
          ElMessage.error(response.message || '密码修改失败')
        }
      } catch (error) {
        console.error('密码修改失败:', error)
        ElMessage.error('密码修改失败，请稍后重试')
      }
    } else {
      console.log('表单验证失败', fields)
    }
  })
}

// 初始化
onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.admin-profile {
  padding: 20px 0;
}

.profile-card {
  height: 100%;
}

.profile-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
  border-bottom: 1px solid #eee;
}

.profile-header h2 {
  margin: 10px 0 5px;
  font-size: 20px;
}

.profile-header p {
  margin: 0;
  color: #909399;
}

.profile-info {
  padding: 20px 0;
}

.profile-info p {
  display: flex;
  align-items: center;
  margin: 10px 0;
  color: #606266;
}

.profile-info .el-icon {
  margin-right: 10px;
  color: #409EFF;
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

.mt-20 {
  margin-top: 20px;
}

.avatar-uploader {
  display: flex;
  justify-content: center;
}

.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  text-align: center;
  line-height: 100px;
}

.avatar {
  width: 100px;
  height: 100px;
  display: block;
}
</style>
