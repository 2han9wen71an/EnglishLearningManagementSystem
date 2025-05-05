<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="welcome-card">
          <div class="welcome-content">
            <h2>欢迎使用英语学习管理系统</h2>
            <p>{{ currentTime }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="mt-20">
      <el-col :span="8">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <span>单词学习</span>
              <el-button class="button" text>查看更多</el-button>
            </div>
          </template>
          <div class="stat-content">
            <el-statistic title="已学单词" :value="wordStats.learned" />
            <el-progress :percentage="wordStats.percentage" :format="format" />
            <p>今日目标: {{ wordStats.target }} 个单词</p>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <span>听力练习</span>
              <el-button class="button" text>查看更多</el-button>
            </div>
          </template>
          <div class="stat-content">
            <el-statistic title="已完成练习" :value="listeningStats.completed" />
            <el-progress :percentage="listeningStats.percentage" :format="format" />
            <p>总共: {{ listeningStats.total }} 个练习</p>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <span>考试成绩</span>
              <el-button class="button" text>查看更多</el-button>
            </div>
          </template>
          <div class="stat-content">
            <el-statistic title="平均分" :value="examStats.average" />
            <el-progress :percentage="examStats.percentage" :format="format" :status="examStats.status" />
            <p>已参加: {{ examStats.count }} 次考试</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="mt-20">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最新公告</span>
            </div>
          </template>
          <div class="notice-content">
            <h3>{{ notice.title }}</h3>
            <p class="notice-time">{{ notice.time }}</p>
            <div class="notice-text">{{ notice.content }}</div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>每日一句</span>
              <el-button class="button" text @click="refreshSentence">刷新</el-button>
            </div>
          </template>
          <div class="sentence-content">
            <p class="sentence-text">{{ sentence.text }}</p>
            <p class="sentence-translation">{{ sentence.translation }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

// 当前时间
const currentTime = ref('')

// 更新当前时间
const updateTime = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth() + 1
  const day = now.getDate()
  const hours = now.getHours().toString().padStart(2, '0')
  const minutes = now.getMinutes().toString().padStart(2, '0')
  const seconds = now.getSeconds().toString().padStart(2, '0')
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  const weekday = weekdays[now.getDay()]
  
  currentTime.value = `${year}年${month}月${day}日 ${hours}:${minutes}:${seconds} ${weekday}`
}

// 单词学习统计
const wordStats = ref({
  learned: 120,
  target: 200,
  percentage: 60
})

// 听力练习统计
const listeningStats = ref({
  completed: 8,
  total: 15,
  percentage: 53
})

// 考试成绩统计
const examStats = ref({
  average: 85,
  count: 5,
  percentage: 85,
  status: 'success'
})

// 进度条格式化
const format = (percentage: number) => {
  return `${percentage}%`
}

// 最新公告
const notice = ref({
  title: '系统更新通知',
  time: '2023-05-01 10:00',
  content: '尊敬的用户，我们将于近期对系统进行升级，新版本将增加情景对话、作文批改等功能，敬请期待！'
})

// 每日一句
const sentence = ref({
  text: 'The best preparation for tomorrow is doing your best today.',
  translation: '对明天最好的准备就是今天做到最好。'
})

// 刷新每日一句
const refreshSentence = () => {
  // 这里应该调用API获取新的每日一句
  // 模拟数据
  const sentences = [
    {
      text: 'The best preparation for tomorrow is doing your best today.',
      translation: '对明天最好的准备就是今天做到最好。'
    },
    {
      text: 'Life is like riding a bicycle. To keep your balance, you must keep moving.',
      translation: '生活就像骑自行车，要保持平衡就得不断前进。'
    },
    {
      text: 'The only limit to our realization of tomorrow will be our doubts of today.',
      translation: '实现明天理想的唯一障碍是今天的疑虑。'
    }
  ]
  
  const randomIndex = Math.floor(Math.random() * sentences.length)
  sentence.value = sentences[randomIndex]
}

// 页面加载时
onMounted(() => {
  updateTime()
  // 每秒更新时间
  setInterval(updateTime, 1000)
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.mt-20 {
  margin-top: 20px;
}

.welcome-card {
  margin-bottom: 20px;
}

.welcome-content {
  text-align: center;
}

.welcome-content h2 {
  font-size: 24px;
  color: #409EFF;
  margin-bottom: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-content {
  text-align: center;
}

.notice-content h3 {
  margin-top: 0;
  color: #333;
}

.notice-time {
  font-size: 12px;
  color: #999;
  margin-bottom: 10px;
}

.notice-text {
  line-height: 1.6;
}

.sentence-content {
  padding: 10px 0;
}

.sentence-text {
  font-size: 16px;
  line-height: 1.6;
  color: #333;
  margin-bottom: 10px;
}

.sentence-translation {
  font-size: 14px;
  color: #666;
}
</style>
