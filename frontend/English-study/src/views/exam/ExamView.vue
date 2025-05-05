<template>
  <div class="exam-container">
    <!-- 考试列表页面 -->
    <div v-if="!currentExam && !examRecord">
      <el-card class="exam-list-card">
        <template #header>
          <div class="card-header">
            <span>考试列表</span>
            <el-button type="primary" @click="viewExamHistory">查看历史考试</el-button>
          </div>
        </template>
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="5" animated />
        </div>
        <div v-else-if="exams.length === 0" class="empty-container">
          <el-empty description="暂无考试" />
        </div>
        <div v-else class="exam-list">
          <el-table :data="exams" style="width: 100%">
            <el-table-column prop="title" label="考试名称" min-width="200" />
            <el-table-column prop="duration" label="考试时长" width="120">
              <template #default="scope">
                {{ scope.row.duration }} 分钟
              </template>
            </el-table-column>
            <el-table-column prop="totalScore" label="总分" width="100" />
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button type="primary" size="small" @click="viewExam(scope.row)">
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>
    </div>

    <!-- 考试详情页面 -->
    <div v-else-if="currentExam && !examRecord">
      <el-card class="exam-detail-card">
        <template #header>
          <div class="card-header">
            <span>考试详情</span>
            <el-button @click="backToList">返回列表</el-button>
          </div>
        </template>
        <div class="exam-detail">
          <h2 class="exam-title">{{ currentExam.title }}</h2>

          <div class="exam-info">
            <div class="info-item">
              <span class="info-label">考试时长:</span>
              <span class="info-value">{{ currentExam.duration }} 分钟</span>
            </div>
            <div class="info-item">
              <span class="info-label">总分:</span>
              <span class="info-value">{{ currentExam.totalScore }} 分</span>
            </div>
            <div class="info-item">
              <span class="info-label">题目数量:</span>
              <span class="info-value">{{ currentExam.questionCount || '未知' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">考试说明:</span>
              <span class="info-value">{{ currentExam.description || '无' }}</span>
            </div>
          </div>

          <div class="exam-actions">
            <el-button type="primary" @click="startExam">开始考试</el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 考试进行页面 -->
    <div v-else-if="examRecord && examRecord.status === 0">
      <el-card class="exam-do-card">
        <template #header>
          <div class="card-header">
            <span>{{ currentExam.title }}</span>
            <div class="exam-timer">
              剩余时间: {{ formatTime(remainingTime) }}
            </div>
          </div>
        </template>
        <div class="exam-content">
          <div class="question-navigation">
            <div class="nav-header">题目导航</div>
            <div class="nav-items">
              <div
                v-for="(question, index) in questions"
                :key="question.questionId"
                class="nav-item"
                :class="{
                  active: currentQuestionIndex === index,
                  answered: userAnswers[question.questionId]
                }"
                @click="switchQuestion(index)"
              >
                {{ index + 1 }}
              </div>
            </div>
          </div>

          <div class="question-container">
            <div v-if="currentQuestion" class="question-content">
              <div class="question-header">
                <span class="question-number">第 {{ currentQuestionIndex + 1 }} 题</span>
                <span class="question-type">
                  {{ getQuestionTypeName(currentQuestion.type) }}
                </span>
              </div>

              <div class="question-text">{{ currentQuestion.content }}</div>

              <!-- 选择题 -->
              <div v-if="currentQuestion.type === 0" class="options-container">
                <el-radio-group v-model="userAnswers[currentQuestion.questionId]">
                  <el-radio
                    v-for="option in parseOptions(currentQuestion.options)"
                    :key="option.key"
                    :label="option.key"
                  >
                    {{ option.key }}. {{ option.value }}
                  </el-radio>
                </el-radio-group>
              </div>

              <!-- 填空题 -->
              <div v-else-if="currentQuestion.type === 1" class="fill-container">
                <el-input
                  v-model="userAnswers[currentQuestion.questionId]"
                  placeholder="请输入答案"
                />
              </div>

              <!-- 简答题 -->
              <div v-else-if="currentQuestion.type === 2" class="essay-container">
                <el-input
                  v-model="userAnswers[currentQuestion.questionId]"
                  type="textarea"
                  :rows="5"
                  placeholder="请输入答案"
                />
              </div>

              <div class="question-actions">
                <el-button
                  v-if="currentQuestionIndex > 0"
                  @click="prevQuestion"
                >
                  上一题
                </el-button>
                <el-button
                  v-if="currentQuestionIndex < questions.length - 1"
                  type="primary"
                  @click="nextQuestion"
                >
                  下一题
                </el-button>
                <el-button
                  v-else
                  type="success"
                  @click="confirmSubmit"
                >
                  提交考试
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 考试结果页面 -->
    <div v-else-if="examRecord && examRecord.status === 1">
      <el-card class="exam-result-card">
        <template #header>
          <div class="card-header">
            <span>考试结果</span>
            <el-button @click="backToList">返回列表</el-button>
          </div>
        </template>
        <div class="exam-result">
          <div class="result-header">
            <h2 class="exam-title">{{ currentExam.title }}</h2>
            <div class="score-display">
              <div class="score-circle">
                <span class="score-value">{{ examRecord.score }}</span>
              </div>
              <span class="score-label">得分</span>
            </div>
          </div>

          <div class="result-info">
            <div class="info-item">
              <span class="info-label">考试时间:</span>
              <span class="info-value">{{ formatDate(examRecord.startTime) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">用时:</span>
              <span class="info-value">{{ formatDuration(examRecord.duration) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">正确率:</span>
              <span class="info-value">{{ calculateAccuracy() }}%</span>
            </div>
          </div>

          <div class="answers-review">
            <h3>答题详情</h3>
            <div v-for="(question, index) in questions" :key="question.questionId" class="review-item">
              <div class="review-header">
                <span class="question-number">第 {{ index + 1 }} 题</span>
                <span class="question-type">{{ getQuestionTypeName(question.type) }}</span>
                <el-tag
                  :type="isAnswerCorrect(question) ? 'success' : 'danger'"
                  size="small"
                >
                  {{ isAnswerCorrect(question) ? '正确' : '错误' }}
                </el-tag>
              </div>

              <div class="question-text">{{ question.content }}</div>

              <div class="answer-comparison">
                <div class="user-answer">
                  <span class="answer-label">你的答案:</span>
                  <span class="answer-value">{{ userAnswers[question.questionId] || '未作答' }}</span>
                </div>
                <div class="correct-answer">
                  <span class="answer-label">正确答案:</span>
                  <span class="answer-value">{{ question.answer }}</span>
                </div>
              </div>

              <div v-if="question.explanation" class="answer-explanation">
                <span class="explanation-label">解析:</span>
                <span class="explanation-value">{{ question.explanation }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 历史考试页面 -->
    <div v-else-if="showHistory">
      <el-card class="exam-history-card">
        <template #header>
          <div class="card-header">
            <span>历史考试</span>
            <el-button @click="backToList">返回列表</el-button>
          </div>
        </template>
        <div v-if="loadingHistory" class="loading-container">
          <el-skeleton :rows="5" animated />
        </div>
        <div v-else-if="examHistory.length === 0" class="empty-container">
          <el-empty description="暂无历史考试记录" />
        </div>
        <div v-else class="history-list">
          <el-table :data="examHistory" style="width: 100%">
            <el-table-column prop="title" label="考试名称" min-width="200" />
            <el-table-column prop="score" label="得分" width="100" />
            <el-table-column prop="startTime" label="考试时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.startTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button type="primary" size="small" @click="viewExamResult(scope.row)">
                  查看结果
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { getUserId } from '@/utils/auth'

// 数据
const loading = ref(true)
const loadingHistory = ref(false)
const exams = ref<any[]>([])
const currentExam = ref<any>(null)
const examRecord = ref<any>(null)
const questions = ref<any[]>([])
const currentQuestionIndex = ref(0)
const currentQuestion = computed(() => questions.value[currentQuestionIndex.value] || null)
const userAnswers = reactive<Record<number, string>>({})
const remainingTime = ref(0)
const timer = ref<number | null>(null)
const showHistory = ref(false)
const examHistory = ref<any[]>([])

// 方法
const fetchExams = async () => {
  loading.value = true
  try {
    const response = await request({
      url: '/exams',
      method: 'get'
    })
    exams.value = response.data || []
  } catch (error) {
    console.error('获取考试列表失败:', error)
    ElMessage.error('获取考试列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const viewExam = (exam: any) => {
  currentExam.value = exam
}

const backToList = () => {
  currentExam.value = null
  examRecord.value = null
  showHistory.value = false
  clearInterval(timer.value as number)
}

const startExam = async () => {
  try {
    const response = await request({
      url: `/exams/${currentExam.value.examId}/start`,
      method: 'post',
      data: {
        userId: getUserId()
      }
    })

    if (response.success) {
      examRecord.value = response.data

      // 获取考试题目
      await fetchQuestions()

      // 设置考试时间
      if (currentExam.value.duration) {
        remainingTime.value = currentExam.value.duration * 60
        startTimer()
      }

      // 从本地存储加载答案
      loadAnswersFromStorage()

      ElMessage.success('考试开始')
    } else {
      ElMessage.error(response.message || '开始考试失败')
    }
  } catch (error) {
    console.error('开始考试失败:', error)
    ElMessage.error('开始考试失败，请稍后重试')
  }
}

const fetchQuestions = async () => {
  try {
    const response = await request({
      url: `/exams/${currentExam.value.examId}/questions`,
      method: 'get'
    })
    questions.value = response.data || []
    currentQuestionIndex.value = 0
  } catch (error) {
    console.error('获取考试题目失败:', error)
    ElMessage.error('获取考试题目失败，请稍后重试')
  }
}

// 使用auth工具类中的getUserId方法

const startTimer = () => {
  timer.value = setInterval(() => {
    if (remainingTime.value > 0) {
      remainingTime.value--
      saveAnswersToStorage() // 定期保存答案
    } else {
      // 时间到，自动提交
      clearInterval(timer.value as number)
      ElMessage.warning('考试时间已到，系统将自动提交')
      submitExam()
    }
  }, 1000) as unknown as number
}

const formatTime = (seconds: number) => {
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60

  return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

const switchQuestion = (index: number) => {
  currentQuestionIndex.value = index
}

const prevQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value--
  }
}

const nextQuestion = () => {
  if (currentQuestionIndex.value < questions.value.length - 1) {
    currentQuestionIndex.value++
  }
}

const parseOptions = (optionsStr: string) => {
  try {
    return JSON.parse(optionsStr || '[]')
  } catch (e) {
    console.error('解析选项出错:', e)
    return []
  }
}

const getQuestionTypeName = (type: number) => {
  switch (type) {
    case 0: return '选择题'
    case 1: return '填空题'
    case 2: return '简答题'
    default: return '未知类型'
  }
}

const confirmSubmit = () => {
  ElMessageBox.confirm(
    '确定要提交考试吗？提交后将无法修改答案。',
    '提交确认',
    {
      confirmButtonText: '确定提交',
      cancelButtonText: '继续答题',
      type: 'warning'
    }
  ).then(() => {
    submitExam()
  }).catch(() => {
    // 用户取消提交，继续答题
  })
}

const submitExam = async () => {
  try {
    // 准备提交数据
    const answers = []
    for (const questionId in userAnswers) {
      answers.push({
        questionId: parseInt(questionId),
        userAnswer: userAnswers[questionId]
      })
    }

    const response = await request({
      url: `/exams/records/${examRecord.value.recordId}/submit`,
      method: 'post',
      data: {
        answers
      }
    })

    if (response.success) {
      clearInterval(timer.value as number)
      clearAnswersFromStorage()

      // 获取考试结果
      const resultResponse = await request({
        url: `/exams/records/${examRecord.value.recordId}`,
        method: 'get'
      })
      examRecord.value = resultResponse.data

      ElMessage.success('考试提交成功')
    } else {
      ElMessage.error(response.message || '提交失败，请稍后重试')
    }
  } catch (error) {
    console.error('提交考试失败:', error)
    ElMessage.error('提交考试失败，请稍后重试')
  }
}

const viewExamHistory = async () => {
  loadingHistory.value = true
  showHistory.value = true

  try {
    const response = await request({
      url: `/exams/records/user/${getUserId()}`,
      method: 'get'
    })
    examHistory.value = response.data || []
  } catch (error) {
    console.error('获取考试历史失败:', error)
    ElMessage.error('获取考试历史失败，请稍后重试')
  } finally {
    loadingHistory.value = false
  }
}

const viewExamResult = async (record: any) => {
  try {
    // 获取考试详情
    const examResponse = await axios.get(`/api/exams/${record.examId}`)
    currentExam.value = examResponse.data.data

    // 获取考试记录详情
    const recordResponse = await axios.get(`/api/exams/records/${record.recordId}`)
    examRecord.value = recordResponse.data.data

    // 获取题目和答案
    const questionsResponse = await axios.get(`/api/exams/${record.examId}/questions`)
    questions.value = questionsResponse.data.data || []

    // 获取用户答案
    const answersResponse = await axios.get(`/api/exams/records/${record.recordId}/answers`)
    const answers = answersResponse.data.data || []

    // 设置用户答案
    userAnswers.value = {}
    answers.forEach((answer: any) => {
      userAnswers[answer.questionId] = answer.userAnswer
    })

    showHistory.value = false
  } catch (error) {
    console.error('获取考试结果失败:', error)
    ElMessage.error('获取考试结果失败，请稍后重试')
  }
}

const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString()
}

const formatDuration = (seconds: number) => {
  if (!seconds) return '0分钟'
  const minutes = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${minutes}分${secs}秒`
}

const calculateAccuracy = () => {
  if (!questions.value.length) return 0

  let correctCount = 0
  questions.value.forEach(question => {
    if (isAnswerCorrect(question)) {
      correctCount++
    }
  })

  return Math.round((correctCount / questions.value.length) * 100)
}

const isAnswerCorrect = (question: any) => {
  const userAnswer = userAnswers[question.questionId]
  if (!userAnswer) return false

  // 选择题直接比较
  if (question.type === 0) {
    return userAnswer === question.answer
  }

  // 填空题和简答题，这里简单实现为完全匹配
  // 实际应用中可能需要更复杂的匹配逻辑
  return userAnswer === question.answer
}

// 本地存储相关
const getStorageKey = () => {
  if (!examRecord.value) return null
  return `exam_answers_${examRecord.value.recordId}`
}

const saveAnswersToStorage = () => {
  const key = getStorageKey()
  if (key) {
    localStorage.setItem(key, JSON.stringify(userAnswers))
  }
}

const loadAnswersFromStorage = () => {
  const key = getStorageKey()
  if (key) {
    const savedAnswers = localStorage.getItem(key)
    if (savedAnswers) {
      try {
        const parsed = JSON.parse(savedAnswers)
        Object.keys(parsed).forEach(key => {
          userAnswers[key] = parsed[key]
        })
      } catch (e) {
        console.error('加载保存的答案失败:', e)
      }
    }
  }
}

const clearAnswersFromStorage = () => {
  const key = getStorageKey()
  if (key) {
    localStorage.removeItem(key)
  }
}

// 生命周期钩子
onMounted(() => {
  fetchExams()

  // 添加页面关闭前的提示
  window.addEventListener('beforeunload', handleBeforeUnload)
})

onBeforeUnmount(() => {
  if (timer.value) {
    clearInterval(timer.value)
  }

  window.removeEventListener('beforeunload', handleBeforeUnload)
})

// 页面关闭前保存答案并提示
const handleBeforeUnload = (e: BeforeUnloadEvent) => {
  if (examRecord.value && examRecord.value.status === 0) {
    saveAnswersToStorage()
    e.preventDefault()
    e.returnValue = '考试进行中，关闭页面可能导致答案丢失！'
    return e.returnValue
  }
}
</script>

<style scoped>
.exam-container {
  padding: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.loading-container, .empty-container {
  padding: 40px 0;
  text-align: center;
}

.exam-list-card, .exam-detail-card, .exam-do-card, .exam-result-card, .exam-history-card {
  margin-bottom: 20px;
}

/* 考试详情页面样式 */
.exam-title {
  text-align: center;
  margin-bottom: 30px;
  font-size: 24px;
}

.exam-info {
  margin-bottom: 30px;
}

.info-item {
  margin-bottom: 15px;
  display: flex;
}

.info-label {
  font-weight: bold;
  width: 100px;
}

.exam-actions {
  text-align: center;
  margin-top: 30px;
}

/* 考试进行页面样式 */
.exam-timer {
  font-size: 16px;
  font-weight: bold;
  color: #f56c6c;
}

.exam-content {
  display: flex;
  height: calc(100vh - 180px);
}

.question-navigation {
  width: 200px;
  border-right: 1px solid #ebeef5;
  padding-right: 20px;
  margin-right: 20px;
}

.nav-header {
  font-weight: bold;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.nav-items {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.nav-item {
  width: 30px;
  height: 30px;
  display: flex;
  justify-content: center;
  align-items: center;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.nav-item:hover {
  background-color: #f5f7fa;
}

.nav-item.active {
  background-color: #409eff;
  color: white;
  border-color: #409eff;
}

.nav-item.answered {
  background-color: #67c23a;
  color: white;
  border-color: #67c23a;
}

.nav-item.active.answered {
  background-color: #409eff;
  border-color: #409eff;
}

.question-container {
  flex: 1;
  overflow-y: auto;
  padding: 0 20px;
}

.question-content {
  padding: 20px;
}

.question-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.question-number {
  font-weight: bold;
  font-size: 16px;
}

.question-type {
  color: #909399;
}

.question-text {
  margin-bottom: 30px;
  line-height: 1.6;
  font-size: 16px;
}

.options-container, .fill-container, .essay-container {
  margin-bottom: 30px;
}

.options-container .el-radio {
  display: block;
  margin-bottom: 15px;
  line-height: 1.6;
}

.question-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 30px;
}

/* 考试结果页面样式 */
.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.score-display {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.score-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background-color: #67c23a;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 10px;
}

.score-value {
  font-size: 28px;
  font-weight: bold;
  color: white;
}

.score-label {
  font-size: 14px;
  color: #606266;
}

.result-info {
  margin-bottom: 30px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.answers-review {
  margin-top: 30px;
}

.answers-review h3 {
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.review-item {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px dashed #ebeef5;
}

.review-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.review-header .question-number {
  margin-right: 15px;
}

.review-header .question-type {
  margin-right: 15px;
}

.answer-comparison {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 15px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.user-answer, .correct-answer {
  display: flex;
}

.answer-label {
  font-weight: bold;
  width: 80px;
}

.answer-explanation {
  margin-top: 15px;
  padding: 15px;
  background-color: #f0f9eb;
  border-radius: 4px;
}

.explanation-label {
  font-weight: bold;
  margin-right: 10px;
}
</style>
