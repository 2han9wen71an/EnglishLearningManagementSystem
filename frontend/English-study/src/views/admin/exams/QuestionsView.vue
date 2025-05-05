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
              <el-button type="danger" icon="Delete" circle @click="removeOption(index)" v-if="questionOptions.length > 2" />
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
            v-for="(option, index) in JSON.parse(previewQuestion.options || '[]')"
            :key="index"
            class="option-item"
            :class="{ 'correct-option': isCorrectOption(option, previewQuestion) }"
          >
            <span class="option-label">{{ String.fromCharCode(65 + index) }}.</span>
            <span class="option-content">{{ option.content }}</span>
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
    case 1: return ''
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
const fetchExamInfo = () => {
  if (!examId.value) {
    ElMessage.error('考试ID不能为空')
    router.push('/admin/exams')
    return
  }

  // 这里应该调用API获取考试信息
  // 暂时使用模拟数据
  setTimeout(() => {
    Object.assign(examInfo, {
      examId: examId.value,
      title: '英语四级词汇测试',
      description: '测试你的英语四级词汇掌握程度',
      duration: 30,
      totalScore: 100,
      passScore: 60,
      gradeId: 1,
      gradeName: '四级',
      status: 1
    })

    // 获取题目列表
    fetchQuestionList()
  }, 500)
}

// 获取题目列表
const fetchQuestionList = () => {
  loading.value = true
  // 这里应该调用API获取题目列表
  // 暂时使用模拟数据
  setTimeout(() => {
    const mockQuestions = [
      { questionId: 1, examId: examId.value, questionType: 1, questionContent: 'What is the capital of France?', options: JSON.stringify([{content: 'Paris', isCorrect: true}, {content: 'London', isCorrect: false}, {content: 'Berlin', isCorrect: false}, {content: 'Rome', isCorrect: false}]), correctAnswer: 'A', score: 5, analysis: 'Paris is the capital of France.', orderNum: 1 },
      { questionId: 2, examId: examId.value, questionType: 2, questionContent: 'Which of the following are mammals?', options: JSON.stringify([{content: 'Dolphin', isCorrect: true}, {content: 'Shark', isCorrect: false}, {content: 'Whale', isCorrect: true}, {content: 'Bat', isCorrect: true}]), correctAnswer: 'A,C,D', score: 10, analysis: 'Dolphins, whales, and bats are mammals.', orderNum: 2 },
      { questionId: 3, examId: examId.value, questionType: 3, questionContent: 'The Earth is flat.', options: '', correctAnswer: 'F', score: 5, analysis: 'The Earth is approximately spherical.', orderNum: 3 },
      { questionId: 4, examId: examId.value, questionType: 4, questionContent: 'The largest planet in our solar system is _______.', options: '', correctAnswer: 'Jupiter', score: 5, analysis: 'Jupiter is the largest planet in our solar system.', orderNum: 4 },
      { questionId: 5, examId: examId.value, questionType: 5, questionContent: 'Explain the water cycle.', options: '', correctAnswer: 'The water cycle is the continuous movement of water within the Earth and atmosphere. It includes processes like evaporation, condensation, precipitation, etc.', score: 15, analysis: '', orderNum: 5 }
    ]

    total.value = mockQuestions.length
    questionList.value = mockQuestions
    loading.value = false
  }, 500)
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
      const options = JSON.parse(row.options)
      questionOptions.value = options

      if (row.questionType === 1) {
        // 单选题，找出正确选项的索引
        const correctIndex = options.findIndex((opt: any) => opt.isCorrect)
        correctOptionIndex.value = correctIndex >= 0 ? correctIndex : 0
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
  ).then(() => {
    // 这里应该调用API删除题目
    ElMessage.success('删除成功')
    fetchQuestionList()
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

    // 序列化选项
    questionForm.options = JSON.stringify(questionOptions.value)
  } else if (questionForm.questionType === 2) {
    // 多选题
    // 设置正确答案（A,B,C...）
    const correctIndices = questionOptions.value
      .map((opt, index) => opt.isCorrect ? String.fromCharCode(65 + index) : null)
      .filter(Boolean)

    questionForm.correctAnswer = correctIndices.join(',')

    // 序列化选项
    questionForm.options = JSON.stringify(questionOptions.value)
  }

  await questionFormRef.value.validate((valid, fields) => {
    if (valid) {
      // 这里应该调用API添加或更新题目
      if (dialogType.value === 'add') {
        // 添加题目
        ElMessage.success('添加成功')
      } else {
        // 更新题目
        ElMessage.success('更新成功')
      }
      dialogVisible.value = false
      fetchQuestionList()
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