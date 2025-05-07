<template>
  <div class="register-container">
    <div class="register-background"></div>
    <div class="register-content">
      <div class="register-box">
        <div class="register-header">
          <h2>英语知识应用网站系统</h2>
          <p>创建新账号</p>
        </div>
        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          label-width="0"
          class="register-form"
        >
          <el-form-item prop="userName">
            <el-input
              v-model="registerForm.userName"
              placeholder="用户名"
              prefix-icon="User"
              size="large"
            />
          </el-form-item>
          <el-form-item prop="email">
            <el-input
              v-model="registerForm.email"
              placeholder="邮箱"
              prefix-icon="Message"
              size="large"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="密码"
              prefix-icon="Lock"
              show-password
              size="large"
            />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="确认密码"
              prefix-icon="Lock"
              show-password
              size="large"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              :loading="loading"
              class="register-button"
              size="large"
              @click="handleRegister"
            >
              注册
            </el-button>
          </el-form-item>
          <el-form-item>
            <div class="login-link">
              <span>已有账号?</span>
              <router-link to="/login">立即登录</router-link>
            </div>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElForm } from 'element-plus'
import { User, Message, Lock } from '@element-plus/icons-vue'
import { register } from '@/api/user'

const router = useRouter()
const registerFormRef = ref<InstanceType<typeof ElForm>>()
const loading = ref(false)

// 注册表单
const registerForm = reactive({
  userName: '',
  email: '',
  password: '',
  confirmPassword: ''
})

// 表单验证规则
const validatePass = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else {
    if (registerForm.confirmPassword !== '') {
      registerFormRef.value?.validateField('confirmPassword')
    }
    callback()
  }
}

const validatePass2 = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePass, trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePass2, trigger: 'blur' }
  ]
}

// 处理注册
const handleRegister = () => {
  registerFormRef.value?.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const { confirmPassword, ...registerData } = registerForm
        const res = await register(registerData)
        if (res.success) {
          ElMessage.success('注册成功，请前往邮箱激活账号')
          router.push('/login')
        } else {
          ElMessage.error(res.message || '注册失败')
        }
      } catch (error: any) {
        ElMessage.error(error.message || '注册失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.register-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}

.register-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #43cea2, #185a9d);
  z-index: -1;
}

.register-content {
  width: 100%;
  max-width: 1200px;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  box-sizing: border-box;
}

.register-box {
  width: 100%;
  max-width: 900px;
  padding: 50px;
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  align-items: center;
  backdrop-filter: blur(10px);
}

.register-header {
  text-align: center;
  margin-bottom: 40px;
}

.register-header h2 {
  font-size: 36px;
  color: #409EFF;
  margin-bottom: 15px;
  font-weight: 600;
}

.register-header p {
  font-size: 18px;
  color: #666;
}

.register-form {
  margin-top: 20px;
  width: 100%;
  max-width: 500px;
}

.register-button {
  width: 100%;
  height: 50px;
  font-size: 16px;
  margin-top: 10px;
}

.login-link {
  text-align: center;
  font-size: 16px;
  color: #666;
  margin-top: 20px;
}

.login-link a {
  color: #409EFF;
  margin-left: 5px;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .register-box {
    padding: 30px;
  }

  .register-header h2 {
    font-size: 28px;
  }

  .register-form {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .register-box {
    padding: 20px;
  }

  .register-header h2 {
    font-size: 24px;
  }
}
</style>
