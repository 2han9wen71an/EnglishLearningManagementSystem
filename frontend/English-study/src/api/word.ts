import request from '@/utils/request'

// 获取单词列表
export function getWordList(params: { gradeId?: number; page?: number; size?: number; query?: string; status?: number; userId?: number }) {
  return request({
    url: '/words',
    method: 'get',
    params
  })
}

// 获取单词统计数据（支持筛选）
export function getWordStats(params: { userId?: number; gradeId?: number; status?: number; query?: string }) {
  return request({
    url: '/words/stats',
    method: 'get',
    params
  })
}

// 获取单词详情
export function getWordDetail(wordId: number) {
  return request({
    url: `/words/${wordId}`,
    method: 'get'
  })
}

// 通过单词名称查询单词
export function getWordByName(wordName: string) {
  return request({
    url: '/words/search',
    method: 'get',
    params: { query: wordName, size: 1 }
  })
}

// 标记单词为已记住
export function markWordAsRemembered(wordId: number) {
  return request({
    url: `/words/${wordId}/remember`,
    method: 'post'
  })
}

// 标记单词为未记住
export function markWordAsForgotten(wordId: number) {
  return request({
    url: `/words/${wordId}/forget`,
    method: 'post'
  })
}

// 收藏/取消收藏单词
export function toggleWordCollection(wordId: number) {
  return request({
    url: `/words/${wordId}/collection`,
    method: 'post'
  })
}

// 添加单词（管理员）
export function addWord(data: any) {
  return request({
    url: '/admin/words',
    method: 'post',
    data
  })
}

// 更新单词（管理员）
export function updateWord(wordId: number, data: any) {
  return request({
    url: `/admin/words/${wordId}`,
    method: 'put',
    data
  })
}

// 删除单词（管理员）
export function deleteWord(wordId: number) {
  return request({
    url: `/admin/words/${wordId}`,
    method: 'delete'
  })
}

// 获取单词等级列表
export function getWordGradeList() {
  return request({
    url: '/words/grades',
    method: 'get'
  })
}
