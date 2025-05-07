<template>
  <div class="dialogue-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="scenarios-card">
          <template #header>
            <div class="card-header">
              <span>对话场景</span>
            </div>
          </template>
          <div class="scenarios-list">
            <el-radio-group v-model="selectedScenarioId" @change="handleScenarioChange">
              <el-scrollbar height="500px">
                <div class="scenario-items">
                  <div
                    v-for="scenario in scenarios"
                    :key="scenario.scenarioId"
                    class="scenario-item"
                  >
                    <el-radio :label="scenario.scenarioId">
                      <div class="scenario-info">
                        <div class="scenario-name">{{ scenario.scenarioName }}</div>
                        <div class="scenario-difficulty">
                          <el-tag :type="getDifficultyType(scenario.difficultyLevel)" size="small">
                            {{ getDifficultyText(scenario.difficultyLevel) }}
                          </el-tag>
                        </div>
                      </div>
                    </el-radio>
                  </div>
                </div>
              </el-scrollbar>
            </el-radio-group>
          </div>
        </el-card>
      </el-col>

      <el-col :span="18">
        <el-card class="dialogue-card">
          <template #header>
            <div class="card-header">
              <span>{{ currentScenario ? currentScenario.scenarioName : '请选择对话场景' }}</span>
              <div class="header-actions">
                <el-switch
                  v-model="useVoiceInput"
                  active-text="语音输入"
                  inactive-text="文本输入"
                  style="margin-right: 10px;"
                ></el-switch>
                <el-switch
                  v-model="useAiMode"
                  active-text="AI模式"
                  inactive-text="预设模式"
                  @change="handleAiModeChange"
                  style="margin-right: 10px;"
                ></el-switch>
                <el-button type="primary" @click="handleResetDialogue">重新开始</el-button>
              </div>
            </div>
          </template>

          <div v-if="!currentScenario" class="empty-state">
            <el-empty description="请从左侧选择一个对话场景开始"></el-empty>
          </div>

          <div v-else class="dialogue-content">
            <div class="scenario-description">
              <p>{{ currentScenario.description }}</p>
            </div>

            <el-divider></el-divider>

            <div class="chat-container">
              <el-scrollbar height="300px" ref="chatContainer">
                <div class="chat-messages">
                  <div
                    v-for="(message, index) in chatMessages"
                    :key="index"
                    :class="['message', message.isUser ? 'user-message' : 'ai-message']"
                  >
                    <div class="message-avatar">
                      <el-avatar :size="40" :src="message.isUser ? userAvatar : aiAvatar"></el-avatar>
                    </div>
                    <div class="message-content">
                      <div class="message-text">{{ message.content }}</div>
                      <div class="message-time">{{ message.time }}</div>
                    </div>
                  </div>
                </div>
              </el-scrollbar>
            </div>

            <div class="input-container">
              <div v-if="currentPrompt" class="prompt-hint">
                <el-alert
                  :title="currentPrompt"
                  type="info"
                  :closable="false"
                  show-icon
                ></el-alert>
              </div>

              <div class="input-box">
                <el-input
                  v-model="userInput"
                  :placeholder="useVoiceInput ? '点击右侧按钮开始语音输入' : '输入您的回复...'"
                  type="textarea"
                  :rows="3"
                  :disabled="useVoiceInput && isRecording"
                  @keyup.enter.ctrl="sendMessage"
                ></el-input>

                <div class="input-actions">
                  <el-tooltip content="按住说话" placement="top" v-if="useVoiceInput">
                    <el-button
                      type="danger"
                      :icon="Microphone"
                      circle
                      size="large"
                      @mousedown="startRecording"
                      @mouseup="stopRecording"
                      :class="{ 'recording': isRecording }"
                    ></el-button>
                  </el-tooltip>

                  <el-button type="primary" @click="sendMessage" :disabled="!userInput.trim()">发送</el-button>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Microphone } from '@element-plus/icons-vue'
