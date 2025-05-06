<template>
  <div class="wordcards-container">
    <el-row :gutter="20">
      <!-- 左侧生成区域 -->
      <el-col :span="16">
        <el-card class="generator-card">
          <template #header>
            <div class="card-header">
              <span>智能单词卡片生成器</span>
              <el-button type="primary" @click="viewHistory">查看历史卡片</el-button>
            </div>
          </template>
          <div class="generator-content">
            <div class="input-section">
              <el-form :model="wordForm" :rules="wordRules" ref="wordFormRef" label-width="80px">
                <el-form-item label="单词" prop="word">
                  <el-input
                    v-model="wordForm.word"
                    placeholder="请输入英文单词"
                    :disabled="generating"
                  >
                    <template #append>
                      <el-button
                        :loading="generating"
                        @click="generateWordCard"
                      >
                        生成卡片
                      </el-button>
                    </template>
                  </el-input>
                </el-form-item>
              </el-form>
              <p class="tip-text">输入任意英文单词，AI将自动生成联想图片、场景例句和记忆口诀</p>
            </div>

            <div v-if="generating" class="loading-container">
              <el-skeleton animated>
                <template #template>
                  <div class="card-skeleton">
                    <div class="image-skeleton"></div>
                    <div class="content-skeleton">
                      <el-skeleton-item variant="h3" style="width: 50%" />
                      <el-skeleton-item variant="text" style="width: 100%" />
                      <el-skeleton-item variant="text" style="width: 100%" />
                      <el-skeleton-item variant="text" style="width: 80%" />
                    </div>
                  </div>
                </template>
              </el-skeleton>
            </div>

            <div v-else-if="currentCard" class="card-display">
              <div class="card-header">
                <h2 class="word-title">{{ currentCard.word }}</h2>
                <p class="word-phonetic">[{{ currentCard.phonetic || '暂无音标' }}]</p>
              </div>

              <div class="card-body">
                <div class="card-image">
                  <el-image
                    :src="currentCard.imageUrl"
                    fit="cover"
                    :preview-src-list="[currentCard.imageUrl]"
                    :initial-index="0"
                    referrerpolicy="no-referrer"
                    @error="handleImageError"
                  >
                    <template #error>
                      <div class="image-error">
                        <el-icon><Picture /></el-icon>
                        <span>图片加载失败</span>
                        <el-button size="small" type="primary" @click="tryLoadImageAgain" style="margin-top: 10px;">
                          重试加载
                        </el-button>
                      </div>
                    </template>
                  </el-image>
                </div>

                <div class="card-content">
                  <div class="explanation-section">
                    <h3>单词解释</h3>
                    <div class="explanation" v-html="currentCard.explanation"></div>
                  </div>

                  <div class="example-section">
                    <h3>场景例句</h3>
                    <div class="example">{{ currentCard.contextSentence }}</div>
                  </div>

                  <div class="memory-section">
                    <h3>记忆口诀</h3>
                    <div class="memory-tip">{{ currentCard.memoryTip }}</div>
                  </div>
                </div>
              </div>

              <div class="card-actions">
                <el-tooltip content="功能开发中，敬请期待" placement="top">
                  <el-button type="primary" @click="saveToCollection">
                    收藏卡片
                  </el-button>
                </el-tooltip>
                <el-button @click="shareCard">分享卡片</el-button>
                <el-button @click="printCard">打印卡片</el-button>
              </div>
            </div>

            <div v-else class="empty-card">
              <el-empty description="请输入单词生成卡片">
                <template #image>
                  <el-icon :size="60"><Document /></el-icon>
                </template>
              </el-empty>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧历史记录 -->
      <el-col :span="8" v-if="showHistory">
        <el-card class="history-card">
          <template #header>
            <div class="card-header">
              <span>历史卡片</span>
            </div>
          </template>
          <div v-if="loadingHistory" class="loading-container">
            <el-skeleton :rows="5" animated />
          </div>
          <div v-else-if="cardHistory.length === 0" class="empty-container">
            <el-empty description="暂无历史卡片" />
          </div>
          <div v-else class="history-list">
            <el-scrollbar height="calc(100vh - 250px)">
              <div
                v-for="card in cardHistory"
                :key="card.word"
                class="history-item"
                :class="{ active: currentCard && currentCard.word === card.word }"
                @click="selectCard(card)"
              >
                <div class="history-item-content">
                  <div class="history-item-word">{{ card.word }}</div>
                  <div class="history-item-phonetic">[{{ card.phonetic || '暂无音标' }}]</div>
                  <div class="history-item-time">{{ formatDate(card.createTime) }}</div>
                </div>
              </div>
            </el-scrollbar>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 分享对话框 -->
    <el-dialog v-model="shareDialogVisible" title="分享单词卡片" width="400px">
      <div class="share-dialog-content">
        <div class="qrcode-container">
          <canvas ref="qrcodeRef" class="qrcode"></canvas>
        </div>
        <p class="share-tip">扫描二维码或复制以下链接分享</p>
        <el-input v-model="shareUrl" readonly>
          <template #append>
            <el-button @click="copyShareUrl">复制</el-button>
          </template>
        </el-input>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, Document } from '@element-plus/icons-vue'
