import request from '@/utils/request'

// 获取系统统计数据（管理员）
export function getSystemStatistics() {
  return request({
    url: '/admin/statistics/system',
    method: 'get'
  })
}

// 获取用户增长趋势（管理员）
export function getUserGrowthTrend(params?: { startDate?: string; endDate?: string }) {
  return request({
    url: '/admin/statistics/users/growth',
    method: 'get',
    params
  })
}

// 获取考试完成情况（管理员）
export function getExamCompletionStats() {
  return request({
    url: '/admin/statistics/exams/completion',
    method: 'get'
  })
}

// 获取系统活跃度（管理员）
export function getSystemActivityStats(params?: { startDate?: string; endDate?: string }) {
  return request({
    url: '/admin/statistics/system/activity',
    method: 'get',
    params
  })
}

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
