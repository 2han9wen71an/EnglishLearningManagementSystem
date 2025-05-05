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

const router = useRouter()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 用户信息
const userInfo = reactive({
  userId: 1,
  userName: '张三',
  email: 'zhangsan@example.com',
  avatar: ''
})

// 学习统计
const stats = reactive({
  wordsLearned: 120,
  daysStreak: 7,
  examsTaken: 3
})

// 学习进度
const progress = reactive({
  words: {
    completed: 120,
    total: 500,
    percentage: 24
  },
  listening: {
    completed: 5,
    total: 20,
    percentage: 25
  },
  reading: {
    completed: 3,
    total: 15,
    percentage: 20
  },
  essays: {
    completed: 2,
    total: 10,
    percentage: 20
  }
})

// 最近活动
const recentActivities = ref([
  {
    content: '完成了单词学习任务',
    time: '2023-05-05 10:30',
    type: 'success'
  },
  {
    content: '参加了英语四级模拟考试',
    time: '2023-05-04 14:20',
    type: 'primary'
  },
  {
    content: '提交了一篇作文《My Hometown》',
    time: '2023-05-03 16:45',
    type: 'info'
  },
  {
    content: '完成了听力练习',
    time: '2023-05-02 09:15',
    type: 'success'
  }
])

// 格式化百分比
const percentageFormat = (percentage: number) => {
  return `${percentage}%`
}

// 跳转到编辑页面
const goToEdit = () => {
  router.push('/profile/edit')
}

// 获取用户信息
onMounted(() => {
  // 这里应该调用API获取用户信息
  // 模拟从localStorage获取用户信息
  const storedUserInfo = localStorage.getItem('userInfo')
  if (storedUserInfo) {
    const parsedUserInfo = JSON.parse(storedUserInfo)
    userInfo.userId = parsedUserInfo.userId || 1
    userInfo.userName = parsedUserInfo.userName || '张三'
    userInfo.email = parsedUserInfo.email || 'zhangsan@example.com'
    userInfo.avatar = parsedUserInfo.avatar || ''
  }
  
  // 这里应该调用API获取学习统计和进度
  // 模拟数据已在上面定义
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
