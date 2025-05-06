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
            <el-table-column label="得分" width="120">
              <template #default="scope">
                <div v-if="scope.row.questionType === 5">
                  <el-input-number
                    v-model="scope.row.score"
                    :min="0"
                    :max="20"
                    size="small"
                    @change="handleScoreChange(scope.row)"
                  />
                </div>
                <span v-else :class="{ 'correct-score': scope.row.isCorrect, 'wrong-score': !scope.row.isCorrect }">
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
import { getScoreList, getScoreDetail, updateEssayScore } from '@/api/score'
import { getExamList } from '@/api/exam'

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
const fetchExamOptions = async () => {
  try {
    const res = await getExamList()
    if (res.success) {
      examOptions.value = res.data.map((exam: any) => ({
        value: exam.examId,
        label: exam.title
      }))
    } else {
      ElMessage.error('获取考试选项失败')
    }
  } catch (error) {
    console.error('获取考试选项出错:', error)
    ElMessage.error('获取考试选项出错')
  }
}

// 获取成绩列表
const fetchScoreList = async () => {
  loading.value = true
  try {
    const params: any = {
      page: currentPage.value,
      size: pageSize.value
    }

    // 添加筛选条件
    if (searchQuery.value) {
      params.query = searchQuery.value
    }

    if (examFilter.value !== '') {
      params.examId = examFilter.value
    }

    if (statusFilter.value !== '') {
      params.status = statusFilter.value
    }

    const res = await getScoreList(params)
    if (res.success) {
      scoreList.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error('获取成绩列表失败')
    }
  } catch (error) {
    console.error('获取成绩列表出错:', error)
    ElMessage.error('获取成绩列表出错')
  } finally {
    loading.value = false
  }
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
const handleViewDetail = async (row: any) => {
  Object.assign(detailScore, row)

  try {
    const res = await getScoreDetail(row.scoreId)
    if (res.success) {
      // 更新详情数据
      Object.assign(detailScore, res.data.score)
      detailAnswers.value = res.data.answers
      dialogVisible.value = true
    } else {
      ElMessage.error('获取成绩详情失败')
    }
  } catch (error) {
    console.error('获取成绩详情出错:', error)
    ElMessage.error('获取成绩详情出错')
  }
}

// 处理分数变更
const handleScoreChange = async (row: any) => {
  try {
    const data = {
      score: row.score
    }

    const res = await updateEssayScore(detailScore.scoreId, row.questionId, data)
    if (res.success) {
      ElMessage.success('评分更新成功')
      // 更新总分
      const totalScore = detailAnswers.value.reduce((sum, item) => sum + item.score, 0)
      detailScore.score = totalScore
    } else {
      ElMessage.error('评分更新失败')
    }
  } catch (error) {
    console.error('更新评分出错:', error)
    ElMessage.error('更新评分出错')
  }
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
