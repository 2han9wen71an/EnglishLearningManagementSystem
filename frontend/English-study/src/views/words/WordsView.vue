<template>
  <div class="words-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="filter-card">
          <template #header>
            <div class="card-header">
              <span>单词筛选</span>
            </div>
          </template>
          <div class="filter-content">
            <el-form :model="filterForm" label-position="top">
              <el-form-item label="难度级别">
                <el-select v-model="filterForm.level" placeholder="选择难度级别" style="width: 100%;">
                  <el-option label="全部" value=""></el-option>
                  <el-option label="初级" value="1"></el-option>
                  <el-option label="中级" value="2"></el-option>
                  <el-option label="高级" value="3"></el-option>
                </el-select>
              </el-form-item>

              <el-form-item label="单词分类">
                <el-select v-model="filterForm.category" placeholder="选择单词分类" style="width: 100%;">
                  <el-option label="全部" value=""></el-option>
                  <el-option label="CET-4" value="cet4"></el-option>
                  <el-option label="CET-6" value="cet6"></el-option>
                  <el-option label="IELTS" value="ielts"></el-option>
                  <el-option label="TOEFL" value="toefl"></el-option>
                </el-select>
              </el-form-item>

              <el-form-item label="学习状态">
                <el-select v-model="filterForm.status" placeholder="选择学习状态" style="width: 100%;">
                  <el-option label="全部" value=""></el-option>
                  <el-option label="未学习" value="0"></el-option>
                  <el-option label="已学习" value="1"></el-option>
                  <el-option label="已掌握" value="2"></el-option>
                </el-select>
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="applyFilter" style="width: 100%;">应用筛选</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-card>

        <el-card class="stats-card" style="margin-top: 20px;">
          <template #header>
            <div class="card-header">
              <span>学习统计</span>
            </div>
          </template>
          <div class="stats-content">
            <el-row>
              <el-col :span="12">
                <div class="stat-item">
                  <div class="stat-value">{{ stats.totalWords }}</div>
                  <div class="stat-label">总单词数</div>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="stat-item">
                  <div class="stat-value">{{ stats.learnedWords }}</div>
                  <div class="stat-label">已学单词</div>
                </div>
              </el-col>
            </el-row>
            <el-row style="margin-top: 20px;">
              <el-col :span="12">
                <div class="stat-item">
                  <div class="stat-value">{{ stats.masteredWords }}</div>
                  <div class="stat-label">已掌握</div>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="stat-item">
                  <div class="stat-value">{{ stats.todayWords }}</div>
                  <div class="stat-label">今日学习</div>
                </div>
              </el-col>
            </el-row>
            <div class="progress-section" style="margin-top: 20px;">
              <div class="progress-label">
                <span>总体进度</span>
                <span>{{ stats.progressPercentage }}%</span>
              </div>
              <el-progress :percentage="stats.progressPercentage"></el-progress>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="18">
        <el-card class="words-card">
          <template #header>
            <div class="card-header">
              <span>单词列表</span>
              <div class="header-actions">
                <el-input
                  v-model="searchKeyword"
                  placeholder="搜索单词"
                  prefix-icon="Search"
                  clearable
                  @input="handleSearch"
                  style="width: 200px; margin-right: 10px;"
                ></el-input>
                <el-button type="primary" @click="startLearning">开始学习</el-button>
              </div>
            </div>
          </template>

          <el-table :data="wordsList" style="width: 100%" v-loading="loading">
            <el-table-column prop="wordName" label="单词" width="150">
              <template #default="scope">
                <div class="word-item">
                  <span class="word-text">{{ scope.row.wordName }}</span>
                  <el-icon class="play-icon" @click="playAudio(scope.row)"><VideoPlay /></el-icon>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="audio" label="音标" width="120"></el-table-column>
            <el-table-column prop="explanation" label="释义"></el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button
                  :type="scope.row.isCollected ? 'danger' : 'default'"
                  :icon="scope.row.isCollected ? 'Star' : 'StarFilled'"
                  circle
                  size="small"
                  @click="toggleCollection(scope.row)"
                ></el-button>
                <el-button
                  type="primary"
                  icon="View"
                  circle
                  size="small"
                  @click="viewWordDetail(scope.row)"
                ></el-button>
                <el-button
                  type="success"
                  icon="Check"
                  circle
                  size="small"
                  @click="markAsLearned(scope.row)"
                ></el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-container">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="totalWords"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            ></el-pagination>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 单词详情对话框 -->
    <el-dialog
      v-model="wordDetailVisible"
      title="单词详情"
      width="600px"
    >
      <div class="word-detail" v-if="currentWord">
        <div class="word-header">
          <h2>{{ currentWord.wordName }}</h2>
          <div class="word-phonetic">{{ currentWord.audio }}</div>
          <el-button type="primary" icon="VideoPlay" circle @click="playAudio(currentWord)"></el-button>
        </div>

        <el-divider></el-divider>

        <div class="word-meaning">
          <h3>释义</h3>
          <p>{{ currentWord.explanation }}</p>
        </div>

        <div class="word-example">
          <h3>例句</h3>
          <p>{{ currentWord.example }}</p>
        </div>

        <div class="word-actions">
          <el-button
            :type="currentWord.isCollected ? 'danger' : 'default'"
            @click="toggleCollection(currentWord)"
          >
            {{ currentWord.isCollected ? '取消收藏' : '收藏单词' }}
          </el-button>
          <el-button type="success" @click="markAsLearned(currentWord)">标记为已学</el-button>
          <el-button type="primary" @click="generateWordCard(currentWord)">生成单词卡片</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, VideoPlay, Star, StarFilled, View, Check } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// 筛选表单
