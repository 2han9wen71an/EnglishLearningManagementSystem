import request from '@/utils/request'

// 生成单词卡片
export function generateWordCard(params: { word: string; userId?: number }) {
  return request({
    url: '/wordcards/generate',
    method: 'post',
    params // 使用params而不是data，因为后端使用@RequestParam
  })
}

// 获取用户的单词卡片历史
export function getUserWordCards(userId: number) {
  return request({
    url: `/wordcards/user/${userId}`,
    method: 'get'
  })
}

// 获取单词卡片详情
export function getWordCardByWord(word: string) {
  return request({
    url: `/wordcards/word/${word}`,
    method: 'get'
  })
}

// 保存单词卡片
export function saveWordCard(data: any) {
  return request({
    url: '/wordcards',
    method: 'post',
    data
  })
}

// 收藏单词（通过单词名称）
export function collectWordByName(wordName: string, userId: number) {
  return request({
    url: `/words/collect-by-name`,
    method: 'post',
    params: {
      wordName,
      userId
    }
  })
}
