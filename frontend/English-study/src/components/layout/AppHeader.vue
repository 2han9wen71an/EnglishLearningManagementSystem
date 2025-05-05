<template>
  <div class="app-header">
    <div class="logo">
      <img src="@/assets/logo.svg" alt="Logo" class="logo-img" />
      <h1 class="logo-text">英语学习管理系统</h1>
    </div>
    <div class="header-right">
      <el-dropdown trigger="click" @command="handleCommand">
        <span class="el-dropdown-link">
          <el-avatar :size="32" :src="userAvatar" />
          <span class="username">{{ userName }}</span>
          <el-icon class="el-icon--right"><arrow-down /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人中心</el-dropdown-item>
            <el-dropdown-item command="settings">设置</el-dropdown-item>
            <el-dropdown-item v-if="isAdminUser" command="adminPanel" divided>管理员后台</el-dropdown-item>
            <el-dropdown-item :divided="!isAdminUser" command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { getUserInfo, clearAuth, isAdmin } from '@/utils/auth'

const router = useRouter()
const userName = ref('用户名')
const userAvatar = ref('')
// 判断当前用户是否为管理员
const isAdminUser = computed(() => isAdmin())

// 获取用户信息
onMounted(() => {
  // 从auth工具类获取用户信息
  const userInfo = getUserInfo() || {}
  userName.value = userInfo.userName || '用户名'
  userAvatar.value = userInfo.avatar || ''
})

// 处理下拉菜单命令
const handleCommand = (command: string) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'settings') {
    router.push('/settings')
  } else if (command === 'adminPanel') {
    // 跳转到管理员后台
    router.push('/admin/dashboard')
    ElMessage({
      type: 'success',
      message: '正在进入管理员后台'
    })
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(() => {
        // 清除所有认证信息
        clearAuth()

        // 跳转到登录页
        router.push('/login')
        ElMessage({
          type: 'success',
          message: '退出登录成功'
        })
      })
      .catch(() => {
        // 取消退出
      })
  }
}
</script>

<style scoped>
.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 100%;
}

.logo {
  display: flex;
  align-items: center;
}

.logo-img {
  height: 40px;
  margin-right: 10px;
}

.logo-text {
  font-size: 18px;
  font-weight: bold;
  color: #fff;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  color: #fff;
  cursor: pointer;
}

.username {
  margin: 0 5px;
}
</style>