const filterForm = reactive({
  level: '',
  category: '',
  status: ''
})

// 学习统计
const stats = reactive({
  totalWords: 500,
  learnedWords: 120,
  masteredWords: 80,
  todayWords: 15,
  progressPercentage: 24
})

// 单词列表
const wordsList = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const totalWords = ref(0)

// 单词详情
const wordDetailVisible = ref(false)
const currentWord = ref(null)

// 应用筛选
const applyFilter = () => {
  loadWords()
}

// 处理搜索
const handleSearch = () => {
  loadWords()
}

// 处理页码变化
const handleCurrentChange = (val: number) => {
  currentPage.value = val
  loadWords()
}

// 处理每页条数变化
const handleSizeChange = (val: number) => {
  pageSize.value = val
  loadWords()
}

// 获取状态类型
const getStatusType = (status: number) => {
  switch (status) {
    case 0: return ''
    case 1: return 'info'
    case 2: return 'success'
    default: return ''
  }
}

// 获取状态文本
const getStatusText = (status: number) => {
  switch (status) {
    case 0: return '未学习'
    case 1: return '已学习'
    case 2: return '已掌握'
    default: return '未知'
  }
}

// 播放音频
const playAudio = (word: any) => {
  // 这里应该调用API获取音频URL并播放
  // 模拟播放音频
  ElMessage.success(`播放单词 ${word.wordName} 的发音`)
}

// 切换收藏状态
const toggleCollection = (word: any) => {
  // 这里应该调用API切换收藏状态
  // 模拟切换收藏状态
  word.isCollected = !word.isCollected
  ElMessage.success(word.isCollected ? `已收藏单词 ${word.wordName}` : `已取消收藏单词 ${word.wordName}`)
}

// 查看单词详情
const viewWordDetail = (word: any) => {
  currentWord.value = word
  wordDetailVisible.value = true
}

// 标记为已学
const markAsLearned = (word: any) => {
  // 这里应该调用API标记单词为已学
  // 模拟标记为已学
  if (word.status < 2) {
    word.status += 1
    ElMessage.success(`已将单词 ${word.wordName} 标记为${getStatusText(word.status)}`)
  } else {
    ElMessage.info(`单词 ${word.wordName} 已经是${getStatusText(word.status)}状态`)
  }
}

// 生成单词卡片
const generateWordCard = (word: any) => {
  // 这里应该调用API生成单词卡片
  // 模拟生成单词卡片
  ElMessage.success(`已为单词 ${word.wordName} 生成单词卡片`)
}

// 开始学习
const startLearning = () => {
  // 这里应该跳转到单词学习页面
  // 模拟开始学习
  ElMessage.success('开始学习单词')
}

// 加载单词列表
const loadWords = () => {
  loading.value = true

  // 这里应该调用API获取单词列表
  // 模拟获取单词列表
  setTimeout(() => {
    const mockWords = []
    for (let i = 1; i <= pageSize.value; i++) {
      const index = (currentPage.value - 1) * pageSize.value + i
      mockWords.push({
        wordId: index,
        wordName: `word${index}`,
        audio: '[wɜːd]',
        explanation: `单词${index}的释义`,
        example: `This is an example sentence for word${index}.`,
        status: Math.floor(Math.random() * 3),
        isCollected: Math.random() > 0.7
      })
    }

    wordsList.value = mockWords
    totalWords.value = 500
    loading.value = false
  }, 500)
}

// 页面加载时获取数据
onMounted(() => {
  loadWords()
})
</script>

<style scoped>
.words-container {
  padding: 20px;
  height: 100%;
  width: 100%;
  box-sizing: border-box;
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}

.filter-content {
  padding: 10px 0;
}

.stats-content {
  padding: 10px 0;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  margin-top: 5px;
}

.progress-label {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}

.word-item {
  display: flex;
  align-items: center;
}

.word-text {
  margin-right: 10px;
}

.play-icon {
  cursor: pointer;
  color: #409EFF;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.word-detail {
  padding: 10px;
}

.word-header {
  display: flex;
  align-items: center;
  gap: 15px;
}

.word-header h2 {
  margin: 0;
}

.word-phonetic {
  color: #606266;
}

.word-meaning, .word-example {
  margin-top: 20px;
}

.word-meaning h3, .word-example h3 {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 16px;
  color: #303133;
}

.word-actions {
  margin-top: 30px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
