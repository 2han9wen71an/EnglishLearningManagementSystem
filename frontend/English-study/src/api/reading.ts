import request from '@/utils/request'

// 获取阅读列表
export function getReadingList(params: { level?: string; page?: number; size?: number; query?: string }) {
  return request({
    url: '/books',
    method: 'get',
    params
  })
}

// 获取阅读详情
export function getReadingDetail(id: number) {
  return request({
    url: `/books/${id}`,
    method: 'get'
  })
}

// 添加阅读（管理员）
export function addReading(data: any) {
  return request({
    url: '/books',
    method: 'post',
    data
  })
}

// 更新阅读（管理员）
export function updateReading(id: number, data: any) {
  return request({
    url: `/books/${id}`,
    method: 'put',
    data
  })
}

// 删除阅读（管理员）
export function deleteReading(id: number) {
  return request({
    url: `/books/${id}`,
    method: 'delete'
  })
}

// 获取阅读等级列表
export function getReadingLevelList() {
  return request({
    url: '/reading/levels',
    method: 'get'
  })
}
