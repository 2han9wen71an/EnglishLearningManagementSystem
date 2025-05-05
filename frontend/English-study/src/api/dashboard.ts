import request from '@/utils/request'

// 获取用户学习统计数据
export function getUserLearningStats(userId: number) {
  return request({
    url: `/users/${userId}/statistics/learning`,
    method: 'get'
  })
}

// 获取用户考试统计数据
export function getUserExamStats(userId: number) {
  return request({
    url: `/users/${userId}/statistics/exams`,
    method: 'get'
  })
}

// 获取用户听力练习统计数据
export function getUserListeningStats(userId: number) {
  return request({
    url: `/users/${userId}/statistics/listening`,
    method: 'get'
  })
}

// 获取仪表盘综合数据
export function getDashboardData(userId: number) {
  return request({
    url: `/users/${userId}/statistics/dashboard`,
    method: 'get'
  })
}
