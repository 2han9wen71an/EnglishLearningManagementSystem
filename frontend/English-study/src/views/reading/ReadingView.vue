<template>
  <div class="reading-container">
    <el-row :gutter="20">
      <!-- 左侧书籍列表 -->
      <el-col :span="6">
        <el-card class="book-list-card">
          <template #header>
            <div class="card-header">
              <span>阅读书籍</span>
            </div>
          </template>
          <div class="filter-section">
            <el-input
              v-model="searchQuery"
              placeholder="搜索书籍"
              prefix-icon="Search"
              clearable
              @input="filterBooks"
            />
          </div>
          <div v-if="loading" class="loading-container">
            <el-skeleton :rows="5" animated />
          </div>
          <div v-else-if="filteredBooks.length === 0" class="empty-container">
            <el-empty description="暂无书籍" />
          </div>
          <div v-else class="book-list">
            <el-scrollbar height="calc(100vh - 250px)">
              <div
                v-for="book in filteredBooks"
                :key="book.bookId"
                class="book-item"
                :class="{ active: currentBook && currentBook.bookId === book.bookId }"
                @click="selectBook(book)"
              >
                <div class="book-cover">
                  <el-image
                    :src="book.coverUrl || '/images/default-book-cover.jpg'"
                    fit="cover"
                    :lazy="true"
                  >
                    <template #error>
                      <div class="image-placeholder">
                        <el-icon><Picture /></el-icon>
                      </div>
                    </template>
                  </el-image>
                </div>
                <div class="book-info">
                  <h4 class="book-title">{{ book.bookName }}</h4>
                  <p class="book-author">{{ book.author }}</p>
                </div>
              </div>
            </el-scrollbar>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧阅读区域 -->
      <el-col :span="18">
        <el-card v-if="!currentBook" class="welcome-card">
          <div class="welcome-content">
            <el-empty description="请从左侧选择一本书开始阅读">
              <template #image>
                <el-icon :size="60"><Reading /></el-icon>
              </template>
            </el-empty>
          </div>
        </el-card>

        <el-card v-else class="reading-content-card">
          <template #header>
            <div class="card-header">
              <span>{{ currentBook.bookName }}</span>
              <div class="header-actions">
                <el-tooltip content="添加书签" placement="top">
                  <el-button :icon="Star" circle @click="addBookmark" />
                </el-tooltip>
                <el-tooltip content="调整字体大小" placement="top">
                  <el-dropdown trigger="click" @command="changeFontSize">
                    <el-button :icon="Edit" circle />
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="small">小</el-dropdown-item>
                        <el-dropdown-item command="medium">中</el-dropdown-item>
                        <el-dropdown-item command="large">大</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </el-tooltip>
              </div>
            </div>
          </template>
          <div class="reading-content" :class="fontSizeClass">
            <div v-html="currentBook.content"></div>
          </div>
          <div class="reading-footer">
            <el-pagination
              v-if="totalPages > 1"
              layout="prev, pager, next"
              :total="totalPages * 10"
              :current-page="currentPage"
              @current-change="changePage"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 生词弹窗 -->
    <el-dialog v-model="wordDialogVisible" title="生词查询" width="400px">
      <div v-if="selectedWord" class="word-detail">
        <h3 class="word-title">{{ selectedWord }}</h3>
        <div v-if="wordLoading" class="word-loading">
          <el-skeleton :rows="3" animated />
        </div>
        <div v-else class="word-content">
          <p class="word-phonetic">[{{ wordDetail.phonetic || '暂无音标' }}]</p>
          <div class="word-meanings">
            <div v-for="(meaning, index) in wordDetail.meanings" :key="index" class="meaning-item">
              <p class="part-of-speech">{{ meaning.partOfSpeech }}</p>
              <p class="definition">{{ meaning.definition }}</p>
              <p v-if="meaning.example" class="example">例句: {{ meaning.example }}</p>
            </div>
          </div>
          <div class="word-actions">
            <el-button type="primary" @click="addToWordCard">添加到单词卡片</el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Reading, Picture, Star, Edit } from '@element-plus/icons-vue'
import request from '@/utils/request'

// 数据
const loading = ref(true)
const books = ref<any[]>([])
const filteredBooks = ref<any[]>([])
const currentBook = ref<any>(null)
const searchQuery = ref('')
const currentPage = ref(1)
const totalPages = ref(1)
const fontSizeClass = ref('font-medium')
const wordDialogVisible = ref(false)
const selectedWord = ref('')
const wordLoading = ref(false)
const wordDetail = reactive({
  phonetic: '',
  meanings: [] as any[]
})

