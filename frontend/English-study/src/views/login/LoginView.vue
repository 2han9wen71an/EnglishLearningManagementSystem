<template>
  <div class="login-container">
    <div class="login-background"></div>
    <div class="login-content">
      <div class="login-box">
        <div class="login-header">
          <h2>英语学习管理系统</h2>
          <p>登录您的账号</p>
        </div>
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          label-width="0"
          class="login-form"
        >
          <el-form-item prop="email">
            <el-input
              v-model="loginForm.email"
              placeholder="邮箱"
              prefix-icon="Message"
              size="large"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="密码"
              prefix-icon="Lock"
              show-password
              size="large"
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item>
            <div class="login-options">
              <el-checkbox v-model="rememberMe">记住我</el-checkbox>
              <el-link type="primary" :underline="false" href="#">忘记密码?</el-link>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              :loading="loading"
              class="login-button"
              size="large"
              @click="handleLogin"
            >
              登录
            </el-button>
          </el-form-item>
          <el-form-item>
            <div class="register-link">
              <span>还没有账号?</span>
              <router-link to="/register">立即注册</router-link>
            </div>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElForm } from 'element-plus'
import { Message, Lock } from '@element-plus/icons-vue'
import { setToken, setUserId, setUserRole, setUserInfo } from '@/utils/auth'
import { login } from '@/api/user'

const router = useRouter()
const route = useRoute()
const loginFormRef = ref<InstanceType<typeof ElForm>>()
const loading = ref(false)
const rememberMe = ref(false)

// 登录表单
const loginForm = reactive({
  email: '',
  password: ''
})

// 表单验证规则
const loginRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' }
  ]
}

// 处理登录
const handleLogin = () => {
  loginFormRef.value?.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await login(loginForm)
        if (res.success) {
          // 保存token和用户信息
          const expiresIn = rememberMe.value ? 7 : 1
          setToken(res.data.token, expiresIn)
          setUserRole(String(res.data.role), expiresIn)
          setUserId(String(res.data.userId), expiresIn)
          setUserInfo(res.data)

          ElMessage.success('登录成功')

          // 跳转到首页或重定向页面
          const redirect = route.query.redirect as string
          router.push(redirect || '/dashboard')
        } else {
          ElMessage.error(res.message || '登录失败')
        }
      } catch (error: any) {
        ElMessage.error(error.message || '登录失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
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

.login-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #1c92d2, #f2fcfe);
  z-index: -1;
}

.login-content {
  width: 100%;
  max-width: 1200px;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  box-sizing: border-box;
}

.login-box {
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

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-header h2 {
  font-size: 36px;
  color: #409EFF;
  margin-bottom: 15px;
  font-weight: 600;
}

.login-header p {
  font-size: 18px;
  color: #666;
}

.login-form {
  margin-top: 20px;
  width: 100%;
  max-width: 500px;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 10px 0;
}

.login-button {
  width: 100%;
  height: 50px;
  font-size: 16px;
  margin-top: 10px;
}

.register-link {
  text-align: center;
  font-size: 16px;
  color: #666;
  margin-top: 20px;
}

.register-link a {
  color: #409EFF;
  margin-left: 5px;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-box {
    padding: 30px;
  }

  .login-header h2 {
    font-size: 28px;
  }

  .login-form {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .login-box {
    padding: 20px;
  }

  .login-header h2 {
    font-size: 24px;
  }
}
</style>