import QRCode from 'qrcode'
import { getUserId } from '@/utils/auth'
import { generateWordCard as apiGenerateWordCard, getUserWordCards } from '@/api/wordcard'
// 暂时注释掉未实现的API导入
// import { collectWordByName } from '@/api/wordcard'

// 数据
const wordFormRef = ref()
const wordForm = reactive({
  word: ''
})
const wordRules = {
  word: [
    { required: true, message: '请输入单词', trigger: 'blur' },
    { pattern: /^[a-zA-Z]+$/, message: '请输入英文单词', trigger: 'blur' }
  ]
}

const generating = ref(false)
const currentCard = ref<any>(null)
const showHistory = ref(false)
const loadingHistory = ref(false)
const cardHistory = ref<any[]>([])
const shareDialogVisible = ref(false)
const shareUrl = ref('')
const qrcodeRef = ref<HTMLCanvasElement | null>(null)

// 方法
const generateWordCard = async () => {
  if (!wordFormRef.value) return

  await wordFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      generating.value = true

      try {
        const userId = getUserId()

        // 显示提示消息，告知用户AI生成可能需要较长时间
        ElMessage({
          message: 'AI正在生成单词卡片，这可能需要30-60秒，请耐心等待...',
          type: 'info',
          duration: 10000
        })

        const response = await apiGenerateWordCard({
          word: wordForm.word,
          userId: userId ? Number(userId) : undefined
        })

        // response是经过request.ts中的拦截器处理后的数据
        // 已经提取了原始响应中的data字段，并检查了success字段
        currentCard.value = response.data
        ElMessage.success('单词卡片生成成功')

        // 如果正在显示历史，刷新历史列表
        if (showHistory.value) {
          fetchCardHistory()
        }
      } catch (error: any) {
        console.error('生成单词卡片失败:', error)

        // 提供更详细的错误信息
        let errorMessage = '生成单词卡片失败'

        if (error.message && error.message.includes('timeout')) {
          errorMessage = '生成单词卡片超时，AI处理时间较长，请稍后重试或使用更简单的单词'
        } else if (error.response && error.response.data && error.response.data.message) {
          errorMessage = `生成失败: ${error.response.data.message}`
        } else if (error.message) {
          errorMessage = `生成失败: ${error.message}`
        }

        ElMessage.error(errorMessage)

        // 如果是超时错误，提供重试选项
        if (error.message && error.message.includes('timeout')) {
          ElMessageBox.confirm(
            '生成单词卡片需要较长时间，是否重试？',
            '请求超时',
            {
              confirmButtonText: '重试',
              cancelButtonText: '取消',
              type: 'warning'
            }
          ).then(() => {
            // 用户点击重试
            generateWordCard()
          }).catch(() => {
            // 用户取消重试
          })
        }
      } finally {
        generating.value = false
      }
    }
  })
}

// 使用auth工具类中的getUserId方法

const viewHistory = async () => {
  showHistory.value = !showHistory.value

  if (showHistory.value) {
    await fetchCardHistory()
  }
}

const fetchCardHistory = async () => {
  loadingHistory.value = true

  try {
    const userId = getUserId()
    if (!userId) {
      ElMessage.warning('请先登录')
      loadingHistory.value = false
      return
    }

    const response = await getUserWordCards(Number(userId))
    cardHistory.value = response.data || []
  } catch (error) {
    console.error('获取单词卡片历史失败:', error)
    ElMessage.error('获取单词卡片历史失败，请稍后重试')
  } finally {
    loadingHistory.value = false
  }
}

const selectCard = (card: any) => {
  currentCard.value = card
}

