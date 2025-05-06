<template>
  <div class="admin-dashboard">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="welcome-card">
          <template #header>
            <div class="card-header">
              <h2>欢迎您，尊敬的管理员</h2>
            </div>
          </template>
          <div class="card-content">
            <p><strong>您拥有以下的操作权限</strong></p>
            <el-row :gutter="20" class="permission-list">
              <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="(item, index) in permissions" :key="index">
                <el-card shadow="hover" class="permission-item">
                  <el-icon :size="24" class="permission-icon">
                    <component :is="item.icon"></component>
                  </el-icon>
                  <div class="permission-text">{{ item.text }}</div>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-col>
    </el-row>

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

    <el-row :gutter="20" class="mt-20">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <h3>快捷操作</h3>
            </div>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="$router.push('/admin/users')">
              <el-icon><User /></el-icon>用户管理
            </el-button>
            <el-button type="success" @click="$router.push('/admin/words')">
              <el-icon><Reading /></el-icon>单词管理
            </el-button>
            <el-button type="warning" @click="$router.push('/admin/exams')">
              <el-icon><DocumentChecked /></el-icon>考试管理
            </el-button>
            <el-button type="danger" @click="$router.push('/admin/notices')">
              <el-icon><Bell /></el-icon>公告管理
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
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
  Document
} from '@element-plus/icons-vue'
import { getSystemStatistics } from '@/api/statistics'

// 权限列表
const permissions = [
  { text: '用户的增删改查', icon: 'User' },
  { text: '单词的增删改查', icon: 'Reading' },
  { text: '听力的增删改查', icon: 'Headset' },
  { text: '阅读的增删改查', icon: 'Document' },
  { text: '公告的增删改查', icon: 'Bell' },
  { text: '考试的管理', icon: 'DocumentChecked' },
  { text: '数据统计分析', icon: 'DataAnalysis' }
]

// 统计卡片数据
const statCards = ref([
  { label: '用户总数', value: 0, icon: 'User', color: '#409EFF' },
  { label: '单词总数', value: 0, icon: 'Reading', color: '#67C23A' },
  { label: '考试总数', value: 0, icon: 'DocumentChecked', color: '#E6A23C' },
  { label: '公告总数', value: 0, icon: 'Bell', color: '#F56C6C' }
])

// 获取统计数据
const fetchStatisticsData = async () => {
  try {
    const response = await getSystemStatistics()
    if (response.success) {
      const data = response.data

      // 更新统计卡片数据
      statCards.value = [
        { label: '用户总数', value: data.userCount || 0, icon: 'User', color: '#409EFF' },
        { label: '单词总数', value: data.wordCount || 0, icon: 'Reading', color: '#67C23A' },
        { label: '考试总数', value: data.examCount || 0, icon: 'DocumentChecked', color: '#E6A23C' },
        { label: '公告总数', value: data.noticeCount || 0, icon: 'Bell', color: '#F56C6C' }
      ]
    } else {
      ElMessage.error(response.message || '获取统计数据失败')
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败，请稍后重试')

    // 使用默认数据
    statCards.value = [
      { label: '用户总数', value: 0, icon: 'User', color: '#409EFF' },
      { label: '单词总数', value: 0, icon: 'Reading', color: '#67C23A' },
      { label: '考试总数', value: 0, icon: 'DocumentChecked', color: '#E6A23C' },
      { label: '公告总数', value: 0, icon: 'Bell', color: '#F56C6C' }
    ]
  }
}

// 页面加载时获取数据
onMounted(() => {
  fetchStatisticsData()
})
</script>

<style scoped>
.admin-dashboard {
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

.permission-list {
  margin-top: 20px;
}

.permission-item {
  height: 100px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 20px;
}

.permission-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.permission-icon {
  margin-bottom: 10px;
}

.permission-text {
  font-size: 14px;
  text-align: center;
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

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.quick-actions .el-button {
  margin-bottom: 10px;
}
</style>
