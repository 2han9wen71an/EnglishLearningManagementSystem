<template>
  <div class="exam-container">
    <!-- 历史考试页面 -->
    <div v-if="showHistory">
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
            <el-table-column prop="examTitle" label="考试名称" min-width="200" />
            <el-table-column prop="score" label="得分" width="100" />
            <el-table-column prop="startTime" label="考试时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.startTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 1 ? 'warning' : 'success'">
                  {{ scope.row.status === 1 ? '待批改' : '已批改' }}
                </el-tag>
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

    <!-- 考试列表页面 -->
    <div v-else-if="!currentExam && !examRecord">
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
              <div v-if="currentQuestion.type === 0 || currentQuestion.questionType === 1" class="options-container">
                <el-radio-group v-model="userAnswers[currentQuestion.questionId]" @change="saveCurrentAnswer">
                  <el-radio
                    v-for="option in parseOptions(currentQuestion.options)"
                    :key="option.key"
                    :label="option.key"
                  >
                    {{ option.key }}. {{ option.value }}
                  </el-radio>
                </el-radio-group>
              </div>

              <!-- 多选题 -->
              <div v-else-if="currentQuestion.questionType === 2" class="options-container">
                <el-checkbox-group v-model="multiSelectAnswers[currentQuestion.questionId]" @change="handleMultiSelectChange(currentQuestion.questionId)">
                  <el-checkbox
                    v-for="option in parseOptions(currentQuestion.options)"
                    :key="option.key"
                    :label="option.key"
                  >
                    {{ option.key }}. {{ option.value }}
                  </el-checkbox>
                </el-checkbox-group>
              </div>

              <!-- 判断题 -->
              <div v-else-if="currentQuestion.questionType === 3" class="options-container">
                <el-radio-group v-model="userAnswers[currentQuestion.questionId]" @change="saveCurrentAnswer">
                  <el-radio label="T">正确</el-radio>
                  <el-radio label="F">错误</el-radio>
                </el-radio-group>
              </div>

              <!-- 填空题 -->
              <div v-else-if="currentQuestion.type === 1 || currentQuestion.questionType === 4" class="fill-container">
                <el-input
                  v-model="userAnswers[currentQuestion.questionId]"
                  placeholder="请输入答案"
                  @blur="saveCurrentAnswer"
                />
              </div>

              <!-- 简答题 -->
              <div v-else-if="currentQuestion.type === 2 || currentQuestion.questionType === 5" class="essay-container">
                <el-input
                  v-model="userAnswers[currentQuestion.questionId]"
                  type="textarea"
                  :rows="5"
                  placeholder="请输入答案"
                  @blur="saveCurrentAnswer"
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
    <div v-else-if="examRecord && (examRecord.status === 1 || examRecord.status === 2)">
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
              <span class="info-value">{{ calculateDuration(examRecord.startTime, examRecord.endTime) }}</span>
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

              <div class="question-text">{{ question.content || question.questionContent }}</div>

              <div class="answer-comparison">
                <div class="user-answer">
                  <span class="answer-label">你的答案:</span>
                  <span class="answer-value">
                    <!-- 选择题和判断题显示选项内容 -->
                    <template v-if="question.type === 0 || question.questionType === 1 || question.questionType === 2 || question.questionType === 3">
                      {{ formatAnswerDisplay(question, userAnswers[question.questionId]) }}
                    </template>
                    <!-- 填空题和简答题直接显示 -->
                    <template v-else>
                      {{ userAnswers[question.questionId] || '未作答' }}
                    </template>
                  </span>
                </div>
                <div class="correct-answer">
                  <span class="answer-label">正确答案:</span>
                  <span class="answer-value">
                    <!-- 选择题和判断题显示选项内容 -->
                    <template v-if="question.type === 0 || question.questionType === 1 || question.questionType === 2 || question.questionType === 3">
                      {{ formatAnswerDisplay(question, question.answer || question.correctAnswer) }}
                    </template>
                    <!-- 填空题和简答题直接显示 -->
                    <template v-else>
                      {{ question.answer || question.correctAnswer }}
                    </template>
                  </span>
                </div>
              </div>

              <div v-if="question.explanation || question.analysis" class="answer-explanation">
                <span class="explanation-label">解析:</span>
                <span class="explanation-value">{{ question.explanation || question.analysis }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserId } from '@/utils/auth'
