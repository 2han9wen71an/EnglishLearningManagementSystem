<template>
  <div class="admin-questions">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <h2>题目管理</h2>
            <el-tag type="info" class="exam-tag">{{ examInfo.title }}</el-tag>
          </div>
          <div class="header-right">
            <el-button type="primary" @click="handleAddQuestion">
              <el-icon><Plus /></el-icon>添加题目
            </el-button>
            <el-button @click="goBack">
              <el-icon><Back /></el-icon>返回考试列表
            </el-button>
          </div>
        </div>
      </template>

      <!-- 表格区域 -->
      <el-table
        :data="questionList"
        border
        stripe
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="questionId" label="ID" width="80" />
        <el-table-column prop="orderNum" label="序号" width="80" />
        <el-table-column label="题目类型" width="120">
          <template #default="scope">
            <el-tag :type="getQuestionTypeTagType(scope.row.questionType)">
              {{ getQuestionTypeName(scope.row.questionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="questionContent" label="题目内容" show-overflow-tooltip />
        <el-table-column prop="score" label="分值" width="80" />
        <el-table-column label="操作" width="250">
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              @click="handleEditQuestion(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              size="small"
              type="success"
              @click="handlePreviewQuestion(scope.row)"
            >
              预览
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDeleteQuestion(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 题目表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '添加题目' : '编辑题目'"
      width="800px"
    >
      <el-form
        ref="questionFormRef"
        :model="questionForm"
        :rules="questionFormRules"
        label-width="100px"
      >
        <el-form-item label="题目类型" prop="questionType">
          <el-select v-model="questionForm.questionType" placeholder="请选择题目类型" @change="handleQuestionTypeChange">
            <el-option
              v-for="item in questionTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="题目内容" prop="questionContent">
          <el-input v-model="questionForm.questionContent" type="textarea" :rows="3" placeholder="请输入题目内容" />
        </el-form-item>

        <!-- 选择题选项 -->
        <template v-if="questionForm.questionType === 1 || questionForm.questionType === 2">
          <el-form-item
            v-for="(option, index) in questionOptions"
            :key="index"
            :label="`选项${String.fromCharCode(65 + index)}`"
            :prop="'options.' + index + '.content'"
            :rules="{ required: true, message: '请输入选项内容', trigger: 'blur' }"
          >
            <div class="option-row">
              <el-input v-model="option.content" placeholder="请输入选项内容" />
              <el-checkbox v-model="option.isCorrect" v-if="questionForm.questionType === 2">正确</el-checkbox>
              <el-radio v-model="correctOptionIndex" :label="index" v-if="questionForm.questionType === 1">正确</el-radio>
              <el-button type="danger" circle @click="removeOption(index)" v-if="questionOptions.length > 2">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="addOption" :disabled="questionOptions.length >= 6">添加选项</el-button>
          </el-form-item>
        </template>

        <!-- 判断题 -->
        <el-form-item label="正确答案" prop="correctAnswer" v-if="questionForm.questionType === 3">
          <el-radio-group v-model="questionForm.correctAnswer">
            <el-radio label="T">正确</el-radio>
            <el-radio label="F">错误</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 填空题和简答题 -->
        <el-form-item label="正确答案" prop="correctAnswer" v-if="questionForm.questionType === 4 || questionForm.questionType === 5">
          <el-input v-model="questionForm.correctAnswer" :type="questionForm.questionType === 5 ? 'textarea' : 'text'" :rows="3" placeholder="请输入正确答案" />
        </el-form-item>

        <el-form-item label="分值" prop="score">
          <el-input-number v-model="questionForm.score" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="解析" prop="analysis">
          <el-input v-model="questionForm.analysis" type="textarea" :rows="3" placeholder="请输入题目解析" />
        </el-form-item>
        <el-form-item label="序号" prop="orderNum">
          <el-input-number v-model="questionForm.orderNum" :min="1" :max="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitQuestionForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 预览对话框 -->
    <el-dialog
      v-model="previewVisible"
      title="题目预览"
      width="800px"
    >
      <div class="preview-question">
        <div class="question-type">
          <el-tag :type="getQuestionTypeTagType(previewQuestion.questionType)">
            {{ getQuestionTypeName(previewQuestion.questionType) }}
          </el-tag>
          <span class="question-score">（{{ previewQuestion.score }}分）</span>
        </div>
        <div class="question-content">{{ previewQuestion.questionContent }}</div>

        <!-- 选择题选项 -->
        <div class="question-options" v-if="previewQuestion.questionType === 1 || previewQuestion.questionType === 2">
          <div
            v-for="(optionKey, index) in Object.keys(parseOptions(previewQuestion.options)).sort()"
            :key="index"
            class="option-item"
            :class="{ 'correct-option': isCorrectOptionKey(optionKey, previewQuestion) }"
          >
            <span class="option-label">{{ optionKey }}.</span>
            <span class="option-content">{{ parseOptions(previewQuestion.options)[optionKey] }}</span>
          </div>
        </div>

        <!-- 判断题 -->
        <div class="question-answer" v-if="previewQuestion.questionType === 3">
          <div class="answer-label">正确答案：</div>
          <div class="answer-content">{{ previewQuestion.correctAnswer === 'T' ? '正确' : '错误' }}</div>
        </div>

        <!-- 填空题和简答题 -->
        <div class="question-answer" v-if="previewQuestion.questionType === 4 || previewQuestion.questionType === 5">
          <div class="answer-label">正确答案：</div>
          <div class="answer-content">{{ previewQuestion.correctAnswer }}</div>
        </div>

        <div class="question-analysis" v-if="previewQuestion.analysis">
          <div class="analysis-label">解析：</div>
          <div class="analysis-content">{{ previewQuestion.analysis }}</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Back, Delete } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { getExamDetail, getExamQuestionList, addExamQuestion, updateExamQuestion, deleteExamQuestion } from '@/api/exam'

const route = useRoute()
const router = useRouter()
const examId = ref(Number(route.params.examId) || 0)

// 考试信息
const examInfo = reactive({
  examId: 0,
  title: '',
  description: '',
  duration: 0,
  totalScore: 0,
  passScore: 0,
  gradeId: 0,
  gradeName: '',
  status: 0
})

// 题目列表数据
const questionList = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 题目类型选项
const questionTypeOptions = [
  { value: 1, label: '单选题' },
  { value: 2, label: '多选题' },
  { value: 3, label: '判断题' },
  { value: 4, label: '填空题' },
  { value: 5, label: '简答题' }
]

// 获取题目类型名称
const getQuestionTypeName = (typeId: number) => {
  const type = questionTypeOptions.find(item => item.value === typeId)
  return type ? type.label : '未知'
}

// 获取题目类型标签类型
const getQuestionTypeTagType = (typeId: number) => {
  switch (typeId) {
    case 1: return 'primary' // 将空字符串改为 'primary'
    case 2: return 'success'
    case 3: return 'info'
    case 4: return 'warning'
    case 5: return 'danger'
    default: return 'info'
  }
}

// 对话框相关
const dialogVisible = ref(false)
const dialogType = ref('add') // 'add' 或 'edit'
const questionFormRef = ref<FormInstance>()
const questionForm = reactive({
  questionId: 0,
  examId: examId.value,
  questionType: 1,
  questionContent: '',
  options: '',
  correctAnswer: '',
  score: 5,
  analysis: '',
  orderNum: 1
})

// 选择题选项
const questionOptions = ref<Array<{content: string, isCorrect: boolean}>>([
  { content: '', isCorrect: false },
  { content: '', isCorrect: false }
])
const correctOptionIndex = ref(0)

// 预览相关
const previewVisible = ref(false)
const previewQuestion = reactive({
  questionId: 0,
  examId: 0,
  questionType: 1,
  questionContent: '',
  options: '',
  correctAnswer: '',
  score: 0,
  analysis: '',
  orderNum: 1
})

// 表单验证规则
const questionFormRules = reactive<FormRules>({
  questionType: [
    { required: true, message: '请选择题目类型', trigger: 'change' }
  ],
  questionContent: [
    { required: true, message: '请输入题目内容', trigger: 'blur' }
  ],
  correctAnswer: [
    { required: true, message: '请输入正确答案', trigger: 'blur' }
  ],
  score: [
    { required: true, message: '请输入分值', trigger: 'blur' },
    { type: 'number', min: 1, max: 100, message: '分值在 1 到 100 之间', trigger: 'blur' }
  ],
  orderNum: [
    { required: true, message: '请输入序号', trigger: 'blur' },
    { type: 'number', min: 1, max: 100, message: '序号在 1 到 100 之间', trigger: 'blur' }
  ]
})

// 获取考试信息
const fetchExamInfo = async () => {
  if (!examId.value) {
    ElMessage.error('考试ID不能为空')
    router.push('/admin/exams')
    return
  }

  try {
    const result = await getExamDetail(examId.value)
    if (result.success) {
      const exam = result.data
      Object.assign(examInfo, {
        examId: exam.examId,
        title: exam.title,
        description: exam.description,
        duration: exam.duration,
        totalScore: exam.totalScore,
        passScore: exam.passScore,
        gradeId: exam.gradeId,
        gradeName: getGradeName(exam.gradeId),
        status: exam.status
      })

      // 获取题目列表
      fetchQuestionList()
    } else {
      ElMessage.error(result.message || '获取考试信息失败')
    }
  } catch (error) {
    console.error('获取考试信息失败:', error)
    ElMessage.error('获取考试信息失败，请稍后重试')
  }
}

// 根据等级ID获取等级名称
const getGradeName = (gradeId: number) => {
  const gradeMap: Record<number, string> = {
    1: '四级',
    2: '六级',
    3: '托福',
    4: '雅思'
  }
  return gradeMap[gradeId] || '未知'
}

// 获取题目列表
const fetchQuestionList = async () => {
  loading.value = true
  try {
    const result = await getExamQuestionList(examId.value)
    if (result.success) {
      questionList.value = result.data || []

      // 处理选项格式，确保是JSON字符串
      questionList.value.forEach(question => {
        if (question.options && typeof question.options === 'object') {
          question.options = JSON.stringify(question.options)
        }
      })

      total.value = questionList.value.length
    } else {
      ElMessage.error(result.message || '获取题目列表失败')
      questionList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取题目列表失败:', error)
    ElMessage.error('获取题目列表失败，请稍后重试')
    questionList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 分页相关
const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchQuestionList()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchQuestionList()
}

// 返回考试列表
const goBack = () => {
  router.push('/admin/exams')
}

// 添加题目
const handleAddQuestion = () => {
  dialogType.value = 'add'
  questionForm.questionId = 0
  questionForm.examId = examId.value
  questionForm.questionType = 1
  questionForm.questionContent = ''
  questionForm.options = ''
  questionForm.correctAnswer = ''
  questionForm.score = 5
  questionForm.analysis = ''
  questionForm.orderNum = questionList.value.length + 1

  // 重置选项
  questionOptions.value = [
    { content: '', isCorrect: false },
    { content: '', isCorrect: false }
  ]
  correctOptionIndex.value = 0

  dialogVisible.value = true
}

// 编辑题目
const handleEditQuestion = (row: any) => {
  dialogType.value = 'edit'
  questionForm.questionId = row.questionId
  questionForm.examId = row.examId
  questionForm.questionType = row.questionType
  questionForm.questionContent = row.questionContent
  questionForm.options = row.options
  questionForm.correctAnswer = row.correctAnswer
  questionForm.score = row.score
  questionForm.analysis = row.analysis
  questionForm.orderNum = row.orderNum

  // 处理选项
  if (row.questionType === 1 || row.questionType === 2) {
    try {
      // 确保options是字符串格式
      const optionsStr = typeof row.options === 'string' ? row.options : JSON.stringify(row.options)
      const optionsObj = JSON.parse(optionsStr)

      // 检查选项格式 - 后端返回的是 {A: "内容", B: "内容"} 格式
      // 需要转换为前端使用的 [{content: "内容", isCorrect: false}] 格式
      if (!Array.isArray(optionsObj)) {
        console.log('转换选项格式', optionsObj)
        // 清空现有选项
        questionOptions.value = []

        // 遍历对象属性（A, B, C, D...）
        Object.keys(optionsObj).forEach(key => {
          questionOptions.value.push({
            content: optionsObj[key],
            isCorrect: false
          })
        })
      } else {
        // 如果已经是数组格式，直接使用
        questionOptions.value = optionsObj
      }

      // 确保至少有两个选项
      if (questionOptions.value.length < 2) {
        while (questionOptions.value.length < 2) {
          questionOptions.value.push({ content: '', isCorrect: false })
        }
      }

      if (row.questionType === 1) {
        // 单选题，找出正确选项的索引
        if (row.correctAnswer) {
          const correctLetter = row.correctAnswer.trim().charAt(0)
          // 将字母转换为索引 (A->0, B->1, ...)
          const letterIndex = correctLetter.charCodeAt(0) - 65 // 'A'的ASCII码是65

          if (letterIndex >= 0 && letterIndex < questionOptions.value.length) {
            correctOptionIndex.value = letterIndex
            console.log('设置单选题正确选项索引为:', correctOptionIndex.value, '(', correctLetter, ')')
          } else {
            correctOptionIndex.value = 0
            console.warn('无法确定正确选项索引，使用默认值0')
          }
        } else {
          correctOptionIndex.value = 0
        }
      } else if (row.questionType === 2) {
        // 多选题，标记所有正确选项
        // 先重置所有选项为未选中
        questionOptions.value.forEach(opt => opt.isCorrect = false)

        // 然后根据correctAnswer标记正确选项
        if (row.correctAnswer) {
          const correctLetters = row.correctAnswer.split(',')
          correctLetters.forEach(letter => {
            const letterIndex = letter.trim().charCodeAt(0) - 65 // 'A'的ASCII码是65
            if (letterIndex >= 0 && letterIndex < questionOptions.value.length) {
              questionOptions.value[letterIndex].isCorrect = true
              console.log('设置多选题正确选项:', letter.trim())
            }
          })
        }
      }
    } catch (e) {
      console.error('解析选项失败', e)
      questionOptions.value = [
        { content: '', isCorrect: false },
        { content: '', isCorrect: false }
      ]
    }
  }

  dialogVisible.value = true
}

// 预览题目
const handlePreviewQuestion = (row: any) => {
  Object.assign(previewQuestion, row)
  previewVisible.value = true
}

// 删除题目
const handleDeleteQuestion = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除题目 ${row.questionContent.substring(0, 20)}... 吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      // 调用API删除题目
      const result = await deleteExamQuestion(examId.value, row.questionId)
      if (result.success) {
        ElMessage.success('删除成功')
        fetchQuestionList() // 重新获取题目列表
      } else {
        ElMessage.error(result.message || '删除失败')
      }
    } catch (error) {
      console.error('删除题目失败:', error)
      ElMessage.error('删除失败，请稍后重试')
    }
  }).catch(() => {
    // 取消删除
  })
}

// 题目类型变更处理
const handleQuestionTypeChange = (val: number) => {
  // 重置选项和答案
  if (val === 1 || val === 2) {
    // 单选题或多选题
    questionOptions.value = [
      { content: '', isCorrect: false },
      { content: '', isCorrect: false }
    ]
    correctOptionIndex.value = 0
    questionForm.correctAnswer = ''
  } else if (val === 3) {
    // 判断题
    questionForm.correctAnswer = 'T'
  } else {
    // 填空题或简答题
    questionForm.correctAnswer = ''
  }
}

// 添加选项
const addOption = () => {
  if (questionOptions.value.length < 6) {
    questionOptions.value.push({ content: '', isCorrect: false })
  }
}

// 移除选项
const removeOption = (index: number) => {
  if (questionOptions.value.length > 2) {
    questionOptions.value.splice(index, 1)

    // 如果删除的是正确选项，重置正确选项索引
    if (questionForm.questionType === 1 && correctOptionIndex.value === index) {
      correctOptionIndex.value = 0
    }
  }
}

// 解析选项字符串为对象
const parseOptions = (optionsStr: string): Record<string, string> => {
  if (!optionsStr) return {}

  try {
    const options = JSON.parse(optionsStr)
    // 如果已经是对象格式，直接返回
    if (typeof options === 'object' && !Array.isArray(options)) {
      return options
    }

    // 如果是数组格式，转换为对象格式
    if (Array.isArray(options)) {
      const result: Record<string, string> = {}
      options.forEach((opt, index) => {
        const key = String.fromCharCode(65 + index) // A, B, C, D...
        result[key] = opt.content
      })
      return result
    }

    return {}
  } catch (e) {
    console.error('解析选项失败', e)
    return {}
  }
}

// 检查选项是否为正确选项
const isCorrectOption = (option: any, question: any) => {
  if (question.questionType === 1) {
    // 单选题
    return option.isCorrect
  } else if (question.questionType === 2) {
    // 多选题
    return option.isCorrect
  }
  return false
}

// 检查选项键是否为正确选项
const isCorrectOptionKey = (optionKey: string, question: any) => {
  if (!question.correctAnswer) return false

  if (question.questionType === 1) {
    // 单选题
    return optionKey === question.correctAnswer
  } else if (question.questionType === 2) {
    // 多选题
    const correctOptions = question.correctAnswer.split(',')
    return correctOptions.includes(optionKey)
  }
  return false
}

// 提交题目表单
const submitQuestionForm = async () => {
  if (!questionFormRef.value) return

  // 处理选项和正确答案
  if (questionForm.questionType === 1) {
    // 单选题
    // 设置正确选项
    questionOptions.value.forEach((opt, index) => {
      opt.isCorrect = index === correctOptionIndex.value
    })

    // 设置正确答案（A, B, C, D...）
    questionForm.correctAnswer = String.fromCharCode(65 + correctOptionIndex.value)

    // 将选项转换为后端期望的格式 {A: "内容", B: "内容", ...}
    const optionsObj: Record<string, string> = {}
    questionOptions.value.forEach((opt, index) => {
      const key = String.fromCharCode(65 + index) // A, B, C, D...
      optionsObj[key] = opt.content
    })

    // 序列化选项
    questionForm.options = JSON.stringify(optionsObj)
    console.log('提交单选题选项:', questionForm.options)
    console.log('提交单选题正确答案:', questionForm.correctAnswer)
  } else if (questionForm.questionType === 2) {
    // 多选题
    // 设置正确答案（A,B,C...）
    const correctIndices = questionOptions.value
      .map((opt, index) => opt.isCorrect ? String.fromCharCode(65 + index) : null)
      .filter(Boolean)

    questionForm.correctAnswer = correctIndices.join(',')

    // 将选项转换为后端期望的格式 {A: "内容", B: "内容", ...}
    const optionsObj: Record<string, string> = {}
    questionOptions.value.forEach((opt, index) => {
      const key = String.fromCharCode(65 + index) // A, B, C, D...
      optionsObj[key] = opt.content
    })

    // 序列化选项
    questionForm.options = JSON.stringify(optionsObj)
    console.log('提交多选题选项:', questionForm.options)
    console.log('提交多选题正确答案:', questionForm.correctAnswer)
  }

  await questionFormRef.value.validate(async (valid, fields) => {
    if (valid) {
      try {
        // 准备提交的数据
        const submitData = {
          questionId: questionForm.questionId,
          examId: questionForm.examId,
          questionType: questionForm.questionType,
          questionContent: questionForm.questionContent,
          options: questionForm.options,
          correctAnswer: questionForm.correctAnswer,
          score: questionForm.score,
          analysis: questionForm.analysis,
          orderNum: questionForm.orderNum
        }

        let result
        if (dialogType.value === 'add') {
          // 添加题目
          result = await addExamQuestion(examId.value, submitData)
          if (result.success) {
            ElMessage.success('添加成功')
            dialogVisible.value = false
            fetchQuestionList() // 重新获取题目列表
          } else {
            ElMessage.error(result.message || '添加失败')
          }
        } else {
          // 更新题目
          result = await updateExamQuestion(examId.value, questionForm.questionId, submitData)
          if (result.success) {
            ElMessage.success('更新成功')
            dialogVisible.value = false
            fetchQuestionList() // 重新获取题目列表
          } else {
            ElMessage.error(result.message || '更新失败')
          }
        }
      } catch (error) {
        console.error('提交题目表单失败:', error)
        ElMessage.error('操作失败，请稍后重试')
      }
    } else {
      console.log('表单验证失败', fields)
    }
  })
}

// 初始化
onMounted(() => {
  fetchExamInfo()
})
</script>

<style scoped>
.admin-questions {
  padding: 20px 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-left h2 {
  margin: 0;
  font-weight: 600;
  margin-right: 10px;
}

.exam-tag {
  margin-left: 10px;
}

.header-right {
  display: flex;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.option-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.option-row .el-input {
  flex: 1;
}

.preview-question {
  padding: 20px;
}

.question-type {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.question-score {
  margin-left: 10px;
  color: #909399;
}

.question-content {
  font-size: 16px;
  margin-bottom: 20px;
  line-height: 1.6;
}

.question-options {
  margin-bottom: 20px;
}

.option-item {
  margin-bottom: 10px;
  padding: 5px 10px;
  border-radius: 4px;
}

.correct-option {
  background-color: rgba(103, 194, 58, 0.1);
  color: #67c23a;
}

.option-label {
  font-weight: bold;
  margin-right: 10px;
}

.question-answer {
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f8f8f8;
  border-radius: 4px;
}

.answer-label, .analysis-label {
  font-weight: bold;
  margin-bottom: 5px;
}

.question-analysis {
  margin-top: 20px;
  padding: 10px;
  background-color: #f8f8f8;
  border-radius: 4px;
  color: #606266;
}
</style>