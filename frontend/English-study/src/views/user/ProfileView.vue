<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="profile-card">
          <div class="profile-header">
            <el-avatar :size="100" :src="userInfo.avatar || defaultAvatar"></el-avatar>
            <h2>{{ userInfo.userName }}</h2>
            <p>{{ userInfo.email }}</p>
          </div>
          <div class="profile-stats">
            <div class="stat-item">
              <h3>{{ stats.wordsLearned }}</h3>
              <p>已学单词</p>
            </div>
            <div class="stat-item">
              <h3>{{ stats.daysStreak }}</h3>
              <p>连续学习天数</p>
            </div>
            <div class="stat-item">
              <h3>{{ stats.examsTaken }}</h3>
              <p>考试次数</p>
            </div>
          </div>
          <div class="profile-actions">
            <el-button type="primary" @click="goToEdit">编辑个人信息</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card class="learning-progress">
          <template #header>
            <div class="card-header">
              <span>学习进度</span>
            </div>
          </template>
          <div class="progress-items">
            <div class="progress-item">
              <div class="progress-label">
                <span>单词学习</span>
                <span>{{ progress.words.completed }}/{{ progress.words.total }}</span>
              </div>
              <el-progress :percentage="progress.words.percentage" :format="percentageFormat"></el-progress>
            </div>
            <div class="progress-item">
              <div class="progress-label">
                <span>听力练习</span>
                <span>{{ progress.listening.completed }}/{{ progress.listening.total }}</span>
              </div>
              <el-progress :percentage="progress.listening.percentage" :format="percentageFormat"></el-progress>
            </div>
            <div class="progress-item">
              <div class="progress-label">
                <span>阅读</span>
                <span>{{ progress.reading.completed }}/{{ progress.reading.total }}</span>
              </div>
              <el-progress :percentage="progress.reading.percentage" :format="percentageFormat"></el-progress>
            </div>
            <div class="progress-item">
              <div class="progress-label">
                <span>作文</span>
                <span>{{ progress.essays.completed }}/{{ progress.essays.total }}</span>
              </div>
              <el-progress :percentage="progress.essays.percentage" :format="percentageFormat"></el-progress>
            </div>
          </div>
        </el-card>

        <el-card class="recent-activities" style="margin-top: 20px;">
          <template #header>
            <div class="card-header">
              <span>最近活动</span>
            </div>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="(activity, index) in recentActivities"
              :key="index"
              :timestamp="activity.time"
              :type="activity.type"
            >
              {{ activity.content }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Cookies from 'js-cookie'
import { getUserInfo, getUserStats, getUserProgress, getUserRecentActivities } from '@/api/profile'
import { ElMessage } from 'element-plus'

const router = useRouter()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 用户信息
const userInfo = reactive({
  userId: 0,
  userName: '',
  email: '',
  avatar: ''
})

// 学习统计
const stats = reactive({
  wordsLearned: 0,
  daysStreak: 0,
  examsTaken: 0
})

// 学习进度
const progress = reactive({
  words: {
    completed: 0,
    total: 0,
    percentage: 0
  },
  listening: {
    completed: 0,
    total: 0,
    percentage: 0
  },
  reading: {
    completed: 0,
    total: 0,
    percentage: 0
  },
  essays: {
    completed: 0,
    total: 0,
    percentage: 0
  }
})

// 最近活动
const recentActivities = ref([])

// 格式化百分比
const percentageFormat = (percentage: number) => {
  return `${percentage}%`
}

// 跳转到编辑页面
const goToEdit = () => {
  router.push('/profile/edit')
}

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    // 从localStorage或cookie获取用户ID
    const userId = Cookies.get('userId') || localStorage.getItem('userId')

    if (!userId) {
      ElMessage.error('用户未登录，请先登录')
      router.push('/login')
      return
    }

    userInfo.userId = parseInt(userId)

    // 调用API获取用户信息
    const userResponse = await getUserInfo(userInfo.userId)
    if (userResponse.success) {
      const userData = userResponse.data
      userInfo.userName = userData.userName
      userInfo.email = userData.email
      userInfo.avatar = userData.avatar || ''

      // 保存到localStorage以便其他页面使用
      localStorage.setItem('userInfo', JSON.stringify(userData))
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    // 如果API调用失败，尝试从localStorage获取
    const storedUserInfo = localStorage.getItem('userInfo')
    if (storedUserInfo) {
      const parsedUserInfo = JSON.parse(storedUserInfo)
      userInfo.userId = parsedUserInfo.userId || 1
      userInfo.userName = parsedUserInfo.userName || '用户'
      userInfo.email = parsedUserInfo.email || 'user@example.com'
      userInfo.avatar = parsedUserInfo.avatar || ''
    }
  }
}

