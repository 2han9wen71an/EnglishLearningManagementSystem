import request from '@/utils/request'

// 获取成绩列表（管理员）
export function getScoreList(params?: { examId?: number; status?: number; page?: number; size?: number; query?: string }) {
  return request({
    url: '/admin/scores',
    method: 'get',
    params
  })
}

// 获取成绩详情（管理员）
export function getScoreDetail(scoreId: number) {
  return request({
    url: `/admin/scores/${scoreId}`,
    method: 'get'
  })
}

// 获取用户成绩列表
export function getUserScoreList(userId: number, params?: { examId?: number; status?: number; page?: number; size?: number }) {
  return request({
    url: `/users/${userId}/scores`,
    method: 'get',
    params
  })
}

// 获取用户成绩详情
export function getUserScoreDetail(userId: number, scoreId: number) {
  return request({
    url: `/users/${userId}/scores/${scoreId}`,
    method: 'get'
  })
}

// 更新简答题评分（管理员）
export function updateEssayScore(scoreId: number, questionId: number, data: { score: number; comment?: string }) {
  return request({
    url: `/admin/scores/${scoreId}/questions/${questionId}`,
    method: 'put',
    data
  })
}
