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
export function sendDialogueMessage(data: { scenarioId: number; userInput: string }) {
  return request({
    url: '/dialogues/process',
    method: 'post',
    data
  })
}
