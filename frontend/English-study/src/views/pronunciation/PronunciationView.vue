<template>
  <div class="pronunciation-container">
    <el-row :gutter="20">
      <!-- 左侧评测区域 -->
      <el-col :span="16">
        <el-card class="assessment-card">
          <template #header>
            <div class="card-header">
              <span>发音评测</span>
            </div>
          </template>
          <div class="assessment-content">
            <div class="text-selection">
              <el-form :model="assessmentForm" label-width="80px">
                <el-form-item label="评测文本">
                  <el-select v-model="assessmentForm.sampleText" placeholder="选择示例文本" clearable @change="selectSampleText">
                    <el-option v-for="item in sampleTexts" :key="item.value" :label="item.label" :value="item.value" />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-input
                    v-model="assessmentForm.text"
                    type="textarea"
                    :rows="4"
                    placeholder="请输入或选择要评测的文本"
                  />
                </el-form-item>
              </el-form>
            </div>

            <div class="recording-section">
              <div class="recording-controls">
                <el-button
                  type="primary"
                  :disabled="!assessmentForm.text || isRecording"
                  @click="startRecording"
                >
                  开始录音
                </el-button>
                <el-button
                  type="danger"
                  :disabled="!isRecording"
                  @click="stopRecording"
                >
                  停止录音
                </el-button>
                <el-button
                  type="success"
                  :disabled="!audioUrl || isSubmitting"
                  @click="submitAssessment"
                >
                  提交评测
                </el-button>
              </div>

              <div v-if="isRecording" class="recording-status">
                <el-icon class="recording-icon"><Microphone /></el-icon>
                <span>正在录音...</span>
              </div>

              <div v-if="audioUrl" class="audio-player">
                <audio ref="audioPlayer" controls :src="audioUrl"></audio>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧历史记录 -->
      <el-col :span="8">
        <el-card class="history-card">
          <template #header>
            <div class="card-header">
              <span>评测历史</span>
            </div>
          </template>
          <div v-if="loading" class="loading-container">
            <el-skeleton :rows="5" animated />
          </div>
          <div v-else-if="assessmentHistory.length === 0" class="empty-container">
            <el-empty description="暂无评测记录" />
          </div>
          <div v-else class="history-list">
            <el-scrollbar height="calc(100vh - 250px)">
              <div
                v-for="item in assessmentHistory"
                :key="item.assessmentId"
                class="history-item"
                :class="{ active: currentAssessment && currentAssessment.assessmentId === item.assessmentId }"
                @click="viewAssessment(item)"
              >
                <div class="history-item-content">
                  <div class="history-item-text">{{ truncateText(item.content, 30) }}</div>
                  <div class="history-item-meta">
                    <span class="history-item-score">得分: {{ item.overallScore }}</span>
                    <span class="history-item-time">{{ formatDate(item.assessmentTime) }}</span>
                  </div>
                </div>
              </div>
            </el-scrollbar>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 评测结果对话框 -->
    <el-dialog v-model="resultDialogVisible" title="发音评测结果" width="70%">
      <div v-if="currentAssessment" class="assessment-result">
        <div class="result-header">
          <div class="overall-score">
            <div class="score-circle">
              <span class="score-value">{{ currentAssessment.overallScore }}</span>
            </div>
            <span class="score-label">总体评分</span>
          </div>

          <div class="assessment-meta">
            <p>评测时间: {{ formatDate(currentAssessment.assessmentTime) }}</p>
            <p>评测文本: {{ currentAssessment.content }}</p>
          </div>
        </div>

        <el-divider />

        <div class="detailed-feedback">
          <h3>详细反馈</h3>

          <div class="feedback-section">
            <h4>音素级评测</h4>
            <div class="phoneme-feedback" v-html="currentAssessment.phonemeFeedback"></div>
          </div>

          <div class="feedback-section">
            <h4>发音问题</h4>
            <el-table :data="pronunciationIssues" border style="width: 100%">
              <el-table-column prop="word" label="单词" width="150" />
              <el-table-column prop="issue" label="问题描述" min-width="200" />
              <el-table-column prop="suggestion" label="改进建议" min-width="200" />
            </el-table>
          </div>

          <div class="feedback-section">
            <h4>改进建议</h4>
            <div class="improvement-suggestions">{{ currentAssessment.improvementSuggestions }}</div>
          </div>
        </div>

        <div class="audio-comparison">
          <h3>音频对比</h3>
          <div class="audio-section">
            <div class="user-audio">
              <h4>您的发音</h4>
              <audio controls :src="currentAssessment.audioUrl"></audio>
            </div>
            <div class="standard-audio">
              <h4>标准发音</h4>
              <audio controls :src="currentAssessment.standardAudioUrl"></audio>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Microphone } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { getUserId } from '@/utils/auth'