// 获取用户学习统计
const fetchUserStats = async () => {
  try {
    const statsResponse = await getUserStats(userInfo.userId)
    if (statsResponse.success) {
      const statsData = statsResponse.data
      stats.wordsLearned = statsData.wordsLearned
      stats.daysStreak = statsData.daysStreak
      stats.examsTaken = statsData.examsTaken
    }
  } catch (error) {
    console.error('获取用户学习统计失败:', error)
    // 使用默认值
    stats.wordsLearned = 0
    stats.daysStreak = 0
    stats.examsTaken = 0
  }
}

// 获取用户学习进度
const fetchUserProgress = async () => {
  try {
    const progressResponse = await getUserProgress(userInfo.userId)
    if (progressResponse.success) {
      const progressData = progressResponse.data

      // 更新单词学习进度
      if (progressData.words) {
        progress.words.completed = progressData.words.completed
        progress.words.total = progressData.words.total
        progress.words.percentage = progressData.words.percentage
      }

      // 更新听力练习进度
      if (progressData.listening) {
        progress.listening.completed = progressData.listening.completed
        progress.listening.total = progressData.listening.total
        progress.listening.percentage = progressData.listening.percentage
      }

      // 更新阅读进度
      if (progressData.reading) {
        progress.reading.completed = progressData.reading.completed
        progress.reading.total = progressData.reading.total
        progress.reading.percentage = progressData.reading.percentage
      }

      // 更新作文进度
      if (progressData.essays) {
        progress.essays.completed = progressData.essays.completed
        progress.essays.total = progressData.essays.total
        progress.essays.percentage = progressData.essays.percentage
      }
    }
  } catch (error) {
    console.error('获取用户学习进度失败:', error)
    // 使用默认值
    progress.words = { completed: 0, total: 100, percentage: 0 }
    progress.listening = { completed: 0, total: 20, percentage: 0 }
    progress.reading = { completed: 0, total: 15, percentage: 0 }
    progress.essays = { completed: 0, total: 10, percentage: 0 }
  }
}

// 获取用户最近活动
const fetchUserRecentActivities = async () => {
  try {
    const activitiesResponse = await getUserRecentActivities(userInfo.userId, 5)
    if (activitiesResponse.success) {
      recentActivities.value = activitiesResponse.data
    }
  } catch (error) {
    console.error('获取用户最近活动失败:', error)
    // 使用默认值
    recentActivities.value = [
      {
        content: '暂无活动记录',
        time: new Date().toLocaleString(),
        type: 'info'
      }
    ]
  }
}

// 初始化数据
onMounted(async () => {
  await fetchUserInfo()

  // 只有在成功获取用户信息后才获取其他数据
  if (userInfo.userId) {
    await Promise.all([
      fetchUserStats(),
      fetchUserProgress(),
      fetchUserRecentActivities()
    ])
  }
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
}

.profile-card {
  text-align: center;
}

.profile-header {
  margin-bottom: 20px;
}

.profile-header h2 {
  margin: 10px 0 5px;
}

.profile-header p {
  color: #909399;
  margin: 0;
}

.profile-stats {
  display: flex;
  justify-content: space-around;
  margin: 20px 0;
}

.stat-item h3 {
  margin: 0;
  font-size: 24px;
  color: #409EFF;
}

.stat-item p {
  margin: 5px 0 0;
  font-size: 14px;
  color: #606266;
}

.profile-actions {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.progress-items {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.progress-label {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}

.recent-activities {
  margin-top: 20px;
}
</style>
