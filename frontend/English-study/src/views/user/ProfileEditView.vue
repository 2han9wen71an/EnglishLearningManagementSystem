<template>
  <div class="profile-edit-container">
    <el-card class="profile-edit-card">
      <template #header>
        <div class="card-header">
          <span>编辑个人信息</span>
        </div>
      </template>
      <el-form :model="userForm" label-position="top" :rules="rules" ref="userFormRef">
        <div class="avatar-upload">
          <el-avatar :size="100" :src="userForm.avatar || defaultAvatar"></el-avatar>
          <el-upload
            class="avatar-uploader"
            action="#"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleAvatarChange"
          >
            <el-button type="primary" size="small">更换头像</el-button>
          </el-upload>
        </div>
        
        <el-form-item label="用户名" prop="userName">
          <el-input v-model="userForm.userName" placeholder="请输入用户名"></el-input>
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" disabled></el-input>
          <div class="form-tip">邮箱不可修改</div>
        </el-form-item>
        
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="userForm.oldPassword" type="password" placeholder="请输入旧密码" show-password></el-input>
        </el-form-item>
        
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="userForm.newPassword" type="password" placeholder="请输入新密码" show-password></el-input>
          <div class="form-tip">如不修改密码，请留空</div>
        </el-form-item>
        
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="userForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password></el-input>
        </el-form-item>
        
        <div class="form-actions">
          <el-button @click="goBack">取消</el-button>
          <el-button type="primary" @click="submitForm">保存</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance } from 'element-plus'

const router = useRouter()
const userFormRef = ref<FormInstance>()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 用户表单
const userForm = reactive({
  userId: 0,
  userName: '',
  email: '',
  avatar: '',
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const validatePass = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback()
  } else if (value.length < 6) {
    callback(new Error('密码长度不能小于6个字符'))
  } else {
    if (userForm.confirmPassword !== '') {
      userFormRef.value?.validateField('confirmPassword')
    }
    callback()
  }
}

const validatePass2 = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback()
  } else if (value !== userForm.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  oldPassword: [
    { required: false, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { validator: validatePass, trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validatePass2, trigger: 'blur' }
  ]
}

// 处理头像上传
const handleAvatarChange = (file: any) => {
  // 这里应该调用API上传头像
  // 模拟上传成功
  const reader = new FileReader()
  reader.readAsDataURL(file.raw)
  reader.onload = () => {
    userForm.avatar = reader.result as string
  }
}

// 提交表单
const submitForm = () => {
  userFormRef.value?.validate((valid) => {
    if (valid) {
      // 这里应该调用API更新用户信息
      // 模拟更新成功
      ElMessage.success('个人信息更新成功')
      
      // 更新localStorage中的用户信息
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      userInfo.userName = userForm.userName
      userInfo.avatar = userForm.avatar
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
      
      router.push('/profile')
    } else {
      return false
    }
  })
}

// 返回个人中心
const goBack = () => {
  router.push('/profile')
}

// 获取用户信息
onMounted(() => {
  // 这里应该调用API获取用户信息
  // 模拟从localStorage获取用户信息
  const storedUserInfo = localStorage.getItem('userInfo')
  if (storedUserInfo) {
    const parsedUserInfo = JSON.parse(storedUserInfo)
    userForm.userId = parsedUserInfo.userId || 0
    userForm.userName = parsedUserInfo.userName || ''
    userForm.email = parsedUserInfo.email || ''
    userForm.avatar = parsedUserInfo.avatar || ''
  }
})
</script>

<style scoped>
.profile-edit-container {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

.avatar-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
}

.avatar-uploader {
  margin-top: 10px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
