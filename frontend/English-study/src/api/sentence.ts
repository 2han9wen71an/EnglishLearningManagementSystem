import request from '@/utils/request'

// 获取随机每日一句
export function getRandomSentence() {
  return request({
    url: '/sentences/random',
    method: 'get'
  })
}

// 获取所有句子
export function getAllSentences() {
  return request({
    url: '/sentences',
    method: 'get'
  })
}

// 获取句子详情
export function getSentenceById(sentenceId: number) {
  return request({
    url: `/sentences/${sentenceId}`,
    method: 'get'
  })
}
