import request from '@/utils/request'

// 获取公告列表
export function getNoticeList(params?: { page?: number; size?: number; query?: string; startDate?: string; endDate?: string }) {
  return request({
    url: '/notices',
    method: 'get',
    params
  })
}

// 获取公告详情
export function getNoticeDetail(noticeId: number) {
  return request({
    url: `/notices/${noticeId}`,
    method: 'get'
  })
}

// 获取最新公告
export function getLatestNotice() {
  return request({
    url: '/notices/latest',
    method: 'get'
  })
}

// 添加公告（管理员）
export function addNotice(data: any) {
  return request({
    url: '/admin/notices',
    method: 'post',
    data
  })
}

// 更新公告（管理员）
export function updateNotice(noticeId: number, data: any) {
  return request({
    url: `/admin/notices/${noticeId}`,
    method: 'put',
    data
  })
}

// 删除公告（管理员）
export function deleteNotice(noticeId: number) {
  return request({
    url: `/admin/notices/${noticeId}`,
    method: 'delete'
  })
}
