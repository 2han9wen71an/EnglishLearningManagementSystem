<template>
  <div class="admin-scores">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>成绩管理</h2>
        </div>
      </template>

      <!-- 搜索和筛选区域 -->
      <div class="search-container">
        <el-input
          v-model="searchQuery"
          placeholder="搜索用户名/考试名称"
          class="search-input"
          clearable
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <el-select v-model="examFilter" placeholder="考试筛选" clearable @change="handleSearch">
          <el-option
            v-for="item in examOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        
        <el-select v-model="statusFilter" placeholder="状态筛选" clearable @change="handleSearch">
          <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>

      <!-- 表格区域 -->
      <el-table
        :data="scoreList"
        border
        stripe
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="scoreId" label="ID" width="80" />
        <el-table-column prop="userName" label="用户名" />
        <el-table-column prop="examTitle" label="考试名称" />
        <el-table-column prop="score" label="得分" width="80" />
        <el-table-column prop="totalScore" label="总分" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusName(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button 
              size="small" 
              type="primary" 
              @click="handleViewDetail(scope.row)"
            >
              查看详情
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

    <!-- 成绩详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="成绩详情"
      width="800px"
    >
      <div class="score-detail">
        <div class="detail-header">
          <h3>{{ detailScore.examTitle }}</h3>
          <div class="detail-info">
            <p><span class="label">用户名：</span>{{ detailScore.userName }}</p>
            <p><span class="label">得分：</span>{{ detailScore.score }} / {{ detailScore.totalScore }}</p>
            <p><span class="label">状态：</span>
              <el-tag :type="getStatusTagType(detailScore.status)">
                {{ getStatusName(detailScore.status) }}
              </el-tag>
            </p>
            <p><span class="label">提交时间：</span>{{ detailScore.submitTime }}</p>
          </div>
        </div>
        
        <div class="detail-answers">
          <h4>答题详情</h4>
          <el-table
            :data="detailAnswers"
            border
            stripe
            style="width: 100%"
          >
            <el-table-column prop="orderNum" label="序号" width="80" />
            <el-table-column label="题目类型" width="100">
              <template #default="scope">
                <el-tag :type="getQuestionTypeTagType(scope.row.questionType)">
                  {{ getQuestionTypeName(scope.row.questionType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="questionContent" label="题目内容" show-overflow-tooltip />
            <el-table-column prop="userAnswer" label="用户答案" width="120" />
            <el-table-column prop="correctAnswer" label="正确答案" width="120" />
            <el-table-column label="得分" width="80">
              <template #default="scope">
                <span :class="{ 'correct-score': scope.row.isCorrect, 'wrong-score': !scope.row.isCorrect }">
                  {{ scope.row.score }}
                </span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

// 成绩列表数据
const scoreList = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchQuery = ref('')
const examFilter = ref('')
const statusFilter = ref('')

// 考试选项
const examOptions = ref([])

// 状态选项
const statusOptions = [
  { value: 0, label: '未通过' },
  { value: 1, label: '通过' },
  { value: 2, label: '待批改' }
]

// 题目类型选项
const questionTypeOptions = [
  { value: 1, label: '单选题' },
  { value: 2, label: '多选题' },
  { value: 3, label: '判断题' },
  { value: 4, label: '填空题' },
  { value: 5, label: '简答题' }
]

// 获取状态名称
const getStatusName = (statusId: number) => {
  const status = statusOptions.find(item => item.value === statusId)
  return status ? status.label : '未知'
}

// 获取状态标签类型
const getStatusTagType = (statusId: number) => {
  switch (statusId) {
    case 0: return 'danger'
    case 1: return 'success'
    case 2: return 'info'
    default: return 'info'
  }
}

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
const detailScore = reactive({
  scoreId: 0,
  userId: 0,
  userName: '',
  examId: 0,
  examTitle: '',
  score: 0,
  totalScore: 0,
  status: 0,
  submitTime: ''
})
const detailAnswers = ref([])

// 获取考试选项
const fetchExamOptions = () => {
  // 这里应该调用API获取考试选项
  // 暂时使用模拟数据
  setTimeout(() => {
    examOptions.value = [
      { value: 1, label: '英语四级词汇测试' },
      { value: 2, label: '英语六级词汇测试' },
      { value: 3, label: '雅思词汇测试' }
    ]
  }, 500)
}

// 获取成绩列表
const fetchScoreList = () => {
  loading.value = true
  // 这里应该调用API获取成绩列表
  // 暂时使用模拟数据
  setTimeout(() => {
    const mockScores = [
      { scoreId: 1, userId: 2, userName: 'user1', examId: 1, examTitle: '英语四级词汇测试', score: 85, totalScore: 100, status: 1, submitTime: '2023-05-01 10:30:00' },
      { scoreId: 2, userId: 3, userName: 'user2', examId: 1, examTitle: '英语四级词汇测试', score: 55, totalScore: 100, status: 0, submitTime: '2023-05-01 11:20:00' },
      { scoreId: 3, userId: 4, userName: 'user3', examId: 2, examTitle: '英语六级词汇测试', score: 90, totalScore: 100, status: 1, submitTime: '2023-05-02 09:15:00' },
      { scoreId: 4, userId: 2, userName: 'user1', examId: 3, examTitle: '雅思词汇测试', score: 0, totalScore: 150, status: 2, submitTime: '2023-05-03 14:45:00' }
    ]
    
    // 根据搜索条件过滤
    let filteredScores = [...mockScores]
    if (searchQuery.value) {
      const query = searchQuery.value.toLowerCase()
      filteredScores = filteredScores.filter(score => 
        score.userName.toLowerCase().includes(query) || 
        score.examTitle.toLowerCase().includes(query)
      )
    }
    
    if (examFilter.value !== '') {
      filteredScores = filteredScores.filter(score => score.examId === examFilter.value)
    }
    
    if (statusFilter.value !== '') {
      filteredScores = filteredScores.filter(score => score.status === statusFilter.value)
    }
    
    total.value = filteredScores.length
    scoreList.value = filteredScores
    loading.value = false
  }, 500)
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchScoreList()
}

// 分页相关
const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchScoreList()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchScoreList()
}

// 查看详情
const handleViewDetail = (row: any) => {
  Object.assign(detailScore, row)
  
  // 获取答题详情
  // 这里应该调用API获取答题详情
  // 暂时使用模拟数据
  const mockAnswers = [
    { questionId: 1, orderNum: 1, questionType: 1, questionContent: 'What is the capital of France?', userAnswer: 'A', correctAnswer: 'A', score: 5, isCorrect: true },
    { questionId: 2, orderNum: 2, questionType: 2, questionContent: 'Which of the following are mammals?', userAnswer: 'A,C', correctAnswer: 'A,C,D', score: 5, isCorrect: false },
    { questionId: 3, orderNum: 3, questionType: 3, questionContent: 'The Earth is flat.', userAnswer: 'F', correctAnswer: 'F', score: 5, isCorrect: true },
    { questionId: 4, orderNum: 4, questionType: 4, questionContent: 'The largest planet in our solar system is _______.', userAnswer: 'Jupiter', correctAnswer: 'Jupiter', score: 5, isCorrect: true },
    { questionId: 5, orderNum: 5, questionType: 5, questionContent: 'Explain the water cycle.', userAnswer: 'The water cycle is the process where water moves around the Earth.', correctAnswer: 'The water cycle is the continuous movement of water within the Earth and atmosphere. It includes processes like evaporation, condensation, precipitation, etc.', score: 10, isCorrect: false }
  ]
  
  detailAnswers.value = mockAnswers
  dialogVisible.value = true
}

// 初始化
onMounted(() => {
  fetchExamOptions()
  fetchScoreList()
})
</script>

<style scoped>
.admin-scores {
  padding: 20px 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-weight: 600;
}

.search-container {
  display: flex;
  margin-bottom: 20px;
  gap: 10px;
}

.search-input {
  width: 300px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.score-detail {
  padding: 0 20px;
}

.detail-header {
  margin-bottom: 20px;
}

.detail-header h3 {
  margin: 0 0 10px 0;
  font-size: 18px;
}

.detail-info {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.detail-info p {
  margin: 0;
}

.label {
  font-weight: bold;
  margin-right: 5px;
}

.detail-answers h4 {
  margin: 20px 0 10px 0;
  font-size: 16px;
}

.correct-score {
  color: #67c23a;
  font-weight: bold;
}

.wrong-score {
  color: #f56c6c;
  font-weight: bold;
}
</style>
