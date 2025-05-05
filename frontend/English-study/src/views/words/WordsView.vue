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

              <!-- 暂时移除分类筛选，因为数据库中没有对应字段 -->
              <!-- <el-form-item label="单词分类">
                <el-select v-model="filterForm.category" placeholder="选择单词分类" style="width: 100%;">
                  <el-option label="全部" value=""></el-option>
                  <el-option label="CET-4" value="cet4"></el-option>
                  <el-option label="CET-6" value="cet6"></el-option>
                  <el-option label="IELTS" value="ielts"></el-option>
                  <el-option label="TOEFL" value="toefl"></el-option>
                </el-select>
              </el-form-item> -->

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
                  <el-tooltip content="播放发音" placement="top">
                    <el-icon class="play-icon" @click="playAudio(scope.row)"><VideoPlay /></el-icon>
                  </el-tooltip>
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
                <el-tooltip content="收藏/取消收藏" placement="top">
                  <el-button
                    :type="scope.row.isCollected ? 'danger' : 'default'"
                    circle
                    size="small"
                    @click="toggleCollectionStatus(scope.row)"
                  >
                    <el-icon>
                      <component :is="scope.row.isCollected ? Star : StarFilled" />
                    </el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip content="查看详情" placement="top">
                  <el-button
                    type="primary"
                    circle
                    size="small"
                    @click="viewWordDetail(scope.row)"
                  >
                    <el-icon><View /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip content="标记为已学" placement="top">
                  <el-button
                    type="success"
                    circle
                    size="small"
                    @click="markAsLearned(scope.row)"
                  >
                    <el-icon><Check /></el-icon>
                  </el-button>
                </el-tooltip>
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
          <el-tooltip content="播放发音" placement="top">
            <el-button type="primary" circle @click="playAudio(currentWord)">
              <el-icon><VideoPlay /></el-icon>
            </el-button>
          </el-tooltip>
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
            :type="currentWord.collection === 1 ? 'danger' : 'default'"
            @click="toggleCollectionStatus(currentWord)"
          >
            {{ currentWord.collection === 1 ? '取消收藏' : '收藏单词' }}
          </el-button>
          <el-button type="success" @click="markAsLearned(currentWord)">标记为已学</el-button>
          <el-button type="primary" @click="generateWordCard(currentWord)">生成单词卡片</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { Search, VideoPlay, Star, StarFilled, View, Check } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getWordList, getWordDetail, getWordStats } from '@/api/word'
import { markWordAsStudied, markWordAsRemembered, markWordAsForgotten, toggleWordCollection } from '@/api/userword'
import Cookies from 'js-cookie'

// 筛选表单
const filterForm = reactive({
  level: '',
  // category: '', // 暂时移除分类筛选，因为数据库中没有对应字段
  status: ''
})

