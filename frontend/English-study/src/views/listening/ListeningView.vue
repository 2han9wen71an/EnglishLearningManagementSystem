<template>
  <div class="listening-container">
    <el-card class="title-card">
      <template #header>
        <div class="card-header">
          <span>听力练习</span>
        </div>
      </template>
      <div class="title-content">
        <p>选择下方的听力练习开始学习</p>
      </div>
    </el-card>

    <el-card class="listen-list-card">
      <template #header>
        <div class="card-header">
          <span>听力列表</span>
        </div>
      </template>
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>
      <div v-else-if="listens.length === 0" class="empty-container">
        <el-empty description="暂无听力练习" />
      </div>
      <div v-else class="listen-list">
        <el-table :data="listens" style="width: 100%">
          <el-table-column prop="listenName" label="听力名称" min-width="300" />
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button type="primary" size="small" @click="viewListen(scope.row)">
                开始练习
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 听力详情对话框 -->
    <el-dialog v-model="listenDetailVisible" title="听力练习" width="80%" fullscreen>
      <div v-if="currentListen" class="listen-detail">
        <h2 class="listen-title">{{ currentListen.listenName }}</h2>

        <div class="audio-section">
          <el-card>
            <div class="audio-player">
              <audio ref="audioPlayer" controls class="full-width">
                <source :src="audioSrc" type="audio/mpeg">
                您的浏览器不支持音频播放
              </audio>
            </div>
          </el-card>
        </div>

        <div class="content-section">
          <el-card>
            <div class="listen-content" v-html="currentListen.content"></div>
          </el-card>
        </div>

        <div v-if="currentListen.questions && currentListen.questions.length > 0" class="questions-section">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>听力题目</span>
              </div>
            </template>
            <div class="questions-list">
              <div v-for="(question, index) in currentListen.questions" :key="index" class="question-item">
                <h4>{{ index + 1 }}. {{ question.content }}</h4>
                <el-radio-group v-model="userAnswers[index]">
                  <el-radio v-for="option in question.options" :key="option.key" :label="option.key">
                    {{ option.key }}. {{ option.value }}
                  </el-radio>
                </el-radio-group>
              </div>
            </div>
            <div class="submit-section">
              <el-button type="primary" @click="submitAnswers">提交答案</el-button>
            </div>
          </el-card>
        </div>
      </div>
    </el-dialog>

    <!-- 结果对话框 -->
    <el-dialog v-model="resultVisible" title="练习结果" width="50%">
      <div class="result-container">
        <div class="score-section">
          <h3>得分: {{ score }} / {{ totalScore }}</h3>
          <el-progress :percentage="scorePercentage" :status="scoreStatus"></el-progress>
        </div>

        <div class="answers-section">
          <h4>答案详情:</h4>
          <div v-for="(question, index) in currentListen.questions" :key="index" class="answer-item">
            <p>
              <span>{{ index + 1 }}. {{ question.content }}</span>
              <el-tag
                :type="userAnswers[index] === question.answer ? 'success' : 'danger'"
                size="small"
                class="result-tag"
              >
                {{ userAnswers[index] === question.answer ? '正确' : '错误' }}
              </el-tag>
            </p>
            <p>
              <span>你的答案: {{ userAnswers[index] || '未作答' }}</span>
              <span v-if="userAnswers[index] !== question.answer">
                正确答案: {{ question.answer }}
              </span>
            </p>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="resultVisible = false">关闭</el-button>
          <el-button type="primary" @click="retryListen">重新练习</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

// 数据
const loading = ref(true)
const listens = ref<any[]>([])
const listenDetailVisible = ref(false)
const currentListen = ref<any>(null)
const userAnswers = ref<any>({})
const resultVisible = ref(false)
const score = ref(0)
const totalScore = ref(0)
const audioSrc = ref('')

// 计算属性
const scorePercentage = computed(() => {
  if (totalScore.value === 0) return 0
  return Math.round((score.value / totalScore.value) * 100)
})

const scoreStatus = computed(() => {
  const percentage = scorePercentage.value
  if (percentage >= 80) return 'success'
  if (percentage >= 60) return 'warning'
  return 'exception'
})

// 方法
const searchListens = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/listens')
    listens.value = response.data.data || []
  } catch (error) {
    console.error('获取听力列表失败:', error)
    ElMessage.error('获取听力列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const viewListen = (listen: any) => {
  currentListen.value = listen
  listenDetailVisible.value = true
  userAnswers.value = {}

  // 设置音频源
  audioSrc.value = `/api/files/audio/${listen.path}`

  // 如果有题目，设置总分
  if (listen.questions && listen.questions.length > 0) {
    totalScore.value = listen.questions.length
  }
}

const submitAnswers = () => {
  // 计算得分
  score.value = 0
  if (currentListen.value && currentListen.value.questions) {
    currentListen.value.questions.forEach((question: any, index: number) => {
      if (userAnswers.value[index] === question.answer) {
        score.value++
      }
    })
  }

  // 显示结果
  resultVisible.value = true
  listenDetailVisible.value = false
}

const retryListen = () => {
  resultVisible.value = false
  listenDetailVisible.value = true
  userAnswers.value = {}
}

// 页面加载时获取数据
onMounted(() => {
  searchListens()
})
</script>

<style scoped>
.listening-container {
  padding: 20px;
}

.title-card {
  margin-bottom: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-content {
  padding: 10px 0;
  text-align: center;
  color: #606266;
}

.loading-container, .empty-container {
  padding: 40px 0;
  text-align: center;
}

.listen-list {
  margin-top: 10px;
}

.listen-detail {
  padding: 20px;
}

.listen-title {
  text-align: center;
  margin-bottom: 30px;
  font-size: 24px;
}

.audio-section {
  margin-bottom: 30px;
}

.audio-player {
  display: flex;
  justify-content: center;
  padding: 20px;
}

.full-width {
  width: 100%;
}

.content-section {
  margin-bottom: 30px;
}

.listen-content {
  padding: 20px;
  line-height: 1.8;
  white-space: pre-wrap;
}

.questions-section {
  margin-bottom: 30px;
}

.question-item {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.question-item:last-child {
  border-bottom: none;
}

.submit-section {
  text-align: center;
  margin-top: 30px;
}

.result-container {
  padding: 20px;
}

.score-section {
  text-align: center;
  margin-bottom: 30px;
}

.answers-section {
  margin-top: 20px;
}

.answer-item {
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px dashed #eee;
}

.result-tag {
  margin-left: 10px;
}
</style>