// 暂时注释掉收藏功能，等待后端接口实现
const saveToCollection = () => {
  if (!currentCard.value) return

  // 显示提示消息，告知用户功能暂未实现
  ElMessage({
    message: `收藏功能暂未实现，后端接口开发中...`,
    type: 'info'
  })

  // 以下是原始实现，等待后端接口准备好后可以取消注释
  /*
  const userId = getUserId()
  if (!userId) {
    ElMessage.warning('请先登录后再收藏单词')
    return
  }

  try {
    // 调用API收藏单词
    const response = await collectWordByName(currentCard.value.word, Number(userId))

    // response已经经过request.ts中的拦截器处理
    ElMessage.success(response.data?.message || `已将 ${currentCard.value.word} 添加到收藏`)
  } catch (error: any) {
    console.error('收藏单词失败:', error)
    let errorMessage = '收藏单词失败'

    if (error.response && error.response.data && error.response.data.message) {
      errorMessage = error.response.data.message
    } else if (error.message) {
      errorMessage = error.message
    }

    ElMessage.error(errorMessage)
  }
  */
}

const shareCard = async () => {
  if (!currentCard.value) return

  // 生成分享链接
  shareUrl.value = `${window.location.origin}/share/wordcard/${currentCard.value.word}`
  shareDialogVisible.value = true

  // 生成二维码
  await nextTick()
  if (qrcodeRef.value) {
    try {
      // 确保DOM元素已经渲染
      await nextTick()

      // 使用toCanvas方法生成二维码
      await QRCode.toCanvas(qrcodeRef.value, shareUrl.value, {
        width: 200,
        margin: 2,
        color: {
          dark: '#409EFF',
          light: '#FFFFFF'
        }
      })
    } catch (error) {
      console.error('生成二维码失败:', error)

      // 如果canvas方法失败，尝试使用toDataURL方法
      try {
        const dataUrl = await QRCode.toDataURL(shareUrl.value, {
          width: 200,
          margin: 2,
          color: {
            dark: '#409EFF',
            light: '#FFFFFF'
          }
        })

        // 创建图片元素显示二维码
        const img = document.createElement('img')
        img.src = dataUrl
        img.style.width = '100%'
        img.style.height = '100%'

        // 清空容器并添加图片
        const container = qrcodeRef.value.parentNode
        if (container && container instanceof HTMLElement) {
          container.innerHTML = ''
          container.appendChild(img)
        }
      } catch (err) {
        console.error('备用二维码生成方法也失败:', err)
        ElMessage.error('生成二维码失败，请手动复制链接分享')
      }
    }
  }
}

const copyShareUrl = () => {
  navigator.clipboard.writeText(shareUrl.value)
    .then(() => {
      ElMessage.success('链接已复制到剪贴板')
    })
    .catch(() => {
      ElMessage.error('复制失败，请手动复制')
    })
}

const printCard = () => {
  if (!currentCard.value) return

  // 创建打印窗口
  const printWindow = window.open('', '_blank')
  if (!printWindow) {
    ElMessage.error('无法打开打印窗口，请检查浏览器设置')
    return
  }

  // 构建打印内容
  const printContent = `
    <!DOCTYPE html>
    <html>
    <head>
      <title>单词卡片 - ${currentCard.value.word}</title>
      <style>
        body {
          font-family: Arial, sans-serif;
          margin: 0;
          padding: 20px;
        }
        .card {
          border: 1px solid #ddd;
          border-radius: 8px;
          padding: 20px;
          max-width: 600px;
          margin: 0 auto;
        }
        .card-header {
          text-align: center;
          margin-bottom: 20px;
        }
        .word-title {
          font-size: 28px;
          margin: 0;
        }
        .word-phonetic {
          font-size: 16px;
          color: #666;
          margin: 5px 0 0;
        }
        .card-body {
          display: flex;
          gap: 20px;
        }
        .card-image {
          width: 200px;
          height: 200px;
          overflow: hidden;
          border-radius: 4px;
        }
        .card-image img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
        .card-content {
          flex: 1;
        }
        h3 {
          margin: 0 0 10px;
          font-size: 16px;
          color: #333;
        }
        .section {
          margin-bottom: 15px;
        }
        @media print {
          body {
            padding: 0;
          }
          .card {
            border: none;
          }
          .print-button {
            display: none;
          }
        }
      </style>
    </head>
    <body>
      <div class="card">
        <div class="card-header">
          <h2 class="word-title">${currentCard.value.word}</h2>
          <p class="word-phonetic">[${currentCard.value.phonetic || '暂无音标'}]</p>
        </div>

        <div class="card-body">
          <div class="card-image">
            <img src="${currentCard.value.imageUrl}" alt="${currentCard.value.word}">
          </div>

          <div class="card-content">
            <div class="section">
              <h3>单词解释</h3>
              <div>${currentCard.value.explanation}</div>
            </div>

            <div class="section">
              <h3>场景例句</h3>
              <div>${currentCard.value.contextSentence}</div>
            </div>

            <div class="section">
              <h3>记忆口诀</h3>
              <div>${currentCard.value.memoryTip}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="print-button" style="text-align: center; margin-top: 20px;">
        <button onclick="window.print()">打印</button>
      </div>
    </body>
    </html>
  `

  // 写入内容并打印
  printWindow.document.open()
  printWindow.document.write(printContent)
  printWindow.document.close()
}