// 学习统计
const stats = reactive({
  totalWords: 0,
  learnedWords: 0,
  masteredWords: 0,
  todayWords: 0,
  progressPercentage: 0
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

// 获取用户ID
const userId = computed(() => {
  const id = Cookies.get('userId')
  return id ? parseInt(id) : null
})

// 加载学习统计数据
const loadStats = async () => {
  if (!userId.value) {
    ElMessage.error('未找到用户信息，请重新登录')
    return
  }

  try {
    // 构建查询参数，与单词列表筛选条件保持一致
    const params: any = {}

    // 添加用户ID
    params.userId = userId.value

    // 添加筛选条件
    if (filterForm.level) {
      params.gradeId = parseInt(filterForm.level)
    }

    // 添加学习状态筛选
    if (filterForm.status) {
      params.status = parseInt(filterForm.status)
    }

    // 添加搜索关键词
    if (searchKeyword.value) {
      params.query = searchKeyword.value
    }

    const response = await getWordStats(params)
    if (response.success && response.data) {
      // 更新统计数据
      stats.totalWords = response.data.totalWords || 0
      stats.learnedWords = response.data.learnedWords || 0
      stats.masteredWords = response.data.masteredWords || 0
      stats.todayWords = response.data.todayWords || 0
      stats.progressPercentage = response.data.progressPercentage || 0
    }
  } catch (error) {
    console.error('获取学习统计数据失败:', error)
    ElMessage.error('获取学习统计数据失败，请稍后重试')
  }
}

// 应用筛选
const applyFilter = () => {
  currentPage.value = 1
  loadWords()
  // 重新加载统计数据，使其与筛选条件一致
  loadStats()
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  loadWords()
  // 重新加载统计数据，使其与搜索条件一致
  loadStats()
}

// 处理页码变化
const handleCurrentChange = (val: number) => {
  currentPage.value = val
  loadWords()
}

// 处理每页条数变化
const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
  loadWords()
}

// 获取状态类型
const getStatusType = (status: number) => {
  switch (status) {
    case 0: return 'info' // 修改为'info'而不是空字符串
    case 1: return 'warning'
    case 2: return 'success'
    default: return 'info' // 默认值也修改为'info'
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
  // 使用有道词典API播放单词发音
  try {
    const audio = new Audio(`http://dict.youdao.com/speech?audio=${word.wordName}`);
    audio.play();
  } catch (error) {
    console.error('播放音频失败:', error);
    ElMessage.error(`播放 ${word.wordName} 的发音失败，请稍后重试`);
  }
}

// 切换收藏状态
const toggleCollectionStatus = async (word: any) => {
  if (!userId.value) {
    ElMessage.error('请先登录')
    return
  }

  try {
    const response = await toggleWordCollection(userId.value, word.wordId)
    if (response.success) {
      // 更新本地状态
      const isCollected = response.data.collected
      word.collection = isCollected ? 1 : 0
      word.isCollected = isCollected
      ElMessage.success(response.data.message)

      // 如果当前正在查看详情，也更新详情中的状态
      if (currentWord.value && currentWord.value.wordId === word.wordId) {
        currentWord.value.collection = word.collection
        currentWord.value.isCollected = isCollected
      }

      // 刷新统计数据
      loadStats()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('切换收藏状态失败:', error)
    ElMessage.error('切换收藏状态失败，请稍后重试')
  }
}

// 查看单词详情
const viewWordDetail = async (word: any) => {
  try {
    const response = await getWordDetail(word.wordId)
    if (response.success) {
      currentWord.value = response.data
      wordDetailVisible.value = true
    } else {
      ElMessage.error(response.message || '获取单词详情失败')
    }
  } catch (error) {
    console.error('获取单词详情失败:', error)
    ElMessage.error('获取单词详情失败，请稍后重试')
  }
}

// 标记为已学
const markAsLearned = async (word: any) => {
  if (!userId.value) {
    ElMessage.error('请先登录')
    return
  }

  try {
    // 先标记为已学习
    const studyResponse = await markWordAsStudied(userId.value, word.wordId)
    if (!studyResponse.success) {
      ElMessage.error(studyResponse.message || '标记为已学习失败')
      return
    }

    // 再标记为已掌握
    const response = await markWordAsRemembered(userId.value, word.wordId)
    if (response.success) {
      // 更新本地状态
      word.remember = 1
      word.study = 1
      word.status = 2 // 已掌握
      ElMessage.success(response.data.message || '单词已标记为已学')

      // 如果当前正在查看详情，也更新详情中的状态
      if (currentWord.value && currentWord.value.wordId === word.wordId) {
        currentWord.value.remember = 1
        currentWord.value.study = 1
        currentWord.value.status = 2
      }

      // 刷新统计数据
      loadStats()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('标记单词状态失败:', error)
    ElMessage.error('标记单词状态失败，请稍后重试')
  }
}

// 生成单词卡片
const generateWordCard = (word: any) => {
  // 目前没有生成单词卡片的API，只显示提示
  ElMessage.info(`单词卡片生成功能正在开发中`)
}

// 开始学习
const startLearning = () => {
  // 跳转到单词学习页面
  window.location.href = '/word-learning'
}

// 加载单词列表
const loadWords = async () => {
  loading.value = true

  try {
    // 构建查询参数
    const params: any = {
      page: currentPage.value,
      size: pageSize.value
    }

    // 添加筛选条件
    if (filterForm.level) {
      params.gradeId = parseInt(filterForm.level)
    }

    // 暂时移除分类筛选，因为数据库中没有对应字段
    // if (filterForm.category) {
    //   params.category = filterForm.category
    // }

    // 添加学习状态筛选
    if (filterForm.status) {
      params.status = parseInt(filterForm.status)
    }

    // 添加用户ID
    const id = userId.value
    if (id) {
      params.userId = id
    }

    // 添加搜索关键词
    if (searchKeyword.value) {
      params.query = searchKeyword.value
    }

    const response = await getWordList(params)
    if (response.success && response.data) {
      // 处理返回的数据
      wordsList.value = response.data.list || []

      // 设置总数量
      totalWords.value = response.data.total || 0

      // 处理单词状态显示
      wordsList.value.forEach(word => {
        // 将remember和study字段映射到status字段用于显示
        if (word.remember === 1) {
          word.status = 2 // 已掌握
        } else if (word.study === 1) {
          word.status = 1 // 已学习
        } else {
          word.status = 0 // 未学习
        }

        // 将collection字段映射到isCollected字段用于显示
        word.isCollected = word.collection === 1
      })
    } else {
      ElMessage.error(response.message || '获取单词列表失败')
    }
  } catch (error) {
    console.error('获取单词列表失败:', error)
    ElMessage.error('获取单词列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadWords().then(() => {
    // 在获取单词列表后再加载统计数据，确保totalWords已经有值
    loadStats()
  })
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
