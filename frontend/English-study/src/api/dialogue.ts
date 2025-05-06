import request from '@/utils/request'

// 获取对话场景列表
export function getScenarioList() {
  return request({
    url: '/dialogues/scenarios',
    method: 'get'
  })
}

// 获取对话场景详情
export function getScenarioDetail(scenarioId: number) {
  return request({
    url: `/dialogues/scenarios/${scenarioId}`,
    method: 'get'
  })
}

// 发送对话消息
export function sendDialogueMessage(params: {
  scenarioId: number;
  userInput: string;
  useAiMode?: boolean;
  initialMessage?: string;
  initialPrompt?: string;
  sessionId?: string
}) {
  return request({
    url: '/dialogues/process',
    method: 'post',
    params // 使用params而不是data，因为后端使用@RequestParam
  })
}

// 重置对话
export function resetDialogue(params: {
  scenarioId: number;
  initialMessage?: string;
  initialPrompt?: string;
  sessionId?: string
}) {
  return request({
    url: '/dialogues/reset',
    method: 'post',
    params
  })
}

// 切换AI模式
export function toggleAiMode(params: { currentMode?: boolean; sessionId?: string }) {
  return request({
    url: '/dialogues/toggle-ai-mode',
    method: 'post',
    params
  })
}