import { getScenarioList, sendDialogueMessage, resetDialogue as resetDialogueApi, toggleAiMode as toggleAiModeApi } from '@/api/dialogue'

// 用户头像
const userAvatar = ref('https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png')
// AI头像
const aiAvatar = ref('https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png')

// 定义类型接口
interface DialogueScenario {
  scenarioId: number
  scenarioName: string
  scenarioDescription?: string
  description?: string // 兼容前端使用的字段名
  difficultyLevel: number
  initialPrompt?: string
  initialMessage?: string
}

interface ChatMessage {
  content: string
  isUser: boolean
  time: string
}

// 对话历史接口
interface DialogueHistory {
  scenarioId: number
  scenarioName: string
  scenarioDescription: string
  messages: string[] // 按顺序存储消息，偶数索引是AI消息，奇数索引是用户消息
  sessionId?: string // 会话ID，用于在后端跟踪对话状态
}

// 对话场景列表
const scenarios = ref<DialogueScenario[]>([])
const selectedScenarioId = ref(0)
const currentScenario = ref<DialogueScenario | null>(null)
const currentPrompt = ref('')

// 聊天消息
const chatMessages = ref<ChatMessage[]>([])
const chatContainer = ref<any>(null)
const userInput = ref('')

// 对话历史
const dialogueHistory = ref<DialogueHistory | null>(null)

// 语音输入
const useVoiceInput = ref(false)
const isRecording = ref(false)

// AI模式
const useAiMode = ref(true)

// 获取难度类型
const getDifficultyType = (level: number) => {
  switch (level) {
    case 1: return 'success'
    case 2: return 'warning'
    case 3: return 'danger'
    default: return 'info'
  }
}

// 获取难度文本
const getDifficultyText = (level: number) => {
  switch (level) {
    case 1: return '初级'
    case 2: return '中级'
    case 3: return '高级'
    default: return '未知'
  }
}

// 生成唯一会话ID
const generateSessionId = () => {
  return 'dialogue_' + Date.now() + '_' + Math.random().toString(36).substring(2, 9)
}

// 处理场景变更
const handleScenarioChange = (scenarioId: number) => {
  // 查找选中的场景
  const scenario = scenarios.value.find(s => s.scenarioId === scenarioId)
  if (scenario) {
    currentScenario.value = scenario

    // 初始化对话历史
    dialogueHistory.value = {
      scenarioId: scenario.scenarioId,
      scenarioName: scenario.scenarioName,
      scenarioDescription: scenario.description || scenario.scenarioDescription || '',
      messages: [],
      sessionId: generateSessionId() // 生成新的会话ID
    }

    // 重置对话
    resetDialogue()
  }
}

// 处理AI模式切换
const handleAiModeChange = async (value: boolean) => {
  try {
    // 准备请求参数
    const params: any = { currentMode: value }

    // 如果有会话ID，添加到请求参数
    if (dialogueHistory.value && dialogueHistory.value.sessionId) {
      params.sessionId = dialogueHistory.value.sessionId
    }

    // 调用API切换AI模式
    const response = await toggleAiModeApi(params)
    if (response && response.data) {
      useAiMode.value = response.data.useAiMode
      ElMessage.success(response.data.message)

      // 重置对话
      resetDialogue()
    }
  } catch (error) {
    console.error('切换AI模式失败:', error)
    ElMessage.error('切换AI模式失败，请稍后重试')
    // 恢复原来的值
    useAiMode.value = !useAiMode.value
  }
}