import { getExamResult, getExamQuestionList, getExamList, getExamDetail, getUserExamRecords } from '@/api/exam'
import { startExam as apiStartExam, submitExam as apiSubmitExam, submitAnswer } from '@/api/exam'

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
// 多选题答案临时存储
const multiSelectAnswers = reactive<Record<number, string[]>>({})
const remainingTime = ref(0)
const timer = ref<number | null>(null)
const showHistory = ref(false)
const examHistory = ref<any[]>([])

// 方法
const fetchExams = async () => {
  loading.value = true
  try {
    const response = await getExamList()
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
    const userId = getUserId()
    if (!userId) {
      throw new Error('用户ID不存在')
    }
    const response = await apiStartExam(currentExam.value.examId, parseInt(userId))

    if (response && response.data) {
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
      ElMessage.error('开始考试失败，返回数据为空')
    }
  } catch (error) {
    console.error('开始考试失败:', error)
    ElMessage.error('开始考试失败，请稍后重试')
  }
}

const fetchQuestions = async () => {
  try {
    const response = await getExamQuestionList(currentExam.value.examId)
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

// 保存当前题目的答案
const saveCurrentAnswer = async () => {
  if (currentQuestion.value && userAnswers[currentQuestion.value.questionId] && examRecord.value) {
    try {
      await submitAnswer({
        recordId: examRecord.value.recordId,
        questionId: currentQuestion.value.questionId,
        userAnswer: userAnswers[currentQuestion.value.questionId]
      })
      console.log('保存答案成功:', currentQuestion.value.questionId, userAnswers[currentQuestion.value.questionId])
    } catch (error) {
      console.error('保存答案失败:', error)
    }
  }
}

const switchQuestion = async (index: number) => {
  // 保存当前题目的答案
  await saveCurrentAnswer()

  // 切换到新题目
  currentQuestionIndex.value = index
}

const prevQuestion = async () => {
  // 保存当前题目的答案
  await saveCurrentAnswer()

  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value--
  }
}

const nextQuestion = async () => {
  // 保存当前题目的答案
  await saveCurrentAnswer()

  if (currentQuestionIndex.value < questions.value.length - 1) {
    currentQuestionIndex.value++
  }
}

const parseOptions = (optionsStr: string): Array<{key: string, value: string}> => {
  if (!optionsStr) return []

  try {
    const options = JSON.parse(optionsStr)

    // 检查选项格式 - 后端返回的是 {A: "内容", B: "内容"} 格式
    // 需要转换为前端使用的 [{key: "A", value: "内容"}] 格式
    if (typeof options === 'object' && !Array.isArray(options)) {
      console.log('转换选项格式', options)
      const result: Array<{key: string, value: string}> = []

      // 遍历对象属性（A, B, C, D...）并按字母顺序排序
      Object.keys(options).sort().forEach(key => {
        result.push({
          key: key,
          value: options[key]
        })
      })

      return result
    } else if (Array.isArray(options)) {
      // 如果已经是数组格式，确保每个元素有key和value属性
      return options.map((opt, index) => {
        if (opt.key && opt.value) {
          return opt
        } else if (opt.content) {
          // 如果是管理端格式 {content: "内容", isCorrect: false}
          return {
            key: String.fromCharCode(65 + index), // A, B, C, D...
            value: opt.content
          }
        } else {
          return {
            key: String.fromCharCode(65 + index),
            value: opt
          }
        }
      })
    }

    return []
  } catch (e) {
    console.error('解析选项出错:', e)
    return []
  }
}

const getQuestionTypeName = (type: number) => {
  // 兼容两种类型编号系统
  switch (type) {
    case 0: return '选择题'
    case 1: return '填空题'
    case 2: return '简答题'
    case 3: return '判断题'
    // 新的类型编号
    case 1: return '单选题'
    case 2: return '多选题'
    case 3: return '判断题'
    case 4: return '填空题'
    case 5: return '简答题'
    default: return '未知类型'
  }
}

// 格式化答案显示
const formatAnswerDisplay = (question: any, answer: string) => {
  if (!answer) return '未作答'

  // 判断题特殊处理
  if (question.questionType === 3) {
    return answer === 'T' ? '正确' : '错误'
  }

  // 单选题
  if (question.type === 0 || question.questionType === 1) {
    try {
      const options = parseOptions(question.options)
      const option = options.find(opt => opt.key === answer)
      if (option) {
        return `${answer}. ${option.value}`
      }
    } catch (e) {
      console.error('格式化答案显示出错:', e)
    }
    return answer
  }

  // 多选题
  if (question.questionType === 2) {
    try {
      const options = parseOptions(question.options)
      const answerArray = answer.split(',')
      const displayParts = []

      for (const key of answerArray) {
        const option = options.find(opt => opt.key === key)
        if (option) {
          displayParts.push(`${key}. ${option.value}`)
        } else {
          displayParts.push(key)
        }
      }

      return displayParts.join('，')
    } catch (e) {
      console.error('格式化多选答案显示出错:', e)
    }
    return answer
  }

  return answer
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

    const response = await apiSubmitExam(examRecord.value.recordId, { answers })

    if (response && response.data) {
      clearInterval(timer.value as number)
      clearAnswersFromStorage()

      // 获取考试结果
      const resultResponse = await getExamResult(examRecord.value.recordId)
      examRecord.value = resultResponse.data

      ElMessage.success('考试提交成功')
    } else {
      ElMessage.error('提交失败，请稍后重试')
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
    const userId = getUserId()
    if (!userId) {
      throw new Error('用户ID不存在')
    }
    const response = await getUserExamRecords(parseInt(userId))
    console.log('历史考试数据:', response)
    examHistory.value = response.data || []

    if (examHistory.value.length === 0) {
      ElMessage.info('暂无历史考试记录')
    } else {
      console.log('历史考试列表:', examHistory.value)
    }
  } catch (error) {
    console.error('获取考试历史失败:', error)
    ElMessage.error('获取考试历史失败，请稍后重试')
  } finally {
    loadingHistory.value = false
  }
}

const viewExamResult = async (record: any) => {
  try {
    console.log('查看考试结果:', record)
    console.log('考试ID:', record.examId)
    console.log('记录ID:', record.recordId)

    // 使用API函数获取考试结果
    const resultResponse = await getExamResult(record.recordId)
    console.log('考试结果数据:', resultResponse)

    if (resultResponse && resultResponse.data) {
      // 设置考试信息
      currentExam.value = resultResponse.data.exam
      console.log('设置考试信息:', currentExam.value)

      // 设置考试记录
      examRecord.value = resultResponse.data.record
      console.log('设置考试记录:', examRecord.value)

      // 从答案中提取题目信息
      const answers = resultResponse.data.answers || []
      console.log('答案数据:', answers)

      if (answers.length > 0) {
        // 将答案转换为题目格式
        questions.value = answers.map((answer: any) => ({
          questionId: answer.questionId,
          questionContent: answer.questionContent,
          questionType: answer.questionType,
          options: answer.options,
          correctAnswer: answer.correctAnswer,
          analysis: answer.analysis,
          // 添加答案信息
          userAnswer: answer.userAnswer,
          isCorrect: answer.isCorrect,
          score: answer.score,
          answerId: answer.answerId
        }))

        console.log('从答案构建的题目:', questions.value)

        // 设置用户答案
        // 清空现有答案
        for (const key in userAnswers) {
          delete userAnswers[key]
        }

        // 设置新答案
        answers.forEach((answer: any) => {
          if (answer.questionId && answer.userAnswer !== undefined) {
            userAnswers[answer.questionId] = answer.userAnswer
          }
        })

        console.log('用户答案设置完成:', userAnswers)

        // 确保考试记录状态正确
        if (examRecord.value && examRecord.value.status !== undefined) {
          console.log('考试记录状态:', examRecord.value.status)
        } else {
          console.error('考试记录状态无效')
        }

        showHistory.value = false
      } else {
        console.error('答案数据为空')
        ElMessage.warning('考试答案数据为空，无法显示详细结果')
      }
    } else {
      console.error('获取考试结果失败: 返回数据为空')
      ElMessage.error('获取考试结果失败，返回数据为空')
    }
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

const calculateDuration = (startTime: number | string, endTime: number | string) => {
  if (!startTime || !endTime) return '未知'

  const start = typeof startTime === 'string' ? new Date(startTime).getTime() : startTime
  const end = typeof endTime === 'string' ? new Date(endTime).getTime() : endTime

  // 计算时间差（毫秒）
  const diff = end - start

  // 转换为秒
  const seconds = Math.floor(diff / 1000)

  // 使用formatDuration格式化
  return formatDuration(seconds)
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
  // 如果答案对象中有isCorrect字段，直接使用
  const answer = question.answerId ?
    { questionId: question.questionId, userAnswer: question.userAnswer, isCorrect: question.isCorrect } :
    null;

  if (answer && answer.isCorrect !== undefined) {
    return answer.isCorrect === 1;
  }

  // 如果没有isCorrect字段，则进行手动比较
  const userAnswer = userAnswers[question.questionId]
  if (!userAnswer) return false

  const correctAnswer = question.correctAnswer || question.answer;
  if (!correctAnswer) return false;

  // 选择题直接比较
  if (question.type === 0 || question.questionType === 1) { // 兼容不同的类型值
    // 处理单选题
    return userAnswer.trim().toUpperCase() === correctAnswer.trim().toUpperCase()
  } else if (question.type === 1 || question.questionType === 4) {
    // 填空题，简单实现为完全匹配（忽略大小写和前后空格）
    return userAnswer.trim().toLowerCase() === correctAnswer.trim().toLowerCase()
  } else if (question.type === 2 || question.questionType === 5) {
    // 简答题，这里简单实现为完全匹配
    // 实际应用中可能需要更复杂的匹配逻辑，如AI评分
    return userAnswer.trim() === correctAnswer.trim()
  } else if (question.questionType === 2) {
    // 多选题，需要比较所有选项
    const userAnswerArray = userAnswer.split(',').map((a: string) => a.trim().toUpperCase()).sort()
    const correctAnswerArray = correctAnswer.split(',').map((a: string) => a.trim().toUpperCase()).sort()

    if (userAnswerArray.length !== correctAnswerArray.length) return false

    for (let i = 0; i < userAnswerArray.length; i++) {
      if (userAnswerArray[i] !== correctAnswerArray[i]) return false
    }

    return true
  } else if (question.questionType === 3) {
    // 判断题
    return userAnswer.trim().toUpperCase() === correctAnswer.trim().toUpperCase()
  }

  return false
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

// 处理多选题答案变更
const handleMultiSelectChange = async (questionId: number) => {
  // 将多选答案数组转换为逗号分隔的字符串，如 "A,B,C"
  const selectedOptions = multiSelectAnswers[questionId] || []
  userAnswers[questionId] = selectedOptions.sort().join(',')
  console.log('多选题答案更新:', questionId, userAnswers[questionId])

  // 保存答案到服务器
  if (examRecord.value) {
    try {
      await submitAnswer({
        recordId: examRecord.value.recordId,
        questionId: questionId,
        userAnswer: userAnswers[questionId]
      })
      console.log('保存多选题答案成功:', questionId, userAnswers[questionId])
    } catch (error) {
      console.error('保存多选题答案失败:', error)
    }
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
          const questionId = parseInt(key)
          userAnswers[questionId] = parsed[key]

          // 如果是多选题，还需要解析为数组
          const question = questions.value.find(q => q.questionId === questionId)
          if (question && (question.questionType === 2)) {
            multiSelectAnswers[questionId] = userAnswers[questionId].split(',')
          }
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
