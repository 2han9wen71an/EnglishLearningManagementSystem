<template>
  <div class="admin-statistics">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="welcome-card">
          <template #header>
            <div class="card-header">
              <h2>数据统计</h2>
            </div>
          </template>
          <div class="card-content">
            <p>系统数据统计和分析</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="mt-20">
      <el-col :xs="24" :sm="12" :md="6" v-for="(item, index) in statCards" :key="index">
        <el-card shadow="hover" class="stat-card" :body-style="{ padding: '20px' }">
          <div class="stat-card-content">
            <el-icon :size="40" class="stat-icon" :style="{ color: item.color }">
              <component :is="item.icon"></component>
            </el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-label">{{ item.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="mt-20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <h3>用户增长趋势</h3>
            </div>
          </template>
          <div class="chart-container" ref="userGrowthChart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <h3>考试完成情况</h3>
            </div>
          </template>
          <div class="chart-container" ref="examCompletionChart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <h3>系统活跃度</h3>
            </div>
          </template>
          <div class="chart-container" ref="systemActivityChart"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  User,
  Reading,
  DocumentChecked,
  Edit,
  Bell,
  Collection,
  ChatDotRound,
  Microphone,
  Headset,
  Document,
  DataAnalysis
} from '@element-plus/icons-vue'

// 统计卡片数据
const statCards = ref([
  { label: '用户总数', value: 0, icon: 'User', color: '#409EFF' },
  { label: '单词总数', value: 0, icon: 'Reading', color: '#67C23A' },
  { label: '考试总数', value: 0, icon: 'DocumentChecked', color: '#E6A23C' },
  { label: '公告总数', value: 0, icon: 'Bell', color: '#F56C6C' }
])

// 图表引用
const userGrowthChart = ref(null)
const examCompletionChart = ref(null)
const systemActivityChart = ref(null)

// 获取统计数据
const fetchStatisticsData = () => {
  // 这里应该调用API获取统计数据
  // 暂时使用模拟数据
  setTimeout(() => {
    statCards.value = [
      { label: '用户总数', value: 128, icon: 'User', color: '#409EFF' },
      { label: '单词总数', value: 5642, icon: 'Reading', color: '#67C23A' },
      { label: '考试总数', value: 24, icon: 'DocumentChecked', color: '#E6A23C' },
      { label: '公告总数', value: 15, icon: 'Bell', color: '#F56C6C' }
    ]
    
    // 初始化图表
    initCharts()
  }, 500)
}

// 初始化图表
const initCharts = () => {
  // 这里应该引入并初始化图表库，如 ECharts, Chart.js 等
  // 示例代码，实际使用时需要替换为真实的图表初始化代码
  
  // 用户增长趋势图表
  if (userGrowthChart.value) {
    const chartElement = userGrowthChart.value as HTMLElement
    chartElement.innerHTML = '<div style="height: 300px; display: flex; align-items: center; justify-content: center; color: #909399;">用户增长趋势图表（需要集成图表库）</div>'
  }
  
  // 考试完成情况图表
  if (examCompletionChart.value) {
    const chartElement = examCompletionChart.value as HTMLElement
    chartElement.innerHTML = '<div style="height: 300px; display: flex; align-items: center; justify-content: center; color: #909399;">考试完成情况图表（需要集成图表库）</div>'
  }
  
  // 系统活跃度图表
  if (systemActivityChart.value) {
    const chartElement = systemActivityChart.value as HTMLElement
    chartElement.innerHTML = '<div style="height: 300px; display: flex; align-items: center; justify-content: center; color: #909399;">系统活跃度图表（需要集成图表库）</div>'
  }
}

// 初始化
onMounted(() => {
  fetchStatisticsData()
})
</script>

<style scoped>
.admin-statistics {
  padding: 20px 0;
}

.welcome-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2, .card-header h3 {
  margin: 0;
  font-weight: 600;
}

.mt-20 {
  margin-top: 20px;
}

.stat-card {
  height: 120px;
  margin-bottom: 20px;
}

.stat-card-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stat-icon {
  margin-right: 20px;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  line-height: 1;
  margin-bottom: 10px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.chart-container {
  height: 300px;
  width: 100%;
}
</style>
