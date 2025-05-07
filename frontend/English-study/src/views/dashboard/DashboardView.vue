<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="welcome-card">
          <div class="welcome-content">
            <h2>欢迎使用英语知识应用网站系统</h2>
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
import { getRandomSentence } from '@/api/sentence'
import { getLatestNotice } from '@/api/notice'
import { getDashboardData } from '@/api/dashboard'
import Cookies from 'js-cookie'
import { ElMessage } from 'element-plus'

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
  learned: 0,
  target: 200,
  percentage: 0
})

// 听力练习统计
const listeningStats = ref({
  completed: 0,
  total: 0,
  percentage: 0
})

// 考试成绩统计
const examStats = ref({
  average: 0,
  count: 0,
  percentage: 0,
  status: 'success'
})

// 进度条格式化
const format = (percentage: number) => {
  return `${percentage}%`
}

// 最新公告
const notice = ref({
  title: '加载中...',
  time: '',
  content: '正在加载公告内容...'
})

// 每日一句
const sentence = ref({
  text: '加载中...',
  translation: '正在加载翻译...'
})

// 获取仪表盘数据
const fetchDashboardData = async () => {
  try {
    const userId = Cookies.get('userId')
    if (!userId) {
      ElMessage.error('未找到用户信息，请重新登录')
      return
    }

    const response = await getDashboardData(parseInt(userId))
    if (response.success) {
      const data = response.data

      // 更新单词学习统计
      if (data.wordStats) {
        wordStats.value = data.wordStats
      }

      // 更新听力练习统计
      if (data.listeningStats) {
        listeningStats.value = data.listeningStats
      }

      // 更新考试成绩统计
      if (data.examStats) {
        examStats.value = data.examStats
      }
    }
  } catch (error) {
    console.error('获取仪表盘数据失败:', error)
    ElMessage.error('获取仪表盘数据失败，请稍后重试')
  }
}

// 获取最新公告
const fetchLatestNotice = async () => {
  try {
    const response = await getLatestNotice()
    if (response.success) {
      const noticeData = response.data
      notice.value = {
        title: noticeData.title,
        time: formatDate(noticeData.creatTime),
        content: noticeData.content
      }
    }
  } catch (error) {
    console.error('获取最新公告失败:', error)
    // 使用默认公告
    notice.value = {
      title: '系统公告',
      time: formatDate(new Date()),
      content: '暂无公告内容'
    }
  }
}

// 格式化日期
const formatDate = (dateStr: string | Date) => {
  const date = typeof dateStr === 'string' ? new Date(dateStr) : dateStr
  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')

  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 刷新每日一句
const refreshSentence = async () => {
  try {
    const response = await getRandomSentence()
    if (response.success) {
      const sentenceData = response.data
      sentence.value = {
        text: sentenceData.sentenceName,
        translation: sentenceData.explain
      }
    }
  } catch (error) {
    console.error('获取每日一句失败:', error)
    // 使用默认句子
    sentence.value = {
      text: 'The best preparation for tomorrow is doing your best today.',
      translation: '对明天最好的准备就是今天做到最好。'
    }
  }
}

// 页面加载时
onMounted(() => {
  updateTime()
  // 每秒更新时间
  setInterval(updateTime, 1000)

  // 获取仪表盘数据
  fetchDashboardData()

  // 获取最新公告
  fetchLatestNotice()

  // 获取每日一句
  refreshSentence()
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