const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString()
}

// 处理图片加载错误
const handleImageError = () => {
  console.warn('图片加载失败:', currentCard.value?.imageUrl)
  // 可以在这里设置一个默认图片
  if (currentCard.value) {
    // 记录原始URL以便重试
    currentCard.value._originalImageUrl = currentCard.value.imageUrl

    // 设置为默认图片（使用一个公共的图片服务）
    currentCard.value.imageUrl = `https://dummyimage.com/400x400/f0f0f0/333333.png&text=${encodeURIComponent(currentCard.value.word)}`
  }
}

// 尝试重新加载图片
const tryLoadImageAgain = () => {
  if (currentCard.value && currentCard.value._originalImageUrl) {
    // 恢复原始URL
    currentCard.value.imageUrl = currentCard.value._originalImageUrl

    // 添加时间戳参数以避免缓存
    if (!currentCard.value.imageUrl.includes('?')) {
      currentCard.value.imageUrl += `?t=${Date.now()}`
    } else {
      currentCard.value.imageUrl += `&t=${Date.now()}`
    }
  }
}

// 生命周期钩子
onMounted(() => {
  // 如果URL中包含单词参数，自动生成该单词的卡片
  const urlParams = new URLSearchParams(window.location.search)
  const wordParam = urlParams.get('word')

  if (wordParam) {
    wordForm.word = wordParam
    generateWordCard()
  }
})
</script>

<style scoped>
.wordcards-container {
  padding: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.generator-card, .history-card {
  height: calc(100vh - 140px);
  display: flex;
  flex-direction: column;
}

.generator-content {
  padding: 20px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.input-section {
  margin-bottom: 30px;
}

.tip-text {
  font-size: 14px;
  color: #909399;
  margin-top: 10px;
}

.loading-container, .empty-container {
  padding: 40px 0;
  text-align: center;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.card-skeleton {
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 100%;
}

.image-skeleton {
  width: 100%;
  height: 200px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.content-skeleton {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.card-display {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-display .card-header {
  text-align: center;
  margin-bottom: 20px;
  display: block;
}

.word-title {
  font-size: 28px;
  margin: 0;
}

.word-phonetic {
  font-size: 16px;
  color: #909399;
  margin: 5px 0 0;
}

.card-body {
  display: flex;
  gap: 30px;
  margin-bottom: 30px;
}

.card-image {
  width: 250px;
  height: 250px;
  border-radius: 4px;
  overflow: hidden;
}

.image-error {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  color: #909399;
}

.image-error .el-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.explanation-section, .example-section, .memory-section {
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.explanation-section h3, .example-section h3, .memory-section h3 {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 16px;
  color: #303133;
}

.explanation {
  line-height: 1.6;
}

.example {
  font-style: italic;
  line-height: 1.6;
}

.memory-tip {
  color: #67c23a;
  font-weight: bold;
  line-height: 1.6;
}

.card-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: auto;
  padding-top: 20px;
}

.empty-card {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.history-list {
  flex: 1;
  overflow: hidden;
}

.history-item {
  padding: 15px;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  transition: background-color 0.3s;
}

.history-item:hover {
  background-color: #f5f7fa;
}

.history-item.active {
  background-color: #ecf5ff;
}

.history-item-content {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.history-item-word {
  font-size: 16px;
  font-weight: bold;
}

.history-item-phonetic {
  font-size: 14px;
  color: #909399;
}

.history-item-time {
  font-size: 12px;
  color: #909399;
}

.share-dialog-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.qrcode-container {
  width: 200px;
  height: 200px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #fff;
  border-radius: 4px;
  overflow: hidden;
}

.share-tip {
  font-size: 14px;
  color: #606266;
  margin: 0;
}
</style>