// 方法
const fetchBooks = async () => {
  loading.value = true
  try {
    const response = await request({
      url: '/books',
      method: 'get'
    })
    books.value = response.data || []
    filteredBooks.value = [...books.value]
  } catch (error) {
    console.error('获取书籍列表失败:', error)
    ElMessage.error('获取书籍列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const filterBooks = () => {
  if (!searchQuery.value) {
    filteredBooks.value = [...books.value]
    return
  }

  const query = searchQuery.value.toLowerCase()
  filteredBooks.value = books.value.filter(book =>
    book.bookName.toLowerCase().includes(query) ||
    (book.author && book.author.toLowerCase().includes(query))
  )
}

const selectBook = async (book: any) => {
  try {
    const response = await request({
      url: `/books/${book.bookId}`,
      method: 'get'
    })
    currentBook.value = response.data
    currentPage.value = 1

    // 计算总页数，假设每页显示1000个字符
    if (currentBook.value.content) {
      totalPages.value = Math.ceil(currentBook.value.content.length / 1000) || 1
    } else {
      totalPages.value = 1
    }
  } catch (error) {
    console.error('获取书籍详情失败:', error)
    ElMessage.error('获取书籍详情失败，请稍后重试')
  }
}

const changePage = (page: number) => {
  currentPage.value = page
  // 在实际应用中，这里可能需要从服务器获取特定页的内容
  // 或者在前端进行分页处理
}

const changeFontSize = (size: string) => {
  fontSizeClass.value = `font-${size}`
}

const addBookmark = () => {
  ElMessage.success('书签添加成功')
}

// 生词查询
const lookupWord = async (word: string) => {
  selectedWord.value = word
  wordLoading.value = true
  wordDialogVisible.value = true

  try {
    // 这里应该调用API获取单词详情
    // 模拟API调用
    setTimeout(() => {
      wordDetail.phonetic = 'ɪɡˈzɑːmpl'
      wordDetail.meanings = [
        {
          partOfSpeech: 'n.',
          definition: '例子，样本',
          example: 'This is an example of how the system works.'
        },
        {
          partOfSpeech: 'v.',
          definition: '作为...的例子',
          example: 'The teacher\'s behavior exampled dedication to the students.'
        }
      ]
      wordLoading.value = false
    }, 1000)
  } catch (error) {
    console.error('查询单词失败:', error)
    ElMessage.error('查询单词失败，请稍后重试')
    wordLoading.value = false
  }
}

const addToWordCard = () => {
  ElMessage.success(`已将 ${selectedWord.value} 添加到单词卡片`)
  wordDialogVisible.value = false
}

// 初始化
onMounted(() => {
  fetchBooks()

  // 添加文本选择事件监听
  document.addEventListener('mouseup', () => {
    const selection = window.getSelection()
    if (selection && selection.toString().trim()) {
      const selectedText = selection.toString().trim()
      // 只处理单个单词
      if (/^[a-zA-Z]+$/.test(selectedText)) {
        lookupWord(selectedText)
      }
    }
  })
})
</script>

<style scoped>
.reading-container {
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

.book-list-card {
  height: calc(100vh - 140px);
  display: flex;
  flex-direction: column;
}

.filter-section {
  margin-bottom: 15px;
}

.book-list {
  flex: 1;
  overflow: hidden;
}

.book-item {
  display: flex;
  padding: 10px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
  margin-bottom: 10px;
}

.book-item:hover {
  background-color: #f5f7fa;
}

.book-item.active {
  background-color: #ecf5ff;
}

.book-cover {
  width: 60px;
  height: 80px;
  margin-right: 10px;
  border-radius: 4px;
  overflow: hidden;
}

.book-info {
  flex: 1;
}

.book-title {
  margin: 0 0 5px;
  font-size: 16px;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.book-author {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.welcome-card, .reading-content-card {
  height: calc(100vh - 140px);
  display: flex;
  flex-direction: column;
}

.welcome-content {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.reading-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  line-height: 1.8;
}

.font-small {
  font-size: 14px;
}

.font-medium {
  font-size: 16px;
}

.font-large {
  font-size: 18px;
}

.reading-footer {
  padding: 15px 0;
  display: flex;
  justify-content: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.image-placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background-color: #f5f7fa;
  color: #909399;
}

.word-detail {
  padding: 10px;
}

.word-title {
  text-align: center;
  margin-bottom: 15px;
}

.word-phonetic {
  text-align: center;
  color: #909399;
  margin-bottom: 15px;
}

.word-meanings {
  margin-bottom: 20px;
}

.meaning-item {
  margin-bottom: 15px;
}

.part-of-speech {
  font-weight: bold;
  margin-bottom: 5px;
}

.definition {
  margin-bottom: 5px;
}

.example {
  font-style: italic;
  color: #606266;
}

.word-actions {
  text-align: center;
}
</style>
