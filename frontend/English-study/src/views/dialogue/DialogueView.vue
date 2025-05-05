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
                ></el-switch>
                <el-button type="primary" @click="resetDialogue" style="margin-left: 10px;">重新开始</el-button>
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
              <el-scrollbar height="300px" ref="chatScrollbar">
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
                      :icon="isRecording ? 'Microphone' : 'Microphone'"
                      circle
                      size="large"
                      @mousedown="startRecording"
                      @mouseup="stopRecording"
                      :class="{ 'recording': isRecording }"
                    ></el-button>
                  </el-tooltip>
                  
                  <el-button type="primary" @click="sendMessage" :disabled="!userInput">发送</el-button>
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
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Microphone } from '@element-plus/icons-vue'

// 用户头像
const userAvatar = ref('https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png')
// AI头像
const aiAvatar = ref('https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png')

// 对话场景列表
const scenarios = ref([])
const selectedScenarioId = ref(0)
const currentScenario = ref(null)
const currentPrompt = ref('')

// 聊天消息
const chatMessages = ref([])
const chatScrollbar = ref(null)
const userInput = ref('')

// 语音输入
const useVoiceInput = ref(false)
const isRecording = ref(false)

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

// 处理场景变更
const handleScenarioChange = (scenarioId: number) => {
  // 查找选中的场景
  const scenario = scenarios.value.find(s => s.scenarioId === scenarioId)
  if (scenario) {
    currentScenario.value = scenario
    resetDialogue()
  }
}

// 重置对话
const resetDialogue = () => {
  chatMessages.value = []
  userInput.value = ''
  currentPrompt.value = currentScenario.value.initialPrompt
  
  // 添加AI的第一条消息
  addMessage(currentScenario.value.initialMessage, false)
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
    if (chatScrollbar.value) {
      chatScrollbar.value.setScrollTop(9999)
    }
  })
}

// 发送消息
const sendMessage = () => {
  if (!userInput.value.trim()) return
  
  // 添加用户消息
  addMessage(userInput.value, true)
  
  // 保存用户输入并清空输入框
  const message = userInput.value
  userInput.value = ''
  
  // 模拟AI思考
  setTimeout(() => {
    // 这里应该调用API获取AI回复
    // 模拟AI回复
    processUserMessage(message)
  }, 1000)
}

// 处理用户消息
const processUserMessage = (message: string) => {
  // 这里应该调用API处理用户消息并获取AI回复
  // 模拟AI回复
  const aiResponse = {
    content: `这是AI对"${message}"的回复。在实际实现中，这里应该返回从DeepSeek API获取的回复。`,
    prompt: '请继续对话...'
  }
  
  // 添加AI消息
  addMessage(aiResponse.content, false)
  
  // 更新提示
  currentPrompt.value = aiResponse.prompt
}

// 开始录音
const startRecording = () => {
  // 检查浏览器是否支持语音识别
  if (!('webkitSpeechRecognition' in window)) {
    ElMessage.error('您的浏览器不支持语音识别功能')
    return
  }
  
  isRecording.value = true
  ElMessage.info('开始录音，请说话...')
  
  // 这里应该调用语音识别API
  // 模拟语音识别
  setTimeout(() => {
    // 模拟识别结果
    userInput.value = '这是语音识别的结果。在实际实现中，这里应该返回从语音识别API获取的文本。'
  }, 2000)
}

// 停止录音
const stopRecording = () => {
  if (!isRecording.value) return
  
  isRecording.value = false
  ElMessage.success('录音结束')
  
  // 实际实现中，这里应该停止语音识别并获取结果
}

// 加载对话场景
const loadScenarios = () => {
  // 这里应该调用API获取对话场景列表
  // 模拟对话场景列表
  scenarios.value = [
    {
      scenarioId: 1,
      scenarioName: '机场值机',
      description: '你正在机场办理值机手续，需要与工作人员进行交流。',
      difficultyLevel: 1,
      initialPrompt: '你需要办理值机手续，可以向工作人员询问相关事宜。',
      initialMessage: '您好，欢迎来到航空公司值机柜台。请问您需要办理什么航班的值机手续？'
    },
    {
      scenarioId: 2,
      scenarioName: '酒店预订',
      description: '你需要预订一间酒店房间，与前台工作人员进行交流。',
      difficultyLevel: 1,
      initialPrompt: '你需要预订一间酒店房间，可以向前台询问价格、设施等信息。',
      initialMessage: '您好，欢迎来到我们酒店。请问您需要预订什么类型的房间？'
    },
    {
      scenarioId: 3,
      scenarioName: '餐厅点餐',
      description: '你在一家餐厅用餐，需要与服务员进行交流点餐。',
      difficultyLevel: 1,
      initialPrompt: '你需要点餐，可以向服务员询问菜单、推荐菜品等。',
      initialMessage: '您好，欢迎光临我们餐厅。这是我们的菜单，请问您想点些什么？'
    },
    {
      scenarioId: 4,
      scenarioName: '面试',
      description: '你正在参加一场工作面试，需要回答面试官的问题。',
      difficultyLevel: 2,
      initialPrompt: '你正在参加面试，需要向面试官介绍自己并回答问题。',
      initialMessage: '您好，很高兴见到您。请简单介绍一下您自己和您的工作经验。'
    },
    {
      scenarioId: 5,
      scenarioName: '商务会议',
      description: '你正在参加一场商务会议，需要与合作伙伴讨论项目细节。',
      difficultyLevel: 3,
      initialPrompt: '你需要在会议中讨论项目进展、问题和解决方案。',
      initialMessage: '早上好，感谢大家参加今天的会议。我们今天的主要议题是讨论项目进展和遇到的问题。请问您的团队目前进展如何？'
    }
  ]
}

// 页面加载时获取数据
onMounted(() => {
  loadScenarios()
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
