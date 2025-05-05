import request from '@/utils/request'

// 获取用户信息
export function getUserInfo(userId: number) {
  return request({
    url: `/users/${userId}`,
    method: 'get'
  })
}

// 获取用户学习统计数据
export function getUserStats(userId: number) {
  return request({
    url: `/users/${userId}/stats`,
    method: 'get'
  })
}

// 获取用户学习进度
export function getUserProgress(userId: number) {
  return request({
    url: `/users/${userId}/stats/progress`,
    method: 'get'
  })
}

// 获取用户最近活动
export function getUserRecentActivities(userId: number, limit: number = 5) {
  return request({
    url: `/users/${userId}/activities/recent`,
    method: 'get',
    params: { limit }
  })
}

// 更新用户信息
export function updateUserInfo(userId: number, data: any) {
  return request({
    url: `/users/${userId}`,
    method: 'put',
    data
  })
}