// 本地重置对话
const resetDialogue = () => {
  chatMessages.value = []
  userInput.value = ''

  if (currentScenario.value) {
    // 重置对话历史
    if (dialogueHistory.value) {
      dialogueHistory.value.messages = []
      // 如果没有sessionId，生成一个新的
      if (!dialogueHistory.value.sessionId) {
        dialogueHistory.value.sessionId = generateSessionId()
      }
    } else {
      dialogueHistory.value = {
        scenarioId: currentScenario.value.scenarioId,
        scenarioName: currentScenario.value.scenarioName,
        scenarioDescription: currentScenario.value.description || currentScenario.value.scenarioDescription || '',
        messages: [],
        sessionId: generateSessionId() // 生成新的会话ID
      }
    }

    // 设置初始提示
    currentPrompt.value = currentScenario.value.initialPrompt || 'Please continue the conversation...'

    // 添加AI的第一条消息
    const initialMessage = currentScenario.value.initialMessage ||
      `Hello, welcome to the ${currentScenario.value.scenarioName} scenario. How can I help you today?`

    // 添加到对话历史
    if (dialogueHistory.value) {
      dialogueHistory.value.messages.push(initialMessage)
    }

    // 添加到UI显示
    addMessage(initialMessage, false)
  }
}

// 处理重置对话（调用API）
const handleResetDialogue = async () => {
  if (!currentScenario.value) {
    ElMessage.error('请先选择对话场景')
    return
  }

  try {
    // 显示加载状态
    const loadingMessage = ElMessage({
      message: '正在重置对话...',
      type: 'info',
      duration: 0
    })

    // 获取初始消息和提示
    const initialMessage = currentScenario.value.initialMessage ||
      `Hello, welcome to the ${currentScenario.value.scenarioName} scenario. How can I help you today?`
    const initialPrompt = currentScenario.value.initialPrompt || 'Please continue the conversation...'

    // 准备请求参数
    const params: any = {
      scenarioId: currentScenario.value.scenarioId,
      initialMessage: initialMessage,
      initialPrompt: initialPrompt
    }

    // 如果有会话ID，添加到请求参数
    if (dialogueHistory.value && dialogueHistory.value.sessionId) {
      params.sessionId = dialogueHistory.value.sessionId
    }

    // 调用API重置对话
    const response = await resetDialogueApi(params)

    // 关闭加载提示
    loadingMessage.close()

    if (response && response.data) {
      // 清空聊天消息
      chatMessages.value = []
      userInput.value = ''

      // 重置对话历史
      if (dialogueHistory.value) {
        dialogueHistory.value.messages = []
      } else {
        dialogueHistory.value = {
          scenarioId: currentScenario.value.scenarioId,
          scenarioName: currentScenario.value.scenarioName,
          scenarioDescription: currentScenario.value.description || currentScenario.value.scenarioDescription || '',
          messages: []
        }
      }

      // 添加初始AI消息
      if (response.data.content) {
        // 添加到对话历史
        if (dialogueHistory.value) {
          dialogueHistory.value.messages.push(response.data.content)
        }

        // 添加到UI显示
        addMessage(response.data.content, false)
      }

      // 更新提示
      currentPrompt.value = response.data.prompt || 'Please continue the conversation...'

      ElMessage.success('对话已重置')
    } else {
      throw new Error('API返回数据格式错误')
    }
  } catch (error) {
    console.error('重置对话失败:', error)
    ElMessage.error('重置对话失败，请稍后重试')

    // 出错时使用本地重置
    resetDialogue()
  }
}

// 滚动到聊天区域底部
const scrollToBottom = () => {
  if (chatContainer.value) {
    chatContainer.value.setScrollTop(9999)
  }
}

// 添加消息
const addMessage = (content: string, isUser: boolean) => {
  const now = new Date()
  const hours = now.getHours().toString().padStart(2, '0')
  const minutes = now.getMinutes().toString().padStart(2, '0')

  chatMessages.value.push({
    content,
    isUser,
    time: `${hours}:${minutes}`
  })

  // 滚动到底部
  nextTick(() => {
    scrollToBottom()
  })
}

// 发送消息
const sendMessage = async () => {
  if (!userInput.value.trim()) return

  // 添加用户消息
  addMessage(userInput.value, true)

  // 添加到对话历史
  if (dialogueHistory.value) {
    dialogueHistory.value.messages.push(userInput.value)
  }

  // 保存用户输入并清空输入框
  const message = userInput.value
  userInput.value = ''

  // 直接处理用户消息
  await processUserMessage(message)
}