// 数据
const loading = ref(true)
const assessmentHistory = ref<any[]>([])
const currentAssessment = ref<any>(null)
const resultDialogVisible = ref(false)
const isRecording = ref(false)
const isSubmitting = ref(false)
const audioUrl = ref('')
const mediaRecorder = ref<MediaRecorder | null>(null)
const audioChunks = ref<BlobPart[]>([])
const pronunciationIssues = ref<any[]>([])

// 表单数据
const assessmentForm = reactive({
  text: '',
  sampleText: ''
})

// 示例文本
const sampleTexts = [
  { label: '简单句子', value: 'The quick brown fox jumps over the lazy dog.' },
  { label: '日常对话', value: 'Hello, how are you? It\'s nice to meet you.' },
  { label: '商务英语', value: 'We need to discuss the quarterly financial report.' },
  { label: '学术英语', value: 'The research paper was published in a peer-reviewed journal.' }
]

// 方法
const fetchAssessmentHistory = async () => {
  loading.value = true
  try {
    const response = await request({
      url: `/pronunciations/user/${getUserId()}`,
      method: 'get'
    })
    assessmentHistory.value = response.data || []
  } catch (error) {
    console.error('获取评测历史失败:', error)
    ElMessage.error('获取评测历史失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 使用auth工具类中的getUserId方法

const selectSampleText = () => {
  if (assessmentForm.sampleText) {
    assessmentForm.text = assessmentForm.sampleText
  }
}

const startRecording = async () => {
  if (!assessmentForm.text) {
    ElMessage.warning('请先输入或选择要评测的文本')
    return
  }

  try {
    // 检查浏览器支持
    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
      ElMessage.error('您的浏览器不支持录音功能，请使用Chrome、Firefox或Edge浏览器')
      return
    }

    // 获取麦克风权限
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })

    // 创建MediaRecorder实例
    mediaRecorder.value = new MediaRecorder(stream)
    audioChunks.value = []

    // 设置数据处理
    mediaRecorder.value.ondataavailable = (event) => {
      if (event.data.size > 0) {
        audioChunks.value.push(event.data)
      }
    }

    // 设置录音结束处理
    mediaRecorder.value.onstop = () => {
      // 创建音频Blob
      const audioBlob = new Blob(audioChunks.value, { type: 'audio/wav' })

      // 创建音频URL
      audioUrl.value = URL.createObjectURL(audioBlob)

      // 停止所有音轨
      if (mediaRecorder.value) {
        mediaRecorder.value.stream.getTracks().forEach(track => track.stop())
      }

      isRecording.value = false
      ElMessage.success('录音完成')
    }

    // 开始录音
    mediaRecorder.value.start()
    isRecording.value = true
    ElMessage.info('开始录音，请说话...')
  } catch (error) {
    console.error('录音失败:', error)
    ElMessage.error('无法访问麦克风: ' + (error as Error).message)
  }
}

const stopRecording = () => {
  if (mediaRecorder.value && mediaRecorder.value.state !== 'inactive') {
    mediaRecorder.value.stop()
  }
}

