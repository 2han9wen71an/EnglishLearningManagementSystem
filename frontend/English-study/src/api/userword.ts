import request from '@/utils/request'

// 获取用户单词关联列表
export function getUserWords(userId: number) {
  return request({
    url: `/users/${userId}/words`,
    method: 'get'
  })
}

// 获取用户单词关联详情
export function getUserWord(userId: number, wordId: number) {
  return request({
    url: `/users/${userId}/words/${wordId}`,
    method: 'get'
  })
}

// 标记单词为已学习
export function markWordAsStudied(userId: number, wordId: number) {
  return request({
    url: `/users/${userId}/words/${wordId}/study`,
    method: 'post'
  })
}

// 标记单词为已掌握
export function markWordAsRemembered(userId: number, wordId: number) {
  return request({
    url: `/users/${userId}/words/${wordId}/remember`,
    method: 'post'
  })
}

// 标记单词为未掌握
export function markWordAsForgotten(userId: number, wordId: number) {
  return request({
    url: `/users/${userId}/words/${wordId}/forget`,
    method: 'post'
  })
}

// 收藏/取消收藏单词
export function toggleWordCollection(userId: number, wordId: number) {
  return request({
    url: `/users/${userId}/words/${wordId}/collection`,
    method: 'post'
  })
}