// 处理用户消息
const processUserMessage = async (message: string) => {
  if (!currentScenario.value) {
    ElMessage.error('请先选择对话场景')
    return
  }

  try {
    // 显示加载状态
    const loadingMessage = ElMessage({
      message: 'AI is thinking...',
      type: 'info',
      duration: 0
    })

    // 准备请求参数
    const params: any = {
      scenarioId: currentScenario.value.scenarioId,
      userInput: message,
      useAiMode: useAiMode.value
    }

    // 如果有会话ID，添加到请求参数
    if (dialogueHistory.value && dialogueHistory.value.sessionId) {
      params.sessionId = dialogueHistory.value.sessionId
    }

    // 如果是首次对话，添加初始消息和提示
    if (!dialogueHistory.value || dialogueHistory.value.messages.length <= 1) {
      const initialMessage = currentScenario.value.initialMessage ||
        `Hello, welcome to the ${currentScenario.value.scenarioName} scenario. How can I help you today?`
      const initialPrompt = currentScenario.value.initialPrompt || 'Please continue the conversation...'

      params.initialMessage = initialMessage
      params.initialPrompt = initialPrompt
    }

    // 调用API处理用户消息并获取AI回复
    const response = await sendDialogueMessage(params)

    // 关闭加载提示
    loadingMessage.close()

    if (response && response.data) {
      const aiResponse = response.data

      // 添加AI消息到UI
      addMessage(aiResponse.content, false)

      // 添加到对话历史
      if (dialogueHistory.value) {
        dialogueHistory.value.messages.push(aiResponse.content)
      }

      // 更新提示
      currentPrompt.value = aiResponse.prompt || 'Please continue the conversation...'
    } else {
      throw new Error('API返回数据格式错误')
    }
  } catch (error) {
    console.error('处理用户消息出错:', error)
    ElMessage.error('获取AI回复失败，请稍后重试')

    // 出错时使用备用回复
    const fallbackResponse = {
      content: `I'm sorry, I couldn't process your message "${message}" at the moment. Please try again later.`,
      prompt: 'Please continue the conversation...'
    }

    // 添加备用AI消息到UI
    addMessage(fallbackResponse.content, false)

    // 添加到对话历史
    if (dialogueHistory.value) {
      dialogueHistory.value.messages.push(fallbackResponse.content)
    }

    // 更新提示
    currentPrompt.value = fallbackResponse.prompt
  }
}

// 声明SpeechRecognition类型
declare global {
  interface Window {
    SpeechRecognition: any;
    webkitSpeechRecognition: any;
  }
}

// 语音识别实例
let recognition: any = null

// 初始化语音识别
const initSpeechRecognition = () => {
  // 检查浏览器是否支持语音识别
  if (!('webkitSpeechRecognition' in window) && !('SpeechRecognition' in window)) {
    ElMessage.error('您的浏览器不支持语音识别功能')
    return false
  }

  // 创建语音识别实例
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
  recognition = new SpeechRecognition()

  // 配置语音识别
  recognition.continuous = false
  recognition.interimResults = true
  recognition.lang = 'en-US' // 设置为英语

  // 监听结果事件
  recognition.onresult = (event: any) => {
    const result = event.results[0]
    const transcript = result[0].transcript

    // 更新输入框
    userInput.value = transcript
  }

  // 监听结束事件
  recognition.onend = () => {
    isRecording.value = false
    ElMessage.success('语音识别结束')
  }

  // 监听错误事件
  recognition.onerror = (event: any) => {
    console.error('语音识别错误:', event.error)
    isRecording.value = false
    ElMessage.error(`语音识别错误: ${event.error}`)
  }

  return true
}