const submitAssessment = async () => {
  if (!audioUrl.value) {
    ElMessage.warning('请先录制音频')
    return
  }

  isSubmitting.value = true

  try {
    // 获取音频Blob
    const response = await fetch(audioUrl.value)
    const audioBlob = await response.blob()

    // 创建FormData
    const formData = new FormData()
    formData.append('userId', getUserId().toString())
    formData.append('content', assessmentForm.text)
    formData.append('audioData', audioBlob, 'recording.wav')

    // 发送请求
    const result = await request({
      url: '/pronunciations',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    if (result.success) {
      ElMessage.success('评测提交成功')

      // 刷新历史记录
      await fetchAssessmentHistory()

      // 显示结果
      viewAssessment(result.data)

      // 重置表单
      assessmentForm.text = ''
      assessmentForm.sampleText = ''
      audioUrl.value = ''
    } else {
      ElMessage.error(result.message || '评测提交失败')
    }
  } catch (error) {
    console.error('提交评测失败:', error)
    ElMessage.error('提交评测失败，请稍后重试')
  } finally {
    isSubmitting.value = false
  }
}

const viewAssessment = async (assessment: any) => {
  try {
    // 获取评测详情
    const response = await request({
      url: `/pronunciations/${assessment.assessmentId}`,
      method: 'get'
    })

    if (response.success) {
      currentAssessment.value = response.data

      // 解析发音问题
      try {
        pronunciationIssues.value = JSON.parse(currentAssessment.value.pronunciationIssues || '[]')
      } catch (e) {
        console.error('解析发音问题出错:', e)
        pronunciationIssues.value = []
      }

      resultDialogVisible.value = true
    } else {
      ElMessage.error(response.message || '获取评测详情失败')
    }
  } catch (error) {
    console.error('获取评测详情失败:', error)
    ElMessage.error('获取评测详情失败，请稍后重试')
  }
}

const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString()
}

const truncateText = (text: string, maxLength: number) => {
  if (!text) return ''
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

// 清理函数
const cleanup = () => {
  if (mediaRecorder.value && mediaRecorder.value.state !== 'inactive') {
    mediaRecorder.value.stop()
  }

  if (audioUrl.value) {
    URL.revokeObjectURL(audioUrl.value)
  }
}

// 生命周期钩子
onMounted(() => {
  fetchAssessmentHistory()
})

onUnmounted(() => {
  cleanup()
})
</script>

<style scoped>
.pronunciation-container {
  padding: 20px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.assessment-card, .history-card {
  height: calc(100vh - 140px);
  display: flex;
  flex-direction: column;
}

.assessment-content {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.text-selection {
  margin-bottom: 20px;
}

.recording-section {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.recording-controls {
  display: flex;
  gap: 10px;
}

.recording-status {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #f56c6c;
  animation: pulse 1.5s infinite;
}

.recording-icon {
  font-size: 20px;
}

.audio-player {
  margin-top: 15px;
  width: 100%;
}

.audio-player audio {
  width: 100%;
}

.loading-container, .empty-container {
  padding: 40px 0;
  text-align: center;
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

.history-item-text {
  font-size: 14px;
  line-height: 1.4;
}

.history-item-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

.history-item-score {
  font-weight: bold;
}

.assessment-result {
  padding: 20px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.overall-score {
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

.assessment-meta {
  font-size: 14px;
  color: #606266;
}

.assessment-meta p {
  margin: 5px 0;
}

.detailed-feedback {
  margin-bottom: 30px;
}

.detailed-feedback h3 {
  margin-bottom: 15px;
  font-size: 18px;
}

.feedback-section {
  margin-bottom: 20px;
}

.feedback-section h4 {
  margin-bottom: 10px;
  font-size: 16px;
  color: #303133;
}

.phoneme-feedback {
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
  line-height: 1.6;
}

.improvement-suggestions {
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
  line-height: 1.6;
}

.audio-comparison {
  margin-top: 30px;
}

.audio-comparison h3 {
  margin-bottom: 15px;
  font-size: 18px;
}

.audio-section {
  display: flex;
  gap: 20px;
}

.user-audio, .standard-audio {
  flex: 1;
}

.user-audio h4, .standard-audio h4 {
  margin-bottom: 10px;
  font-size: 16px;
  color: #303133;
}

@keyframes pulse {
  0% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
  100% {
    opacity: 1;
  }
}
</style>