// 开始录音
const startRecording = () => {
  // 初始化语音识别（如果尚未初始化）
  if (!recognition && !initSpeechRecognition()) {
    return
  }

  try {
    // 开始录音
    recognition.start()
    isRecording.value = true
    ElMessage.info('开始录音，请说话...')
  } catch (error) {
    console.error('启动语音识别失败:', error)
    ElMessage.error('启动语音识别失败，请重试')
    isRecording.value = false
  }
}

// 停止录音
const stopRecording = () => {
  if (!isRecording.value || !recognition) return

  try {
    // 停止录音
    recognition.stop()
    isRecording.value = false
  } catch (error) {
    console.error('停止语音识别失败:', error)
    ElMessage.error('停止语音识别失败')
    isRecording.value = false
  }
}

// 加载对话场景
const loadScenarios = async () => {
  try {
    // 调用API获取对话场景列表
    const { data } = await getScenarioList()
    if (data && Array.isArray(data)) {
      // 处理API返回的数据
      scenarios.value = data.map(scenario => ({
        ...scenario,
        // 确保有初始提示和初始消息，如果API没有返回这些字段
        initialPrompt: scenario.initialPrompt || '请开始对话...',
        initialMessage: scenario.initialMessage || `您好，欢迎来到${scenario.scenarioName}场景。`
      }))
    } else {
      ElMessage.error('获取对话场景失败')
    }
  } catch (error) {
    console.error('加载对话场景出错:', error)
    ElMessage.error('获取对话场景失败，请稍后重试')

    // 加载失败时尝试再次请求
    ElMessage.info('正在重新尝试获取对话场景...')

    try {
      // 再次尝试获取对话场景
      setTimeout(async () => {
        try {
          const retryResponse = await getScenarioList()
          if (retryResponse && retryResponse.data && Array.isArray(retryResponse.data)) {
            scenarios.value = retryResponse.data.map(scenario => ({
              ...scenario,
              initialPrompt: scenario.initialPrompt || '请开始对话...',
              initialMessage: scenario.initialMessage || `您好，欢迎来到${scenario.scenarioName}场景。`
            }))
          } else {
            // 如果再次失败，显示错误消息
            ElMessage.error('无法获取对话场景，请刷新页面重试')
          }
        } catch (retryError) {
          console.error('重新加载对话场景失败:', retryError)
          ElMessage.error('无法获取对话场景，请刷新页面重试')
        }
      }, 3000)
    } catch (retrySetupError) {
      console.error('设置重试失败:', retrySetupError)
    }
  }
}

// 页面加载时获取数据
onMounted(() => {
  // 加载对话场景
  loadScenarios()

  // 初始化语音识别
  initSpeechRecognition()

  // 初始滚动到底部
  nextTick(() => {
    scrollToBottom()
  })
})
</script>

<style scoped>
.dialogue-container {
  padding: 20px;
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

.scenarios-list {
  padding: 10px 0;
}

.scenario-items {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.scenario-item {
  border-bottom: 1px solid #EBEEF5;
  padding-bottom: 15px;
}

.scenario-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.scenario-name {
  font-weight: bold;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
}

.dialogue-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.scenario-description {
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.chat-container {
  flex: 1;
  margin: 20px 0;
}

.chat-messages {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 10px;
}

.message {
  display: flex;
  gap: 10px;
  max-width: 80%;
}

.user-message {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.ai-message {
  align-self: flex-start;
}

.message-content {
  background-color: #f5f7fa;
  padding: 10px 15px;
  border-radius: 8px;
}

.user-message .message-content {
  background-color: #ecf5ff;
}

.message-text {
  word-break: break-word;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
  text-align: right;
}

.input-container {
  margin-top: 20px;
}

.prompt-hint {
  margin-bottom: 10px;
}

.input-box {
  display: flex;
  gap: 10px;
}

.input-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  justify-content: flex-end;
}

.recording {
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(245, 108, 108, 0.7);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(245, 108, 108, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(245, 108, 108, 0);
  }
}
</style>
